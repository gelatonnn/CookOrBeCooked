package stations;

import items.core.Item;
import model.engine.EffectManager;
import utils.TimerUtils;

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
        return false; 
    }

    public void spin() {
        if (onCooldown) {
            System.out.println("⏳ Lucky Spin is cooling down!");
            return;
        }

        System.out.println("🎰 SPINNING THE WHEEL...");
        
        startCooldown();

        EffectManager.getInstance().startGachaSequence();
    }

    private void startCooldown() {
        onCooldown = true;
        cooldownTime = 45;
        
        TimerUtils.repeat(() -> {
            if (cooldownTime > 0) cooldownTime--;
            else {
                onCooldown = false;
            }
        }, 1000);
    }
    
    @Override
    public boolean isOccupied() {
        return true; 
    }
}