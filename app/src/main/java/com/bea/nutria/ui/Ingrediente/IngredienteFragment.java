package com.bea.nutria.ui.Ingrediente;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bea.nutria.R;
import com.bea.nutria.ui.Tabela.TabelaFragment;

public class IngredienteFragment extends Fragment {

    private ViewPager2 viewPager;
    private IngredienteViewPagerAdapter adapter;
    private TextView txtRegistrados;
    private TextView txtNovo;
    private ImageView linha;
    private TextView btVoltar;

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