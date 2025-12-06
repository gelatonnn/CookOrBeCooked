package model.engine;

public class GameConfig {
    public int stageTimeSeconds = 180;
    public int maxFailedStreak = 5;
    public int fps = 20;

    public GameConfig() {}

    public GameConfig(int stageTimeSeconds, int maxFailedStreak, int fps) {
        this.stageTimeSeconds = stageTimeSeconds;
        this.maxFailedStreak = maxFailedStreak;
        this.fps = fps;
    }
}