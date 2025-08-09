package app.patterns.fixed_lock_ordering.task;

import java.util.UUID;

public class BankAccount {
    private final UUID accountId;
    private double balance;

    public BankAccount(UUID accountId, double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        this.accountId = accountId;
        this.balance = initialBalance;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public double getBalance() {
        return balance;
    }

    void setBalance(double balance) {
        this.balance = balance;
    }
}

class BankService {
    public void transfer(BankAccount fromAccount, BankAccount toAccount, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (fromAccount == toAccount) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        BankAccount firstLock, secondLock;
        if (fromAccount.getAccountId().compareTo(toAccount.getAccountId()) < 0) {
            firstLock = fromAccount;
            secondLock = toAccount;
        } else {
            firstLock = toAccount;
            secondLock = fromAccount;
        }

        synchronized (firstLock) {
            synchronized (secondLock) {
                if (fromAccount.getBalance() < amount) {
                    throw new IllegalStateException(
                            String.format("Insufficient funds: %.2f (needed: %.2f)",
                                    fromAccount.getBalance(), amount));
                }

                fromAccount.setBalance(fromAccount.getBalance() - amount);
                toAccount.setBalance(toAccount.getBalance() + amount);
            }
        }
    }
}