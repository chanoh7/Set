package puzzle.card.set.home;

import android.view.View;

public class HomeViewModel {
    private final ViewContract mViewContract;

    interface ViewContract{
        void goGame();
        void goHelp();
    }

    HomeViewModel(ViewContract viewContract) {
        mViewContract = viewContract;
    }

    public void onClickStartGame(View view){
        mViewContract.goGame();
    }

    public void onClickHelp(View view){
        mViewContract.goHelp();
    }
}
