package org.delcom.ifs25026pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25026pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25026pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25026pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25026pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25026pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25026pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25026pbo2026springbootcashflow.usecase.FinanceUseCase;

import java.util.List;

/**
 * Tampilan web Vaadin aplikasi Cashflow.
 * Menampilkan ringkasan saldo, daftar transaksi, pencarian keterangan,
 * pengurutan berbasis {@link SortOption}, serta dialog tambah dan hapus.
 */
@Route("")
@PageTitle("Cashflow - Catatan Pemasukan dan Pengeluaran")
public class FinanceView extends VerticalLayout {

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<SortOption> sortSelect = new ComboBox<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span("- Data transaksi belum tersedia!");
    private final Span totalCount = new Span("0");
    private final Span incomeTotal = new Span("Rp 0.00");
    private final Span expenseTotal = new Span("Rp 0.00");
    private final Span balanceTotal = new Span("Rp 0.00");

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Cashflow");
        // Halaman "Tentang" sengaja berupa halaman publik statis (/about), bukan view Vaadin,
        // sehingga tautan memakai anchor biasa dan tetap bisa diindeks crawler.
        Anchor aboutLink = new Anchor("/about", "Tentang Cashflow");
        Nav navigation = new Nav(aboutLink);
        navigation.getElement().setAttribute("aria-label", "Navigasi utama");

        Header header = new Header(title, navigation);
        header.setWidthFull();
        header.getStyle()
                .set("display", "flex")
                .set("align-items", "baseline")
                .set("justify-content", "space-between")
                .set("flex-wrap", "wrap")
                .set("gap", "0.75rem");

        configureGrid();
        configureToolbar();

        // Status daftar transaksi diumumkan ulang saat isi berubah.
        statusLabel.getElement().setAttribute("aria-live", "polite");

        Main main = new Main(buildSummaryCards(), buildToolbar(), statusLabel, grid);
        main.setWidthFull();
        main.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("flex-grow", "1")
                .set("gap", "1rem")
                .set("min-height", "0");

