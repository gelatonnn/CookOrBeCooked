package tile;

import chef.Chef;
import items.core.CookingDevice;
import model.Position;
import java.util.List;

public class CookingStation extends Station {
    private List<CookingDevice> deviceOnStove;

    public CookingStation(Position pos, List<CookingDevice> deviceOnStove) {
        super(pos, StationType.COOKING);
        this.deviceOnStove = deviceOnStove;
    }

    @Override
    public InteractionResult interact(Chef c) {
        if (c.getHeldItem() instanceof CookingDevice) {
            CookingDevice device = (CookingDevice) c.getHeldItem();
            if (deviceOnStove.contains(device)) {
                device.startCooking();
                return new InteractionResult(true, "Memasak menggunakan " + device.getName());
            }
        }
        return new InteractionResult(false, "Alat masak tidak cocok.");
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public void onEnter(Chef chef) {
        System.out.println("Chef " + chef.getName() + " memasuki Cooking Station.");
    }
}
