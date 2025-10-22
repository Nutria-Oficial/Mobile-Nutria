package com.bea.nutria.ui.Ingrediente;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bea.nutria.R;
import com.bea.nutria.api.IngredienteAPI;
import okhttp3.Response;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IngredienteFragment extends Fragment {

    private ViewPager2 viewPager;
    private IngredienteViewPagerAdapter adapter;
    private IngredienteFragment binding;
    private TextView txtRegistrados;
    private TextView txtNovo;
    private ImageView linha;
    private ImageView btVoltar;
    private IngredienteAPI api;
    private String credenciais;
    private Retrofit retrofit;
    private OkHttpClient client;
    private long ultimoWakeMs = 0L;
    private static final long JANELA_WAKE_MS = 60_000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ingrediente, container, false);

        txtRegistrados = view.findViewById(R.id.registrados);
        txtNovo = view.findViewById(R.id.novo);
        linha = view.findViewById(R.id.imageView2);
        viewPager = view.findViewById(R.id.viewPager2);
        btVoltar = view.findViewById(R.id.btVoltar);

        adapter = new IngredienteViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // 0 para ingredientes registrados e 1 para novo ingrediente
        txtRegistrados.setOnClickListener(v -> viewPager.setCurrentItem(0));
        txtNovo.setOnClickListener(v -> viewPager.setCurrentItem(1));

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                atualizarTabSelecionada(position);
            }
        });

        atualizarTabSelecionada(0);

        btVoltar.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(IngredienteFragment.this);
            navController.navigate(R.id.action_ingrediente_to_tabela);
        });

        client = new OkHttpClient.Builder()
                .connectTimeout(25, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .pingInterval(30, TimeUnit.SECONDS)
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


        retrofit = new Retrofit.Builder()
                .baseUrl("https://api-spring-mongodb.onrender.com")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(IngredienteAPI.class);

        return view;
    }

    private void atualizarTabSelecionada(int position) {
        if (position == 0) {
            txtRegistrados.setTextColor(getResources().getColor(R.color.orange, null));
            txtNovo.setTextColor(getResources().getColor(R.color.gray, null));

            // move a linha para a esquerda
            linha.animate()
                    .translationX(0)
                    .setDuration(200)
                    .start();
        } else {
            txtRegistrados.setTextColor(getResources().getColor(R.color.gray, null));
            txtNovo.setTextColor(getResources().getColor(R.color.orange, null));

            // move a linha para a direita
            float deslocamento = getResources().getDisplayMetrics().widthPixels / 2f - 30;
            linha.animate()
                    .translationX(deslocamento)
                    .setDuration(200)
                    .start();
        }
    }

    private void iniciandoServidor(Runnable proximoPasso) {
        long agora = System.currentTimeMillis();
        if (agora - ultimoWakeMs < JANELA_WAKE_MS) {
            if (proximoPasso != null) proximoPasso.run();
            return;
        }
        new Thread(() -> {
            boolean ok = false;
            for (int tent = 1; tent <= 3 && !ok; tent++) {
                try {
                    Request req = new Request.Builder()
                            .url("https://api-spring-mongodb.onrender.com")
                            .header("Authorization", credenciais)
                            .build();
                    try (Response resp = client.newCall(req).execute()) {
                        ok = (resp != null && resp.isSuccessful());
                    }
                } catch (Exception ignore) {
                }
            }
            ultimoWakeMs = System.currentTimeMillis();
            if (isAdded()){
                requireActivity().runOnUiThread(() -> {
                    if (proximoPasso != null) proximoPasso.run();
                });
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        // limpa as referências para evitar memory leaks
        txtRegistrados = null;
        txtNovo = null;
        linha = null;
        viewPager = null;
        adapter = null;
    }
}