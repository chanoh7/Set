package puzzle.card.set.game;

import android.graphics.Color;
import android.view.View;
import android.widget.ToggleButton;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

public class GameViewModel {
    public final ObservableBoolean superCheatEnabled = new ObservableBoolean(false);
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

        enableSuperCheat(false);
    }

    public void onClickCheat(View view) {
        mViewContract.cheat();
    }

    public void onClickShuffle(View view) {
        mViewContract.reShuffle();
    }

    public void onClickSuperCheat(View view) {
        enableSuperCheat(((ToggleButton) view).isChecked());
    }

    public void enableSuperCheat(boolean enable) {
        superCheatEnabled.set(enable);
        superCheatBgColor.set(enable ? Color.RED : Color.DKGRAY);
        superCheatTextColor.set(enable ? Color.YELLOW : Color.WHITE);
        mViewContract.superCheat(enable);
    }
}