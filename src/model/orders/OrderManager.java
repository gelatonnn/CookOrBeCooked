package model.orders;

import model.recipes.*;
import java.util.*;

public class OrderManager {
    private final List<Order> active = new ArrayList<>();
    private final int max = 4;
    private int score = 0;

    public OrderManager(boolean backgroundThread) {}

    public void tick() {
        active.forEach(Order::tick);
        active.removeIf(Order::isExpired);

        if (active.size() < max) generateOrder();
    }

    private void generateOrder() {
        DishType type = RecipeBook.getRandomDish();
        Recipe recipe = RecipeBook.getRecipe(type);
        active.add(new Order(recipe, 60));
    }

    public boolean submitDish(DishType type) {
        for (Order o : active) {
            if (o.getRecipe().getType() == type && !o.isExpired()) {
                o.complete();
                score += 100;
                active.remove(o);
                return true;
            }
        }
        score -= 30;
        return false;
    }

    public List<Order> getActiveOrders() {
        return new ArrayList<>(active);
    }

    public int getScore() { return score; }
}