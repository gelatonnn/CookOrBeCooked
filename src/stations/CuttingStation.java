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
            if (i.getState() != ItemState.RAW) {
                return false;
            }
            return p.canBeChopped();
        }
        return false;
    }

    @Override
    public boolean place(Item item) {
        if (!canPlace(item)) {
            System.out.println("❌ CuttingStation: Hanya menerima bahan MENTAH!");
            return false;
        }
        
        view.gui.AssetManager.getInstance().playSound("place");
        
        System.out.println("📍 Menaruh " + item.getName() + " di Cutting Station");
        return super.place(item);
    }
}