package puzzle.card.set.game;

import java.util.ArrayList;
import java.util.List;

public class CardManager {
    public static final int KIND_OF_VALUES = Card.ATTRIBUTE.values().length;
    private final List<Card> deck;

    CardManager() {
        deck = new ArrayList<>();
    }

    public void reset() {
        deck.clear();

        for (int i = 0; i < KIND_OF_VALUES; ++i) {
            for (int j = 0; j < KIND_OF_VALUES; ++j) {
                for (int k = 0; k < KIND_OF_VALUES; ++k) {
                    for (int l = 0; l < KIND_OF_VALUES; ++l) {
                        deck.add(new Card(i, j, k, l));
                    }
                }
            }
        }

    }

    public Card draw() {
        if (deck.size() == 0)
            return null;

        int selected = (int) (Math.random() * deck.size());
        return deck.remove(selected);
    }

    public int remain() {
        return deck.size();
    }

    public void bounce(Card card) {
        deck.add(card);
    }
}
