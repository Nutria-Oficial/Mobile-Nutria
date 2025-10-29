package com.bea.nutria;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.app.AlarmManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.TextView;

import com.bea.nutria.api.UsuarioAPI;
import com.bea.nutria.model.Usuario;
import com.bea.nutria.notification.NotificationScheduler;
import com.bea.nutria.ui.Cadastro.CadastroActivity;
import com.bea.nutria.ui.Login.LoginActivity;
import com.bea.nutria.ui.Perfil.PerfilActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bea.nutria.databinding.ActivityMainBinding;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ImageView perfil;
    private UsuarioAPI api;
    private String credenciais = Credentials.basic("nutria", "nutria123");
    private static final int REQ_POST_NOTIFICATIONS = 42;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{POST_NOTIFICATIONS}, REQ_POST_NOTIFICATIONS);
                return;
            }
        }

        maybeRequestExactAlarm();
        //notificação
        NotificationScheduler.startLoop(this);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_historico, R.id.navigation_nutria, R.id.navigation_scanner,R.id.navigation_comparacao, R.id.navigation_tabela)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        perfil = findViewById(R.id.perfil);

        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> {
                    Request original = chain.request();
                    Request req = original.newBuilder()
                            .header("Authorization", credenciais)
                            .header("Accept", "application/json")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(req);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api-spring-aql.onrender.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(UsuarioAPI.class);

        carregarFotoPerfil();

        perfil.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PerfilActivity.class)));
    }
    @Override
    protected void onResume() {
        super.onResume();
        //recarrega caso a foto tenha sido atualizada no Perfil
        carregarFotoPerfil();
    }
    private void maybeRequestExactAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.app.AlarmManager am = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                i.setData(Uri.parse("package:" + getPackageName()));
                // Não obriga ninguém; só abre a tela. O fallback inexacto já está implementado.
                startActivity(i);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, perms, results);
        if (requestCode == REQ_POST_NOTIFICATIONS) {
            // Se concedeu, segue o baile. Se negou, azar: sem notificação não tem mágica.
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                maybeRequestExactAlarm();
                NotificationScheduler.startLoop(this);
            }
        }
    }

    private void carregarFotoPerfil() {
        String prefsName = "nutria_prefs";
        String email = getSharedPreferences(prefsName, MODE_PRIVATE).getString("email", null);
        if (email == null || email.trim().isEmpty()) {
            // sem sessão: usa placeholder
            Glide.with(this).load(R.drawable.foto_padrao).circleCrop().into(perfil);
            return;
        }
        email = email.trim().toLowerCase();

        //busca no backend a foto do usuario
        api.buscarUsuario(email).enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Usuario u = response.body();
                    String url = u.getUrlFoto();

                    if (url != null && !url.isEmpty()) {
                        Glide.with(MainActivity.this).load(url)
                                .placeholder(R.drawable.foto_padrao)
                                .circleCrop()
                                .into(perfil);
                    } else {
                        Glide.with(MainActivity.this).load(R.drawable.foto_padrao)
                                .circleCrop()
                                .into(perfil);
                    }
                } else {
                    Glide.with(MainActivity.this).load(R.drawable.foto_padrao).circleCrop().into(perfil);
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                Glide.with(MainActivity.this).load(R.drawable.foto_padrao).circleCrop().into(perfil);
            }
        });
    }
}