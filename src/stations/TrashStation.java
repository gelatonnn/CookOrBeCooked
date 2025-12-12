package stations;

import items.core.CookingDevice;
import items.core.Item;
import view.gui.AssetManager; // Jangan lupa import

public class TrashStation implements Station {
    @Override
    public String getName() { return "Trash Station"; }

    @Override
    public boolean canPlace(Item item) {
        return true;
    }

    @Override
    public boolean place(Item item) {
        AssetManager audio = AssetManager.getInstance();

        if (item instanceof CookingDevice dev) {
            if (!dev.getContents().isEmpty()) {
                dev.clearContents();
                System.out.println("🗑️  Emptied " + item.getName() + " contents into trash");
                
                audio.playSound("trash"); 
            }
            return false; 
        }

        System.out.println("🗑️  Threw away " + item.getName());
        
        audio.playSound("trash"); 
        
        return true;
    }

    @Override public Item pick() { return null; }
    @Override public Item peek() { return null; }
    @Override public boolean isOccupied() { return false; }
}