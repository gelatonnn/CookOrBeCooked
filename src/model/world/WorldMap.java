package model.world;

import items.utensils.*;
import java.util.*;
import model.world.tiles.*;
import stations.*; // Import utensils
import utils.Position;

public class WorldMap {
    private final int width = 14;
    private final int height = 10;
    private final Tile[][] grid;
    private final boolean[][] wallMask;
    
    // Antrean untuk spawn objek dinamis (agar tidak hardcoded koordinatnya)
    private final Queue<String> ingredientQueue = new LinkedList<>();
    private final Queue<items.core.CookingDevice> deviceQueue = new LinkedList<>();

    private final List<Position> spawnPoints = new ArrayList<>();

    // Constructor Default (Langsung Generate Random Map)
    public WorldMap() {
        this.grid = new Tile[height][width];
        this.wallMask = new boolean[height][width];
        
        // 1. Siapkan resource yang dibutuhkan Map B (Pasta)
        setupMapResources();
        
        // 2. Generate Layout Map secara Random
        char[][] mapLayout = generateRandomLayout();
        
        // 3. Parse karakter menjadi Object Tile
        parseMap(mapLayout);
    }

    public List<Position> getSpawnPoints() {
        return spawnPoints;
    }

    public boolean[][] getWallMask() {
        return wallMask;
    }

    // --- LOGIKA PERSIAPAN RESOURCE (Map Type B Spec) ---
    private void setupMapResources() {
        // Reset Queue
        ingredientQueue.clear();
        deviceQueue.clear();

        // 1. Masukkan Bahan Wajib Map B (Pasta)
        List<String> ingredients = Arrays.asList("tomato", "meat", "pasta", "shrimp", "fish");
        Collections.shuffle(ingredients); // Acak urutan bahan
        ingredientQueue.addAll(ingredients);

        // 2. Masukkan Alat Masak Wajib Map B
        // Spec: 2 Boiling Pot, 2 Frying Pan
        List<items.core.CookingDevice> devices = new ArrayList<>();
        devices.add(new BoilingPot());
        devices.add(new BoilingPot());
        devices.add(new FryingPan());
        devices.add(new FryingPan());
        Collections.shuffle(devices); // Acak posisi alat masak
        deviceQueue.addAll(devices);
    }

