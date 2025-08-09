package app.patterns.thread_safe.lock_split.task;

import java.time.Instant;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BankAccount {
    private double checkingBalance;
    private double savingsBalance;
    private int transactionsCounter;
    private Instant lastAccessTime;

    private final Lock checkingLock = new ReentrantLock();
    private final Lock savingsLock = new ReentrantLock();
    private final Lock sharedCounterAndAccessTimeLock = new ReentrantLock();

    public BankAccount(double checkingBalance, double savingsBalance) {
        this.checkingBalance = checkingBalance;
        this.savingsBalance = savingsBalance;
        this.transactionsCounter = 0;
        this.lastAccessTime = Instant.now();
    }

    public void depositToChecking(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        checkingLock.lock();
        try {
            this.checkingBalance += amount;
            updateCounterAndAccessTime();
        } finally {
            checkingLock.unlock();
        }
    }


    public void transferToSavings(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        // Establish consistent lock ordering
        checkingLock.lock();
        try {
            savingsLock.lock();
            try {
                if (checkingBalance < amount) {
                    throw new IllegalArgumentException("Insufficient funds");
                }
                this.checkingBalance -= amount;
                this.savingsBalance += amount;
                updateCounterAndAccessTime();
            } finally {
                savingsLock.unlock();
            }
        } finally {
            checkingLock.unlock();
        }
    }

    private void updateCounterAndAccessTime() {
        sharedCounterAndAccessTimeLock.lock();
        try {
            this.transactionsCounter++;
            this.lastAccessTime = Instant.now();
        } finally {
            sharedCounterAndAccessTimeLock.unlock();
        }
    }

}