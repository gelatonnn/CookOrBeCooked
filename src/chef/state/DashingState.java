package chef.state;

import chef.Chef;
import input.Command;

public class DashingState extends ChefState {

    private int remainingSteps;

    public DashingState(int steps) {
        this.remainingSteps = steps;
    }

    @Override
    public void handleInput(Chef chef, Command cmd) {
        // biasanya ignore input ketika dash
    }

    @Override
    public void update(Chef chef) {
        // dash movement
        // if remainingSteps <= 0 → change to IdleState
    }
}
