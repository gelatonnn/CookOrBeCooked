package factory;

import items.ingredients.*;
import items.utensils.*;

public class ItemRegistryInit {

    public static void registerAll() {

        // INGREDIENTS
        ItemFactory.register("pasta", Pasta::new);
        ItemFactory.register("tomato", Tomato::new);
        ItemFactory.register("shrimp", Shrimp::new);
        ItemFactory.register("fish", Fish::new);
        ItemFactory.register("meat", Meat::new);

        // UTENSILS
        ItemFactory.register("plate", Plate::new);
        ItemFactory.register("boiling_pot", BoilingPot::new);
        ItemFactory.register("frying_pan", FryingPan::new);

        // DISHES
        ItemFactory.register("pasta_marinara", PastaMarinara::new);
        ItemFactory.register("pasta_bolognese", PastaBolognese::new);
        ItemFactory.register("pasta_frutti_di_mare", PastaFruttiDiMare::new);
    }
}
