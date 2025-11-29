package items.core;

public abstract class Item {

    protected boolean portable = true;     // default item bisa dipegang
    protected ItemState state = ItemState.RAW;

    public boolean isPortable() {
        return portable;
    }

    public ItemState getState() {
        return state;
    }

    public void setState(ItemState state) {
        this.state = state;
    }

    public abstract String getName();

    @Override
    public String toString() {
        return getName() + " (" + state + ")";
    }
}
