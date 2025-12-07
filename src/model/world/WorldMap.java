package model.world;

import model.world.tiles.*;
import stations.*;
import items.core.Item;
import utils.Position;

import java.util.HashMap;
import java.util.Map;

public class WorldMap {
    private final int width;
    private final int height;
    private final Tile[][] grid;
    private final boolean[][] wallMask;
    private final Map<Position, Item> itemsOnFloor = new HashMap<>();

    // CORRECT LAYOUT from specification
    private final String[] MAP = {
            "AARRAAXXXXXXXX",  // Row 1
            "I....AXXX....W",  // Row 2
            "I....AXXX....W",  // Row 3
            "I.V..AXXX....A",  // Row 4
            "A....XXXX....R",  // Row 5
            "P....XXXC....R",  // Row 6
            "S....XXXC..V.I",  // Row 7
            "S....XXXA....I",  // Row 8
            "A.............T",  // Row 9
            "XXXXXXXXXXXXXX"   // Row 10
    };

    public WorldMap() {
        this.height = MAP.length;
        this.width = 14; // Fixed width
        this.grid = new Tile[height][width];
        this.wallMask = new boolean[height][width];
        parseMap();
    }

    public boolean[][] getWallMask() {
        return wallMask;
    }

    private void parseMap() {
        for (int y = 0; y < height; y++) {
            String row = MAP[y];
            for (int x = 0; x < width; x++) {
                char c = (x < row.length()) ? row.charAt(x) : '.';
                Position pos = new Position(x, y);

                switch (c) {
                    case 'X', 'x':
                        grid[y][x] = new WallTile(pos);
                        wallMask[y][x] = true;
                        break;

                    case '.', 'V':
                        grid[y][x] = new WalkableTile(pos);
                        break;

                    case 'C':
                        grid[y][x] = new StationTile(pos, new CuttingStation());
                        wallMask[y][x] = true;
                        break;

                    case 'R':
                        grid[y][x] = new StationTile(pos, new CookingStation());
                        wallMask[y][x] = true;
                        break;

                    case 'W':
                        grid[y][x] = new StationTile(pos, new WashingStation());
                        wallMask[y][x] = true;
                        break;

                    case 'S':
                        grid[y][x] = new StationTile(pos, new ServingStation());
                        wallMask[y][x] = true;
                        break;

                    case 'I':
                        // Multiple ingredient storages with different types
                        String ingredientType = determineIngredientType(x, y);
                        grid[y][x] = new StationTile(pos, new IngredientStorage(ingredientType));
                        wallMask[y][x] = true;
                        break;

                    case 'A':
                        grid[y][x] = new StationTile(pos, new AssemblyStation());
                        wallMask[y][x] = true;
                        break;

                    case 'P':
                        grid[y][x] = new StationTile(pos, new PlateStorage());
                        wallMask[y][x] = true;
                        break;

                    case 'T':
                        grid[y][x] = new StationTile(pos, new TrashStation());
                        wallMask[y][x] = true;
                        break;

                    default:
                        grid[y][x] = new WalkableTile(pos);
                }
            }
        }
    }

    private String determineIngredientType(int x, int y) {
        // Distribute different ingredients across storages
        if (y <= 2) return "pasta";      // Top left storages
        if (y == 3) return "tomato";
        if (y == 6) return "fish";       // Right side storage
        if (y == 7) return "shrimp";
        return "meat";                    // Default
    }

    // Floor item management
    public void placeItemAt(Position p, Item item) {
        itemsOnFloor.put(p, item);
    }

    public Item pickItemAt(Position p) {
        return itemsOnFloor.remove(p);
    }

    public Item peekItemAt(Position p) {
        return itemsOnFloor.get(p);
    }

    public int getWidth()  { return width; }
    public int getHeight() { return height; }

    public Tile getTile(Position p) {
        if (!inBounds(p)) return null;
        return grid[p.y][p.x];
    }

    public boolean inBounds(Position p) {
        return p.x >= 0 && p.x < width && p.y >= 0 && p.y < height;
    }

    public boolean isWalkable(Position p) {
        if (!inBounds(p)) return false;
        return grid[p.y][p.x].isWalkable();
    }

    public Station getStationAt(Position p) {
        if (!inBounds(p)) return null;
        Tile t = grid[p.y][p.x];

        if (t instanceof StationTile st) {
            return st.getStation();
        }
        return null;
    }
}
