package items.utensils;

import items.core.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import utils.TimerUtils;

public class FryingPan extends UtensilBase implements CookingDevice {
    private final List<Preparable> contents = new ArrayList<>();
    private final int capacity = 1; // Biasanya Pan kapasitasnya lebih sedikit untuk Map B

    // Concurrency controls
    private boolean cooking = false;
    private ScheduledFuture<?> cookTask;
    private ScheduledFuture<?> burnTask;

    @Override
    public String getName() { return "Frying Pan"; }

    @Override
    public boolean isPortable() { return true; }

    @Override
    public int capacity() { return capacity; }

    @Override
    public boolean canAccept(Preparable ingredient) {
        if (contents.size() >= capacity) return false;
        if (cooking) return false;
        
        Item item = (Item) ingredient;
        // Frying Pan menerima bahan CHOPPED (Meat, Fish, Shrimp, Tomato)
        // Tidak menerima Pasta (karena Pasta butuh air/pot)
        return item.getState() == ItemState.CHOPPED && !item.getName().toLowerCase().contains("pasta");
    }

    @Override
    public void addIngredient(Preparable ingredient) {
        if (canAccept(ingredient)) {
            contents.add(ingredient);
            startCooking(); // Auto start
        }
    }

    @Override
    public void startCooking() {
        if (contents.isEmpty() || cooking) return;

        System.out.println("🔥 Frying Pan mulai menggoreng (12 detik)...");
        cooking = true;

        // TASK 1: Matang
        cookTask = TimerUtils.schedule(() -> {
            for (Preparable p : contents) p.cook();
            System.out.println("✅ Frying Pan: MATANG! (Segera angkat sebelum gosong)");
            scheduleBurn();
        }, 12000); 
    }

    private void scheduleBurn() {
        // TASK 2: Gosong
        burnTask = TimerUtils.schedule(() -> {
            for (Preparable p : contents) {
                if (p instanceof Item i) i.setState(ItemState.BURNED);
            }
            System.out.println("💀 Frying Pan: GOSONG!");
            cooking = false;
        }, 12000);
    }

    @Override
    public void finishCooking() {
        if (cookTask != null) cookTask.cancel(false);
        if (burnTask != null) burnTask.cancel(false);
        cooking = false;
    }

    @Override
    public List<Preparable> getContents() {
        return new ArrayList<>(contents);
    }

    @Override
    public void clearContents() {
        finishCooking();
        contents.clear();
        cooking = false;
    }

    @Override
    public boolean isCooking() { return cooking; }

    @Override
    public String toString() {
        String status = "";
        if (cooking) status = " [FRYING...]";
        else if (!contents.isEmpty() && ((Item)contents.get(0)).getState() == ItemState.BURNED) status = " [BURNED]";
        else if (!contents.isEmpty() && ((Item)contents.get(0)).getState() == ItemState.COOKED) status = " [READY]";

        return getName() + " (" + contents.size() + "/" + capacity + ")" + status;
    }
}