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
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║          PASTA KITCHEN MAP                ║");
        System.out.println("╚════════════════════════════════════════════╝");

        // Column numbers
        System.out.print("   ");
        for (int x = 0; x < map.getWidth(); x++) {
            System.out.printf("%2d ", x + 1);
        }
        System.out.println();

        for (int y = 0; y < map.getHeight(); y++) {
            System.out.printf("%2d ", y + 1);

            for (int x = 0; x < map.getWidth(); x++) {
                Position p = new Position(x, y);

                boolean chefHere = false;
                for (int i = 0; i < chefs.length; i++) {
                    if (chefs[i].getPos().equals(p)) {
                        System.out.print(" C" + (i + 1));
                        chefHere = true;
                        break;
                    }
                }
                if (chefHere) continue;

                if (map.getStationAt(p) != null) {
                    Station st = map.getStationAt(p);
                    char code = stationSymbol(st);
                    System.out.print("  " + code);
                } else if (!map.isWalkable(p)) {
                    System.out.print("  X");
                } else if (map.peekItemAt(p) != null) {
                    System.out.print("  •");  // Item on floor
                } else {
                    System.out.print("  .");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    private char stationSymbol(Station st) {
        String name = st.getName().toLowerCase();
        if (name.contains("cut")) return 'C';
        if (name.contains("cook")) return 'R';
        if (name.contains("wash")) return 'W';
        if (name.contains("serv")) return 'S';
        if (name.contains("plate")) return 'P';
        if (name.contains("ingredient")) return 'I';
        if (name.contains("trash")) return 'T';
        if (name.contains("assembly")) return 'A';
        return '?';
    }
}