package ru.checkscan.app.presentation;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ru.checkscan.app.MainActivity;

public abstract class ScreenFragment extends Fragment {
    protected ReceiptViewModel model;
    protected Ui ui;

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        model = new ViewModelProvider(requireActivity()).get(ReceiptViewModel.class);
        ui = new Ui(requireContext());
    }

    protected MainActivity host() {
        return (MainActivity) requireActivity();
    }
}
