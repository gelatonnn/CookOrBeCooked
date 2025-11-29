package items.dish;

import items.core.Preparable;
import items.ingredients.Pasta;
import items.ingredients.Shrimp;
import items.ingredients.Fish;

import java.util.List;

public class PastaFruttiDiMare extends DishBase {

    @Override
    public String getName() {
        return "Pasta Frutti di Mare";
    }

    @Override
    public boolean validateRecipe(List<Preparable> list) {

        boolean pasta = false;
        boolean shrimp = false;
        boolean fish = false;

        for (Preparable p : list) {
            if (p instanceof Pasta) pasta = true;
            if (p instanceof Shrimp) shrimp = true;
            if (p instanceof Fish) fish = true;
        }

        return pasta && shrimp && fish && list.size() == 3;
    }
}
