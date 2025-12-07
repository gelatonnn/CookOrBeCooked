package controller.commands;

import model.engine.GameEngine;
import model.chef.Chef;
import model.world.WorldMap;
import items.core.Item;
import utils.Position;
import utils.Direction;

public class ThrowCommand implements Command {
    private final GameEngine engine;
    private final Chef chef;
    private final WorldMap world;
    private final int throwDistance;

    public ThrowCommand(GameEngine engine, Chef chef, WorldMap world, int distance) {
        this.engine = engine;
        this.chef = chef;
        this.world = world;
        this.throwDistance = distance;
    }

    @Override
    public void execute() {
        Item held = chef.getHeldItem();
        if (held == null) {
            System.out.println("❌ No item to throw!");
            return;
        }

        Position start = chef.getPos();
        Direction dir = chef.getDirection();
        Position target = start;

        // Calculate throw trajectory
        for (int i = 1; i <= throwDistance; i++) {
            Position next = target.move(dir);
            if (!world.inBounds(next) || !world.isWalkable(next)) {
                break;
            }
            target = next;
        }

        System.out.println("🎯 Threw " + held.getName() + " to " + target);
        world.placeItemAt(target, held);
        chef.setHeldItem(null);
    }
}