package chef;

import chef.state.ChefState;
import chef.state.IdleState;
import items.core.Item;
import model.Position;
import model.Direction; 

public class Chef {

    private String id;
    private String name;
    private Position position;
    private Direction direction;
    private Item inventory;
    private ChefState currentState;
    private boolean active;

    public Chef(String id, String name, Position startPos) {
        this.id = id;
        this.name = name;
        this.position = startPos;
        this.direction = Direction.UP;
        this.inventory = null;
        this.currentState = new IdleState();
        this.active = true;
    }

    public boolean move(Direction d) {
        return false;
    }

    public void changeState(ChefState newState) {
        this.currentState = newState;
    }

    public void interact() {
        // panggil state
        // implementasi nanti
    }

    public void pickUp(Item i) {
        // implementasi nanti
    }

    public void putDown() {
        // implementasi nanti
    }

    public void throwTo(Position target) {
        // implementasi nanti
    }

    // getters & setters
    public Position getPosition() {
        return position;
    }

    public Item getInventory() {
        return inventory;
    }

    public ChefState getCurrentState() {
        return currentState;
    }
}
