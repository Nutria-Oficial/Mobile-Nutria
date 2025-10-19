package com.bea.nutria;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton; // Importação necessária para o MaterialButton

public class ComparacaoParte2Fragment extends Fragment {

    private static final String KEY_TEXT_MUDADO = "text_mudado";
    private static final String KEY_ITEMS_VISIVEL = "items_visivel";
    private static final String KEY_BUTTON_VISIVEL = "button_visivel"; // Nova chave para o botão

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout correspondente ao seu XML
        return inflater.inflate(R.layout.activity_comparacao_fragment_parte2, container, false); // Ajuste o nome do layout se necessário
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Encontra e configura o Listener do botão de voltar
        view.findViewById(R.id.voltar).setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        // 2. Encontra os elementos
        TextView subtitulo = view.findViewById(R.id.textViewSelecionarProduto3);
        Button botaoSimulacao = view.findViewById(R.id.botao_teste_transicao);

        // Encontra os itens da lista
        final ConstraintLayout headerItem = view.findViewById(R.id.header_item);
        final ConstraintLayout headerItem1 = view.findViewById(R.id.header_item1);

        // Encontra o novo botão que deve aparecer
        final MaterialButton botaoEscolherTabelas2 = view.findViewById(R.id.btn_escolherTabelas2);


        // 3. Configura o Listener do botão de simulação
        botaoSimulacao.setOnClickListener(v -> {
            // Ação 1: Mudar o texto do subtítulo
            subtitulo.setText("Hora de comparar suas tabelas!");

            // Ação 2: Tornar os dois itens da lista visíveis
            headerItem.setVisibility(View.VISIBLE);
            headerItem1.setVisibility(View.VISIBLE);

            // Ação 3: Tornar o botão "Escolher aaaa" visível
            botaoEscolherTabelas2.setVisibility(View.VISIBLE);
        });

        // 4. Lógica para restaurar o estado (e.g., após rotação de tela)
        boolean itemsVisivel = false;
        boolean buttonVisivel = false;

        if (savedInstanceState != null) {
            // Restaura o texto do subtítulo
            if (savedInstanceState.getBoolean(KEY_TEXT_MUDADO, false)) {
                subtitulo.setText("Hora de comparar suas tabelas!");
            }

            // Restaura o estado dos itens e do botão
            itemsVisivel = savedInstanceState.getBoolean(KEY_ITEMS_VISIVEL, false);
            buttonVisivel = savedInstanceState.getBoolean(KEY_BUTTON_VISIVEL, false);
        }

        // Se os itens estavam visíveis antes da recriação, restaura a visibilidade
        if (itemsVisivel) {
            headerItem.setVisibility(View.VISIBLE);
            headerItem1.setVisibility(View.VISIBLE);
        }

        // Se o botão estava visível antes da recriação, restaura a visibilidade
        if (buttonVisivel) {
            botaoEscolherTabelas2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Salva o estado atual do TextView
        TextView subtitulo = getView().findViewById(R.id.textViewSelecionarProduto3);
        if (subtitulo != null && "Hora de comparar suas tabelas!".equals(subtitulo.getText().toString())) {
            outState.putBoolean(KEY_TEXT_MUDADO, true);
        }

        // Salva o estado de visibilidade dos itens e do botão
        ConstraintLayout headerItem = getView().findViewById(R.id.header_item);
        MaterialButton botaoComparar = getView().findViewById(R.id.btn_comparar);

        if (headerItem != null && headerItem.getVisibility() == View.VISIBLE) {
            outState.putBoolean(KEY_ITEMS_VISIVEL, true);
        }

        if (botaoComparar != null && botaoComparar.getVisibility() == View.VISIBLE) {
            outState.putBoolean(KEY_BUTTON_VISIVEL, true);
        }
    }
}