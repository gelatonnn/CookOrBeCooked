package items.utensils;

import items.core.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import utils.TimerUtils;

public class BoilingPot extends UtensilBase implements CookingDevice {
    private final List<Preparable> contents = new ArrayList<>();
    private final int capacity = 3; // Kapasitas disesuaikan

    // Concurrency controls
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
        if (cooking) return false; // Tidak bisa tambah bahan saat sedang proses masak

        // Sesuai Spec Map B: Boiling Pot untuk Pasta
        // Kita cek nama itemnya mengandung "pasta"
        String n = ((Item)ingredient).getName().toLowerCase();
        return n.contains("pasta");
    }

    @Override
    public void addIngredient(Preparable ingredient) {
        if (canAccept(ingredient)) {
            contents.add(ingredient);
            // AUTO START: Sesuai spec, masak otomatis berjalan saat bahan masuk
            startCooking();
        }
    }

    @Override
    public void startCooking() {
        if (contents.isEmpty() || cooking) return;

        System.out.println("🔥 Boiling Pot mulai memasak (12 detik)...");
        cooking = true;

        // TASK 1: Masak sampai Matang (12 Detik)
        cookTask = TimerUtils.schedule(() -> {
            for (Preparable p : contents) {
                p.cook(); // Ubah state menjadi COOKED
            }
            System.out.println("✅ Boiling Pot: MATANG! (Segera angkat sebelum gosong)");

            // TASK 2: Gosong jika tidak diangkat (12 Detik setelah matang)
            scheduleBurn();
        }, 12000); // 12000 ms = 12 detik
    }

    private void scheduleBurn() {
        burnTask = TimerUtils.schedule(() -> {
            for (Preparable p : contents) {
                if (p instanceof Item i) {
                    i.setState(ItemState.BURNED);
                }
            }
            System.out.println("💀 Boiling Pot: GOSONG!");
            cooking = false; // Siklus masak selesai (karena sudah gosong)
        }, 12000); // 12 detik setelah matang (Total 24 detik)
    }

    @Override
    public void finishCooking() {
        // Method ini dipanggil saat alat diangkat oleh Chef
        // Kita batalkan timer supaya tidak lanjut gosong di tangan Chef
        if (cookTask != null && !cookTask.isDone()) {
            cookTask.cancel(false); // Batal masak jika belum matang
        }
        if (burnTask != null && !burnTask.isDone()) {
            burnTask.cancel(false); // Stop timer gosong
        }
        cooking = false;
    }

    @Override
    public List<Preparable> getContents() {
        return new ArrayList<>(contents);
    }

    @Override
    public void clearContents() {
        finishCooking(); // Pastikan timer mati
        contents.clear();
        cooking = false;
    }

    @Override
    public boolean isCooking() {
        return cooking;
    }

    @Override
    public String toString() {
        String status = "";
        if (cooking) status = " [COOKING...]";
        else if (!contents.isEmpty() && ((Item)contents.get(0)).getState() == ItemState.BURNED) status = " [BURNED]";
        else if (!contents.isEmpty() && ((Item)contents.get(0)).getState() == ItemState.COOKED) status = " [READY]";

        return getName() + " (" + contents.size() + "/" + capacity + ")" + status;
    }
}