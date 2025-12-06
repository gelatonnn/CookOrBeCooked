package model.engine;

public class GameClock {
    private int timeRemaining;

    public GameClock(int seconds) {
        this.timeRemaining = seconds;
    }

    public void tick() {
        if (timeRemaining > 0) timeRemaining--;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public boolean isOver() {
        return timeRemaining <= 0;
    }
}