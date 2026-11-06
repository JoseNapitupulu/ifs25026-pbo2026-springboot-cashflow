package org.delcom.ifs25026pbo2026springbootcashflow.usecase;

import org.delcom.ifs25026pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25026pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25026pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25026pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25026pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25026pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;

/**
 * Use case yang menangani logika bisnis pencatatan keuangan.
 * Tidak melakukan I/O tampilan — hanya memproses data dan melempar checked
 * exception domain jika operasi tidak valid atau data tidak ditemukan.
 */
public class FinanceUseCase {

    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua transaksi yang tersedia. */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /** Menambahkan transaksi pemasukan setelah validasi. */
    public Transaction addIncome(String description, double amount) throws ValidationException {
        validate(description, amount);
        return transactionRepository.saveIncome(description.trim(), amount);
    }

    /** Menambahkan transaksi pengeluaran setelah validasi. */
    public Transaction addExpense(String description, double amount) throws ValidationException {
        validate(description, amount);
        return transactionRepository.saveExpense(description.trim(), amount);
    }

    /** Menghapus transaksi berdasarkan ID; melempar exception jika tidak ditemukan. */
    public void removeTransaction(int id) throws EntityNotFoundException {
        boolean deleted = transactionRepository.deleteById(id);
        if (!deleted) {
            throw new EntityNotFoundException(id);
        }
    }

    /** Mencari transaksi yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return transactionRepository.findAll();
        }
        String lowerKeyword = keyword.trim().toLowerCase();
        return transactionRepository.findBy(
                transaction -> transaction.getDescription().toLowerCase().contains(lowerKeyword)
        );
    }

    /** Mengurutkan transaksi sesuai kriteria {@link SortOption} yang dipilih. */
    public List<Transaction> sortTransactions(SortOption option) {
        return transactionRepository.findAll().stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Saldo saat ini: total pemasukan dikurangi total pengeluaran. */
    public double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    /** Total seluruh pemasukan. */
    public double getTotalIncome() {
        return sumByType(TransactionType.INCOME);
    }

    /** Total seluruh pengeluaran. */
    public double getTotalExpense() {
        return sumByType(TransactionType.EXPENSE);
    }

    /** Menjumlahkan nominal transaksi untuk jenis tertentu. */
    private double sumByType(TransactionType type) {
        return transactionRepository.findAll().stream()
                .filter(transaction -> transaction.getType() == type)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /** Aturan validasi bisnis untuk keterangan dan nominal transaksi. */
    private void validate(String description, double amount) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan transaksi tidak boleh kosong");
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new ValidationException("Jumlah transaksi harus lebih besar dari 0");
        }
    }
}
