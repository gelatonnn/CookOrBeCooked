package model.orders;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import model.recipes.DishType;
import model.recipes.Recipe;
import model.recipes.RecipeBook;
import utils.TimerUtils;

public class OrderManager {
    private final List<Order> active = new ArrayList<>();
    private final int max = 3; 
    private int score = 0;
    private int completedOrdersCount = 0; 
    private int failedOrdersCount = 0;
    private ScheduledFuture<?> tickTask;
    
    private int spawnTimer = 0;
    private final int SPAWN_INTERVAL = 10; 

    public OrderManager(boolean startThread) {
        if (startThread) {
            tickTask = TimerUtils.repeat(() -> tick(), 1000);
        }
    }

    public void tick() {
        active.forEach(Order::tick);

        List<Order> expired = active.stream()
                .filter(Order::isExpired)
                .toList();

        for (Order o : expired) {
            System.out.println("⏰ Order #" + o.getOrderId() + " EXPIRED!");
            deductScore(30);
            failedOrdersCount++;
        }
        active.removeIf(Order::isExpired);

        spawnTimer++;
        
        if (spawnTimer >= SPAWN_INTERVAL) {
            if (active.size() < max) {
                generateOrder();
                spawnTimer = 0; 
            }
        }
    }

    private void generateOrder() {
        DishType type = RecipeBook.getRandomDish();
        Recipe recipe = RecipeBook.getRecipe(type);
        Order newOrder = new Order(recipe, 90);
        active.add(newOrder);
        System.out.println("📋 New Order #" + newOrder.getOrderId() + ": " + recipe.getName());
    }

    public boolean submitDish(DishType type) {
        for (Order o : active) {
            if (o.getRecipe().getType() == type && !o.isExpired()) {
                o.complete();
                
                int baseScore = 100;
                int speedBonus = (o.getTimeLeft() > 45) ? 20 : 0; 
                int total = baseScore + speedBonus;
                if (model.engine.EffectManager.getInstance().isDoubleMoney()) {
                    total *= 2;
                    System.out.println("💰 DOUBLE MONEY ACTIVE! Score x2");
                }
                addScore(total);
                
                active.remove(o);
                completedOrdersCount++;
                System.out.println("✅ Order completed!");
                
                spawnTimer = 0; 
                
                return true;
            }
        }
        deductScore(10);
        System.out.println("❌ Wrong dish served!");
        failedOrdersCount++;
        return false;
    }

    private void addScore(int amount) { score += amount; }
    private void deductScore(int amount) { score -= amount; }

    public List<Order> getActiveOrders() { return new ArrayList<>(active); }
    public int getScore() { return score; }
    public void stop() { if (tickTask != null) tickTask.cancel(true); }
    public int getCompletedCount() { return completedOrdersCount; }
    public int getFailedCount() { return failedOrdersCount; }
}