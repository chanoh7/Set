package puzzle.card.set.game;

public class Card {
    public static final int TYPE_COLOR = 0;
    public static final int TYPE_SHAPE = 1;
    public static final int TYPE_FILL = 2;
    public static final int TYPE_NUMBER = 3;

    public enum ATTRIBUTE {
        VALUE_1, VALUE_2, VALUE_3;

        public static ATTRIBUTE fromInteger(int x) {
            switch (x) {
                case 0:
                    return VALUE_1;//green,     dia,    full,   1
                case 1:
                    return VALUE_2;//purple,    round,  null,   2
                case 2:
                    return VALUE_3;//red,       wave,   stripe, 3
            }
            return null;
        }
    }

    private final ATTRIBUTE[] mAttributes;

    Card(int color, int shape, int fill, int number) {
        mAttributes = new ATTRIBUTE[]{
                ATTRIBUTE.fromInteger(color),
                ATTRIBUTE.fromInteger(shape),
                ATTRIBUTE.fromInteger(fill),
                ATTRIBUTE.fromInteger(number)
        };
    }

    public int getAttribute(int type) {
        return mAttributes[type].ordinal();
    }
}
