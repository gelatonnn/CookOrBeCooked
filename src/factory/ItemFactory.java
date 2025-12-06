package factory;

import items.core.Item;
import java.util.HashMap;
import java.util.Map;

public class ItemFactory {
    private static final Map<String, ItemCreator> registry = new HashMap<>();

    public static void register(String id, ItemCreator creator) {
        registry.put(id.toLowerCase(), creator);
    }

    public static Item create(String id) {
        ItemCreator c = registry.get(id.toLowerCase());
        if (c == null) throw new IllegalArgumentException("Unknown item id: " + id);
        return c.create();
    }

    public static boolean isRegistered(String id) {
        return registry.containsKey(id.toLowerCase());
    }
}
