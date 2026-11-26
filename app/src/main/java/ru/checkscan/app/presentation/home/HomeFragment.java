package ru.checkscan.app.presentation.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.checkscan.app.domain.model.Receipt;
import ru.checkscan.app.presentation.ScreenFragment;
import ru.checkscan.app.presentation.Ui;
import ru.checkscan.app.presentation.history.ReceiptList;

import java.util.List;

public class HomeFragment extends ScreenFragment {
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle state) {
        LinearLayout content = ui.column();
        View view = ui.scroll(content);
        model.receipts().observe(this, receipts -> render(content, receipts));
        model.refreshReceipts();
        return view;
    }

    private void render(LinearLayout content, List<Receipt> receipts) {
        content.removeAllViews();
        ui.heading(
                content, "ЧекСкан", "Покупки под контролем", "Все чеки и расходы — в одном месте");
        long total = 0;
        for (Receipt receipt : receipts) total += receipt.total;
        LinearLayout hero = ui.card();
        hero.setBackground(ui.shape(Ui.GREEN, 24));
        hero.addView(ui.text("ВСЕГО ПО ЧЕКАМ", 11, 0xffb9d3c4, true));
        ui.space(hero, 12);
        hero.addView(ui.text(Ui.money(total), 32, Color.WHITE, true));
        ui.space(hero, 8);
        hero.addView(ui.text(receipts.size() + " чеков в вашей коллекции", 14, 0xffc9dfd0, false));
        content.addView(hero);
        ui.space(content, 16);
        content.addView(ui.button("＋  Сканировать чек", () -> host().scan(), true));
        ui.space(content, 10);
        content.addView(ui.button("Ввести QR-код вручную", () -> host().manual(), false));
        ui.space(content, 28);
        content.addView(ui.text("Последние покупки", 20, Ui.INK, true));
        ui.space(content, 14);
        ReceiptList.render(ui, content, receipts, 5, id -> host().openReceipt(id));
    }

    @Override
    public void onDestroyView() {
        model.receipts().removeObservers(this);
        super.onDestroyView();
    }
}
