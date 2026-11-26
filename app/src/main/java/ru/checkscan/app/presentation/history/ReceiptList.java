package ru.checkscan.app.presentation.history;

import android.widget.LinearLayout;

import ru.checkscan.app.domain.model.Receipt;
import ru.checkscan.app.presentation.Ui;

import java.util.List;
import java.util.function.LongConsumer;

public class ReceiptList {
    public static void render(
            Ui ui, LinearLayout content, List<Receipt> receipts, int limit, LongConsumer select) {
        if (receipts.isEmpty()) {
            ui.empty(
                    content,
                    "Пока здесь пусто",
                    "Отсканируйте первый чек, чтобы увидеть покупки и аналитику.");
            return;
        }
        int count = 0;
        for (Receipt receipt : receipts) {
            if (limit > 0 && count++ >= limit) break;
            LinearLayout card = ui.card(), row = new LinearLayout(content.getContext());
            String name =
                    receipt.merchant == null || receipt.merchant.isEmpty()
                            ? "Магазин"
                            : receipt.merchant;
            row.addView(ui.text(name, 16, Ui.INK, true), new LinearLayout.LayoutParams(0, -2, 1));
            row.addView(ui.text(Ui.money(receipt.total), 16, Ui.GREEN, true));
            card.addView(row);
            ui.space(card, 7);
            card.addView(
                    ui.text(
                            Ui.date(receipt.date) + "  ·  " + receipt.items.size() + " позиций  ›",
                            13,
                            Ui.MUTED,
                            false));
            card.setOnClickListener(v -> select.accept(receipt.id));
            content.addView(card);
            ui.space(content, 10);
        }
    }
}
