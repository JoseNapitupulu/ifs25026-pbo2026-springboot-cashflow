package org.delcom.ifs25026pbo2026springbootcashflow.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;

/**
 * Konfigurasi app shell Vaadin.
 * Anotasi seperti {@link Meta} hanya sah bila diletakkan pada kelas
 * {@link AppShellConfigurator}, bukan pada view.
 */
@Meta(
        name = "description",
        content = "Aplikasi cashflow untuk mencatat pemasukan dan pengeluaran, "
                + "lengkap dengan ringkasan saldo, pencarian, dan pengurutan transaksi."
)
public class AppShell implements AppShellConfigurator {
}
