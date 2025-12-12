package stations;

import java.util.List;

import items.core.CookingDevice;
import items.core.Item;
import items.core.Preparable; 
import items.dish.DishBase;
import items.dish.PastaBolognese;
import items.dish.PastaFruttiDiMare;
import items.dish.PastaMarinara;
import items.utensils.Plate;
import model.recipes.DishType;
import model.recipes.RecipeBook;

public class AssemblyStation extends BaseStation {
    @Override
    public String getName() { return "Assembly Station"; }

    @Override
    public boolean canPlace(Item item) {
        if (storedItem == null) {
            return item instanceof Plate || item instanceof Preparable || item instanceof DishBase;
        }
        if (storedItem instanceof Plate) {
            if (item instanceof Preparable) return true;
            if (item instanceof CookingDevice) return true;
        }
        return false;
    }

    @Override
    public boolean place(Item item) {
        if (storedItem == null) return super.place(item);

        boolean ingredientAdded = false;

        // KASUS 2: Tuang isi Panci ke Piring
        if (storedItem instanceof Plate plate && item instanceof CookingDevice device) {
            List<Preparable> contents = device.getContents();
            if (contents.isEmpty()) return false;

            for (Preparable p : contents) plate.addIngredient(p);
            device.clearContents();
            ingredientAdded = true;
        }

        // KASUS 3: Taruh Bahan Satuan ke Piring
        else if (storedItem instanceof Plate plate && item instanceof Preparable prep) {
            plate.addIngredient(prep);
            ingredientAdded = true;
        }

        if (ingredientAdded && storedItem instanceof Plate plate) {
            checkAndConvertDish(plate);
            return !(item instanceof CookingDevice);
        }

        return false;
    }

    private void checkAndConvertDish(Plate plate) {
        DishType match = RecipeBook.findMatch(plate.getContents());
        
        if (match != null) {
            System.out.println("Recipe Complete: " + match);
            if (match == DishType.PASTA_MARINARA) storedItem = new PastaMarinara();
            else if (match == DishType.PASTA_BOLOGNESE) storedItem = new PastaBolognese();
            else if (match == DishType.PASTA_FRUTTI_DI_MARE) storedItem = new PastaFruttiDiMare();
        }
    }
}