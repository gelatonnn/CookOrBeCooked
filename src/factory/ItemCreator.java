package factory;

import items.core.Item;

@FunctionalInterface
public interface ItemCreator {
    Item create();
}
