package puzzle.card.set.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import puzzle.card.set.databinding.HomeActivityBinding;
import puzzle.card.set.game.GameActivity;
import puzzle.card.set.help.HelpActivity;
import puzzle.card.set.R;


public class HomeActivity extends AppCompatActivity implements HomeViewModel.ViewContract {
    private HomeActivityBinding mBinding;
    private HomeViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.home_activity);
        mViewModel = new HomeViewModel(this);
        mBinding.setHomeViewModel(mViewModel);
    }

    @Override
    public void goGame() {
        startActivity(new Intent(this, GameActivity.class));
    }

    @Override
    public void goHelp() {
        startActivity(new Intent(this, HelpActivity.class));
    }
}
