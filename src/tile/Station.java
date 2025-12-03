package tile;


import chef.Chef;
import items.core.Item;
import model.Position;


public abstract class Station extends Tile {
    protected Station type;  
    protected Item storedItem;   

    public Station(Position pos, Station type) {
        super(pos, true); 
        this.type = type;
        this.storedItem = null;
    }

    public abstract void interact(Chef chef);

    public Station getType() {
        return type;
    }

    public void setStoredItem(Item item) {
        this.storedItem = item;
    }

    public Item getStoredItem() {
        return storedItem;
    }
}
