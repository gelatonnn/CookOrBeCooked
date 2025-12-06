package utils;

public class PathUtils {
    public static boolean pathClear(Position from, Position to, boolean[][] walls) {
        if (from.x == to.x) {
            int step = from.y < to.y ? 1 : -1;
            for (int y = from.y; y != to.y; y += step) {
                if (walls[y][from.x]) return false;
            }
            return true;
        } else if (from.y == to.y) {
            int step = from.x < to.x ? 1 : -1;
            for (int x = from.x; x != to.x; x += step) {
                if (walls[from.y][x]) return false;
            }
            return true;
        }
        return false;
    }
}