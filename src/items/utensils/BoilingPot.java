package items.utensils;

import items.core.*;
import items.ingredients.IngredientBase; 
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import utils.TimerUtils;

public class BoilingPot extends UtensilBase implements CookingDevice {
    private final List<Preparable> contents = new ArrayList<>();
    private final int capacity = 3;

    // Timer controls
    private boolean cooking = false;
    private ScheduledFuture<?> cookTask;
    private ScheduledFuture<?> burnTask;

    @Override
    public String getName() { return "Boiling Pot"; }
    @Override
    public boolean isPortable() { return true; }
    @Override
    public int capacity() { return capacity; }

    @Override
    public boolean canAccept(Preparable ingredient) {
        if (contents.size() >= capacity) return false;
        if (cooking) return false; // Jangan terima bahan kalau api sudah nyala
        
        // Map B: Panci Rebus hanya untuk Pasta
        String n = ((Item)ingredient).getName().toLowerCase();
        return n.contains("pasta");
    }

    @Override
    public void addIngredient(Preparable ingredient) {
        if (canAccept(ingredient)) {
            contents.add(ingredient);
            // AUTO START: Api otomatis nyala pas bahan masuk
            startCooking();
        }
    }

    @Override
    public void startCooking() {
        if (contents.isEmpty() || cooking) return;
        view.gui.AssetManager.getInstance().playSound("boil");
        System.out.println("🔥 Boiling Pot mulai merebus (12 detik)...");
        cooking = true;

        // TASK 1: OTW MATANG
        cookTask = TimerUtils.schedule(() -> {
            if (!cooking) return;

            // Matangkan semua isi
            for (Preparable p : contents) {
                p.cook(); 
            }
            System.out.println("✅ Boiling Pot: MATANG! (Angkat sebelum gosong)");
            
            // Lanjut jadwalkan gosong
            scheduleBurn(); 
            
        }, 12000); // 12 Detik
    }

    private void scheduleBurn() {
        // TASK 2: OTW GOSONG
        burnTask = TimerUtils.schedule(() -> {
            if (!cooking) return;

            for (Preparable p : contents) {
                if (p instanceof IngredientBase ib) {
                    ib.burn();
                } else if (p instanceof Item i) {
                     i.setState(ItemState.BURNED);
                }
            }
            System.out.println("💀 Boiling Pot: GOSONG!");
            
            // Kita matikan flag cooking, tapi api imajiner tetap nyala sampai diangkat
            // cooking = false; (Opsional, tergantung mau pemain matikan manual atau tidak)
        }, 12000); // 12 Detik setelah matang
    }

    @Override
    public void finishCooking() {
        // Matikan semua timer kalau panci diangkat
        if (cookTask != null && !cookTask.isDone()) cookTask.cancel(false);
        if (burnTask != null && !burnTask.isDone()) burnTask.cancel(false);
        
        cooking = false;
        System.out.println("🔕 Api Boiling Pot mati.");
    }

    @Override
    public List<Preparable> getContents() { return new ArrayList<>(contents); }

    @Override
    public void clearContents() {
        finishCooking();
        contents.clear();
    }

    @Override
    public boolean isCooking() { return cooking; }

    @Override
    public String toString() {
        String status = "";
        if (!contents.isEmpty()) {
            ItemState s = ((Item)contents.get(0)).getState();
            if (s == ItemState.COOKED) status = " [READY]";
            else if (s == ItemState.BURNED) status = " [GOSONG]";
            else if (cooking) status = " [COOKING...]";
        }
        return getName() + " (" + contents.size() + "/" + capacity + ")" + status;
    }
}