        add(header, main);
        refreshAll();
    }

    private HorizontalLayout buildSummaryCards() {
        // Warna caption dipilih agar rasio kontras terhadap background kartu minimal 4.5:1.
        HorizontalLayout summary = new HorizontalLayout(
                createSummaryCard("Total Transaksi", totalCount, "#DBEAFE", "#1D4ED8", "#1E3A8A"),
                createSummaryCard("Total Pemasukan", incomeTotal, "#DCFCE7", "#166534", "#14532D"),
                createSummaryCard("Total Pengeluaran", expenseTotal, "#FEE2E2", "#991B1B", "#7F1D1D"),
                createSummaryCard("Saldo", balanceTotal, "#E0E7FF", "#4338CA", "#312E81")
        );
        summary.setWidthFull();
        summary.setSpacing(true);
        summary.getStyle().set("align-items", "stretch").set("flex-wrap", "wrap");
        return summary;
    }

    private Div createSummaryCard(
            String label,
            Span value,
            String background,
            String accent,
            String valueColor
    ) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("color", accent);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("line-height", "1.1")
                .set("color", valueColor)
                .set("overflow-wrap", "anywhere");

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "180px")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }

    private void updateSummary() {
        totalCount.setText(String.valueOf(financeUseCase.getAllTransactions().size()));
        incomeTotal.setText(presenter.formatCurrency(financeUseCase.getTotalIncome()));
        expenseTotal.setText(presenter.formatCurrency(financeUseCase.getTotalExpense()));
        balanceTotal.setText(presenter.formatCurrency(financeUseCase.getBalance()));
    }

    private void configureGrid() {
        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1);

        grid.addColumn(transaction -> presenter.formatCurrency(transaction.getAmount()))
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END);

        grid.addColumn(transaction -> presenter.formatType(transaction.getType()))
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true);

        grid.addColumn(transaction -> presenter.formatCreatedAt(transaction))
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("tindakan")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setEmptyStateComponent(emptyState);
        grid.setWidthFull();
        grid.getStyle().set("flex-grow", "1").set("min-height", "0");
        // Grid Vaadin adalah tabel tanpa elemen <caption>; beri nama aksesibel eksplisit.
        grid.getElement().setAttribute("aria-label", "Daftar transaksi cashflow");
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button deleteButton = new Button(
                "Hapus",
                VaadinIcon.TRASH.create(),
                event -> openDeleteDialog(transaction)
        );
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        return new HorizontalLayout(deleteButton);
    }

    private void configureToolbar() {
        // Label eksplisit (bukan hanya placeholder) agar setiap input punya <label for>
        // dan terbaca oleh pembaca layar.
        searchField.setLabel("Cari transaksi");
        searchField.setPlaceholder("Cari keterangan...");
        Span searchIcon = new Span(VaadinIcon.SEARCH.create());
        searchIcon.getElement().setAttribute("aria-hidden", "true");
        searchField.setPrefixComponent(searchIcon);
        searchField.setClearButtonVisible(true);

        sortSelect.setItems(SortOption.values());
        sortSelect.setItemLabelGenerator(SortOption::label);
        sortSelect.setLabel("Urutkan transaksi");
        sortSelect.setPlaceholder("Pilih urutan");
        sortSelect.setClearButtonVisible(true);
        sortSelect.addValueChangeListener(event -> {
            SortOption option = event.getValue();
            if (option == null) {
                refreshAll();
            } else {
                applySort(option);
            }
        });
    }

    private HorizontalLayout buildToolbar() {
        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), event -> searchTransactions());
        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), event -> refreshAll());

        Button addIncomeButton = new Button(
                "Tambah Pemasukan",
                VaadinIcon.PLUS.create(),
                event -> openTransactionDialog(TransactionType.INCOME)
        );
        addIncomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button addExpenseButton = new Button(
                "Tambah Pengeluaran",
                VaadinIcon.MINUS.create(),
                event -> openTransactionDialog(TransactionType.EXPENSE)
        );
        addExpenseButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField,
                searchButton,
                refreshButton,
                sortSelect,
                addIncomeButton,
                addExpenseButton
        );
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, searchField);
        toolbar.getStyle().set("flex-wrap", "wrap");
        return toolbar;
    }

    private void openTransactionDialog(TransactionType type) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();
        descriptionField.setPlaceholder(
                type == TransactionType.INCOME ? "Contoh: Gaji bulanan" : "Contoh: Beli makan siang"
        );

        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setWidthFull();
        amountField.setPlaceholder("Contoh: 1000000");
        amountField.setPrefixComponent(new Span("Rp"));

        Dialog dialog = new Dialog();
        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create(), event -> {
            try {
                double amount = parseAmount(amountField.getValue());
                Transaction created = type == TransactionType.INCOME
                        ? financeUseCase.addIncome(descriptionField.getValue(), amount)
                        : financeUseCase.addExpense(descriptionField.getValue(), amount);
                showSuccess(presenter.showAddSuccess(created));
                dialog.close();
                refreshAll();
            } catch (NumberFormatException ex) {
                showError(presenter.showInvalidAmount());
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());

        VerticalLayout form = new VerticalLayout(descriptionField, amountField);
        form.setPadding(false);
        form.setSpacing(true);

        String title = type == TransactionType.INCOME
                ? "Menambah Pemasukan"
                : "Menambah Pengeluaran";
        buildDialog(dialog, title, DialogTone.PRIMARY, form, cancelButton, saveButton);
        dialog.open();
    }

    /**
     * Mengubah teks nominal menjadi angka.
     * Menerima "1000000", "1.000.000", "1,000,000", atau "1.500.000,50".
     */
    private double parseAmount(String raw) {
        if (raw == null) {
            throw new NumberFormatException("Jumlah kosong");
        }
        String normalized = raw.trim().replace("Rp", "").replace(" ", "");
        if (normalized.isEmpty()) {
            throw new NumberFormatException("Jumlah kosong");
        }

        if (normalized.contains(",")) {
            // Koma dianggap desimal; titik adalah pemisah ribuan.
            normalized = normalized.replace(".", "").replace(',', '.');
        } else if (normalized.indexOf('.') != normalized.lastIndexOf('.')) {
            // Lebih dari satu titik berarti pemisah ribuan.
            normalized = normalized.replace(".", "");
        }

        return Double.parseDouble(normalized);
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph(
                "Yakin ingin menghapus transaksi \"" + transaction.getDescription()
                        + "\" (" + presenter.format(transaction) + ")?"
        );
        message.getStyle().set("margin", "0");

        Dialog dialog = new Dialog();
        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        buildDialog(dialog, "Menghapus Transaksi", DialogTone.DANGER, message, cancelButton, confirmButton);
        dialog.open();
    }

    private enum DialogTone {
        PRIMARY,
        DANGER
    }

    /**
     * Merakit dialog dengan header berwarna, body, dan footer abu-abu.
     * Pakai Div + warna solid agar background pasti terlihat.
     */
    private void buildDialog(
            Dialog dialog,
            String title,
            DialogTone tone,
            Component bodyContent,
            Button... footerButtons
    ) {
        dialog.setWidth("500px");
        dialog.setMaxWidth("95vw");
        dialog.setResizable(false);
        dialog.addClassName("cashflow-form-dialog");
        // Dialog ini dibuat manual (bukan setHeaderTitle), jadi nama aksesibelnya diisi eksplisit.
        dialog.getElement().setAttribute("aria-label", title);

        boolean danger = tone == DialogTone.DANGER;
        String headerBackground = danger ? "#DC2626" : "#2563EB";

        Span headerTitle = new Span(title);
        headerTitle.getStyle()
                .set("margin", "0")
                .set("color", "#FFFFFF")
                .set("font-size", "1.125rem")
                .set("font-weight", "600")
                .set("line-height", "1.4");

        Div header = new Div(headerTitle);
        header.getStyle()
                .set("background", headerBackground)
                .set("padding", "1rem 1.5rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        Div body = new Div(bodyContent);
        body.getStyle()
                .set("background", "#FFFFFF")
                .set("padding", "1.25rem 1.5rem")
                .set("overflow", "hidden")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        Div footer = new Div();
        footer.getStyle()
                .set("display", "flex")
                .set("justify-content", "flex-end")
                .set("align-items", "center")
                .set("gap", "0.75rem")
                .set("background", "#F3F4F6")
                .set("border-top", "1px solid #E5E7EB")
                .set("padding", "0.85rem 1.5rem 1.1rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");
        footer.add(footerButtons);

        Div shell = new Div(header, body, footer);
        shell.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("width", "100%")
                .set("overflow", "hidden")
                .set("border-radius", "12px")
                .set("background", "#FFFFFF");

        dialog.removeAll();
        dialog.add(shell);
        removeOverlayPadding(dialog);
    }

    private void removeOverlayPadding(Dialog dialog) {
        dialog.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                dialog.getElement().executeJs("""
                        const overlay = this.$.overlay;
                        if (!overlay || !overlay.shadowRoot) {
                            return;
                        }
                        const content = overlay.shadowRoot.querySelector('[part="content"]');
                        if (content) {
                            content.style.padding = '0';
                            content.style.overflow = 'visible';
                            content.style.maxHeight = 'none';
                        }
                        const overlayPart = overlay.shadowRoot.querySelector('[part="overlay"]');
                        if (overlayPart) {
                            overlayPart.style.padding = '0';
                            overlayPart.style.overflow = 'hidden';
                        }
                        """);
            }
        });
    }

    private void deleteTransaction(Transaction transaction) {
        try {
            financeUseCase.removeTransaction(transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        }
    }

    private void searchTransactions() {
        String keyword = searchField.getValue();
        if (keyword == null || keyword.isBlank()) {
            refreshAll();
            return;
        }

        List<Transaction> results = financeUseCase.searchTransactions(keyword);
        emptyState.setText(results.isEmpty()
                ? "- Transaksi tidak ditemukan!"
                : "- Data transaksi belum tersedia!");
        refreshGrid(results, "Hasil Pencarian: \"" + keyword + "\"");
    }

    private void applySort(SortOption option) {
        emptyState.setText("- Data transaksi belum tersedia!");
        refreshGrid(financeUseCase.sortTransactions(option), "Daftar Transaksi (Terurut)");
    }

    private void refreshAll() {
        emptyState.setText("- Data transaksi belum tersedia!");
        SortOption option = sortSelect.getValue();
        List<Transaction> transactions = option == null
                ? financeUseCase.getAllTransactions()
                : financeUseCase.sortTransactions(option);
        refreshGrid(transactions, "Daftar Transaksi");
        searchField.clear();
    }

    private void refreshGrid(List<Transaction> transactions, String title) {
        grid.setItems(transactions);
        updateSummary();
        if (transactions.isEmpty()) {
            statusLabel.setText(title + " — " + emptyState.getText().replaceFirst("^- ", ""));
        } else {
            statusLabel.setText(title);
        }
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
