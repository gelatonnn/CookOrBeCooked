package items.dish;

import items.core.Preparable;
import items.ingredients.Pasta;
import items.ingredients.Tomato;

import java.util.List;

public class PastaMarinara extends DishBase {

    @Override
    public String getName() {
        return "Pasta Marinara";
    }

    @Override
    public boolean validateRecipe(List<Preparable> list) {
        boolean pasta = false;
        boolean tomato = false;

        for (Preparable p : list) {
            if (p instanceof Pasta) pasta = true;
            if (p instanceof Tomato) tomato = true;
        }

        return pasta && tomato && list.size() == 2;
    }
}
