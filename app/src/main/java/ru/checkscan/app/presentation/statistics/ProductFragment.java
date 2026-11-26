package ru.checkscan.app.presentation.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.checkscan.app.domain.model.ProductStats;
import ru.checkscan.app.presentation.ScreenFragment;
import ru.checkscan.app.presentation.Ui;

public class ProductFragment extends ScreenFragment {
    public static ProductFragment create(String name) {
        ProductFragment f = new ProductFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle state) {
        String name = requireArguments().getString("name");
        LinearLayout content = ui.column();
        View view = ui.scroll(content);
        model.product()
                .observe(
                        this,
                        product -> {
                            if (product == null || !product.name.equals(name)) return;
                            content.removeAllViews();
                            ui.heading(
                                    content,
                                    "Статистика  /  Товар",
                                    name,
                                    "История цены за единицу · за всё время");
                            LinearLayout card = ui.card();
                            card.addView(ui.text("Средняя цена", 13, Ui.MUTED, false));
                            ui.space(card, 5);
                            card.addView(ui.text(Ui.money(product.average), 26, Ui.GREEN, true));
                            ui.space(card, 8);
                            card.addView(
                                    ui.text(
                                            "От "
                                                    + Ui.money(product.min)
                                                    + " до "
                                                    + Ui.money(product.max)
                                                    + "  ·  "
                                                    + product.count
                                                    + " записей",
                                            13,
                                            Ui.MUTED,
                                            false));
                            content.addView(card);
                            ui.space(content, 20);
                            content.addView(ui.text("История цен", 20, Ui.INK, true));
                            ui.space(content, 12);
                            for (ProductStats.PriceEntry entry : product.history) {
                                LinearLayout item = ui.card();
                                item.addView(
                                        ui.text(
                                                Ui.date(entry.date)
                                                        + "    "
                                                        + Ui.money(entry.price),
                                                15,
                                                Ui.INK,
                                                true));
                                ui.space(item, 4);
                                item.addView(
                                        ui.text(
                                                "Количество: "
                                                        + entry.quantity
                                                        + "  ·  "
                                                        + (entry.merchant == null
                                                                        || entry.merchant.isEmpty()
                                                                ? "Магазин"
                                                                : entry.merchant),
                                                13,
                                                Ui.MUTED,
                                                false));
                                content.addView(item);
                                ui.space(content, 8);
                            }
                            content.addView(
                                    ui.button(
                                            "← К статистике",
                                            () -> host().showStatistics(),
                                            false));
                        });
        model.loadProduct(name);
        return view;
    }

    @Override
    public void onDestroyView() {
        model.product().removeObservers(this);
        super.onDestroyView();
    }
}
