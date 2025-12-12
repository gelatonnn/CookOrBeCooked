package items.utensils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import items.core.CookingDevice;
import items.core.Item;
import items.core.ItemState;
import items.core.Preparable;
import items.ingredients.IngredientBase;
import utils.TimerUtils;

public class FryingPan extends UtensilBase implements CookingDevice {
    private final List<Preparable> contents = new ArrayList<>();
    private final int capacity = 1; 

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
        return item.getState() == ItemState.CHOPPED && !item.getName().toLowerCase().contains("pasta");
    }

    @Override
    public void addIngredient(Preparable ingredient) {
        if (canAccept(ingredient)) {
            contents.add(ingredient);
            startCooking();
        }
    }

    @Override
    public void startCooking() {
        if (contents.isEmpty() || cooking) return;
        view.gui.AssetManager.getInstance().playSound("fry");

        System.out.println("🔥 Frying Pan mulai menggoreng (12 detik)...");
        cooking = true;

        cookTask = TimerUtils.schedule(() -> {
            if (!cooking) return;
            for (Preparable p : contents) p.cook();
            System.out.println("✅ Frying Pan: MATANG! (Angkat sebelum gosong)");
            scheduleBurn();
        }, 12000);
    }

    private void scheduleBurn() {
        burnTask = TimerUtils.schedule(() -> {
            if (!cooking) return;
            for (Preparable p : contents) {
                if (p instanceof IngredientBase ib) ib.burn();
                else if (p instanceof Item i) i.setState(ItemState.BURNED);
            }
            System.out.println("💀 Frying Pan: GOSONG!");
        }, 12000);
    }

    @Override
    public void finishCooking() {
        if (cookTask != null) cookTask.cancel(false);
        if (burnTask != null) burnTask.cancel(false);
        cooking = false;
        System.out.println("🔕 Api Frying Pan mati.");
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
            else if (cooking) status = " [FRYING...]";
        }
        return getName() + " (" + contents.size() + "/" + capacity + ")" + status;
    }
}