package tile;


import items.core.Item;
import items.dish.DishBase;
import items.dish.DishState;
import chef.Chef;


public class ServingStation extends Station {
    public ServingStation(Position pos) {
        super(pos, StationType.SERVING);
    }

    @Override
    public InteractionResult interact(Chef c) {
        if (c.getHeldItem() == null || !(c.getHeldItem() instanceof DishBase)) {
            return new InteractionResult(false, "Tidak ada hidangan untuk disajikan.");
        }

        DishBase dish = (DishBase) c.getHeldItem();
        if (dish.getDishState() == DishState.COMPLETE) {
            // Menyajikan dish
            return new InteractionResult(true, "Hidangan berhasil disajikan.");
        } else {
            return new InteractionResult(false, "Hidangan belum lengkap.");
        }
    }
    
    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public void onEnter(Chef chef) {
        System.out.println("Chef " + chef.getName() + " memasuki Serving Station.");
    }
}
