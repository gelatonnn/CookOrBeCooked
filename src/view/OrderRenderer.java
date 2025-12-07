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
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║            ACTIVE ORDERS                  ║");
        System.out.println("╚════════════════════════════════════════════╝");

        if (orderManager.getActiveOrders().isEmpty()) {
            System.out.println("  No active orders");
        } else {
            for (Order o : orderManager.getActiveOrders()) {
                String timeBar = createTimeBar(o.getTimeLeft(), 60);
                System.out.printf("  #%d | %s\n",
                        o.getOrderId(),
                        o.getRecipe().getName());
                System.out.printf("       [%s] %ds\n", timeBar, o.getTimeLeft());
            }
        }

        System.out.println();
        System.out.printf("  💰 SCORE: %d points\n", orderManager.getScore());
        System.out.println("════════════════════════════════════════════\n");
    }

    private String createTimeBar(int current, int max) {
        int bars = 10;
        int filled = (int) ((double) current / max * bars);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bars; i++) {
            if (i < filled) {
                sb.append("█");
            } else {
                sb.append("░");
            }
        }
        return sb.toString();
    }
}