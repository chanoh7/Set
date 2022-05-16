package puzzle.card.set.game;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import puzzle.card.set.R;
import puzzle.card.set.databinding.CardContainerBinding;
import puzzle.card.set.databinding.GameActivityBinding;
import puzzle.card.set.result.ResultActivity;

import static puzzle.card.set.game.Card.ATTRIBUTE.*;
import static puzzle.card.set.game.Card.*;

public class GameActivity extends AppCompatActivity implements GameViewModel.ViewContract {
    private static final int START_NUM_OF_CARD = 15;    //초기 배치 카드 수
    private static final int TIME_LIMIT = 600;          //제한시간(초)
    private static final int TIME_COST_SHUFFLE = 10000; //카드 재배치 시 깎이는 시간(ms)
    private static final int TIME_COST_CHEAT = 30000;   //치트 사용 시 깎이는 시간(ms)
    private static final int TIME_COST_SUPER_CHEAT = 90000;//슈퍼치트 사용 시 깎이는 시간(ms)
    private static final int GET_POINT = 3;         //한 셋 찾을 때 득점
    private static final int LOSE_POINT = 1;        //틀렸을 때 감점
    private static final int NOTHING = -1;      //선택 배열에서 선택된 게 없음을 나타냄
    private static final int FOCUS_ON = Color.YELLOW;
    private static final int FOCUS_OFF = Color.WHITE;
    private static final int CHEAT_COLOR = Color.BLUE;

    private GameActivityBinding mBinding;
    private GameViewModel mViewModel;
    private CardManager mCardManager;   //덱 관리
    private Runnable mOneSecStopwatch;  //1초를 세는 초시계
    private Handler mHandler;           //초시계가 울릴 때 화면을 갱신할 핸들러

    private Drawable[][][] mCardImage;  //카드 그림 모음
    private int mPickCount;             //현재 선택한 카드 수(최대:카드의 속성이 갖는 값의 종류(3), 최대치가 되면 채점)
    private int[] mPickPosition;        //선택한 카드
    private int[] mSolversAnswer;       //답이 없는 판인지 검사할 때, 답을 한 개는 찾을것이다. 그것을 저장한다.

