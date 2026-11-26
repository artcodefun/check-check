package ru.checkscan.app.presentation;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ru.checkscan.app.di.AppContainer;
import ru.checkscan.app.domain.model.ProductStats;
import ru.checkscan.app.domain.model.Receipt;
import ru.checkscan.app.domain.model.SpendingReport;
import ru.checkscan.app.domain.usecase.BuildSpendingReport;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiptViewModel extends AndroidViewModel {
    public static class State {
        public final boolean loading;
        public final String message;
        public final boolean success;

        State(boolean loading, String message, boolean success) {
            this.loading = loading;
            this.message = message;
            this.success = success;
        }
    }

    private final AppContainer app;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final MutableLiveData<State> state =
            new MutableLiveData<>(new State(false, null, false));
    private final MutableLiveData<List<Receipt>> receipts = new MutableLiveData<>();
    private final MutableLiveData<List<ProductStats>> products = new MutableLiveData<>();
    private final MutableLiveData<SpendingReport> spending = new MutableLiveData<>();
    private SpendingReport.Period spendingPeriod = SpendingReport.Period.MONTH;
    private final MutableLiveData<Receipt> receipt = new MutableLiveData<>();
    private final MutableLiveData<ProductStats> product = new MutableLiveData<>();

    public ReceiptViewModel(@NonNull Application application) {
        super(application);
        app = new AppContainer(application);
    }

    public LiveData<State> state() {
        return state;
    }

    public LiveData<List<Receipt>> receipts() {
        return receipts;
    }

    public LiveData<List<ProductStats>> products() {
        return products;
    }

    public LiveData<SpendingReport> spending() {
        return spending;
    }

    public SpendingReport.Period spendingPeriod() {
        return spendingPeriod;
    }

    public void refreshSpending(SpendingReport.Period period) {
        spendingPeriod = period;
        worker.execute(
                () ->
                        spending.postValue(
                                new BuildSpendingReport()
                                        .execute(app.queries.history(), period, new Date())));
    }

    public LiveData<Receipt> receipt() {
        return receipt;
    }

    public LiveData<ProductStats> product() {
        return product;
    }

    public void refreshReceipts() {
        worker.execute(() -> receipts.postValue(app.queries.history()));
    }

    public void refreshProducts() {
        worker.execute(() -> products.postValue(app.queries.products()));
    }

    public void loadReceipt(long id) {
        worker.execute(() -> receipt.postValue(app.queries.receipt(id)));
    }

    public void loadProduct(String name) {
        worker.execute(() -> product.postValue(app.queries.product(name)));
    }

    public void delete(long id) {
        worker.execute(
                () -> {
                    app.queries.delete(id);
                    receipts.postValue(app.queries.history());
                });
    }

    public void importReceipt(String qr) {
        State current = state.getValue();
        if (current != null && current.loading) return;
        state.setValue(new State(true, null, false));
        worker.execute(
                () -> {
                    try {
                        app.importReceipt.execute(qr);
                        state.postValue(new State(false, "Чек добавлен", true));
                    } catch (Exception e) {
                        state.postValue(
                                new State(
                                        false,
                                        e.getMessage() == null
                                                ? "Не удалось обработать чек"
                                                : e.getMessage(),
                                        false));
                    }
                });
    }

    public void clearMessage() {
        state.setValue(new State(false, null, false));
    }

    @Override
    protected void onCleared() {
        worker.shutdown();
    }
}
