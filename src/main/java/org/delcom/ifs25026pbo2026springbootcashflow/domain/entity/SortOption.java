package org.delcom.ifs25026pbo2026springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Kriteria pengurutan transaksi.
 * Setiap opsi membawa comparator-nya sendiri sehingga logika pengurutan
 * terpusat di domain, bukan tersebar sebagai angka ajaib di view/use case.
 */
public enum SortOption {

    /** Urutkan nominal dari yang terkecil. */
    AMOUNT_SMALLEST(
            "Jumlah Terkecil",
            Comparator.comparingDouble(Transaction::getAmount)
                    .thenComparingInt(Transaction::getId)
    ),

    /** Urutkan nominal dari yang terbesar. */
    AMOUNT_LARGEST(
            "Jumlah Terbesar",
            Comparator.comparingDouble(Transaction::getAmount).reversed()
                    .thenComparingInt(Transaction::getId)
    ),

    /** Tampilkan pemasukan terlebih dahulu. */
    INCOME_FIRST(
            "Pemasukan Dulu",
            Comparator.comparing((Transaction transaction) ->
                            transaction.getType() != TransactionType.INCOME)
                    .thenComparingInt(Transaction::getId)
    ),

    /** Tampilkan pengeluaran terlebih dahulu. */
    EXPENSE_FIRST(
            "Pengeluaran Dulu",
            Comparator.comparing((Transaction transaction) ->
                            transaction.getType() != TransactionType.EXPENSE)
                    .thenComparingInt(Transaction::getId)
    );

    /** Label berbahasa Indonesia untuk pilihan pengurutan. */
    private final String label;

    /** Comparator yang digunakan untuk mengurutkan daftar transaksi. */
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    /** Mengembalikan label tampilan dari opsi pengurutan. */
    public String label() {
        return label;
    }

    /** Mengembalikan comparator yang sesuai dengan opsi ini. */
    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
