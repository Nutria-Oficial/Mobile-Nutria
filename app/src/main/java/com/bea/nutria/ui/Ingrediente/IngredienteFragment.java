package com.bea.nutria.ui.Ingrediente;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bea.nutria.R;
import com.bea.nutria.api.IngredienteAPI;
import com.bea.nutria.databinding.FragmentIngredienteBinding;
import okhttp3.Response;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IngredienteFragment extends Fragment {

    private FragmentIngredienteBinding binding;
    private IngredienteViewPagerAdapter adapter;
    private IngredienteAPI api;
    private String credenciais;
    private Retrofit retrofit;
    private OkHttpClient client;
    private long ultimoWakeMs = 0L;
    private static final long JANELA_WAKE_MS = 60_000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentIngredienteBinding.inflate(inflater, container, false);

        adapter = new IngredienteViewPagerAdapter(this);
        binding.viewPager2.setAdapter(adapter);

        // 0 para ingredientes registrados e 1 para novo ingrediente
        binding.registrados.setOnClickListener(v -> binding.viewPager2.setCurrentItem(0));
        binding.novo.setOnClickListener(v -> binding.viewPager2.setCurrentItem(1));

        binding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                atualizarTabSelecionada(position);
            }
        });

        atualizarTabSelecionada(0);

        binding.btVoltar.setOnClickListener(v -> {
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

        return binding.getRoot();
    }

    private void atualizarTabSelecionada(int position) {
        if (position == 0) {
            binding.registrados.setTextColor(getResources().getColor(R.color.orange, null));
            binding.novo.setTextColor(getResources().getColor(R.color.gray, null));

            // move a linha laranja para a esquerda
            binding.imageView2.animate()
                    .translationX(0)
                    .setDuration(200)
                    .start();
        } else {
            binding.registrados.setTextColor(getResources().getColor(R.color.gray, null));
            binding.novo.setTextColor(getResources().getColor(R.color.orange, null));

            // move a linha laranja para a direita --> depois precisa ajustar para tablet
            float deslocamento = getResources().getDisplayMetrics().widthPixels / 2f - 30;
            binding.imageView2.animate()
                    .translationX(deslocamento)
                    .setDuration(200)
                    .start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}