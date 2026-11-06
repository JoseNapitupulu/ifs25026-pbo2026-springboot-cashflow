package org.delcom.ifs25026pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25026pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25026pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25026pbo2026springbootcashflow.domain.repository.ITransactionRepository;

/**
 * Implementasi repository transaksi menggunakan basis generic
 * {@link InMemoryRepository}. Data disimpan di memori (belum menggunakan database).
 */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {

    /** Penghitung ID otomatis, bertambah setiap kali transaksi baru disimpan. */
    private int idCounter = 0;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction saveIncome(String description, double amount) {
        return save(new Transaction(nextId(), description, amount, TransactionType.INCOME));
    }

    @Override
    public Transaction saveExpense(String description, double amount) {
        return save(new Transaction(nextId(), description, amount, TransactionType.EXPENSE));
    }

    /** Menghasilkan ID unik berikutnya. */
    private int nextId() {
        return ++idCounter;
    }
}
