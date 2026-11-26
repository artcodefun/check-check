package ru.checkscan.app.presentation.statistics;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.checkscan.app.domain.model.Receipt;
import ru.checkscan.app.domain.model.SpendingReport;
import ru.checkscan.app.domain.model.SpendingReport.Period;
import ru.checkscan.app.presentation.ScreenFragment;
import ru.checkscan.app.presentation.Ui;

import java.util.Locale;

public class StatisticsFragment extends ScreenFragment {
    private LinearLayout content;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle state) {
        content = ui.column();
        return ui.scroll(content);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        model.spending()
                .observe(
                        getViewLifecycleOwner(),
                        report -> {
                            if (report.period == model.spendingPeriod()) render(report);
                        });
        model.refreshSpending(model.spendingPeriod());
    }

    private void render(SpendingReport report) {
        content.removeAllViews();
        ui.heading(content, "Аналитика", "Ваши расходы", "Покупки, магазины и цены в одном месте");
        LinearLayout filters = new LinearLayout(requireContext());
        addFilter(filters, "Месяц", Period.MONTH, report.period);
        addFilter(filters, "Год", Period.YEAR, report.period);
        addFilter(filters, "Всё время", Period.ALL, report.period);
        content.addView(filters);
        ui.space(content, 18);
        if (report.count == 0) {
            ui.empty(
                    content,
                    "Нет чеков за этот период",
                    "Выберите другой период или добавьте чек, чтобы увидеть статистику.");
            return;
        }
        LinearLayout hero = ui.card();
        hero.setBackground(ui.shape(Ui.GREEN, 24));
        hero.addView(ui.text("РАСХОДЫ ЗА ПЕРИОД", 11, 0xffc4ded0, true));
        ui.space(hero, 8);
        hero.addView(ui.text(Ui.money(report.total), 30, Color.WHITE, true));
        ui.space(hero, 8);
        hero.addView(
                ui.text(
                        report.count
                                + " чеков  ·  средний чек "
                                + Ui.money(Math.round((double) report.total / report.count)),
                        13,
                        0xffd7e9dd,
                        false));
        if (report.period != Period.ALL) {
            ui.space(hero, 8);
            String comparison;
            if (report.previousCount == 0) comparison = "Нет чеков за предыдущий период";
            else if (report.previousTotal == 0)
                comparison = "За предыдущий период: " + Ui.money(report.previousTotal);
            else {
                double percent =
                        (report.total - report.previousTotal) * 100.0 / report.previousTotal;
                comparison =
                        String.format(
                                new Locale("ru", "RU"),
                                "%+.1f%% к предыдущему полному периоду",
                                percent);
            }
            hero.addView(ui.text(comparison, 12, 0xffc4ded0, false));
        }
        content.addView(hero);
        ui.space(content, 22);
        LinearLayout graph = ui.card();
        graph.addView(ui.text("Динамика расходов", 18, Ui.INK, true));
        ui.space(graph, 6);
        graph.addView(
                ui.text(
                        report.period == Period.MONTH
                                ? "По дням месяца · нажмите на столбец"
                                : "По месяцам · нажмите на столбец",
                        13,
                        Ui.MUTED,
                        false));
        ui.space(graph, 12);
        HorizontalScrollView scroll = new HorizontalScrollView(requireContext());
        scroll.setHorizontalScrollBarEnabled(false);
        SpendingChart chart = new SpendingChart(requireContext(), report.buckets, this::showBucket);
        int width = ui.dp(Math.max(300, report.buckets.size() * 42));
        scroll.addView(chart, new HorizontalScrollView.LayoutParams(width, ui.dp(205)));
        graph.addView(scroll);
        content.addView(graph);
        if (report.period == Period.ALL) scroll.post(() -> scroll.fullScroll(View.FOCUS_RIGHT));
        ui.space(content, 22);
        section("На что уходит больше", "Суммы позиций из чеков");
        long itemTotal = 0;
        for (SpendingReport.Product p : report.products) itemTotal += p.total;
        if (report.products.isEmpty())
            ui.empty(content, "Нет позиций", "В чеках за период нет товаров.");
        else
            for (int i = 0; i < Math.min(5, report.products.size()); i++) {
                SpendingReport.Product p = report.products.get(i);
                LinearLayout card =
                        entry(
                                p.name,
                                Ui.money(p.total),
                                p.count
                                        + " позиций  ·  "
                                        + share(p.total, itemTotal)
                                        + " от суммы товаров  ›");
                card.setOnClickListener(v -> host().openProduct(p.name));
                content.addView(card);
                ui.space(content, 8);
            }
        if (report.products.size() > 5)
            content.addView(
                    ui.button(
                            "Все товары · " + report.products.size(),
                            () -> showProducts(report),
                            false));
        ui.space(content, 22);
        section("Где покупаем", "Распределение по сумме чеков");
        for (int i = 0; i < Math.min(5, report.merchants.size()); i++) {
            SpendingReport.Merchant m = report.merchants.get(i);
            content.addView(
                    entry(
                            m.name,
                            Ui.money(m.total),
                            m.count
                                    + " чеков  ·  "
                                    + share(m.total, report.total)
                                    + " от расходов"));
            ui.space(content, 8);
        }
        if (report.merchants.size() > 5)
            content.addView(
                    ui.button(
                            "Все магазины · " + report.merchants.size(),
                            () -> showMerchants(report),
                            false));
        ui.space(content, 22);
        section("Изменение цен", "Последние две покупки товара за период");
        if (report.priceChanges.isEmpty())
            ui.empty(
                    content,
                    "Пока не с чем сравнить",
                    "Здесь появятся товары, купленные хотя бы дважды за выбранный период.");
        else {
            for (int i = 0; i < Math.min(5, report.priceChanges.size()); i++) {
                SpendingReport.PriceChange change = report.priceChanges.get(i);
                LinearLayout card = priceEntry(change);
                card.setOnClickListener(v -> host().openProduct(change.name));
                content.addView(card);
                ui.space(content, 8);
            }
            if (report.priceChanges.size() > 5)
                content.addView(
                        ui.button(
                                "Все изменения · " + report.priceChanges.size(),
                                () -> showChanges(report),
                                false));
        }
        ui.space(content, 12);
        content.addView(
                ui.text(
                        "Товары объединяются по точному названию из чека. Итоги чеков и позиций"
                                + " могут различаться.",
                        12,
                        Ui.MUTED,
                        false));
    }

    private void addFilter(LinearLayout filters, String label, Period value, Period selected) {
        TextView button = ui.text(label, 13, value == selected ? Color.WHITE : Ui.GREEN, true);
        button.setGravity(Gravity.CENTER);
        button.setBackground(ui.shape(value == selected ? Ui.GREEN : 0xffe7eee8, 14));
        ui.pad(button, 5, 11, 5, 11);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1);
        params.rightMargin = ui.dp(6);
        filters.addView(button, params);
        button.setOnClickListener(v -> model.refreshSpending(value));
    }

    private void section(String title, String subtitle) {
        content.addView(ui.text(title, 20, Ui.INK, true));
        ui.space(content, 5);
        content.addView(ui.text(subtitle, 13, Ui.MUTED, false));
        ui.space(content, 12);
    }

    private String share(long value, long total) {
        return total == 0
                ? "—"
                : String.format(new Locale("ru", "RU"), "%.1f%%", value * 100.0 / total);
    }

    private LinearLayout entry(String title, String value, String subtitle) {
        LinearLayout card = ui.card(), row = new LinearLayout(requireContext());
        row.addView(ui.text(title, 15, Ui.INK, true), new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(ui.text(value, 15, Ui.GREEN, true));
        card.addView(row);
        ui.space(card, 6);
        card.addView(ui.text(subtitle, 13, Ui.MUTED, false));
        return card;
    }

    private LinearLayout priceEntry(SpendingReport.PriceChange change) {
        long difference = change.current - change.previous;
        String delta =
                (difference > 0 ? "+" : difference < 0 ? "−" : "") + Ui.money(Math.abs(difference));
        String percent =
                change.previous == 0
                        ? ""
                        : String.format(
                                new Locale("ru", "RU"),
                                " (%+.1f%%)",
                                difference * 100.0 / change.previous);
        return entry(
                change.name,
                delta,
                Ui.money(change.previous)
                        + " → "
                        + Ui.money(change.current)
                        + percent
                        + "  ·  "
                        + Ui.date(change.date)
                        + "  ›");
    }

    private void showBucket(SpendingReport.Bucket bucket) {
        if (bucket.receipts.isEmpty()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(bucket.label)
                    .setMessage("За этот период чеков нет.")
                    .setPositiveButton("Понятно", null)
                    .show();
            return;
        }
        String[] labels = new String[bucket.receipts.size()];
        for (int i = 0; i < labels.length; i++) {
            Receipt r = bucket.receipts.get(i);
            labels[i] =
                    Ui.date(r.date)
                            + "  ·  "
                            + (r.merchant == null ? "Магазин" : r.merchant)
                            + "  ·  "
                            + Ui.money(r.total);
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(bucket.label + "  ·  " + Ui.money(bucket.total))
                .setItems(
                        labels,
                        (dialog, which) -> host().openReceipt(bucket.receipts.get(which).id))
                .setNegativeButton("Закрыть", null)
                .show();
    }

    private void showProducts(SpendingReport report) {
        String[] labels = new String[report.products.size()];
        for (int i = 0; i < labels.length; i++) {
            SpendingReport.Product p = report.products.get(i);
            labels[i] = p.name + "  ·  " + Ui.money(p.total);
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Все товары")
                .setItems(
                        labels,
                        (dialog, which) -> host().openProduct(report.products.get(which).name))
                .setNegativeButton("Закрыть", null)
                .show();
    }

    private void showMerchants(SpendingReport report) {
        String[] labels = new String[report.merchants.size()];
        for (int i = 0; i < labels.length; i++) {
            SpendingReport.Merchant m = report.merchants.get(i);
            labels[i] = m.name + "  ·  " + Ui.money(m.total) + "  ·  " + m.count + " чеков";
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Все магазины")
                .setItems(labels, null)
                .setNegativeButton("Закрыть", null)
                .show();
    }

    private void showChanges(SpendingReport report) {
        String[] labels = new String[report.priceChanges.size()];
        for (int i = 0; i < labels.length; i++) {
            SpendingReport.PriceChange c = report.priceChanges.get(i);
            labels[i] = c.name + "  ·  " + Ui.money(c.previous) + " → " + Ui.money(c.current);
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Изменения цен")
                .setItems(
                        labels,
                        (dialog, which) -> host().openProduct(report.priceChanges.get(which).name))
                .setNegativeButton("Закрыть", null)
                .show();
    }
}
