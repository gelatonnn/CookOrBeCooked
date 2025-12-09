package stations;

import items.core.Item;
import items.core.ItemState;
import items.core.Preparable;

public class CuttingStation extends BaseStation {
    @Override
    public String getName() { return "Cutting Station"; }

    @Override
    public boolean canPlace(Item item) {
        if (item instanceof Preparable p) {
            Item i = (Item) p;
            // FIX PENTING: Cuma boleh taruh kalau statusnya RAW (Mentah)
            return i.getState() == ItemState.RAW && p.canBeChopped();
        }
        return false;
    }

    @Override
    public boolean place(Item item) {
        if (!canPlace(item)) {
            // Beri tahu kenapa gagal (berguna buat debug di console)
            System.out.println("❌ CuttingStation: Hanya menerima bahan mentah (RAW)!");
            return false;
        }
        System.out.println("📍 Menaruh " + item.getName() + " di Cutting Station");
        return super.place(item);
    }
}