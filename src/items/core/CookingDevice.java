package items.core;

public interface CookingDevice {

    boolean isPortable();    // pot & pan = true, oven = false
    int capacity();
    boolean canAccept(Preparable ingredient);

    void addIngredient(Preparable ingredient);
    void startCooking();
}
