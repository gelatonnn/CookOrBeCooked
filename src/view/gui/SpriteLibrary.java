package view.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class SpriteLibrary {
    private static SpriteLibrary instance;
    private final Map<String, BufferedImage> cache = new HashMap<>();
    private final int SIZE = 64; // Ukuran resolusi sprite (High Quality)

    private SpriteLibrary() {}

    public static SpriteLibrary getInstance() {
        if (instance == null) instance = new SpriteLibrary();
        return instance;
    }

    public BufferedImage getSprite(String name) {
        name = name.toLowerCase();
        if (cache.containsKey(name)) return cache.get(name);

        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        
        // Anti-aliasing biar mulus (opsional untuk pixel art, tapi bagus untuk shapes)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (name) {
            // --- INGREDIENTS ---
            case "tomato" -> drawTomato(g);
            case "meat" -> drawMeat(g);
            case "pasta" -> drawPasta(g);
            case "shrimp" -> drawShrimp(g);
            case "fish" -> drawFish(g);
            
            // --- UTENSILS ---
            case "plate" -> drawPlate(g);
            case "boiling pot" -> drawPot(g);
            case "frying pan" -> drawPan(g);
            
            // --- STATIONS ---
            case "wall" -> drawWall(g);
            case "floor" -> drawFloor(g);
            case "counter" -> drawCounter(g);
            case "cutting station" -> drawCuttingStation(g);
            case "cooking station" -> drawStove(g);
            case "washing station" -> drawSink(g);
            case "serving station" -> drawServing(g);
            case "ingredient storage" -> drawCrate(g);
            case "plate storage" -> drawPlateStorage(g);
            case "trash station" -> drawTrash(g);

            // --- CHEFS (BARU) ---
            case "chef1" -> drawChefCharacter(g, Color.BLUE); // Gordon (Biru)
            case "chef2" -> drawChefCharacter(g, Color.RED);  // Ramsay (Merah)

            
            default -> { // Placeholder Error (Kotak Pink)
                g.setColor(Color.MAGENTA);
                g.fillRect(0,0, SIZE, SIZE);
            }
        }

        g.dispose();
        cache.put(name, img);
        return img;
    }

    // --- DRAWING LOGIC (SENIMAN KODE) ---
    // --- Method Baru untuk Menggambar Plate Storage ---
    private void drawPlateStorage(Graphics2D g) {
        // 1. Gambar dasarnya dulu (meja counter kayu)
        drawCounter(g); 

        // 2. Gambar tumpukan piring di atasnya
        // Kita gambar 3 piring bertumpuk agar terlihat jelas
        int stackX = 12;
        int stackWidth = 40;
        int stackHeight = 18; // Piring dilihat dari samping agak gepeng
        int baseY = 38;

        // Loop menggambar dari bawah ke atas
        for (int i = 0; i < 3; i++) {
            int y = baseY - (i * 6); // Geser ke atas setiap iterasi
            
            // Badan piring (Putih cerah)
            g.setColor(new Color(245, 245, 245)); 
            g.fillOval(stackX, y, stackWidth, stackHeight);
            
            // Pinggiran piring (Abu-abu halus)
            g.setColor(new Color(180, 180, 180));
            g.setStroke(new BasicStroke(1));
            g.drawOval(stackX, y, stackWidth, stackHeight);

            // Khusus piring paling atas, gambar lingkaran dalam
            if (i == 2) {
                 g.drawOval(stackX + 5, y + 4, stackWidth - 10, stackHeight - 8);
            }
        }
    }

    private void drawChefCharacter(Graphics2D g, Color apronColor) {
        // 1. Badan (Putih)
        g.setColor(Color.WHITE);
        g.fillOval(12, 20, 40, 40);
        
        // 2. Celemek (Apron) - Warna Pembeda
        g.setColor(apronColor);
        g.fillRoundRect(20, 30, 24, 28, 10, 10);
        
        // 3. Kepala
        g.setColor(new Color(255, 224, 189)); // Warna Kulit
        g.fillOval(16, 10, 32, 32);
        
        // 4. Topi Koki (Toque)
        g.setColor(Color.WHITE);
        g.fillRoundRect(16, 2, 32, 14, 5, 5); // Bagian atas topi
        g.fillOval(16, 10, 32, 10); // Pinggiran topi
        
        // 5. Mata
        g.setColor(Color.BLACK);
        g.fillOval(24, 22, 4, 4);
        g.fillOval(36, 22, 4, 4);
        
        // 6. Border Halus
        g.setColor(new Color(0, 0, 0, 50));
        g.setStroke(new BasicStroke(1));
        g.drawOval(12, 20, 40, 40);
    }

    private void drawTomato(Graphics2D g) {
        g.setColor(new Color(220, 20, 60)); // Merah Tomat
        g.fillOval(16, 16, 32, 32);
        g.setColor(new Color(34, 139, 34)); // Hijau Daun
        g.fillPolygon(new int[]{32, 28, 36}, new int[]{16, 10, 10}, 3);
    }

    private void drawMeat(Graphics2D g) {
        g.setColor(new Color(139, 69, 19)); // Coklat Daging
        g.fillRoundRect(14, 20, 36, 24, 10, 10);
        g.setColor(new Color(205, 133, 63)); // Tulang/Lemak
        g.fillOval(20, 25, 10, 5);
        g.fillOval(35, 30, 8, 4);
    }

    private void drawPasta(Graphics2D g) {
        g.setColor(new Color(255, 215, 0)); // Kuning Pasta
        g.setStroke(new BasicStroke(3));
        g.drawArc(16, 16, 10, 20, 0, 180);
        g.drawArc(26, 16, 10, 20, 180, 180);
        g.drawArc(36, 16, 10, 20, 0, 180);
    }

    private void drawShrimp(Graphics2D g) {
        g.setColor(new Color(255, 127, 80)); // Oranye Udang
        g.fillArc(16, 16, 32, 32, 45, 270);
    }

    private void drawFish(Graphics2D g) {
        g.setColor(new Color(100, 149, 237)); // Biru Ikan
        g.fillOval(16, 20, 32, 16); // Badan
        g.fillPolygon(new int[]{48, 58, 58}, new int[]{28, 20, 36}, 3); // Ekor
        g.setColor(Color.WHITE);
        g.fillOval(20, 24, 4, 4); // Mata
    }

    private void drawPlate(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillOval(10, 10, 44, 44);
        g.setColor(Color.LIGHT_GRAY);
        g.drawOval(10, 10, 44, 44);
        g.drawOval(16, 16, 32, 32); // Bagian dalam
    }

    private void drawPot(Graphics2D g) {
        g.setColor(Color.GRAY);
        g.fillRoundRect(12, 16, 40, 32, 5, 5); // Panci
        g.setColor(Color.DARK_GRAY);
        g.fillRect(8, 20, 4, 8); // Pegangan Kiri
        g.fillRect(52, 20, 4, 8); // Pegangan Kanan
        g.setColor(new Color(200, 200, 255, 100)); // Efek Air
        g.fillOval(14, 18, 36, 10);
    }

    private void drawPan(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        g.fillOval(12, 12, 40, 40); // Wajan
        g.setStroke(new BasicStroke(6));
        g.drawLine(50, 50, 60, 60); // Gagang
    }

    private void drawCounter(Graphics2D g) {
        g.setColor(new Color(139, 69, 19)); // Kayu Tua (Border)
        g.fillRect(0, 0, SIZE, SIZE);
        g.setColor(new Color(205, 133, 63)); // Kayu Muda (Atas)
        g.fillRect(2, 2, SIZE-4, SIZE-4);
        g.setColor(new Color(160, 82, 45)); // Garis Kayu
        g.drawLine(10, 2, 10, SIZE-4);
        g.drawLine(30, 2, 30, SIZE-4);
        g.drawLine(50, 2, 50, SIZE-4);
    }

    private void drawCuttingStation(Graphics2D g) {
        drawCounter(g); // Base Meja
        g.setColor(new Color(222, 184, 135)); // Talenan
        g.fillRect(16, 16, 32, 32);
        g.setColor(Color.GRAY); // Pisau
        g.fillRect(40, 10, 4, 20); 
        g.setColor(Color.BLACK); // Gagang Pisau
        g.fillRect(40, 30, 4, 10);
    }

    private void drawStove(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, SIZE, SIZE);
        g.setColor(Color.GRAY); // Pintu Oven
        g.fillRect(8, 32, 48, 28);
        
        // Kompor (Burner)
        g.setColor(Color.BLACK);
        g.fillOval(16, 4, 32, 24);
        g.setColor(Color.RED); // Api
        g.setStroke(new BasicStroke(2));
        g.drawOval(22, 10, 20, 12);
    }

    private void drawSink(Graphics2D g) {
        drawCounter(g);
        g.setColor(new Color(176, 196, 222)); // Steel
        g.fillRect(10, 10, 44, 44);
        g.setColor(new Color(100, 149, 237)); // Air
        g.fillRect(14, 14, 36, 36);
        g.setColor(Color.WHITE); // Busa
        g.fillOval(20, 20, 5, 5);
        g.fillOval(35, 40, 8, 8);
    }

    private void drawServing(Graphics2D g) {
        drawCounter(g);
        g.setColor(Color.GREEN); // Taplak
        g.fillRect(0, 0, 20, SIZE);
        g.fillRect(44, 0, 20, SIZE);
        g.setColor(Color.YELLOW); // Bell
        g.fillOval(28, 28, 8, 8);
    }

    private void drawCrate(Graphics2D g) {
        g.setColor(new Color(160, 82, 45)); // Kayu
        g.fillRect(4, 4, 56, 56);
        g.setColor(new Color(139, 69, 19)); // Dalam Box
        g.fillRect(8, 8, 48, 48);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.drawString("ITEM", 20, 35);
    }

    private void drawTrash(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(12, 12, 40, 40);
        g.setColor(Color.DARK_GRAY);
        g.fillRect(14, 14, 36, 36); // Lubang
    }
    
    private void drawWall(Graphics2D g) {
        g.setColor(new Color(105, 105, 105)); // Abu-abu Bata
        g.fillRect(0, 0, SIZE, SIZE);
        g.setColor(new Color(80, 80, 80)); // Garis Bata
        g.drawLine(0, 32, 64, 32);
        g.drawLine(32, 0, 32, 32);
        g.drawLine(16, 32, 16, 64);
    }

    private void drawFloor(Graphics2D g) {
        g.setColor(new Color(245, 222, 179)); // Cream
        g.fillRect(0, 0, SIZE, SIZE);
        g.setColor(new Color(230, 210, 160)); // Checkered pattern halus
        g.fillRect(0, 0, 32, 32);
        g.fillRect(32, 32, 32, 32);
    }
}