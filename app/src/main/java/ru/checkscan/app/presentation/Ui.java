package ru.checkscan.app.presentation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Locale;

public class Ui {
    public static final int INK = 0xff1e2b24,
            MUTED = 0xff6c7a70,
            GREEN = 0xff255c4a,
            BG = 0xfff6f7f3;
    private final Context context;

    public Ui(Context context) {
        this.context = context;
    }

    public int dp(float n) {
        return (int) (n * context.getResources().getDisplayMetrics().density + .5f);
    }

    public void pad(View v, int a, int b, int c, int d) {
        v.setPadding(dp(a), dp(b), dp(c), dp(d));
    }

    public LinearLayout column() {
        LinearLayout l = new LinearLayout(context);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    public GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    public TextView text(String text, int size, int color, boolean bold) {
        TextView v = new TextView(context);
        v.setText(text);
        v.setTextSize(size);
        v.setTextColor(color);
        if (bold) v.setTypeface(null, Typeface.BOLD);
        return v;
    }

    public void space(LinearLayout layout, int height) {
        layout.addView(new View(context), new LinearLayout.LayoutParams(1, dp(height)));
    }

    public LinearLayout card() {
        LinearLayout l = column();
        l.setBackground(shape(Color.WHITE, 20));
        pad(l, 18, 18, 18, 18);
        return l;
    }

    public TextView button(String label, Runnable action, boolean primary) {
        TextView v = text(label, 15, primary ? Color.WHITE : GREEN, true);
        v.setGravity(Gravity.CENTER);
        v.setBackground(shape(primary ? GREEN : 0xffe7eee8, 16));
        pad(v, 16, 15, 16, 15);
        v.setOnClickListener(w -> action.run());
        return v;
    }

    public void heading(LinearLayout layout, String eyebrow, String title, String description) {
        TextView e = text(eyebrow.toUpperCase(new Locale("ru")), 11, GREEN, true);
        e.setLetterSpacing(.12f);
        layout.addView(e);
        space(layout, 8);
        layout.addView(text(title, 30, INK, true));
        space(layout, 7);
        layout.addView(text(description, 14, MUTED, false));
        space(layout, 24);
    }

    public void empty(LinearLayout layout, String title, String body) {
        LinearLayout b = card();
        b.addView(text(title, 16, INK, true));
        space(b, 8);
        b.addView(text(body, 14, MUTED, false));
        layout.addView(b);
    }

    public ScrollView scroll(LinearLayout content) {
        ScrollView s = new ScrollView(context);
        s.setFillViewport(true);
        pad(content, 22, 24, 22, 24);
        s.addView(content);
        return s;
    }

    public static String money(long cents) {
        return NumberFormat.getNumberInstance(new Locale("ru", "RU")).format(cents / 100.0) + " ₽";
    }

    public static String date(String date) {
        return date.length() >= 10
                ? date.substring(8, 10) + "." + date.substring(5, 7) + "." + date.substring(0, 4)
                : date;
    }
}
