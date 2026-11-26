package ru.checkscan.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ru.checkscan.app.presentation.ReceiptViewModel;
import ru.checkscan.app.presentation.Ui;
import ru.checkscan.app.presentation.history.HistoryFragment;
import ru.checkscan.app.presentation.home.HomeFragment;
import ru.checkscan.app.presentation.receipt.ReceiptFragment;
import ru.checkscan.app.presentation.settings.SettingsFragment;
import ru.checkscan.app.presentation.statistics.ProductFragment;
import ru.checkscan.app.presentation.statistics.StatisticsFragment;

public class MainActivity extends AppCompatActivity {
    private static final int CONTAINER_ID = 0x10001;
    private ReceiptViewModel model;
    private ProgressDialog progress;
    private Ui ui;
    private LinearLayout nav;
    private String selected = "home";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getPreferences(0).edit().remove("token").apply();
        ui = new Ui(this);
        model = new ViewModelProvider(this).get(ReceiptViewModel.class);
        LinearLayout root = ui.column();
        root.setBackgroundColor(Ui.BG);
        setContentView(root);
        FrameLayout container = new FrameLayout(this);
        container.setId(CONTAINER_ID);
        root.addView(container, new LinearLayout.LayoutParams(-1, 0, 1));
        nav = new LinearLayout(this);
        nav.setBackgroundColor(Color.WHITE);
        ui.pad(nav, 8, 8, 8, 12);
        root.addView(nav);
        selected = state == null ? "home" : state.getString("selected", "home");
        drawNav();
        if (state == null) show(new HomeFragment(), false);
        model.state()
                .observe(
                        this,
                        result -> {
                            if (result.loading) {
                                if (progress == null) {
                                    progress = new ProgressDialog(this);
                                    progress.setMessage("Получаем данные чека…");
                                    progress.setCancelable(false);
                                    progress.show();
                                }
                            } else {
                                if (progress != null) {
                                    progress.dismiss();
                                    progress = null;
                                }
                                if (result.message != null) {
                                    String message = result.message;
                                    boolean success = result.success;
                                    model.clearMessage();
                                    if (success) {
                                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                                        showHistory();
                                    } else
                                        new AlertDialog.Builder(this)
                                                .setTitle("Не удалось загрузить чек")
                                                .setMessage(message)
                                                .setPositiveButton("Понятно", null)
                                                .show();
                                }
                            }
                        });
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        out.putString("selected", selected);
        super.onSaveInstanceState(out);
    }

    @Override
    protected void onDestroy() {
        if (progress != null) progress.dismiss();
        super.onDestroy();
    }

    private void drawNav() {
        nav.removeAllViews();
        navItem("Обзор", "home", R.drawable.ic_nav_home, () -> openTab("home", new HomeFragment()));
        navItem("История", "history", R.drawable.ic_nav_history, this::showHistory);
        navItem("Статистика", "stats", R.drawable.ic_nav_statistics, this::showStatistics);
        navItem(
                "Настройки",
                "settings",
                R.drawable.ic_nav_settings,
                () -> openTab("settings", new SettingsFragment()));
    }

    private void navItem(String label, String id, int icon, Runnable action) {
        boolean active = selected.equals(id);
        int color = active ? Ui.GREEN : Ui.MUTED;
        LinearLayout item = ui.column();
        item.setGravity(Gravity.CENTER);
        item.setSelected(active);
        item.setContentDescription(label);
        ui.pad(item, 2, 6, 2, 6);

        ImageView image = new ImageView(this);
        image.setImageResource(icon);
        image.setImageTintList(ColorStateList.valueOf(color));
        image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        item.addView(image, new LinearLayout.LayoutParams(ui.dp(24), ui.dp(24)));

        TextView text = ui.text(label, 11, color, active);
        text.setGravity(Gravity.CENTER);
        text.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(-1, -2);
        textParams.topMargin = ui.dp(4);
        item.addView(text, textParams);
        nav.addView(item, new LinearLayout.LayoutParams(0, -2, 1));
        item.setOnClickListener(w -> action.run());
    }

    private void show(Fragment fragment, boolean back) {
        androidx.fragment.app.FragmentTransaction t =
                getSupportFragmentManager().beginTransaction().replace(CONTAINER_ID, fragment);
        if (back) t.addToBackStack(null);
        t.commit();
    }

    private void openTab(String tab, Fragment fragment) {
        selected = tab;
        drawNav();
        getSupportFragmentManager()
                .popBackStackImmediate(
                        null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        show(fragment, false);
    }

    public void showHistory() {
        openTab("history", new HistoryFragment());
    }

    public void showStatistics() {
        openTab("stats", new StatisticsFragment());
    }

    public void openReceipt(long id) {
        show(ReceiptFragment.create(id), true);
    }

    public void openProduct(String name) {
        show(ProductFragment.create(name), true);
    }

    public void scan() {
        if (!Boolean.TRUE.equals(
                model.state().getValue() != null && model.state().getValue().loading))
            startActivityForResult(new Intent(this, ScannerActivity.class), 1);
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == 1 && result == RESULT_OK && data != null)
            model.importReceipt(data.getStringExtra("qr"));
    }

    public void manual() {
        EditText input = new EditText(this);
        input.setHint("t=...&s=...&fn=...&i=...&fp=...&n=...");
        new AlertDialog.Builder(this)
                .setTitle("Данные QR-кода")
                .setView(input)
                .setNegativeButton("Отмена", null)
                .setPositiveButton(
                        "Загрузить", (d, w) -> model.importReceipt(input.getText().toString()))
                .show();
    }
}
