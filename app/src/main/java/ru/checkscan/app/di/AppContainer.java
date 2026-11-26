package ru.checkscan.app.di;

import android.content.Context;

import ru.checkscan.app.BuildConfig;
import ru.checkscan.app.data.local.ReceiptDatabase;
import ru.checkscan.app.data.remote.ProverkaChekaClient;
import ru.checkscan.app.data.repository.SqliteReceiptRepository;
import ru.checkscan.app.domain.repository.ReceiptRepository;
import ru.checkscan.app.domain.usecase.ImportReceiptUseCase;
import ru.checkscan.app.domain.usecase.ReceiptQueries;

public class AppContainer {
    public final ImportReceiptUseCase importReceipt;
    public final ReceiptQueries queries;

    public AppContainer(Context context) {
        ReceiptRepository repository =
                new SqliteReceiptRepository(new ReceiptDatabase(context.getApplicationContext()));
        importReceipt =
                new ImportReceiptUseCase(
                        repository, new ProverkaChekaClient(BuildConfig.FNS_TOKEN));
        queries = new ReceiptQueries(repository);
    }
}
