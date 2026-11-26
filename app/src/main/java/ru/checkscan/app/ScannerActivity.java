package ru.checkscan.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScannerActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final com.google.mlkit.vision.barcode.BarcodeScanner scanner =
            BarcodeScanning.getClient(
                    new BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                            .build());
    private final AtomicBoolean found = new AtomicBoolean();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private PreviewView preview;
    private TextView status;

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        FrameLayout frame = new FrameLayout(this);
        preview = new PreviewView(this);
        frame.addView(preview, new FrameLayout.LayoutParams(-1, -1));
        frame.addView(new ScanOverlay(this), new FrameLayout.LayoutParams(-1, -1));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER);
        TextView title = new TextView(this);
        title.setText("Сканирование чека");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        header.addView(title);
        TextView hint = new TextView(this);
        hint.setText("Расположите QR-код в рамке");
        hint.setTextColor(0xffd3e0d8);
        hint.setTextSize(14);
        hint.setPadding(0, dp(8), 0, 0);
        header.addView(hint);
        FrameLayout.LayoutParams headerParams = new FrameLayout.LayoutParams(-1, -2, Gravity.TOP);
        headerParams.topMargin = dp(48);
        frame.addView(header, headerParams);

        status = new TextView(this);
        status.setText("Подготавливаем камеру…");
        status.setTextColor(Color.WHITE);
        status.setTextSize(14);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(18), dp(11), dp(18), dp(11));
        GradientDrawable background = new GradientDrawable();
        background.setColor(0xcc253d32);
        background.setCornerRadius(dp(24));
        status.setBackground(background);
        FrameLayout.LayoutParams statusParams =
                new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        statusParams.bottomMargin = dp(64);
        frame.addView(status, statusParams);
        setContentView(frame);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            status.setText("Ожидаем разрешение на камеру…");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int request, @NonNull String[] permissions, @NonNull int[] grants) {
        super.onRequestPermissionsResult(request, permissions, grants);
        if (request == 1 && grants.length > 0 && grants[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            android.widget.Toast.makeText(
                            this,
                            "Для сканирования нужен доступ к камере",
                            android.widget.Toast.LENGTH_LONG)
                    .show();
            finish();
        }
    }

    private void startCamera() {
        com.google.common.util.concurrent.ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);
        future.addListener(
                () -> {
                    if (isFinishing() || isDestroyed()) return;
                    try {
                        ProcessCameraProvider provider = future.get();
                        androidx.camera.core.Preview cameraPreview =
                                new androidx.camera.core.Preview.Builder().build();
                        cameraPreview.setSurfaceProvider(preview.getSurfaceProvider());
                        ImageAnalysis analysis =
                                new ImageAnalysis.Builder()
                                        .setBackpressureStrategy(
                                                ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build();
                        analysis.setAnalyzer(executor, this::analyze);
                        provider.unbindAll();
                        provider.bindToLifecycle(
                                this, CameraSelector.DEFAULT_BACK_CAMERA, cameraPreview, analysis);
                        status.setText("Ищем QR-код…");
                    } catch (Exception e) {
                        android.widget.Toast.makeText(
                                        this,
                                        "Не удалось открыть камеру",
                                        android.widget.Toast.LENGTH_LONG)
                                .show();
                        finish();
                    }
                },
                ContextCompat.getMainExecutor(this));
    }

    @androidx.camera.core.ExperimentalGetImage
    private void analyze(ImageProxy image) {
        if (found.get() || image.getImage() == null) {
            image.close();
            return;
        }
        scanner.process(
                        InputImage.fromMediaImage(
                                image.getImage(), image.getImageInfo().getRotationDegrees()))
                .addOnSuccessListener(
                        codes -> {
                            if (!found.get()) status.setText("Ищем QR-код…");
                            for (Barcode code : codes) {
                                String raw = code.getRawValue();
                                if (raw != null && found.compareAndSet(false, true)) {
                                    status.setText("QR-код распознан ✓");
                                    handler.postDelayed(
                                            () -> {
                                                if (!isFinishing() && !isDestroyed()) {
                                                    setResult(
                                                            RESULT_OK,
                                                            new Intent().putExtra("qr", raw));
                                                    finish();
                                                }
                                            },
                                            500);
                                    break;
                                }
                            }
                        })
                .addOnFailureListener(
                        error -> {
                            if (!found.get())
                                status.setText("Не удалось распознать кадр. Пробуем снова…");
                        })
                .addOnCompleteListener(task -> image.close());
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        scanner.close();
        executor.shutdown();
        super.onDestroy();
    }
}
