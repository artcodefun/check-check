package ru.checkscan.app.presentation.receipt;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.checkscan.app.domain.model.ReceiptItem;
import ru.checkscan.app.presentation.ScreenFragment;
import ru.checkscan.app.presentation.Ui;

public class ReceiptFragment extends ScreenFragment {
    public static ReceiptFragment create(long id) {
        ReceiptFragment f = new ReceiptFragment();
        Bundle args = new Bundle();
        args.putLong("id", id);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle state) {
        long id = requireArguments().getLong("id");
        LinearLayout content = ui.column();
        View view = ui.scroll(content);
        model.receipt()
                .observe(
                        this,
                        receipt -> {
                            if (receipt == null || receipt.id != id) return;
                            content.removeAllViews();
                            ui.heading(
                                    content,
                                    "История  /  Чек",
                                    receipt.merchant,
                                    Ui.date(receipt.date) + "  ·  " + Ui.money(receipt.total));
                            for (ReceiptItem item : receipt.items) {
                                LinearLayout card = ui.card();
                                card.addView(ui.text(item.name, 15, Ui.INK, true));
                                ui.space(card, 5);
                                card.addView(
                                        ui.text(
                                                item.quantity
                                                        + " × "
                                                        + Ui.money(item.price)
                                                        + "   ·   "
                                                        + Ui.money(item.total),
                                                13,
                                                Ui.MUTED,
                                                false));
                                content.addView(card);
                                ui.space(content, 8);
                            }
                            content.addView(
                                    ui.button(
                                            "Удалить чек",
                                            () ->
                                                    new AlertDialog.Builder(requireContext())
                                                            .setTitle("Удалить чек?")
                                                            .setMessage(
                                                                    "Все его позиции также будут"
                                                                            + " удалены.")
                                                            .setNegativeButton("Отмена", null)
                                                            .setPositiveButton(
                                                                    "Удалить",
                                                                    (d, w) -> {
                                                                        model.delete(id);
                                                                        host().showHistory();
                                                                    })
                                                            .show(),
                                            false));
                            ui.space(content, 8);
                            content.addView(
                                    ui.button("← К истории", () -> host().showHistory(), false));
                        });
        model.loadReceipt(id);
        return view;
    }

    @Override
    public void onDestroyView() {
        model.receipt().removeObservers(this);
        super.onDestroyView();
    }
}
