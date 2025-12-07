package factory;

import items.ingredients.*;
import items.utensils.*;

public class ItemRegistryInit {
    public static void registerAll() {
        // --- INGREDIENTS (Sesuai Spec Pasta Map) ---
        // Pastikan nama string (id) sama dengan yang dipanggil di WorldMap dan Resep
        ItemFactory.register("tomato", Tomato::new);
        ItemFactory.register("meat", Meat::new);
        ItemFactory.register("pasta", Pasta::new);
        ItemFactory.register("shrimp", Shrimp::new);
        ItemFactory.register("fish", Fish::new);

        // --- UTENSILS ---
        ItemFactory.register("plate", Plate::new);
        ItemFactory.register("boiling pot", BoilingPot::new);
        ItemFactory.register("frying pan", FryingPan::new);
        
        // --- DISHES (Opsional jika ingin spawn dish via debug, tapi factory dish biasanya complex) ---
        // Disini cukup Ingredients dan Utensils dasar
        
        System.out.println("DEBUG: All items registered successfully.");
    }
}