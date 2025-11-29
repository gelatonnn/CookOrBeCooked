package items.core;

public interface Preparable {
    boolean canBeChopped();
    boolean canBeCooked();
    boolean canBePlacedOnPlate();

    void chop();
    void cook();
}