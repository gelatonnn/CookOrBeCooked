package items.core;

import java.util.List;

public interface CookingDevice {
    boolean isPortable();
    int capacity();
    boolean canAccept(Preparable ingredient);
    void addIngredient(Preparable ingredient);
    void startCooking();
    void finishCooking();
    List<Preparable> getContents();
    void clearContents();
    boolean isCooking();
}