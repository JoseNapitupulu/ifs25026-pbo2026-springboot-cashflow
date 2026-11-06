package org.delcom.ifs25026pbo2026springbootcashflow.domain.entity;

import java.time.LocalDateTime;

/**
 * Entity inti yang merepresentasikan satu transaksi keuangan.
 * Berada di layer domain — tidak bergantung pada tampilan, penyimpanan,
 * maupun framework.
 */
public class Transaction {

    /** ID unik transaksi, tidak boleh diubah setelah dibuat. */
    private final int id;

    /** Keterangan singkat transaksi. */
    private String description;

    /** Nominal transaksi dalam rupiah, selalu bernilai positif. */
    private double amount;

    /** Jenis transaksi: pemasukan atau pengeluaran. */
    private final TransactionType type;

    /** Waktu transaksi dicatat. */
    private final LocalDateTime createdAt;

    /** Membuat transaksi baru dengan waktu catat saat ini. */
    public Transaction(int id, String description, double amount, TransactionType type) {
        this(id, description, amount, type, LocalDateTime.now());
    }

    /** Membuat transaksi dengan waktu catat yang sudah ditentukan. */
    public Transaction(
            int id,
            String description,
            double amount,
            TransactionType type,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** True jika transaksi ini merupakan pemasukan. */
    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }

    /** Mengubah keterangan transaksi. */
    public void changeDescription(String description) {
        this.description = description;
    }

    /** Mengubah nominal transaksi. */
    public void changeAmount(double amount) {
        this.amount = amount;
    }
}
