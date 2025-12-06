package view;

import model.orders.Order;
import model.orders.OrderManager;

public class OrderRenderer implements Observer {
    private final OrderManager orderManager;

    public OrderRenderer(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    @Override
    public void update() {
        render();
    }

    public void render() {
        System.out.println("\n=== ACTIVE ORDERS ===");

        for (Order o : orderManager.getActiveOrders()) {
            System.out.println(
                    "#" + o.getOrderId() +
                            " | Dish: " + o.getRecipe().getName() +
                            " | Time Left: " + o.getTimeLeft() + "s"
            );
        }

        System.out.println("Score: " + orderManager.getScore());
        System.out.println("=======================\n");
    }
}