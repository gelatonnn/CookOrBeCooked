package factory;

import items.ingredients.*;
import items.utensils.*;

public class ItemRegistryInit {

    public static void registerAll() {

        // INGREDIENTS
        ItemFactory.register("pasta", Pasta::new);
        ItemFactory.register("tomato", Tomato::new);
        ItemFactory.register("udang", Udang::new);
        ItemFactory.register("ikan", Ikan::new);
        ItemFactory.register("daging", Daging::new);

        // UTENSILS
        ItemFactory.register("plate", Plate::new);
        ItemFactory.register("boiling_pot", BoilingPot::new);
        ItemFactory.register("frying_pan", FryingPan::new);
    }
}
