package ru.checkscan.app.data.remote;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.checkscan.app.domain.model.Receipt;
import ru.checkscan.app.domain.model.ReceiptItem;
import ru.checkscan.app.domain.repository.ReceiptProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ProverkaChekaClient implements ReceiptProvider {
    private final String token;

    public ProverkaChekaClient(String token) {
        this.token = token.trim();
    }

    @Override
    public Receipt fetch(String qr) throws Exception {
        if (token.isEmpty())
            throw new IOException("Сервис загрузки чеков не настроен в этой сборке приложения.");
        String body =
                "token="
                        + URLEncoder.encode(token, "UTF-8")
                        + "&qrraw="
                        + URLEncoder.encode(qr, "UTF-8");
        HttpURLConnection connection =
                (HttpURLConnection)
                        new URL("https://proverkacheka.com/api/v1/check/get").openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);
            connection.setRequestProperty(
                    "Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            try (OutputStream out = connection.getOutputStream()) {
                out.write(body.getBytes(StandardCharsets.UTF_8));
            }
            int status = connection.getResponseCode();
            if (status != 200) throw new IOException("Сервис вернул код " + status);
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
            }
            JSONObject root = new JSONObject(response.toString());
            if (root.optInt("code", 1) != 1)
                throw new IOException("Сервис не смог получить чек. Проверьте QR-код.");
            JSONObject json = root.getJSONObject("data").getJSONObject("json");
            String date = json.optString("dateTime", "");
            if (date.isEmpty()) throw new IOException("В ответе нет даты чека");
            JSONArray array = json.getJSONArray("items");
            List<ReceiptItem> items = new ArrayList<>();
            long sum = 0;
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                long total = item.getLong("sum");
                sum += total;
                items.add(
                        new ReceiptItem(
                                item.getString("name"),
                                item.getDouble("quantity"),
                                item.getLong("price"),
                                total));
            }
            return new Receipt(
                    0,
                    qr,
                    date,
                    json.optString("user", "Магазин"),
                    json.optLong("totalSum", sum),
                    items);
        } finally {
            connection.disconnect();
        }
    }
}
