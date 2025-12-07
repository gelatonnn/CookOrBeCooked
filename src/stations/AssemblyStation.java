package stations;

import items.core.CookingDevice;
import items.core.Item;
import items.core.Preparable;
import items.dish.*; // Import Dish classes
import items.utensils.Plate;
import model.recipes.DishType;
import model.recipes.RecipeBook;

import java.util.List;

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

        // --- CEK RESEP SETELAH BAHAN DITAMBAH ---
        if (ingredientAdded && storedItem instanceof Plate plate) {
            checkAndConvertDish(plate);
            // Jika item berasal dari tangan (preparable), return true (item hilang dari tangan)
            // Jika item adalah CookingDevice, return false (device tetap di tangan)
            return !(item instanceof CookingDevice);
        }

        return false;
    }

    private void checkAndConvertDish(Plate plate) {
        DishType match = RecipeBook.findMatch(plate.getContents());
        
        if (match != null) {
            System.out.println("Recipe Complete: " + match);
            // Ganti Plate dengan Dish Jadi
            if (match == DishType.PASTA_MARINARA) storedItem = new PastaMarinara();
            else if (match == DishType.PASTA_BOLOGNESE) storedItem = new PastaBolognese();
            else if (match == DishType.PASTA_FRUTTI_DI_MARE) storedItem = new PastaFruttiDiMare();
        }
    }
}