package tile;

import items.core.Item;
import chef.Chef;
import model.Position;

public class TrashStation extends Station {
    public TrashStation(Position pos) {
        super(pos, StationType.TRASH);
    }

    @Override
    public InteractionResult interact(Chef c) {
        if (c.getHeldItem() == null) {
            return new InteractionResult(false, "Tidak ada item yang dibuang.");
        }

        c.setHeldItem(null);
        return new InteractionResult(true, "Item berhasil dibuang.");
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public void onEnter(Chef chef) {
        System.out.println("Chef " + chef.getName() + " memasuki Trash Station.");
    }
}