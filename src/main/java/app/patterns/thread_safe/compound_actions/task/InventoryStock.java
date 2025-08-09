package app.patterns.thread_safe.compound_actions.task;

import app.patterns.GuardedBy;
import app.patterns.ThreadSafe;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@ThreadSafe
public class InventoryStock {

    @GuardedBy("this")
    private final Map<String, Integer> stock;

    public InventoryStock(Map<String, Integer> initialStock) {
        this.stock = new HashMap<>(initialStock); // defensive copy
    }

    public synchronized int getItemsCount(String item) {
        checkIfItemIsPresent(item);
        return stock.get(item);
    }

    public synchronized void restock(String item, int amount) {
        checkIfItemIsPresent(item);
        if (amount <= 0) {
            throw new IllegalArgumentException("Value must be greater than zero");
        }
        // read-modify-write (atomic under lock)
        stock.put(item, stock.get(item) + amount);
    }

    public synchronized void buyProduct(String item, int quantity) {
        checkIfItemIsPresent(item);
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        // check-then-act (atomic under lock)
        if (stock.get(item) >= quantity) {
            stock.put(item, stock.get(item) - quantity);
            System.out.println(Thread.currentThread().getName() + " bought " + quantity + " of " + item);
        } else {
            throw new IllegalStateException("Not enough stock for " + item);
        }
    }

    private void checkIfItemIsPresent(String item) {
        if (!stock.containsKey(item)) {
            throw new IllegalArgumentException("Item not found: " + item);
        }
    }

    public synchronized void printStock() {
        System.out.println("Current stock:");
        stock.forEach((key, value) -> System.out.println(key + ": " + value));
    }

    public static void main(String[] args) throws InterruptedException {
        Map<String, Integer> initial = new HashMap<>();
        initial.put("Table", 33);
        initial.put("Monitor", 29);

        var inventory = new InventoryStock(initial);

        var executors = Executors.newFixedThreadPool(10);

        Runnable buyTable = () -> {
            try {
                inventory.buyProduct("Table", 10);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        };

        Runnable buyMonitor = () -> {
            try {
                inventory.buyProduct("Monitor", 15);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        };

        // First wave of buys
        for (int i = 0; i < 5; i++) {
            executors.execute(buyTable);
            executors.execute(buyMonitor);
        }

        executors.shutdown();
        executors.awaitTermination(1, TimeUnit.SECONDS);

        // Restock
        inventory.restock("Monitor", 1000);
        inventory.restock("Table", 1000);

        // Second wave of buys
        var executors2 = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 20; i++) {
            executors2.execute(buyTable);
            executors2.execute(buyMonitor);
        }
        executors2.shutdown();
        executors2.awaitTermination(1, TimeUnit.SECONDS);

        // Print final stock
        inventory.printStock();
    }
}
