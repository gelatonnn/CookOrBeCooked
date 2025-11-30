package chef.state;

import chef.Chef;
import input.Command;

public class IdleState extends ChefState {

    @Override
    public void handleInput(Chef chef, Command cmd) {
        // implementasi nanti
    }

    @Override
    public void update(Chef chef) {
        // idle no-op
    }
}
