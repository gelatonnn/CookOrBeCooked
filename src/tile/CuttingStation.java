package tile;

import chef.Chef;
import model.Position;
import items.core.Preparable;


public class CuttingStation extends Station {
    public CuttingStation(Position pos) {
        super(pos, StationType.CUTTING);
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public InteractionResult interact(Chef c) {
        if (c.getHeldItem() == null) {
            return new InteractionResult(false, "Tidak ada bahan yang dipegang.");
        }

        if (!(c.getHeldItem() instanceof Preparable)) {
            return new InteractionResult(false, "Item yang dipegang tidak bisa dipotong.");
        }

        Preparable ingredient = (Preparable) c.getHeldItem();
        ingredient.chop();
        return new InteractionResult(true, "Item berhasil dipotong.");
    }

    @Override
    public void onEnter(Chef chef) {
        System.out.println("Chef " + chef.getName() + " memasuki Cutting Station.");
    }

}
