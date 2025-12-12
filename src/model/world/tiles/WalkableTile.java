package model.world.tiles;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import items.core.Item;
import model.world.Tile;
import utils.Position;

public class WalkableTile extends Tile {
    private final List<DroppedItem> itemsOnFloor = new CopyOnWriteArrayList<>();

    public WalkableTile(Position pos) {
        super(pos);
    }

    @Override
    public boolean isWalkable() {
        return true;
    }
    public static class DroppedItem {
        public final Item item;
        public final double x, y; 

        public DroppedItem(Item item, double x, double y) {
            this.item = item;
            this.x = x;
            this.y = y;
        }
    }

    public void addItem(Item item, double x, double y) {
        if (item != null) {
            itemsOnFloor.add(new DroppedItem(item, x, y));
        }
    }

    public void removeItem(DroppedItem droppedItem) {
        itemsOnFloor.remove(droppedItem);
    }

    public List<DroppedItem> getItems() {
        return itemsOnFloor;
    }

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


    public Item getItem() {
        return itemsOnFloor.isEmpty() ? null : itemsOnFloor.get(0).item;
    }

    public Item pick() {
        if (!itemsOnFloor.isEmpty()) {
            DroppedItem di = itemsOnFloor.get(0);
            itemsOnFloor.remove(0);
            return di.item;
        }
        return null;
    }

    public void setItem(Item item) {
        if (item != null) {
            addItem(item, pos.x + 0.5, pos.y + 0.5);
        }
    }
}