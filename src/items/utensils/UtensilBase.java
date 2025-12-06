package items.utensils;

import items.core.Item;

public abstract class UtensilBase extends Item {
    public UtensilBase() {
        this.portable = true;
    }

    @Override
    public abstract String getName();
}