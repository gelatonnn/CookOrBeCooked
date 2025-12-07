package stations;

import items.core.CookingDevice;
import items.core.Item;

public class CookingStation extends BaseStation {
    @Override
    public String getName() { return "Cooking Station"; }

    @Override
    public boolean canPlace(Item item) {
        return item instanceof CookingDevice;
    }

    @Override
    public boolean place(Item item) {
        if (!canPlace(item)) {
            System.out.println("❌ Only cooking devices (pots/pans) can be placed on stove!");
            return false;
        }

        boolean result = super.place(item);
        if (result && item instanceof CookingDevice dev) {
            if (!dev.getContents().isEmpty()) {
                System.out.println("🔥 Cooking device placed on stove. Ready to start cooking!");
            } else {
                System.out.println("📍 Empty cooking device placed on stove. Add ingredients first.");
            }
        }
        return result;
    }
}