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
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║           CHEF STATUS                     ║");
        System.out.println("╚════════════════════════════════════════════╝");

        for (int i = 0; i < chefs.length; i++) {
            Chef c = chefs[i];
            Item held = c.getHeldItem();
            String active = (i == activeChefIndex) ? " ⭐ ACTIVE" : "";

            System.out.printf("  👨‍🍳 %s [%s]%s\n",
                    c.getName(),
                    c.getCurrentAction(),
                    active);
            System.out.printf("     Position: (%d,%d) facing %s\n",
                    c.getX() + 1,
                    c.getY() + 1,
                    c.getDirection());

            if (held == null) {
                System.out.println("     Holding: Empty hands");
            } else {
                System.out.printf("     Holding: %s [%s]\n",
                        held.getName(),
                        held.getState());
            }
            System.out.println();
        }
        System.out.println("════════════════════════════════════════════\n");
    }
}