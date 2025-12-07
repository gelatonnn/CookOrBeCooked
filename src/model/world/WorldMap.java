package model.world;

import model.world.tiles.*;
import stations.*;
import utils.Position;

public class WorldMap {
    private final int width;
    private final int height;
    private final Tile[][] grid;
    private final boolean[][] wallMask;

    // Layout Map berdasarkan Spec Type B: Pasta Map
    private final String[] MAP = {
            "AARRAAXXXXXXXX",
            "I....AXXX...W.", // y=1: Ingredient Storage 1
            "I....AXxX...W.", // y=2: Ingredient Storage 2
            "I.V..AXXX...A.", // y=3: Ingredient Storage 3
            "A....XXXX...R.",
            "P....XXC....R.",
            "S....XXC..V.I.", // y=6: Ingredient Storage 4
            "S....XXA....I.", // y=7: Ingredient Storage 5
            "A..........T.",
            "XXXXXXXXXXXXXX"
    };

    public WorldMap() {
        this.height = MAP.length;
        this.width = MAP[0].length();
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
                    case 'X':
                    case 'x':
                        grid[y][x] = new WallTile(pos);
                        wallMask[y][x] = true;
                        break;

                    case '.':
                    case 'V':
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
                        // LOGIKA BARU: Mapping Bahan berdasarkan Spec Map B (Pasta)
                        // Kiri Atas (x=0): Tomato, Meat, Pasta
                        // Kanan Bawah (x=13): Shrimp, Fish

                        String ingType = "pasta"; // Default fallback

                        if (x == 0 && y == 1) ingType = "tomato";
                        else if (x == 0 && y == 2) ingType = "meat";
                        else if (x == 0 && y == 3) ingType = "pasta";
                        else if (x == 13 && y == 6) ingType = "shrimp";
                        else if (x == 13 && y == 7) ingType = "fish";

                        grid[y][x] = new StationTile(pos, new IngredientStorage(ingType));
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