package model.orders;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import model.recipes.*;
import utils.TimerUtils;

public class OrderManager {
    private final List<Order> active = new ArrayList<>();
    private final int max = 3; // Maksimal order di layar
    private int score = 0;
    private int completedOrdersCount = 0; // TAMBAHAN
    private int failedOrdersCount = 0;
    private ScheduledFuture<?> tickTask;
    
    // --- VARIABEL BARU UNTUK SPAWN ---
    private int spawnTimer = 0;
    private final int SPAWN_INTERVAL = 10; // Order baru muncul setiap 10 detik
    // ---------------------------------

    public OrderManager(boolean startThread) {
        if (startThread) {
            tickTask = TimerUtils.repeat(() -> tick(), 1000);
        }
    }

    public void tick() {
        // Update waktu setiap order aktif
        active.forEach(Order::tick);

        // Hapus order expired
        List<Order> expired = active.stream()
                .filter(Order::isExpired)
                .toList();

        for (Order o : expired) {
            System.out.println("⏰ Order #" + o.getOrderId() + " EXPIRED!");
            deductScore(30);
            failedOrdersCount++;
        }
        active.removeIf(Order::isExpired);

        // --- LOGIC BARU: SPAWN SATU PER SATU ---
        spawnTimer++;
        
        // Cek apakah sudah waktunya spawn order baru
        if (spawnTimer >= SPAWN_INTERVAL) {
            // Hanya spawn jika slot masih ada
            if (active.size() < max) {
                generateOrder();
                spawnTimer = 0; // Reset timer setelah spawn
            }
        }
        // ---------------------------------------
    }

    private void generateOrder() {
        DishType type = RecipeBook.getRandomDish();
        Recipe recipe = RecipeBook.getRecipe(type);
        // Durasi order 90 detik (lebih santai)
        Order newOrder = new Order(recipe, 90);
        active.add(newOrder);
        System.out.println("📋 New Order #" + newOrder.getOrderId() + ": " + recipe.getName());
    }

    public boolean submitDish(DishType type) {
        for (Order o : active) {
            if (o.getRecipe().getType() == type && !o.isExpired()) {
                o.complete();
                
                int baseScore = 100;
                int speedBonus = (o.getTimeLeft() > 45) ? 20 : 0; // Bonus jika cepat
                int total = baseScore + speedBonus;
                if (model.engine.EffectManager.getInstance().isDoubleMoney()) {
                    total *= 2;
                    System.out.println("💰 DOUBLE MONEY ACTIVE! Score x2");
                }
                addScore(total);
                
                active.remove(o);
                completedOrdersCount++;
                System.out.println("✅ Order completed!");
                
                // Opsional: Reset spawn timer agar order pengganti tidak muncul instan
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