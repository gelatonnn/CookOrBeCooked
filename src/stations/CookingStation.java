package stations;

import items.core.CookingDevice;
import items.core.Item;
import items.core.Preparable;

public class CookingStation extends BaseStation {
    @Override
    public String getName() { return "Cooking Station"; }

    @Override
    public boolean canPlace(Item item) {
        if (storedItem == null) return item instanceof CookingDevice;
        if (storedItem instanceof CookingDevice device && item instanceof Preparable prep) {
            return device.canAccept(prep);
        }
        return false;
    }

    @Override
    public boolean place(Item item) {
        // --- BAGIAN INI YANG DIPERBAIKI ---
        if (storedItem == null) {
            // 1. Coba taruh item dulu menggunakan logika parent
            if (super.place(item)) {

                // 2. Jika berhasil ditaruh, cek apakah itu CookingDevice
                if (storedItem instanceof CookingDevice device) {
                    // 3. Cek apakah ada isinya dan belum gosong
                    // Jika ya, nyalakan kembali apinya (Resume Cooking)
                    if (!device.getContents().isEmpty() &&
                            device.getState() != items.core.ItemState.BURNED) {
                        device.startCooking();
                    }
                }
                return true;
            }
            return false;
        }
        // ----------------------------------

        // Logika menaruh bahan ke dalam panci (tidak berubah)
        if (storedItem instanceof CookingDevice device && item instanceof Preparable prep) {
            if (device.canAccept(prep)) {
                device.addIngredient(prep);
                return true;
            }
        }
        return false;
    }

    @Override
    public Item pick() {
        Item item = super.pick();

        if (item instanceof CookingDevice device) {
            device.finishCooking();
        }

        return item;
    }
}