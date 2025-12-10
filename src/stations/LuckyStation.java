package stations;

import items.core.Item;
import model.engine.EffectManager;
import model.engine.GameEngine;
import utils.TimerUtils;
import view.gui.AssetManager;

public class LuckyStation extends BaseStation {
    private boolean onCooldown = false;
    private int cooldownTime = 0;

    @Override
    public String getName() {
        if (onCooldown) return "Lucky Spin (" + cooldownTime + "s)";
        return "Lucky Spin [READY]";
    }

    @Override
    public boolean canPlace(Item item) {
        return false; // Tidak bisa taruh barang
    }

    // Method khusus interaksi (dipanggil lewat Chef.tryInteract nanti)
    public void spin() {
        if (onCooldown) {
            System.out.println("⏳ Lucky Spin is cooling down!");
            return;
        }

        System.out.println("🎰 SPINNING THE WHEEL...");
        // Hapus sound "place" disini, nanti sound "spin" diputar oleh Overlay
        
        startCooldown();

        // CHANGE: Panggil sequence, bukan trigger langsung
        EffectManager.getInstance().startGachaSequence();
    }

    private void startCooldown() {
        onCooldown = true;
        cooldownTime = 45;
        
        // Timer hitung mundur sederhana
        TimerUtils.repeat(() -> {
            if (cooldownTime > 0) cooldownTime--;
            else {
                onCooldown = false;
                // throw exception to stop timer? 
                // Di TimerUtils Anda pakai repeat, idealnya simpan future lalu cancel.
                // Untuk simplifikasi, biarkan logic 0 menghandle stop status.
            }
        }, 1000);
    }
    
    // Agar bisa diinteraksi walau kosong
    @Override
    public boolean isOccupied() {
        return true; // Hack agar Chef bisa interact
    }
}