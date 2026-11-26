package ru.checkscan.app.presentation.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.checkscan.app.presentation.ScreenFragment;

public class HistoryFragment extends ScreenFragment {
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle state) {
        LinearLayout content = ui.column();
        View view = ui.scroll(content);
        model.receipts()
                .observe(
                        this,
                        receipts -> {
                            content.removeAllViews();
                            ui.heading(
                                    content,
                                    "Коллекция",
                                    "История чеков",
                                    "Ваши покупки в хронологическом порядке");
                            content.addView(
                                    ui.button("＋  Добавить чек", () -> host().scan(), true));
                            ui.space(content, 20);
                            ReceiptList.render(
                                    ui, content, receipts, 0, id -> host().openReceipt(id));
                        });
        model.refreshReceipts();
        return view;
    }

    @Override
    public void onDestroyView() {
        model.receipts().removeObservers(this);
        super.onDestroyView();
    }
}
