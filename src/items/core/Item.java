package items.core;

public abstract class Item {
    protected boolean portable = true;
    protected ItemState state = ItemState.RAW;

    // Ukuran default item dalam unit (misal 0.4 berarti 40% dari lebar tile)
    protected double size = 0.4;

    public boolean isPortable() {
        return portable;
    }

    public ItemState getState() {
        return state;
    }

    public void setState(ItemState s) {
        this.state = s;
    }

    public double getSize() {
        return size;
    }

    public abstract String getName();

    @Override
    public String toString() {
        return getName() + " (" + state + ")";
    }
}