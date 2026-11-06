package org.delcom.ifs25026pbo2026springbootcashflow.domain.exception;

/**
 * Exception yang dilempar ketika validasi input bisnis gagal.
 */
public class ValidationException extends DomainException {

    public ValidationException(String message) {
        super(message);
    }
}
