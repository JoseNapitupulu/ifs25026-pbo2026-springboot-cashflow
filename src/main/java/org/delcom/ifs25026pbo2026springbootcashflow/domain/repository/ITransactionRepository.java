package org.delcom.ifs25026pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25026pbo2026springbootcashflow.domain.entity.Transaction;

/**
 * Port spesifik transaksi yang memperluas generic {@link IRepository}.
 * Menambahkan method penyimpanan khusus pemasukan dan pengeluaran
 * dengan penomoran ID otomatis.
 */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {

    /** Menyimpan transaksi pemasukan baru. */
    Transaction saveIncome(String description, double amount);

    /** Menyimpan transaksi pengeluaran baru. */
    Transaction saveExpense(String description, double amount);
}
