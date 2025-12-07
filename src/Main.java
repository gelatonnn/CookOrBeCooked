package main;

import controller.GameController;
import controller.MenuController;
import factory.ItemRegistryInit;
import model.engine.GameEngine;
import model.orders.OrderManager;
import model.world.WorldMap;
import model.chef.Chef;
import view.*;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ItemRegistryInit.registerAll();

        MenuController menu = new MenuController();
        menu.showMainMenu();
    }
}
