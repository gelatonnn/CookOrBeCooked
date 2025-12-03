package tile;


import chef.Chef;
import model.Position;
import java.util.LinkedList;
import java.util.Queue;


public class ServingStation extends Station {
    // private Queue<Order> pendingOrders; // Antrian pesanan yang belum diproses

    // public ServingStation(Position pos) {
    //     super(pos, StationType.SERVING);
    //     this.pendingOrders = new LinkedList<>();
    // }

    // @Override
    // public void interact(Chef chef) {
    //     if (!pendingOrders.isEmpty()) {
    //         Order order = pendingOrders.peek();
    //         if (order.matches(chef.getInventory())) {
    //             System.out.println("Pesanan " + order.getId() + " disajikan dengan sukses!");
    //             pendingOrders.poll(); 
    //         } else {
    //             System.out.println("Pesanan tidak cocok, diberikan penalti.");
    //         }
    //     } else {
    //         System.out.println("Tidak ada pesanan yang harus disajikan.");
    //     }
    // }

    // public void addOrder(Order order) {
    //     pendingOrders.add(order);
    // }

    // @Override
    // public void onEnter(Chef chef) {
    //     // Handle chef entering the serving station
    // }
}
