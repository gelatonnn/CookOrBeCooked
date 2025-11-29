package factory;

import items.core.Item;
import java.util.HashMap;
import java.util.Map;

public class ItemFactory {

    private static final Map<String, ItemCreator> registry = new HashMap<>();

    private ItemFactory() {}

    public static void register(String id, ItemCreator creator) {
        registry.put(id.toLowerCase(), creator);
    }

    public static Item create(String id) {
        ItemCreator creator = registry.get(id.toLowerCase());
        if (creator == null) {
            throw new IllegalArgumentException("Unknown item id: " + id);
        }
        return creator.create();
    }

    public static boolean isRegistered(String id) {
        return registry.containsKey(id.toLowerCase());
    }
}
