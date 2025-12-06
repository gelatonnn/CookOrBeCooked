package items.core;

public abstract class Item {
    protected boolean portable = true;
    protected ItemState state = ItemState.RAW;

    public boolean isPortable() {
        return portable;
    }

    public ItemState getState() {
        return state;
    }

    public void setState(ItemState s) {
        this.state = s;
    }

    public abstract String getName();

    @Override
    public String toString() {
        return getName() + " (" + state + ")";
    }
}