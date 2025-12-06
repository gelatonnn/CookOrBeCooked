package view;

import model.world.WorldMap;
import model.chef.Chef;
import stations.Station;
import utils.Position;

public class ConsoleRenderer implements Observer {
    private final WorldMap map;
    private final Chef[] chefs;

    public ConsoleRenderer(WorldMap map, Chef[] chefs) {
        this.map = map;
        this.chefs = chefs;
    }

    @Override
    public void update() {
        render();
    }

    public void render() {
        System.out.println("\n=== CURRENT WORLD STATE ===");

        int height = map.getHeight();
        int width  = map.getWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Position p = new Position(x, y);

                boolean chefHere = false;
                for (int i = 0; i < chefs.length; i++) {
                    if (chefs[i].getPos().equals(p)) {
                        System.out.print(" C" + (i+1));
                        chefHere = true;
                        break;
                    }
                }
                if (chefHere) continue;

                if (map.getStationAt(p) != null) {
                    Station st = map.getStationAt(p);
                    char code = stationSymbol(st);
                    System.out.print(" " + code + " ");
                }
                else if (!map.isWalkable(p)) {
                    System.out.print(" X ");
                }
                else {
                    System.out.print(" . ");
                }
            }
            System.out.println();
        }

        System.out.println("==========================\n");
    }

    private char stationSymbol(Station st) {
        String name = st.getName().toLowerCase();
        if (name.contains("cut")) return 'C';
        if (name.contains("cook") || name.contains("stove")) return 'R';
        if (name.contains("wash") || name.contains("sink")) return 'W';
        if (name.contains("serve")) return 'S';
        if (name.contains("plate")) return 'P';
        if (name.contains("ingredient")) return 'I';
        if (name.contains("trash")) return 'T';
        if (name.contains("assembly")) return 'A';
        return '?';
    }
}