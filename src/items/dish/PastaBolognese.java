package items.dish;

import items.core.Preparable;
import items.ingredients.Pasta;
import items.ingredients.Meat;

import java.util.List;

public class PastaBolognese extends DishBase {

    @Override
    public String getName() {
        return "Pasta Bolognese";
    }

    @Override
    public boolean validateRecipe(List<Preparable> list) {
        boolean pasta = false;
        boolean meat = false;

        for (Preparable p : list) {
            if (p instanceof Pasta) pasta = true;
            if (p instanceof Meat) meat = true;
        }

        return pasta && meat && list.size() == 2;
    }
}
