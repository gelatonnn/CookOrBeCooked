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
        switch (dir) {
            case UP:    return new Position(x, y - 1);
            case DOWN:  return new Position(x, y + 1);
            case LEFT:  return new Position(x - 1, y);
            case RIGHT: return new Position(x + 1, y);
        }
        return this;
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