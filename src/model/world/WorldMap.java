package model.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import items.utensils.BoilingPot;
import items.utensils.FryingPan;
import model.world.tiles.StationTile;
import model.world.tiles.WalkableTile;
import model.world.tiles.WallTile;
import stations.AssemblyStation;
import stations.CookingStation;
import stations.CuttingStation;
import stations.IngredientStorage;
import stations.LuckyStation;
import stations.PlateStorage;
import stations.ServingStation;
import stations.Station;
import stations.TrashStation;
import stations.WashingStation;
import utils.Position;

public class WorldMap {
    private final int width = 14;
    private final int height = 10;
    private final Tile[][] grid;
    private final boolean[][] wallMask;

    private final Queue<String> ingredientQueue = new LinkedList<>();
    private final Queue<items.core.CookingDevice> deviceQueue = new LinkedList<>();
    private final List<Position> spawnPoints = new ArrayList<>();

    public WorldMap(int mapType) {
        this.grid = new Tile[height][width];
        this.wallMask = new boolean[height][width];

        setupMapResources();

        char[][] layout;
        int attempts = 0;
        do {
            if (mapType == 1) {
                layout = generateStaticLayout();
            } else {
                int roomCount = (mapType == 2) ? 2 : 3;
                layout = generateOrganicLayout(roomCount);
            }
            attempts++;
        } while (!isValidMap(layout) && attempts < 100);

        parseMap(layout);
    }

    public WorldMap() { this(3); }

