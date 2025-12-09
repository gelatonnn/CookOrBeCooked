package stations;

import items.core.Item;
import items.utensils.DirtyPlate;
import items.utensils.Plate;

public class WashingStation extends BaseStation {
    private boolean isWashing = false;
    private long washStartTime = 0;
    private final int WASH_DURATION = 3000; // 3 detik mencuci

    @Override
    public String getName() { return "Washing Station"; }

    @Override
    public boolean canPlace(Item item) {
        // Hanya terima piring kotor jika station sedang kosong
        return storedItem == null && item instanceof DirtyPlate;
    }

    @Override
    public boolean place(Item item) {
        if (canPlace(item)) {
            super.place(item);
            startWashing(); // Mulai cuci otomatis
            return true;
        }
        return false;
    }

    private void startWashing() {
        isWashing = true;
        washStartTime = System.currentTimeMillis();
        
        // Gunakan timer sederhana (bisa juga pakai ScheduledExecutor seperti CookingStation)
        // Di sini kita pakai Thread simple untuk demo, atau cek di tick()
        new Thread(() -> {
            try {
                Thread.sleep(WASH_DURATION);
                finishWashing();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void finishWashing() {
        if (storedItem instanceof DirtyPlate) {
            // Ubah DirtyPlate menjadi Plate bersih
            storedItem = new Plate();
            isWashing = false;
            System.out.println("Washing complete! Plate is clean.");
        }
    }

    // Untuk keperluan visual (opsional)
    public boolean isWashing() {
        return isWashing;
    }
}