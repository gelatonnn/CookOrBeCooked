package view.gui;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class SpriteLibrary {
    private static SpriteLibrary instance;
    private final AssetManager assets;
    private final Map<String, BufferedImage> cache = new HashMap<>();

    private SpriteLibrary() {
        this.assets = AssetManager.getInstance();
    }

    public static SpriteLibrary getInstance() {
        if (instance == null) instance = new SpriteLibrary();
        return instance;
    }

    public BufferedImage getSprite(String name) {
        if (name == null) return null;
        String key = name.toLowerCase().trim();

        if (cache.containsKey(key)) return cache.get(key);

        int col = -1, row = -1;

        switch (key) {
            //INGREDIENTS
            case "tomato" -> { col = 0; row = 0; }
            case "meat" -> { col = 0; row = 1; }
            case "fish" -> { col = 0; row = 2; }
            case "shrimp" -> { col = 0; row = 3; }
            case "pasta" -> { col = 0; row = 4; }

            //PROCESSED (COOKED/CHOPPED)
            case "meat_cooked" -> { col = 2; row = 1; }
            case "meat_burned" -> { col = 3; row = 1; }
            case "fish_cooked" -> { col = 2; row = 2; }
            case "fish_burned" -> { col = 3; row = 2; }
            case "shrimp_cooked" -> { col = 2; row = 3; }
            case "shrimp_burned" -> { col = 3; row = 3; }
            case "pasta_cooked" -> { col = 1; row = 4; }
            case "pasta_burned" -> {  col = 2;  row = 4; }
            case "tomato_cooked" -> { col = 2; row = 0; }
            case "tomato_burned" -> { col = 3; row = 0; }

            
            //Ingredients Chopped 
            case "tomato_chopped" -> { col = 1; row = 0; }
            case "meat_chopped" -> { col = 1; row = 1; }
            case "fish_chopped" -> { col = 1; row = 2; }
            case "shrimp_chopped" -> { col = 1; row = 3; }

            //DISHES
            case "pasta marinara" -> { col = 4; row = 1; }
            case "pasta bolognese" -> { col = 5; row = 1; }
            case "pasta frutti di mare" -> { col = 6; row = 1; }
            
            //UTENSILS
            case "plate" -> { col = 7; row = 2; }
            case "plate_dirty" -> { col = 8; row = 2; }
            
            //Panci & Wajan 
            case "boiling pot",
                    "pot" -> { col = 1; row = 8; }
            case "pot_cooking" -> { col = 9; row = 0; }
            
            case "frying pan",
                    "pan" -> { col = 0; row = 8; }
            case "pan_cooking" -> { col = 0; row = 8; }

            
            //Environment
            case "wall" -> { col = 9; row = 3; }
            case "floor" -> { col = 2; row = 8; }
            case "lucky_station" -> { col = 3; row = 8; }
            case "cloud" -> { col = 8; row = 8; }

            //Counter
            case "counter",
                    "assembly station" -> { col = 9; row = 4; }
            case "serving station" -> { col = 9; row = 5; }
            case "cutting station" -> { col = 4; row = 3; }

            // Stations Lain
            case "cooking station",
                    "stove" -> { col = 4; row = 0; }
            
            case "washing station",
                    "sink" -> { col = 4; row = 2; }
            
            case "trash station" -> { col = 3; row = 4; }
            
            case "plate storage" -> { col = 6; row = 2; }

            //INGREDIENT STORAGE
            case "crate_pasta" -> { col = 4; row = 4; }
            case "crate_shrimp" -> { col = 5; row = 4; }
            case "crate_meat" -> { col = 6; row = 4; }
            case "crate_tomato" -> { col = 7; row = 4; }
            case "crate_fish" -> { col = 8; row = 4; }
            
            case "ingredient storage" -> { col = 4; row = 4; }

            //CHEFS
            case "chef1" -> { col = 0; row = 5; }
            case "chef2" -> { col = 0; row = 6; }
            default -> {}
        }

        if (col == -1) return null;

        BufferedImage img = assets.getSprite(col, row);
        if (img != null) cache.put(key, img);
        return img;
    }

    public BufferedImage getChefSprite(int chefId, String direction, boolean hasItem, boolean busy, int animationStep) {
        int row = (chefId == 0) ? 5 : 6; 
        int col;

        //FRAME ANIMASI
        // DOWN 
        int[] animDown = {1, 2, 1, 2}; 
        
        // RIGHT 
        int[] animRight = {3, 4, 3, 4}; 
        
        // LEFT (
        int[] animLeft = {5, 6, 5, 6}; 
        
        // UP 
        int[] animUp = {7, 8, 7, 8};

        int stepIndex = animationStep % 4;

        if (direction.contains("UP")) {
            col = animUp[stepIndex];
        } 
        else if (direction.contains("LEFT")) {
            col = animLeft[stepIndex];
        } 
        else if (direction.contains("RIGHT")) {
            col = animRight[stepIndex];
        } 
        else {
            col = animDown[stepIndex];
        }

        return assets.getSprite(col, row);
    }
}