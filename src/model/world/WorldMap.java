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

    public WorldMap() {
        this.grid = new Tile[height][width];
        this.wallMask = new boolean[height][width];
        setupMapResources();
        char[][] mapLayout = generateRandomLayout();
        parseMap(mapLayout);
    }

    public List<Position> getSpawnPoints() { return spawnPoints; }
    public boolean[][] getWallMask() { return wallMask; }

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

    private char[][] generateRandomLayout() {
        char[][] layout = new char[height][width];
        for (int y = 0; y < height; y++) Arrays.fill(layout[y], 'X');

        int cx = width / 2;
        int cy = height / 2;
        layout[cy][cx] = '.';
        
        int floorCount = 1;
        int targetFloor = (int) (width * height * 0.55);
        Random rand = new Random();

        int currX = cx, currY = cy;
        while (floorCount < targetFloor) {
            int dir = rand.nextInt(4);
            int nx = currX, ny = currY;
            switch(dir) { case 0->ny--; case 1->ny++; case 2->nx--; case 3->nx++; }

            if (nx > 0 && nx < width - 1 && ny > 0 && ny < height - 1) {
                if (layout[ny][nx] == 'X') {
                    layout[ny][nx] = '.';
                    floorCount++;
                }
                currX = nx;
                currY = ny;
            } else {
                currX = width / 2;
                currY = height / 2;
            }
        }

        List<Position> validStationSpots = new ArrayList<>();
        List<Position> floorSpots = new ArrayList<>();

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (layout[y][x] == '.') {
                    floorSpots.add(new Position(x, y));
                    checkAndAddWall(layout, x+1, y, validStationSpots);
                    checkAndAddWall(layout, x-1, y, validStationSpots);
                    checkAndAddWall(layout, x, y+1, validStationSpots);
                    checkAndAddWall(layout, x, y-1, validStationSpots);
                }
            }
        }
        
        Collections.shuffle(validStationSpots);
        Collections.shuffle(floorSpots);

        Queue<Character> stationsToPlace = new LinkedList<>();
        for (int i=0; i<5; i++) stationsToPlace.add('I');
        for (int i=0; i<4; i++) stationsToPlace.add('R');
        stationsToPlace.add('S'); stationsToPlace.add('S');
        stationsToPlace.add('W'); stationsToPlace.add('W');
        stationsToPlace.add('P'); stationsToPlace.add('T');
        stationsToPlace.add('C'); stationsToPlace.add('C'); stationsToPlace.add('C');

        for (Position p : validStationSpots) {
            if (!stationsToPlace.isEmpty()) {
                layout[p.y][p.x] = stationsToPlace.poll();
            } else {
                if (rand.nextBoolean()) layout[p.y][p.x] = 'A';
            }
        }

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
            boolean exists = false;
            for(Position existing : list) {
                if(existing.x == x && existing.y == y) { exists = true; break; }
            }
            if (!exists) list.add(p);
        }
    }

    private void parseMap(char[][] layout) {
        // FIX: Clear HARUS DILUAR LOOP
        spawnPoints.clear(); 

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char c = layout[y][x];
                Position pos = new Position(x, y);

                switch (c) {
                    case 'X': 
                        grid[y][x] = new WallTile(pos);
                        wallMask[y][x] = true;
                        break;
                    case '.':
                        grid[y][x] = new WalkableTile(pos);
                        break;
                    case 'V': 
                        grid[y][x] = new WalkableTile(pos);
                        spawnPoints.add(pos); // Simpan posisi spawn
                        break;
                    case 'C':
                        grid[y][x] = new StationTile(pos, new CuttingStation());
                        wallMask[y][x] = true;
                        break;
                    case 'R':
                        StationTile cookTile = new StationTile(pos, new CookingStation());
                        CookingStation cs = (CookingStation) cookTile.getStation();
                        items.core.CookingDevice device = deviceQueue.poll();
                        if (device == null) device = new items.utensils.FryingPan(); 
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
                        String ingType = ingredientQueue.poll();
                        if (ingType == null) ingType = "pasta"; 
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
    public Tile getTile(Position p) { return inBounds(p) ? grid[p.y][p.x] : null; }
    public boolean inBounds(Position p) { return p.x >= 0 && p.x < width && p.y >= 0 && p.y < height; }
    public boolean isWalkable(Position p) { return inBounds(p) && grid[p.y][p.x].isWalkable(); }
    public Station getStationAt(Position p) {
        if (!inBounds(p)) return null;
        Tile t = grid[p.y][p.x];
        return (t instanceof StationTile st) ? st.getStation() : null;
    }
}