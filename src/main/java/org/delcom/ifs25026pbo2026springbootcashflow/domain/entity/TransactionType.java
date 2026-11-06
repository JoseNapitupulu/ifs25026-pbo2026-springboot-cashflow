package org.delcom.ifs25026pbo2026springbootcashflow.domain.entity;

/**
 * Jenis transaksi keuangan: uang masuk atau uang keluar.
 */
public enum TransactionType {

    /** Pemasukan — menambah saldo. */
    INCOME("Pemasukan"),

    /** Pengeluaran — mengurangi saldo. */
    EXPENSE("Pengeluaran");

    /** Label berbahasa Indonesia untuk ditampilkan di antarmuka. */
    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    /** Mengembalikan label tampilan dari jenis transaksi. */
    public String label() {
        return label;
    }
}
