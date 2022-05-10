package puzzle.card.set.result;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import puzzle.card.set.R;
import puzzle.card.set.databinding.ResultActivityBinding;

public class ResultActivity extends AppCompatActivity implements ResultViewModel.ViewContract {

    public static final String RESULT = "result";
    public static final String RESULT_POINT = "result_point";
    public static final String RESULT_MISS = "result_miss";
    public static final String RESULT_TIME = "result_time";
    public static final String RESULT_REMAIN = "result_remain";
    public static final String RESULT_SHUFFLE = "result_shuffle";
    public static final String RESULT_CHEAT = "result_cheat";
    public static final String RESULT_SUPER_CHEAT = "result_super_cheat";
    private ResultActivityBinding mBinding;
    private ResultViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.result_activity);
        mViewModel = new ResultViewModel(this);
        mBinding.setResultViewModel(mViewModel);

        unpackData();
    }

    private void unpackData() {
        Intent intent = getIntent();
        mViewModel.result.set(intent.getStringExtra(RESULT));
        mViewModel.resultRemainCard.set(intent.getStringExtra(RESULT_REMAIN));
        mViewModel.resultPoint.set(intent.getStringExtra(RESULT_POINT));
        mViewModel.resultMiss.set(intent.getStringExtra(RESULT_MISS));
        mViewModel.resultTime.set(intent.getStringExtra(RESULT_TIME));
        mViewModel.resultShuffleCount.set(intent.getStringExtra(RESULT_SHUFFLE));
        mViewModel.resultCheatCount.set(intent.getStringExtra(RESULT_CHEAT));
        mViewModel.resultSuperCheatCount.set(intent.getStringExtra(RESULT_SUPER_CHEAT));
    }
}
