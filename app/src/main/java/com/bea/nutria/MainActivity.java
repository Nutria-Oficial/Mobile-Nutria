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

import com.bea.nutria.api.UsuarioAPI;
import com.bea.nutria.model.Usuario;
import com.bea.nutria.notification.NotificationScheduler;
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
        NotificationScheduler.startLoop(this);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_historico, R.id.navigation_nutria, R.id.navigation_scanner,
                R.id.navigation_comparacao, R.id.navigation_tabela, R.id.navigation_ingrediente
        ).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        binding.navView.setOnItemSelectedListener(item -> {
            try {
                androidx.navigation.NavOptions opts = new androidx.navigation.NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setRestoreState(true)
                        .setPopUpTo(navController.getGraph().getStartDestinationId(), false)
                        .build();

                navController.navigate(item.getItemId(), null, opts);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });

        binding.navView.setOnItemReselectedListener(item -> {
            navController.popBackStack(item.getItemId(), false);
        });
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
        carregarFotoPerfil();
    }

    private void maybeRequestExactAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                i.setData(Uri.parse("package:" + getPackageName()));
                startActivity(i);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, perms, results);
        if (requestCode == REQ_POST_NOTIFICATIONS) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                maybeRequestExactAlarm();
                NotificationScheduler.startLoop(this);
            }
        }
    }

    private void carregarFotoPerfil() {
        String email = null;
        try {
            if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
                email = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getEmail();
            }
        } catch (Throwable ignore) {}

        if (email == null || email.trim().isEmpty()) {
            Glide.with(this).load(R.drawable.foto_padrao).circleCrop().into(perfil);
            return;
        }

        email = email.trim().toLowerCase();

        api.buscarUsuario(email).enqueue(new Callback<Usuario>() {
            @Override public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String url = response.body().getUrlFoto();
                    Glide.with(MainActivity.this)
                            .load((url != null && !url.isEmpty()) ? url : R.drawable.foto_padrao)
                            .circleCrop()
                            .into(perfil);
                } else {
                    Glide.with(MainActivity.this).load(R.drawable.foto_padrao).circleCrop().into(perfil);
                }
            }
            @Override public void onFailure(Call<Usuario> call, Throwable t) {
                Glide.with(MainActivity.this).load(R.drawable.foto_padrao).circleCrop().into(perfil);
            }
        });
    }
}