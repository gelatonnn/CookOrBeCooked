package model.orders;

import model.recipes.*;
import utils.TimerUtils;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

public class OrderManager {
    private final List<Order> active = new ArrayList<>();
    private final int max = 4;
    private int score = 0;
    private ScheduledFuture<?> tickTask;

    public OrderManager(boolean startThread) {
        if (startThread) {
            // Start independent timer thread (1 tick per second)
            tickTask = TimerUtils.repeat(() -> tick(), 1000);
        }
    }

    public void tick() {
        // Tick all active orders
        active.forEach(Order::tick);

        // Remove expired orders and apply penalty
        List<Order> expired = active.stream()
                .filter(Order::isExpired)
                .toList();

        for (Order o : expired) {
            System.out.println("⏰ Order #" + o.getOrderId() + " EXPIRED! -30 points");
            score -= 30;
        }

        active.removeIf(Order::isExpired);

        // Generate new orders to maintain queue
        while (active.size() < max) {
            generateOrder();
        }
    }

    private void generateOrder() {
        DishType type = RecipeBook.getRandomDish();
        Recipe recipe = RecipeBook.getRecipe(type);
        Order newOrder = new Order(recipe, 60);
        active.add(newOrder);
        System.out.println("📋 New Order #" + newOrder.getOrderId() + ": " + recipe.getName());
    }

    public boolean submitDish(DishType type) {
        for (Order o : active) {
            if (o.getRecipe().getType() == type && !o.isExpired()) {
                o.complete();
                score += 100;
                active.remove(o);
                System.out.println("✅ Order #" + o.getOrderId() + " completed! +100 points");
                return true;
            }
        }
        score -= 30;
        System.out.println("❌ Wrong dish served! -30 points");
        return false;
    }

    public List<Order> getActiveOrders() {
        return new ArrayList<>(active);
    }

    public int getScore() { return score; }

    public void stop() {
        if (tickTask != null) {
            tickTask.cancel(true);
        }
    }
}