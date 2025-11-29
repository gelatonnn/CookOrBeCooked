package com.nimonscooked;

import com.nimonscooked.model.GameEngine;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Nimonscooked...");
        
        // 1. Ambil instance engine (Singleton)
        GameEngine engine = GameEngine.getInstance();
        
        // 2. Cek apakah peta terbentuk dengan benar
        System.out.println("=== INITIAL MAP DEBUG ===");
        engine.getWorldMap().printMap();
        
        // 3. Jalankan Loop Game (Multithreading start)
        engine.start();
    }
}