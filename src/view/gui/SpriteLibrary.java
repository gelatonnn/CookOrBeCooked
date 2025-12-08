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
            case "tomato": col = 0; row = 0; break;
            case "meat":   col = 0; row = 1; break;
            case "fish":   col = 0; row = 2; break;
            case "shrimp": col = 0; row = 3; break; 
            case "pasta":  col = 0; row = 4; break;

            //PROCESSED (COOKED/CHOPPED)
            case "meat_cooked":   col = 2; row = 1; break;
            case "meat_burned":   col = 3; row = 1; break;
            case "fish_cooked":   col = 2; row = 2; break;
            case "fish_burned":   col = 3; row = 2; break; 
            case "shrimp_cooked": col = 1; row = 3; break;
            case "pasta_cooked":  col = 1; row = 4; break;
            
            //Ingredients Chopped 
            case "tomato_chopped": col = 1; row = 0; break;
            case "meat_chopped":   col = 1; row = 1; break;
            case "fish_chopped":   col = 1; row = 2; break;
            case "shrimp_chopped": col = 1; row = 3; break;

            //DISHES
            case "pasta marinara":       col = 4; row = 1; break;
            case "pasta bolognese":      col = 5; row = 1; break;
            case "pasta frutti di mare": col = 6; row = 1; break;
            
            //UTENSILS
            case "plate":       col = 7; row = 2; break;
            case "plate_dirty": col = 8; row = 2; break;
            
            // PERBAIKAN STOVE ITEM: Panci & Wajan 
            case "boiling pot": 
            case "pot":         col = 1; row = 8; break; 
            case "pot_cooking": col = 9; row = 0; break;
            
            case "frying pan":  
            case "pan":         col = 0; row = 8; break;
            case "pan_cooking": col = 7; row = 2; break;

            // === STATIONS (PERBAIKAN UTAMA) ===
            
            //Wall: Bata Merah
            case "wall": col = 9; row = 3; break;

            //Counter/Meja: Talenan Kosong 
            //INI YANG SEBELUMNYA SALAH 
            case "counter":
            case "assembly station":
                col = 9; row = 4; break;
            case "serving station":
                col = 9; row = 5; break;
            case "cutting station": 
                col = 4; row = 3; break; 

            // Stations Lain
            case "cooking station": 
            case "stove":           col = 4; row = 0; break;
            
            case "washing station": 
            case "sink":            col = 4; row = 2; break; 
            
            case "trash station":   col = 3; row = 4; break; 
            
            case "plate storage":   col = 6; row = 2; break;

            //INGREDIENT STORAGE
            case "crate_pasta":  col = 4; row = 4; break;
            case "crate_shrimp": col = 5; row = 4; break;
            case "crate_meat":   col = 6; row = 4; break;
            case "crate_tomato": col = 7; row = 4; break;
            case "crate_fish":   col = 8; row = 4; break;
            
            case "ingredient storage": col = 4; row = 4; break; 

            //CHEFS
            case "chef1": col = 0; row = 5; break; 
            case "chef2": col = 0; row = 6; break; 
        }

        if (col == -1) return null;

        BufferedImage img = assets.getSprite(col, row);
        if (img != null) cache.put(key, img);
        return img;
    }

    public BufferedImage getChefSprite(int chefId, String direction, boolean hasItem, boolean busy) {
        int row = (chefId == 0) ? 5 : 6; 
        int col = 0; 

        if (direction.contains("UP")) col = 8;
        else if (direction.contains("LEFT")) col = 5;
        else if (direction.contains("RIGHT")) col = 3;
        else col = 1;

        return assets.getSprite(col, row);
    }
}