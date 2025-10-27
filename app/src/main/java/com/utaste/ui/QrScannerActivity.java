package com.utaste.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.utaste.R;

import java.util.List;

public class QrScannerActivity extends AppCompatActivity {

    public static final String EXTRA_QR_TEXT = "qr_text";
    private DecoratedBarcodeView barcodeView;
    private boolean handledOnce = false;

    // Gestion permission caméra
    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startScanning();
                else {
                    Toast.makeText(this, "Permission caméra refusée", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        barcodeView = findViewById(R.id.barcode_view);
        barcodeView.decodeContinuous(callback);

        // Vérifie la permission caméra
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }
    }

    private void startScanning() {
        handledOnce = false;
        barcodeView.resume();
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result == null || result.getText() == null) return;
            if (handledOnce) return;
            handledOnce = true;

            String text = result.getText().trim();
            Intent data = new Intent();
            data.putExtra(EXTRA_QR_TEXT, text);
            setResult(RESULT_OK, data);
            finish();
        }

        // La méthode possibleResultPoints a été supprimée car elle était vide et redondante.
        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) { }
    };

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        barcodeView.pause();
        super.onPause();
    }
}
