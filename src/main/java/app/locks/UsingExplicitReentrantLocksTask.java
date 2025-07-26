package app.locks;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class UsingExplicitReentrantLocksTask {
    private static class BankAccount {
        private final ReentrantLock lock = new ReentrantLock();
        private int balance;

        public void performTransaction(Operation operation) {
            lock.lock();
            try {
                switch (operation) {
                    case DEPOSIT -> balance++;
                    case WITHDRAW -> balance--;
                }
            } finally {
                lock.unlock();
            }
        }

        public int getBalance() {
            lock.lock();
            try {
                return balance;
            } finally {
                lock.unlock();
            }
        }
    }

    private enum Operation {
        DEPOSIT, WITHDRAW
    }

    public static void main(String[] args) {
        var executors = Executors.newFixedThreadPool(5);
        var bankAccount = new BankAccount();
        for (int i = 0; i < 500; i++) {
            executors.execute(() -> {
                bankAccount.performTransaction(Operation.DEPOSIT);
                bankAccount.performTransaction(Operation.WITHDRAW);
            });
        }
        executors.shutdown();
        System.out.printf("Deposited withdrawal: %d\n", bankAccount.getBalance());
    }
}


