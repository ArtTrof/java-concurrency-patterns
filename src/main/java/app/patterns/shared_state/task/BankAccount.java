package app.patterns.shared_state.task;

import app.patterns.GuardedBy;
import app.patterns.ThreadSafe;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@ThreadSafe
public class BankAccount {
    @GuardedBy("this")
    private int balance;

    public BankAccount(int initialBalance) {
        this.balance = initialBalance;
    }

    public synchronized int getBalance() {
        return this.balance;
    }

    public synchronized void deposit(int amount) {
        this.balance += amount;
    }

    public synchronized void withdraw(int amount) {
        this.balance -= amount;
    }

    public static void main(String[] args) throws InterruptedException {
        var account = new BankAccount(100_000);
        var executors = Executors.newFixedThreadPool(10);
        Runnable deposit = () -> {
            for (int i = 0; i < 100_000; i++) {
                account.deposit(1);
            }
        };
        Runnable withdraw = () -> {
            for (int i = 0; i < 100_000; i++) {
                account.withdraw(1);
            }
        };
        for (int i = 0; i < 5; i++) {
            executors.execute(deposit);
            executors.execute(withdraw);
        }

        executors.shutdown();
        executors.awaitTermination(1000, TimeUnit.MILLISECONDS);


        System.out.println("Remaining balance " + account.getBalance());

    }
}
