package com.nimonscooked.model;

public class WorldMap {
    private int width;
    private int height;
    private String[][] grid; 

    public WorldMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new String[height][width];
        initializeEmptyMap();
    }

    private void initializeEmptyMap() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (i == 0 || i == height - 1 || j == 0 || j == width - 1) {
                    grid[i][j] = "X"; 
                } else {
                    grid[i][j] = "."; 
                }
            }
        }
    }
    
    public boolean isWalkable(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        return !grid[y][x].equals("X");
    }

    public void printMap() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
    }
}