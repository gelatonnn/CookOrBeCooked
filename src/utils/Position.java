package utils;

import java.util.Objects;

public class Position {
    public final int x;
    public final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position move(Direction dir) {
        return switch (dir) {
            case UP -> new Position(x, y - 1);
            case DOWN -> new Position(x, y + 1);
            case LEFT -> new Position(x - 1, y);
            case RIGHT -> new Position(x + 1, y);
            case UP_LEFT -> new Position(x - 1, y - 1);
            case UP_RIGHT -> new Position(x + 1, y - 1);
            case DOWN_LEFT -> new Position(x - 1, y + 1);
            case DOWN_RIGHT -> new Position(x + 1, y + 1);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return p.x == x && p.y == y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}