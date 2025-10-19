package com.bea.nutria.ui.SplashScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bea.nutria.R;
import com.bea.nutria.ui.Login.LoginActivity;
import com.bumptech.glide.Glide;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove EdgeToEdge e ViewCompat para simplificar o splash (se aplicável ao seu projeto)
        setContentView(R.layout.activity_splash_screen);

        // Carrega o GIF com o Glide
        ImageView imageView = (ImageView) findViewById(R.id.imageViewSplashScreen);
        Glide.with(this).load(R.drawable.gif_splash_screen_recortado).into(imageView);

        // Dispara a próxima tela após 3 segundos
        new Handler().postDelayed(this::abrirTelaDeEntrada, 3000);
    }

    private void abrirTelaDeEntrada() {
        Intent rota = new Intent(this, LoginActivity.class);
        startActivity(rota);
        // Garante que o usuário não possa voltar para a splash screen
        finish();
    }
}