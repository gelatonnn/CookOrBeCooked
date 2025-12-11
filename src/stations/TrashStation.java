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
        // Panggil Sound Manager
        AssetManager audio = AssetManager.getInstance();

        if (item instanceof CookingDevice dev) {
            if (!dev.getContents().isEmpty()) {
                dev.clearContents();
                System.out.println("🗑️  Emptied " + item.getName() + " contents into trash");
                
                // Bunyi sampah saat mengosongkan panci
                audio.playSound("trash"); 
            }
            return false; 
        }

        System.out.println("🗑️  Threw away " + item.getName());
        
        // Bunyi sampah saat membuang item
        audio.playSound("trash"); 
        
        return true;
    }

    // ... (sisa method pick, peek, isOccupied tetap sama) ...
    @Override public Item pick() { return null; }
    @Override public Item peek() { return null; }
    @Override public boolean isOccupied() { return false; }
}