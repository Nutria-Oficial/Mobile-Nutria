package com.bea.nutria.ui.Scanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bea.nutria.R;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class ScannerFragment extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST = 42;

    private FrameLayout previewContainer;
    private PreviewView previewView;
    private LifecycleCameraController cameraController;
    private ImageButton btnTirarFoto;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        previewContainer = view.findViewById(R.id.previewContainer);
        btnTirarFoto = view.findViewById(R.id.btnTirarFoto);

        btnTirarFoto.setOnClickListener(v -> tirarFoto());

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            iniciarCamera();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        }
    }

    private void iniciarCamera() {
        previewView = new PreviewView(requireContext());
        previewView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        previewContainer.removeAllViews();
        previewContainer.addView(previewView);

        cameraController = new LifecycleCameraController(requireContext());
        cameraController.bindToLifecycle(getViewLifecycleOwner());
        cameraController.setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE);
        previewView.setController(cameraController);
    }

    private void tirarFoto() {
        if (cameraController == null) {
            Toast.makeText(requireContext(), "Câmera não inicializada", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void processarImagem(@NonNull ImageProxy imageProxy) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarCamera();
            } else {
                Toast.makeText(requireContext(), "Permissão da câmera negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        previewView = null;
        cameraController = null;
        super.onDestroyView();
    }
}