    private List<Card> mGameField;      //판에 나와있는 카드 목록
    private boolean mSuperCheating;     //채점 안 하고 통과시키는 플래그
    private boolean nowPlaying;         //초시계 진행 여부 플래그
    private boolean goResult;           //게임 종료 플래그(시간초과, 덱 소진 시 true)
    private int mPoint;                 //득점
    private int mMissCount;             //실점량
    private int mShuffleCount;          //카드 재배치 횟수
    private int mCheatCount;            //치트 사용 횟수
    private int mSuperCheatCount;       //슈퍼치트 사용 횟수
    private long mStartTime;            //남은시간 계산 기준(시작시간과 현재시간의 차를 계산하여 표시함)
    private long mOnPauseTime;          //일시정지 시간을 남은 시간에 반영하기 위해 저장

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.game_activity);
        mViewModel = new GameViewModel(this);
        mBinding.setGameViewModel(mViewModel);

        mCardManager = new CardManager();
        mGameField = new ArrayList<>();
        mPickPosition = new int[CardManager.KIND_OF_VALUES];
        mSolversAnswer = new int[CardManager.KIND_OF_VALUES - 1];

        initTimer();
        initCardImage();
        startElementsAnimation();
    }

    //게임 시작 전에 재생하는 애니메이션
    private void startElementsAnimation() {
        ImageView cardAnimationView = new ImageView(this);
        cardAnimationView.setBackgroundResource(R.drawable.card_animation);
        AnimationDrawable animation = (AnimationDrawable) cardAnimationView.getBackground();

        mBinding.gameBoard.addView(cardAnimationView);
        animation.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        //시계 멈춤, 게임 상태를 일시정지로 바꿈, 멈춘 시간을 저장
        mHandler.removeCallbacks(mOneSecStopwatch);
        nowPlaying = false;
        mOnPauseTime = System.currentTimeMillis();
    }

    @Override
    public void onResume() {
        super.onResume();
        //게임이 일시정지 상태인지 판정해서 시계 재개, 게임상태를 진행중으로 바꿈, 멈춰있던 시간만큼 시작시간을 보정
        if (mStartTime > 0 && !goResult) {
            mHandler.post(mOneSecStopwatch);
            nowPlaying = true;
            mStartTime += System.currentTimeMillis() - mOnPauseTime;
        }
    }

    //시계 초기화.
    private void initTimer() {
        mHandler = new Handler();
        //게임이 진행중이면 1초마다 반복되는 초시계 가동
        mOneSecStopwatch = () -> {
            if (nowPlaying) {
                runStopWatch();
            }
        };
    }

    //카드 이미지 배열 초기화
    private void initCardImage() {
        TypedArray elements = getResources().obtainTypedArray(R.array.card_elements);

        //1차원 배열인 원본 데이터를 카드의 속성에 맞게 3x3x3 배열로 재배치(넷째 속성은 그림의 개수이므로 저장하지 않고 그 수만큼 반복해서 그림)
        mCardImage = new Drawable[CardManager.KIND_OF_VALUES][CardManager.KIND_OF_VALUES][CardManager.KIND_OF_VALUES];
        int imageIndex = 0;
        for (int i = 0; i < CardManager.KIND_OF_VALUES; ++i) {
            for (int j = 0; j < CardManager.KIND_OF_VALUES; ++j) {
                for (int k = 0; k < CardManager.KIND_OF_VALUES; ++k) {
                    mCardImage[i][j][k] = elements.getDrawable(imageIndex++);
                }
            }
        }
    }

    private Drawable findCardImageElement(Card card) {
        return mCardImage
                [card.getAttribute(TYPE_COLOR)]
                [card.getAttribute(TYPE_SHAPE)]
                [card.getAttribute(TYPE_FILL)];
    }

    @Override
    public void init() {
        mBinding.gameBoard.removeAllViews();
        mBinding.flippedCardView.removeAllViews();
        mCardManager.reset();
        mGameField.clear();

        goResult = false;
        mPoint = 0;
        updatePoint();
        resetPick();
        updateRemainTime(TIME_LIMIT);
    }

    //카드 선택 정보 초기화
    private void resetPick() {
        try {
            for (int position : mPickPosition) {
                setCardFocus(position, FOCUS_OFF);
            }
        } catch (Exception e) {
            //do nothing(게임 처음 실행할 때, 판에 깔린 카드가 없어서 한 번 여기로 진입한다.)
        }
        initPick();
    }

    private void initPick() {
        mPickCount = 0;
        Arrays.fill(mPickPosition, NOTHING);
    }

    //게임 시작시 n장을 판에 깔고 타이머 시작
    @Override
    public void fieldSetUp() {
        nowPlaying = true;
        shuffle();
        updateRemainCard();
        mStartTime = System.currentTimeMillis();
        runStopWatch();
        resetCount();
    }

    //수동으로 셔플 눌렀을 때
    @Override
    public void reShuffle() {
        if (!nowPlaying) {
            return;
        }

        mStartTime -= TIME_COST_SHUFFLE;
        mShuffleCount++;
        shuffle();
    }

    //게임 도중 세는 것들 초기화
    private void resetCount() {
        mMissCount = 0;
        mShuffleCount = 0;
        mCheatCount = 0;
        mSuperCheatCount = 0;
    }

    //판 엎고 다시 섞어서 뽑기
    public void shuffle() {
        if (!nowPlaying) {
            return;
        }

        for (Card card : mGameField) {
            mCardManager.bounce(card);
        }
        mGameField.clear();
        mBinding.gameBoard.removeAllViews();
        dealSetOfCards();

        initPick();

        //답이 없는 조합으로 카드가 깔리면 다시 배치한다
        if (!solver()) {
            shuffle();
        }
    }

    //카드 한 세트 뽑아서 필드에 놓기
    private void dealSetOfCards() {
        for (int i = 0; i < START_NUM_OF_CARD; ++i) {
            drawNewCard(i);
        }
    }

    //답이 있기는 하는지 검사
    private boolean solver() {
        for (int i = 0; i < mGameField.size() - 2; ++i) {
            mPickPosition[0] = i;
            for (int j = i + 1; j < mGameField.size() - 1; ++j) {
                mPickPosition[1] = j;
                for (int k = j + 1; k < mGameField.size(); ++k) {
                    mPickPosition[2] = k;

                    //답을 찾았으면 치트에 쓰기 위해 적어둔다.
                    if (judge()) {
                        mSolversAnswer[0] = i;
                        mSolversAnswer[1] = j;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    //남은시간 갱신
    private void runStopWatch() {
        long passedTime = System.currentTimeMillis() - mStartTime;
        passedTime /= 1000l;
        updateRemainTime(TIME_LIMIT - passedTime);

        if (passedTime < TIME_LIMIT) {
            mHandler.postDelayed(mOneSecStopwatch, 1000);
        } else {
            gameFinish();
        }
    }

    //카드 뽑아서 필드에 추가.
    private void drawNewCard(int position) {
        Card card;
        if (mCardManager.remain() > 0) {
            card = mCardManager.draw();
            mBinding.gameBoard.addView(makeCardImage(card, position), position);
            mGameField.add(position, card);
        }
    }

    //카드 객체의 정보로 카드 레이아웃을 만들고, 클릭 리스너에 이 카드가 배치되는 위치까지 저장한다
    private LinearLayout makeCardImage(@NonNull final Card card, final int position) {
        LinearLayout cardContainer = (LinearLayout) getLayoutInflater().inflate(R.layout.card_container, mBinding.gameBoard, false);
        CardContainerBinding cardContainerBinding = DataBindingUtil.bind(cardContainer);

        //리스너에 데이터를 꽂아버리기 때문에 리스너를 매번 새로 만들어야 한다
        cardContainer.setOnClickListener(view -> {
            if (!nowPlaying) {
                return;
            }

            //중복 선택이 아니면 선택된 카드 목록에 추가
            if (mPickCount == 0 || (mPickPosition[0] != position && mPickPosition[1] != position)) {
                addSetElement(position);
            }
        });

        if (cardContainerBinding == null) {
            return cardContainer;
        }

        //카드 뷰에 모양을 채운다.(1개 or 2개 or 3개)
        //가운데 칸은 공통으로 그림
        cardContainerBinding.elementCenter.setImageDrawable(findCardImageElement(card));
        int number = card.getAttribute(TYPE_NUMBER);

        if (number != VALUE_1.ordinal()) {
            //2개 or 3개면 왼쪽 칸에도 그림
            cardContainerBinding.elementLeft.setImageDrawable(findCardImageElement(card));
            if (number == VALUE_2.ordinal()) {
                //2개는 오른쪽 뷰 삭제
                cardContainerBinding.elementRight.setVisibility(View.GONE);
            } else {
                //3개는 오른쪽에도 그림
                cardContainerBinding.elementRight.setImageDrawable(findCardImageElement(card));
            }
        }

        return cardContainer;
    }

    //선택한 카드에 포커스 표시
    private void addSetElement(int position) {
        setCardFocus(position, FOCUS_ON);
        mPickPosition[mPickCount] = position;
        ++mPickCount;

        //선택 최대치가 되면 채점
        if (mPickCount == CardManager.KIND_OF_VALUES) {
            if (judge()) {
                gainPoint();
            } else {
                losePoint();
            }
        }
    }

    //지정된 위치의 카드 배경색을 지정된 색으로 칠한다
    private void setCardFocus(int position, int color) {
        mBinding.gameBoard.getChildAt(position).setBackgroundColor(color);
    }

    //카드 세 장이 셋이 되면 true, 아니면 false
    private boolean judge() {
        if (mSuperCheating) {
            return true;
        }

        return compareSetElement(pick(0).getAttribute(TYPE_COLOR), pick(1).getAttribute(TYPE_COLOR), pick(2).getAttribute(TYPE_COLOR)) &&
                compareSetElement(pick(0).getAttribute(TYPE_SHAPE), pick(1).getAttribute(TYPE_SHAPE), pick(2).getAttribute(TYPE_SHAPE)) &&
                compareSetElement(pick(0).getAttribute(TYPE_FILL), pick(1).getAttribute(TYPE_FILL), pick(2).getAttribute(TYPE_FILL)) &&
                compareSetElement(pick(0).getAttribute(TYPE_NUMBER), pick(1).getAttribute(TYPE_NUMBER), pick(2).getAttribute(TYPE_NUMBER));
    }

    //선택된 카드 배열을 보고 게임판의 해당 위치에 있는 카드 정보를 return
    private Card pick(int position) {
        return mGameField.get(mPickPosition[position]);
    }

    //세 카드의 값이 모두 같거나 셋 다 다르면 true
    private boolean compareSetElement(Object arg1, Object arg2, Object arg3) {
        if (arg1 == arg2) {
            return (arg2 == arg3);
        } else {
            return (arg2 != arg3 && arg3 != arg1);
        }
    }

    //득점 및 필드 갱신
    private void gainPoint() {
        if (mSuperCheating) {
            mStartTime -= TIME_COST_SUPER_CHEAT;
            mSuperCheatCount++;
        }

        mPoint += GET_POINT;
        updatePoint();
        updateField();//픽 정보를 필드 갱신에 쓰기 때문에 픽 리셋보다 필드 갱신이 먼저임
        resetPick();
    }

    //감점
    private void losePoint() {
        mMissCount++;
        mPoint -= LOSE_POINT;
        updatePoint();
        resetPick();
    }

    //셋에 성공한 카드들을 뽑은 카드 목록으로 옮기고 3장 추가로 뽑기
    private void updateField() {
        for (int position : mPickPosition) {
            LinearLayout pickedCard = (LinearLayout) (mBinding.gameBoard.getChildAt(position));
            mBinding.gameBoard.removeViewAt(position);
            mBinding.flippedCardView.addView(pickedCard, 0);
            mGameField.remove(position);
            drawNewCard(position);
        }

        //셋 사이에 경계선 추가
        putSeparator();

        updateRemainCard();

        //답이 없게 뽑혔으면 재배치
        if (mCardManager.remain() > 0 && !solver()) {
            shuffle();
            Toast.makeText(this, R.string.no_answer, Toast.LENGTH_SHORT).show();
        }
    }

    //셋 사이에 경계선 추가
    private void putSeparator() {
        ImageView separator = new ImageView(this);
        separator.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(10, LinearLayout.LayoutParams.MATCH_PARENT);
        mBinding.flippedCardView.addView(separator, 0, layoutParams);
    }

    //남은 카드 수를 화면에 표시, 남은 카드가 0이면 게임 종료
    private void updateRemainCard() {
        mViewModel.remainCard.set(String.format("%2d", mCardManager.remain()));

        if (mCardManager.remain() == 0) {
            gameFinish();
        }
    }

    //남은 시간 표시
    private void updateRemainTime(long time) {
        mViewModel.remainTime.set(String.format("%2d : %2d", time / 60, time % 60));
    }

    //점수 변동을 화면에 반영
    private void updatePoint() {
        mViewModel.point.set(String.format("%2d", mPoint));
    }

    //게임 결과를 저장하고 결과 액티비티로 전달
    private void gameFinish() {
        nowPlaying = false;
        goResult = true;
        mViewModel.startButtonVisibility.set(View.VISIBLE);

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.RESULT_REMAIN, mViewModel.remainCard.get());
        intent.putExtra(ResultActivity.RESULT_POINT, mViewModel.point.get());
        intent.putExtra(ResultActivity.RESULT_TIME, mViewModel.remainTime.get());
        intent.putExtra(ResultActivity.RESULT_MISS, String.format("%d", mMissCount));
        intent.putExtra(ResultActivity.RESULT_SHUFFLE, String.format("%d", mShuffleCount));
        intent.putExtra(ResultActivity.RESULT_CHEAT, String.format("%d", mCheatCount));
        intent.putExtra(ResultActivity.RESULT_SUPER_CHEAT, String.format("%d", mSuperCheatCount));

        if (mCardManager.remain() == 0) {
            intent.putExtra(ResultActivity.RESULT, getResources().getString(R.string.game_clear));
        } else {
            intent.putExtra(ResultActivity.RESULT, getResources().getString(R.string.time_over));
        }

        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //DO NOTHING (아직은...)

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.exit)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> finish())
                .create()
                .show();
    }

    @Override
    public void cheat() {
        if (nowPlaying && !mSuperCheating) {
            mStartTime -= TIME_COST_CHEAT;
            mCheatCount++;
            resetPick();
            setCardFocus(mSolversAnswer[0], CHEAT_COLOR);
            setCardFocus(mSolversAnswer[1], CHEAT_COLOR);
        }
    }

    @Override
    public void superCheat(boolean mode) {
        mSuperCheating = mode;
        resetPick();
        if (mSuperCheating) {
            mPickPosition[0] = mSolversAnswer[0];
            mPickPosition[1] = mSolversAnswer[1];
            resetPick();
        } else {
            //슈퍼치트를 끌 때, 슈퍼치트 사용에 의해 답이 없는 상태가 되었는지 검사해서 답이 없으면 재배치한다.
            if (!solver()) {
                shuffle();
            }
        }
    }
}
