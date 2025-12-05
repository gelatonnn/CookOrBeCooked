package tile;

import chef.Chef;
import items.core.Item;
import model.Position;
public abstract class Station extends Tile {

    protected StationType type;
    protected Item storedItem;

    public Station(Position pos, StationType type) {
        super(pos, true);
        this.type = type;
        this.storedItem = null;
    }

    public abstract InteractionResult interact(Chef c);

    public Item getStoredItem() {
        return storedItem;
    }

    public void setStoredItem(Item item) {
        this.storedItem = item;
    }
}