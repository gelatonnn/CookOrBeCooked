package tile;

import chef.Chef;
import model.Position;
import items.core.CookingDevice;


public class CookingStation extends Station {
    private CookingDevice cookingDevice; 

    public CookingStation(Position pos, CookingDevice device) {
        super(pos, null);
        this.cookingDevice = device;
    }

    @Override
    public void interact(Chef chef) {
        System.out.println("Chef " + chef.getName() + " memasak bahan di Cooking Station.");
        cookingDevice.startCooking();
    }

    @Override
    public void onEnter(Chef chef) {
        // Handle chef entering the cooking station
    }
}
