package puzzle.card.set.result;

import androidx.databinding.ObservableField;

public class ResultViewModel {
    public final ObservableField<String> result = new ObservableField<>("");
    public final ObservableField<String> resultPoint = new ObservableField<>("");
    public final ObservableField<String> resultMiss = new ObservableField<>("");
    public final ObservableField<String> resultTime = new ObservableField<>("");
    public final ObservableField<String> resultRemainCard = new ObservableField<>("");
    public final ObservableField<String> resultShuffleCount = new ObservableField<>("");
    public final ObservableField<String> resultCheatCount = new ObservableField<>("");
    public final ObservableField<String> resultSuperCheatCount = new ObservableField<>("");

    private final ViewContract mViewContract;

    interface ViewContract {

    }

    ResultViewModel(ViewContract viewContract) {
        mViewContract = viewContract;
    }


}
