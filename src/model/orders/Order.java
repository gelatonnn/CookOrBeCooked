package model.orders;

import model.recipes.Recipe;

public class Order {
    private static int nextId = 1;

    private final int orderId;
    private final Recipe recipe;
    private int timeLeft;
    private boolean completed = false;
    private boolean expired = false;

    public Order(Recipe recipe, int timeLimit) {
        this.orderId = nextId++;
        this.recipe = recipe;
        this.timeLeft = timeLimit;
    }

    public void tick() {
        if (completed || expired) return;
        timeLeft--;
        if (timeLeft <= 0) {
            expired = true;
        }
    }

    public boolean isExpired() { return expired; }
    public boolean isCompleted() { return completed; }
    public void complete() { completed = true; }
    public Recipe getRecipe() { return recipe; }
    public int getTimeLeft() { return timeLeft; }
    public int getOrderId() { return orderId; }

    @Override
    public String toString() {
        return "Order #" + orderId + ": " + recipe.getName() +
                " (" + timeLeft + "s remaining)";
    }
}