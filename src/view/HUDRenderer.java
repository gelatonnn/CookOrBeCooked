package view;

import model.chef.Chef;
import items.core.Item;

public class HUDRenderer implements Observer {
    private final Chef[] chefs;
    private int activeChefIndex = 0;

    public HUDRenderer(Chef[] chefs) {
        this.chefs = chefs;
    }

    public void setActiveChefIndex(int idx) {
        this.activeChefIndex = idx;
    }

    @Override
    public void update() {
        render();
    }

    public void render() {
        System.out.println("\n=== CHEF STATUS ===");

        for (int i = 0; i < chefs.length; i++) {
            Chef c = chefs[i];
            Item held = c.getHeldItem();

            System.out.print("Chef " + (i+1) + (i == activeChefIndex ? " [ACTIVE]" : "") + " : ");

            if (held == null) {
                System.out.println("Empty-handed");
            } else {
                System.out.println("Holding → " + held.getName() + " (" + held.getState() + ")");
            }
        }

        System.out.println("====================\n");
    }
}