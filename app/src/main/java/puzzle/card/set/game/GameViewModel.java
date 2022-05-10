package puzzle.card.set.game;

import android.graphics.Color;
import android.view.View;
import android.widget.ToggleButton;

import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

public class GameViewModel {
    public final ObservableInt startButtonVisibility = new ObservableInt(View.VISIBLE);
    public final ObservableInt superCheatTextColor = new ObservableInt(Color.WHITE);
    public final ObservableInt superCheatBgColor = new ObservableInt(Color.DKGRAY);

    public final ObservableField<String> remainTime = new ObservableField<>("00 : 00");
    public final ObservableField<String> remainCard = new ObservableField<>("81");
    public final ObservableField<String> point = new ObservableField<>("00");

    private final ViewContract mViewContract;

    interface ViewContract {
        void init();

        void fieldSetUp();

        void reShuffle();

        void cheat();

        void superCheat(boolean mode);
    }

    GameViewModel(ViewContract viewContract) {
        mViewContract = viewContract;
    }

    public void onClickStart(View view) {
        startButtonVisibility.set(View.GONE);
        mViewContract.init();
        mViewContract.fieldSetUp();
    }

    public void onClickCheat(View view) {
        mViewContract.cheat();
    }

    public void onClickShuffle(View view) {
        mViewContract.reShuffle();
    }

    public void onClickSuperCheat(View view) {
        boolean checked = ((ToggleButton) view).isChecked();

        superCheatBgColor.set(checked ? Color.RED : Color.DKGRAY);
        superCheatTextColor.set(checked ? Color.YELLOW : Color.WHITE);
        mViewContract.superCheat(checked);
    }
}