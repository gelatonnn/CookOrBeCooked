package model;

public class GameEngine implements Runnable {
    private static GameEngine instance;
    private boolean isRunning;
    private Thread gameThread;
    private WorldMap worldMap;
    
    // Settingan Waktu: 60 FPS
    private final int FPS = 60;
    private final long TARGET_TIME = 1000 / FPS;

    // Private Constructor (Singleton)
    private GameEngine() {
        // Inisialisasi Peta 14x10 sesuai spek tubes
        worldMap = new WorldMap(14, 10);
    }

    public static synchronized GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start(); // Ini akan memanggil method run() di thread baru
        System.out.println("Game Engine Thread Started!");
    }

    public void stop() {
        isRunning = false;
    }

    @Override
    public void run() {
        long start, elapsed, wait;

        while (isRunning) {
            start = System.nanoTime();

            update(); // Logika game berjalan di sini
            
            // render(); // Nanti di sini update GUI

            elapsed = System.nanoTime() - start;
            wait = TARGET_TIME - (elapsed / 1000000);

            if (wait < 0) wait = 5;

            try {
                Thread.sleep(wait); // Jeda agar CPU tidak panas (menjaga 60 FPS)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        // Tempat logika: Gerakan Chef, Timer Masak, dll.
        // Kosong dulu untuk tahap pondasi
    }
    
    public WorldMap getWorldMap() {
        return worldMap;
    }
}