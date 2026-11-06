package org.delcom.ifs25026pbo2026springbootcashflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Pemuat sederhana file .env ke system properties.
 * Dipanggil sebelum Spring Boot start agar ${APP_PORT} di application.yaml terisi.
 */
public final class DotEnv {

    private DotEnv() {
    }

    /** Memuat .env dari direktori kerja jika file tersebut tersedia. */
    public static void load() {
        Path envFile = Path.of(".env");
        if (!Files.isRegularFile(envFile)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(envFile);
            for (String line : lines) {
                parseLine(line);
            }
        } catch (IOException ex) {
            System.err.println("Gagal membaca .env: " + ex.getMessage());
        }
    }

    private static void parseLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return;
        }

        int separator = trimmed.indexOf('=');
        if (separator <= 0) {
            return;
        }

        String key = trimmed.substring(0, separator).trim();
        String value = trimmed.substring(separator + 1).trim();

        if (value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            value = value.substring(1, value.length() - 1);
        }

        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }
}
