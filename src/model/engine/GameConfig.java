package model.engine;

public class GameConfig {
    // Basic settings
    public int stageTimeSeconds = 180;
    public int maxFailedStreak = 5;
    public int fps = 60;

    // Stage settings
    public int targetOrders = 0;    // Target untuk menang (Stage 1 & 2)
    public int minScore = 0;        // Target score minimal (Stage 3)
    public boolean isSurvival = false;
    public String stageName = "Custom Stage";

    public GameConfig() {}

    // Constructor lengkap untuk Stage Select
    public GameConfig(String name, int time, int maxFail, int targetOrders, int minScore, boolean isSurvival) {
        this.stageName = name;
        this.stageTimeSeconds = time;
        this.maxFailedStreak = maxFail;
        this.targetOrders = targetOrders;
        this.minScore = minScore;
        this.isSurvival = isSurvival;
        this.fps = 60; // Default FPS
    }
}