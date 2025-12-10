package model.orders;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import model.recipes.*;
import utils.TimerUtils;

public class OrderManager {
    private final List<Order> active = new ArrayList<>();
    private final int max = 4;
    private int score = 0;
    private ScheduledFuture<?> tickTask;

    public OrderManager(boolean startThread) {
        if (startThread) {
            // Jalankan timer independen (1 tick per detik)
            tickTask = TimerUtils.repeat(() -> tick(), 1000);
        }
    }

    public void tick() {
        // Update waktu setiap order aktif
        active.forEach(Order::tick);

        // Cari order yang sudah kadaluarsa (Expired)
        List<Order> expired = active.stream()
                .filter(Order::isExpired)
                .toList();

        for (Order o : expired) {
            System.out.println("⏰ Order #" + o.getOrderId() + " EXPIRED! -30 points");
            deductScore(30); // Kurangi skor dengan aman
        }

        // Hapus order kadaluarsa dari daftar
        active.removeIf(Order::isExpired);

        // Generate order baru jika slot masih ada
        while (active.size() < max) {
            generateOrder();
        }
    }

    private void generateOrder() {
        DishType type = RecipeBook.getRandomDish();
        Recipe recipe = RecipeBook.getRecipe(type);
        // Durasi order 60 detik
        Order newOrder = new Order(recipe, 60);
        active.add(newOrder);
        System.out.println("📋 New Order #" + newOrder.getOrderId() + ": " + recipe.getName());
    }

    public boolean submitDish(DishType type) {
        for (Order o : active) {
            // Cek apakah jenis makanan cocok dan order belum expired
            if (o.getRecipe().getType() == type && !o.isExpired()) {
                o.complete();
                
                // --- FITUR BARU: SPEED BONUS ---
                // Jika selesai saat sisa waktu > 30 detik (separuh jalan), dapat bonus +20
                int baseScore = 100;
                int speedBonus = (o.getTimeLeft() > 30) ? 20 : 0;
                int totalReward = baseScore + speedBonus;

                addScore(totalReward);
                
                active.remove(o);
                
                String bonusMsg = (speedBonus > 0) ? " (⚡ Speed Bonus +" + speedBonus + "!)" : "";
                System.out.println("✅ Order #" + o.getOrderId() + " completed! +" + totalReward + " points" + bonusMsg);
                return true;
            }
        }
        
        // Penalti salah sajian (dikurangi jadi -10 biar tidak terlalu sadis)
        deductScore(10);
        System.out.println("❌ Wrong dish served! -10 points");
        return false;
    }

    // --- Helper untuk mengatur skor agar tidak negatif ---
    private void addScore(int amount) {
        score += amount;
    }

    private void deductScore(int amount) {
        score -= amount;
        // Opsional: Jika Anda ingin skor tidak pernah minus, aktifkan baris di bawah:
        // if (score < 0) score = 0; 
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