    // --- BONUS: RANDOM LEVEL GENERATOR ---
    private char[][] generateRandomLayout() {
        char[][] layout = new char[height][width];

        // Step 1: Isi semua dengan Dinding ('X')
        for (int y = 0; y < height; y++) {
            Arrays.fill(layout[y], 'X');
        }

        // Step 2: Algoritma Random Walk untuk membuat Lantai ('.')
        // Mulai dari tengah
        int cx = width / 2;
        int cy = height / 2;
        layout[cy][cx] = '.';
        
        int floorCount = 1;
        int targetFloor = (int) (width * height * 0.55); // 55% area harus bisa jalan (tingkat kesulitan)
        Random rand = new Random();

        int currX = cx, currY = cy;
        
        while (floorCount < targetFloor) {
            // Pilih arah acak
            int dir = rand.nextInt(4); // 0: Up, 1: Down, 2: Left, 3: Right
            int nx = currX, ny = currY;

            switch(dir) {
                case 0 -> ny--;
                case 1 -> ny++;
                case 2 -> nx--;
                case 3 -> nx++;
            }

            // Cek bounds (sisakan border 1 tile di tepi map agar tidak keluar)
            if (nx > 0 && nx < width - 1 && ny > 0 && ny < height - 1) {
                if (layout[ny][nx] == 'X') {
                    layout[ny][nx] = '.';
                    floorCount++;
                }
                currX = nx;
                currY = ny;
            } else {
                // Reset ke tengah jika mentok pinggir (agar map lebih membulat/pusat)
                currX = width / 2;
                currY = height / 2;
            }
        }

        // Step 3: Identifikasi Dinding yang bisa diakses (Adjacent Walls)
        // Ini adalah tempat potensial untuk menaruh Station
        List<Position> validStationSpots = new ArrayList<>();
        List<Position> floorSpots = new ArrayList<>();

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (layout[y][x] == '.') {
                    floorSpots.add(new Position(x, y));
                    // Cek tetangga untuk mencari dinding
                    checkAndAddWall(layout, x+1, y, validStationSpots);
                    checkAndAddWall(layout, x-1, y, validStationSpots);
                    checkAndAddWall(layout, x, y+1, validStationSpots);
                    checkAndAddWall(layout, x, y-1, validStationSpots);
                }
            }
        }
        
        Collections.shuffle(validStationSpots);
        Collections.shuffle(floorSpots);

        // Step 4: Tempatkan Station Wajib
        // Kita butuh: 5 Ing, 4 Cook, 2 Serve, 1 Wash, 1 Plate, 1 Trash, 3 Cutting, Sisa Assembly
        Queue<Character> stationsToPlace = new LinkedList<>();
        
        // Masukkan station wajib ke antrean
        for (int i=0; i<5; i++) stationsToPlace.add('I'); // 5 Ingredients
        for (int i=0; i<4; i++) stationsToPlace.add('R'); // 4 Cooking (2 Pot + 2 Pan)
        stationsToPlace.add('S'); // 1 Serving
        stationsToPlace.add('S'); // 1 Serving (Extra agar mudah)
        stationsToPlace.add('W'); // 1 Washing
        stationsToPlace.add('W'); // 1 Washing (Extra)
        stationsToPlace.add('P'); // 1 Plate Storage
        stationsToPlace.add('T'); // 1 Trash
        stationsToPlace.add('C'); // Cutting
        stationsToPlace.add('C'); // Cutting
        stationsToPlace.add('C'); // Cutting

        // Taruh station di dinding yang valid
        for (Position p : validStationSpots) {
            if (!stationsToPlace.isEmpty()) {
                layout[p.y][p.x] = stationsToPlace.poll();
            } else {
                // Jika station wajib habis, sisanya jadi Assembly (A) atau Wall (X)
                // 50% chance jadi Assembly agar meja kerja banyak
                if (rand.nextBoolean()) {
                    layout[p.y][p.x] = 'A';
                }
            }
        }

        // Step 5: Tentukan Spawn Point Chef ('V')
        // Ambil 2 titik lantai acak
        if (floorSpots.size() >= 2) {
            Position p1 = floorSpots.get(0);
            Position p2 = floorSpots.get(floorSpots.size() - 1);
            layout[p1.y][p1.x] = 'V';
            layout[p2.y][p2.x] = 'V';
        }

        return layout;
    }

    private void checkAndAddWall(char[][] layout, int x, int y, List<Position> list) {
        if (layout[y][x] == 'X') {
            Position p = new Position(x, y);
            // Cek agar tidak duplikat (secara sederhana, list contains pakai equals)
            boolean exists = false;
            for(Position existing : list) {
                if(existing.x == x && existing.y == y) {
                    exists = true; 
                    break;
                }
            }
            if (!exists) list.add(p);
        }
    }

    // --- PARSING LOGIC (Updated) ---
    private void parseMap(char[][] layout) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char c = layout[y][x];
                Position pos = new Position(x, y);
                spawnPoints.clear();

                switch (c) {
                    case 'X': // Wall murni (bukan station)
                        grid[y][x] = new WallTile(pos);
                        wallMask[y][x] = true;
                        break;

                    case '.': // Lantai
                    case 'V': // Spawn point (dianggap lantai saat init, chef diatur GameEngine)
                        grid[y][x] = new WalkableTile(pos);
                        spawnPoints.add(pos);
                        break;

                    case 'C':
                        grid[y][x] = new StationTile(pos, new CuttingStation());
                        wallMask[y][x] = true;
                        break;

                    case 'R':
                        StationTile cookTile = new StationTile(pos, new CookingStation());
                        CookingStation cs = (CookingStation) cookTile.getStation();
                        
                        // LOGIC BARU: Ambil device dari Queue yang sudah diacak
                        items.core.CookingDevice device = deviceQueue.poll();
                        if (device == null) device = new items.utensils.FryingPan(); // Fallback
                        
                        cs.place((items.core.Item) device);
                        grid[y][x] = cookTile;
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
                        // LOGIC BARU: Ambil bahan dari Queue yang sudah diacak
                        String ingType = ingredientQueue.poll();
                        if (ingType == null) ingType = "pasta"; // Fallback aman
                        
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