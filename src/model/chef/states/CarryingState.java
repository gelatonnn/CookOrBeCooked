package model.chef.states;

import items.core.Item;
import items.core.Preparable;
import items.utensils.Plate;
import model.chef.Chef;
import model.chef.ChefState;
import stations.Station;

public class CarryingState implements ChefState {
    @Override
    public void enter(Chef chef) {}

    @Override
    public void move(Chef chef, int dx, int dy) {
        chef.changeState(new MovingState(this));
        chef.move(dx, dy);
    }

    @Override
    public void pickItem(Chef chef, Item item) {
        System.out.println("⚠ Hands are full! Drop current item first.");
    }

    @Override
    public void placeItem(Chef chef, Station st) {
        if (st == null || chef.getHeldItem() == null) return;

        Item held = chef.getHeldItem();

        if (st.getName().toLowerCase().contains("assembly")) {
            handlePlating(chef, st, held);
            view.gui.AssetManager.getInstance().playSound("place");
            return;
        }

        if (st.getName().toLowerCase().contains("serv")) {
            handleServing(chef, st, held);
            return;
        }

        if (!st.canPlace(held)) {
            System.out.println("❌ Cannot place " + held.getName() + " on " + st.getName());
            return;
        }

        if (st.place(held)) {
            chef.setHeldItem(null);
            chef.changeState(new IdleState());
            view.gui.AssetManager.getInstance().playSound("place");
            System.out.println("✅ Placed " + held.getName() + " on " + st.getName());
        }
    }

    private void handlePlating(Chef chef, Station st, Item held) {
        if (held instanceof Plate plate) {
            if (!plate.isClean()) {
                System.out.println("❌ Cannot use dirty plate! Wash it first.");
                return;
            }
            st.place(held);
            chef.setHeldItem(null);
            chef.changeState(new IdleState());
            System.out.println("✅ Placed clean plate on Assembly Station");
        } else if (held instanceof Preparable) {
            Item onStation = st.peek();
            if (onStation instanceof Plate plate && plate.isClean()) {
                plate.addIngredient((Preparable) held);
                chef.setHeldItem(null);
                chef.changeState(new IdleState());
                System.out.println("✅ Added " + held.getName() + " to plate");
            } else {
                System.out.println("❌ Need clean plate on station first!");
            }
        }
    }

    private void handleServing(Chef chef, Station st, Item held) {
        if (held instanceof Plate plate) {
            if (plate.getContents().isEmpty()) {
                System.out.println("❌ Cannot serve empty plate!");
                return;
            }

            st.place(held);
            chef.setHeldItem(null);
            chef.changeState(new IdleState());
            System.out.println("🍽️  Served dish to customer!");
        } else {
            System.out.println("❌ Must serve on a plate!");
        }
    }

    @Override
    public void interact(Chef chef, Station st) {
        placeItem(chef, st);
    }
}
