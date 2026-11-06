package org.delcom.ifs25026pbo2026springbootcashflow.domain.exception;

/**
 * Exception yang dilempar ketika transaksi dengan ID tertentu tidak ditemukan.
 */
public class EntityNotFoundException extends DomainException {

    private final int id;

    public EntityNotFoundException(int id) {
        super("Transaksi dengan ID " + id + " tidak ditemukan.");
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
