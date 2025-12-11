package model.world;

import items.utensils.*;
import java.util.*;
import model.world.tiles.*;
import stations.*;
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

        // --- VALIDATION LOOP ---
        // Ulangi generate sampai map valid (ada Assembly Station)
        int attempts = 0;
        do {
            if (mapType == 1) {
                layout = generateStaticLayout();
            } else {
                int roomCount = (mapType == 2) ? 2 : 3;
                layout = generateRoomBasedLayout(roomCount);
            }
            attempts++;
        } while (!isValidMap(layout) && attempts < 100);

        parseMap(layout);
    }

    public WorldMap() { this(3); }

    // --- FIX 1: METHOD VALIDASI ---
    // Memastikan minimal ada 1 Assembly Station ('A') di peta
    private boolean isValidMap(char[][] layout) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (layout[y][x] == 'A') return true;
            }
        }
        System.out.println("⚠️ Map generation failed (No Assembly Station). Retrying...");
        return false;
    }

    // --- MAP TIPE 1: STATIC ---
    private char[][] generateStaticLayout() {
        String[] mapData = {
                "XXXXXXXXXXXXXX",
                "X............X",
                "X.I.C.L.C.I.AX", // FIX 2: Tambahkan 'A' di pojok kanan atas
                "X............X",
                "X.R........R.X",
                "X.V........V.X",
                "X............X",
                "X............X",
                "X.P..W..S..T.X",
                "XXXXXXXXXXXXXX"
        };
        char[][] layout = new char[height][width];
        for (int y=0; y<height; y++) layout[y] = mapData[y].toCharArray();
        return layout;
    }

    // --- ALGORITMA BARU: ROOM-BASED LAYOUT ---
    private char[][] generateRoomBasedLayout(int roomAttempts) {
        char[][] layout = new char[height][width];
        // 1. Isi Penuh Tembok
        for (char[] row : layout) Arrays.fill(row, 'X');

        Random rand = new Random();
        List<Position> floors = new ArrayList<>();

        // 2. Buat Ruangan Utama
        createRoom(layout, floors, width/2 - 3, height/2 - 2, 6, 4);

        // 3. Tambah Ruangan Tambahan
        for (int i = 0; i < roomAttempts; i++) {
            if (floors.isEmpty()) break;
            Position anchor = floors.get(rand.nextInt(floors.size()));

            int rw = rand.nextInt(3) + 3;
            int rh = rand.nextInt(3) + 3;

            int dir = rand.nextInt(4);
            int rx = anchor.x, ry = anchor.y;

            switch(dir) {
                case 0 -> ry -= rh;
                case 1 -> ry += 1;
                case 2 -> rx -= rw;
                case 3 -> rx += 1;
            }

            createRoom(layout, floors, rx, ry, rw, rh);
        }

        // 4. Tambahkan Kitchen Island
        addStructuredIslands(layout, floors);

        // 5. Identifikasi Dinding Perimeter
        List<Position> wallSpots = new ArrayList<>();
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (layout[y][x] == 'X' && hasFloorNeighbor(layout, x, y)) {
                    wallSpots.add(new Position(x, y));
                }
            }
        }

        // 6. Tempatkan Station
        placeStationsSmartly(layout, wallSpots);

        // 7. Spawn Points
        findSpawnPoints(layout, floors);

        return layout;
    }

    private void createRoom(char[][] layout, List<Position> floors, int x, int y, int w, int h) {
        if (x < 1) x = 1;
        if (y < 1) y = 1;
        if (x + w >= width - 1) w = width - 1 - x;
        if (y + h >= height - 1) h = height - 1 - y;

        for (int i = y; i < y + h; i++) {
            for (int j = x; j < x + w; j++) {
                if (layout[i][j] == 'X') {
                    layout[i][j] = '.';
                    floors.add(new Position(j, i));
                }
            }
        }
    }

    private void addStructuredIslands(char[][] layout, List<Position> floors) {
        Random rand = new Random();
        for (int y = 3; y < height - 3; y++) {
            for (int x = 3; x < width - 3; x++) {
                boolean areaClear = true;
                for (int dy = -1; dy <= 2; dy++) {
                    for (int dx = -1; dx <= 2; dx++) {
                        if (layout[y+dy][x+dx] != '.') areaClear = false;
                    }
                }

                if (areaClear && rand.nextDouble() > 0.6) {
                    layout[y][x] = 'A';
                    layout[y][x+1] = 'A';
                    if (rand.nextBoolean()) {
                        layout[y+1][x] = 'A';
                        layout[y+1][x+1] = 'A';
                    }
                    x += 3;
                }
            }
        }
    }

    private void placeStationsSmartly(char[][] layout, List<Position> wallSpots) {
        // FIX 3: Tambahkan 'A' ke dalam daftar essential (Wajib)
        List<Character> essential = new ArrayList<>();
        essential.add('A'); // <--- PENTING: Assembly Station Wajib Ada
        essential.add('L');
        for (int i=0; i<5; i++) essential.add('I');
        for (int i=0; i<4; i++) essential.add('R');
        essential.add('C'); essential.add('C'); essential.add('C');
        essential.add('W'); essential.add('W');
        essential.add('S'); essential.add('S');
        essential.add('P');
        essential.add('T');

        Collections.shuffle(wallSpots);

        // Isi station utama
        for (Character c : essential) {
            if (wallSpots.isEmpty()) break;
            Position bestSpot = wallSpots.remove(0);
            layout[bestSpot.y][bestSpot.x] = c;
        }

        // Isi sisa dinding
        Random rand = new Random();
        for (Position p : wallSpots) {
            if (layout[p.y][p.x] == 'X') {
                if (rand.nextDouble() > 0.3) {
                    layout[p.y][p.x] = 'A';
                }
            }
        }
    }

    private void findSpawnPoints(char[][] layout, List<Position> floors) {
        List<Position> candidates = new ArrayList<>();
        for (Position p : floors) {
            if (layout[p.y][p.x] == '.') candidates.add(p);
        }

        if (candidates.size() >= 2) {
            layout[candidates.get(0).y][candidates.get(0).x] = 'V';
            layout[candidates.get(candidates.size()-1).y][candidates.get(candidates.size()-1).x] = 'V';
        }
    }

    private boolean hasFloorNeighbor(char[][] layout, int x, int y) {
        if (layout[y-1][x] == '.') return true;
        if (layout[y+1][x] == '.') return true;
        if (layout[y][x-1] == '.') return true;
        if (layout[y][x+1] == '.') return true;
        return false;
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
                    case 'X': grid[y][x] = new WallTile(pos); wallMask[y][x] = true; break;
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
                    case 'S': grid[y][x] = new StationTile(pos, new ServingStation()); wallMask[y][x] = true; break;
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