package model.world.tiles;

import items.core.Item;
import model.world.Tile;
import utils.Position;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WalkableTile extends Tile {
    // Thread-safe list untuk menyimpan banyak item + koordinatnya
    private final List<DroppedItem> itemsOnFloor = new CopyOnWriteArrayList<>();

    public WalkableTile(Position pos) {
        super(pos);
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    // --- Inner Class untuk menyimpan Item + Koordinat Pixel ---
    public static class DroppedItem {
        public final Item item;
        public final double x, y; // Koordinat World

        public DroppedItem(Item item, double x, double y) {
            this.item = item;
            this.x = x;
            this.y = y;
        }
    }

    // Tambah item di posisi spesifik
    public void addItem(Item item, double x, double y) {
        if (item != null) {
            itemsOnFloor.add(new DroppedItem(item, x, y));
        }
    }

    // Hapus item spesifik (dipanggil oleh GameEngine saat diambil)
    public void removeItem(DroppedItem droppedItem) {
        itemsOnFloor.remove(droppedItem);
    }

    // Ambil semua item untuk rendering/logic collision
    public List<DroppedItem> getItems() {
        return itemsOnFloor;
    }

    // Mencari item terdekat dari titik (x, y) dalam radius tertentu
    public DroppedItem pickNearest(double x, double y, double radius) {
        DroppedItem nearest = null;
        double minDst = Double.MAX_VALUE;

        for (DroppedItem di : itemsOnFloor) {
            double dist = Math.sqrt(Math.pow(di.x - x, 2) + Math.pow(di.y - y, 2));
            if (dist <= radius && dist < minDst) {
                minDst = dist;
                nearest = di;
            }
        }
        return nearest;
    }

    // --- Compatibility Methods (Agar kode lama/station logic tidak error) ---

    // Method lama: ambil item sembarang (biasanya yg pertama)
    public Item getItem() {
        return itemsOnFloor.isEmpty() ? null : itemsOnFloor.get(0).item;
    }

    // Method lama: ambil dan hapus item pertama
    public Item pick() {
        if (!itemsOnFloor.isEmpty()) {
            DroppedItem di = itemsOnFloor.get(0);
            itemsOnFloor.remove(0);
            return di.item;
        }
        return null;
    }

    // Method lama: taruh di tengah tile (fallback)
    public void setItem(Item item) {
        if (item != null) {
            addItem(item, pos.x + 0.5, pos.y + 0.5);
        }
    }
}