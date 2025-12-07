package stations;

import items.core.Item;
import items.core.ItemState; // Import ItemState
import items.core.Preparable;

public class CuttingStation extends BaseStation {
    @Override
    public String getName() { return "Cutting Station"; }

    @Override
    public boolean canPlace(Item item) {
        // Cek apakah item bisa dipotong
        if (item instanceof Preparable p && p.canBeChopped()) {
            // FIX: Hanya terima jika statusnya RAW
            // Jika sudah CHOPPED, tolak (return false)
            if (item.getState() != ItemState.RAW) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean place(Item item) {
        return super.place(item);
    }
}