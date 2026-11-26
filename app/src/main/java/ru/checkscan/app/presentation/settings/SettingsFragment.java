package ru.checkscan.app.presentation.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.checkscan.app.presentation.ScreenFragment;

public class SettingsFragment extends ScreenFragment {
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle state) {
        LinearLayout content = ui.column();
        ui.heading(content, "Приложение", "Настройки", "Данные приложения");
        ui.empty(
                content,
                "Данные на устройстве",
                "Чеки хранятся локально. При удалении приложения они будут удалены.");
        return ui.scroll(content);
    }
}