    private boolean isValidMap(char[][] layout) {
        boolean hasAssembly = false;
        int floorCount = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (layout[y][x] == 'A') hasAssembly = true;
                if (layout[y][x] == '.') floorCount++;
            }
        }
        return hasAssembly && floorCount > 15;
    }

    private char[][] generateStaticLayout() {
        String[] mapData = {
                "              ",
                "  XXXXXXXXXX  ",
                "  X.I.C.L.IX  ",
                "  X........X  ",
                "  X.R....R.X  ",
                "  X.V....V.X  ",
                "  X........X  ",
                "  X.P.W.S.AX  ",
                "  XXXXXXXXXX  ",
                "              "
        };
        char[][] layout = new char[height][width];
        for (int y=0; y<height; y++) {
            String row = (y < mapData.length) ? mapData[y] : "";
            for (int x=0; x<width; x++) {
                layout[y][x] = (x < row.length()) ? row.charAt(x) : ' ';
            }
        }
        return layout;
    }

    private char[][] generateOrganicLayout(int complexity) {
        char[][] layout = new char[height][width];

        // 1. Inisialisasi VOID (' ')
        for (char[] row : layout) Arrays.fill(row, ' ');

        Random rand = new Random();
        List<Position> floors = new ArrayList<>();

        // 2. Buat Lantai (Padding Aman)
        int centerX = width / 2;
        int centerY = height / 2;

        fillArea(layout, floors, centerX - 2, centerY - 2, 5, 4, '.');

        for (int i = 0; i < complexity + 4; i++) {
            if (floors.isEmpty()) break;
            Position anchor = floors.get(rand.nextInt(floors.size()));

            int rw = rand.nextInt(3) + 2;
            int rh = rand.nextInt(3) + 2;
            int nx = anchor.x - rand.nextInt(rw);
            int ny = anchor.y - rand.nextInt(rh);

            fillArea(layout, floors, nx, ny, rw, rh, '.');
        }

        // 3. BANGUN BORDER DENGAN STRICT CORNER CHECK
        List<Position> stationSpots = new ArrayList<>();
        buildStrictBorder(layout, stationSpots);

        // 4. Kitchen Island (Meja Tengah)
        addStructuredIslands(layout, floors);

        // 5. Tempatkan Station
        placeStationsSmartly(layout, stationSpots);

        // 6. Spawn Points
        findSpawnPoints(layout, floors);

        return layout;
    }

    private void fillArea(char[][] layout, List<Position> floors, int x, int y, int w, int h, char type) {
        int startX = Math.max(2, x);
        int startY = Math.max(2, y);
        int endX = Math.min(width - 2, x + w);
        int endY = Math.min(height - 2, y + h);

        for (int i = startY; i < endY; i++) {
            for (int j = startX; j < endX; j++) {
                layout[i][j] = type;
                if (type == '.') floors.add(new Position(j, i));
            }
        }
    }

    private void buildStrictBorder(char[][] layout, List<Position> stationSpots) {
        char[][] temp = new char[height][width];
        for(int y=0; y<height; y++) temp[y] = layout[y].clone();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (layout[y][x] == ' ') { 
                    int floorNeighbors = countCardinalFloorNeighbors(temp, x, y);
                    boolean diagonal = hasDiagonalFloor(temp, x, y);

                    if (floorNeighbors == 1) {
                        layout[y][x] = '?';
                        stationSpots.add(new Position(x, y));

                    } else if (floorNeighbors >= 2) {
                        layout[y][x] = 'X';

                    } else if (diagonal) {
                        layout[y][x] = 'X';
                    }
                }
            }
        }
    }

    private int countCardinalFloorNeighbors(char[][] grid, int x, int y) {
        int count = 0;
        if (y > 0 && grid[y-1][x] == '.') count++;
        if (y < height-1 && grid[y+1][x] == '.') count++; 
        if (x > 0 && grid[y][x-1] == '.') count++; 
        if (x < width-1 && grid[y][x+1] == '.') count++; 
        return count;
    }

    private boolean hasDiagonalFloor(char[][] grid, int x, int y) {
        if (y > 0 && x > 0 && grid[y-1][x-1] == '.') return true;
        if (y > 0 && x < width-1 && grid[y-1][x+1] == '.') return true;
        if (y < height-1 && x > 0 && grid[y+1][x-1] == '.') return true;
        if (y < height-1 && x < width-1 && grid[y+1][x+1] == '.') return true;
        return false;
    }

    private void addStructuredIslands(char[][] layout, List<Position> floors) {
        Random rand = new Random();
        int attempts = 0, placed = 0;
        while (attempts < 50 && placed < 1) {
            if (floors.isEmpty()) break;
            Position p = floors.get(rand.nextInt(floors.size()));
            boolean clean = true;
            for(int dy=-1; dy<=1; dy++) {
                for(int dx=-1; dx<=1; dx++) {
                    if (layout[p.y+dy][p.x+dx] != '.') clean = false;
                }
            }
            if (clean) {
                layout[p.y][p.x] = 'A';
                if (layout[p.y][p.x+1] == '.') layout[p.y][p.x+1] = 'A';
                placed++;
            }
            attempts++;
        }
    }

    private void placeStationsSmartly(char[][] layout, List<Position> spots) {
        List<Character> essential = new ArrayList<>();
        essential.add('A'); essential.add('L');
        for (int i=0; i<5; i++) essential.add('I');
        for (int i=0; i<4; i++) essential.add('R');
        essential.add('C'); essential.add('C'); essential.add('C');
        essential.add('W'); essential.add('W');
        essential.add('S'); essential.add('S');
        essential.add('P'); essential.add('T');

        Collections.shuffle(spots);

        for (Character c : essential) {
            if (spots.isEmpty()) break;
            Position p = spots.remove(0);
            layout[p.y][p.x] = c;
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (layout[y][x] == '?') {
                    layout[y][x] = 'A';
                }
            }
        }
    }

    private void findSpawnPoints(char[][] layout, List<Position> floors) {
        List<Position> valid = new ArrayList<>();
        for (Position p : floors) {
            if (layout[p.y][p.x] == '.') valid.add(p);
        }
        if (valid.size() >= 2) {
            layout[valid.get(0).y][valid.get(0).x] = 'V';
            layout[valid.get(valid.size()-1).y][valid.get(valid.size()-1).x] = 'V';
        }
    }

    private void setupMapResources() {
        ingredientQueue.clear();
        deviceQueue.clear();
        List<String> ingredients = Arrays.asList("tomato", "meat", "pasta", "shrimp", "fish");
        Collections.shuffle(ingredients);
        ingredientQueue.addAll(ingredients);

        List<items.core.CookingDevice> devices = new ArrayList<>();
        devices.add(new BoilingPot());
        devices.add(new BoilingPot());
        devices.add(new FryingPan());
        devices.add(new FryingPan());
        Collections.shuffle(devices);
        deviceQueue.addAll(devices);
    }

    private void parseMap(char[][] layout) {
        spawnPoints.clear();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char c = layout[y][x];
                Position pos = new Position(x, y);
                switch (c) {
                    case ' ':
                        grid[y][x] = new WallTile(pos);
                        wallMask[y][x] = false;
                        break;
                    case 'X': // Tembok Mati (Corner)
                        grid[y][x] = new WallTile(pos);
                        wallMask[y][x] = true;
                        break;
                    case '?': // Fallback jika masih ada
                        grid[y][x] = new StationTile(pos, new AssemblyStation());
                        wallMask[y][x] = true;
                        break;
                    case '.': grid[y][x] = new WalkableTile(pos); break;
                    case 'V': grid[y][x] = new WalkableTile(pos); spawnPoints.add(pos); break;
                    case 'L': grid[y][x] = new StationTile(pos, new LuckyStation()); wallMask[y][x] = true; break;
                    case 'C': grid[y][x] = new StationTile(pos, new CuttingStation()); wallMask[y][x] = true; break;
                    case 'R':
                        StationTile cookTile = new StationTile(pos, new CookingStation());
                        items.core.CookingDevice dev = deviceQueue.poll();
                        if (dev==null) dev=new FryingPan();
                        ((CookingStation)cookTile.getStation()).place((items.core.Item)dev);
                        grid[y][x] = cookTile; wallMask[y][x] = true; break;
                    case 'W': grid[y][x] = new StationTile(pos, new WashingStation()); wallMask[y][x] = true; break;
                    case 'S':
                        grid[y][x] = new StationTile(pos, new ServingStation()); wallMask[y][x] = true; break;
                    case 'I':
                        String type = ingredientQueue.poll();
                        grid[y][x] = new StationTile(pos, new IngredientStorage(type != null ? type : "pasta"));
                        wallMask[y][x] = true; break;
                    case 'A': grid[y][x] = new StationTile(pos, new AssemblyStation()); wallMask[y][x] = true; break;
                    case 'P': grid[y][x] = new StationTile(pos, new PlateStorage()); wallMask[y][x] = true; break;
                    case 'T': grid[y][x] = new StationTile(pos, new TrashStation()); wallMask[y][x] = true; break;
                    default: grid[y][x] = new WalkableTile(pos);
                }
            }
        }
    }

    public List<Position> getSpawnPoints() { return spawnPoints; }
    public boolean[][] getWallMask() { return wallMask; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Tile getTile(Position p) { return (p.x>=0 && p.x<width && p.y>=0 && p.y<height) ? grid[p.y][p.x] : null; }
    public boolean inBounds(Position p) { return p.x>=0 && p.x<width && p.y>=0 && p.y<height; }
    public boolean isWalkable(Position p) { return inBounds(p) && grid[p.y][p.x].isWalkable(); }
    public Station getStationAt(Position p) {
        Tile t = getTile(p);
        return (t instanceof StationTile st) ? st.getStation() : null;
    }
}