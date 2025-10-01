package com.bea.nutria.ui.Comparacao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import com.bea.nutria.R;
import com.bea.nutria.databinding.FragmentComparacaoBinding;

public class ComparacaoFragment extends Fragment {

    private FragmentComparacaoBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComparacaoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Referência aos elementos do Estado 1
        View demonstracaoItem1 = view.findViewById(R.id.View_demonstracaoItem_1);
        TextView textViewSelecionarProduto1 = view.findViewById(R.id.textViewSelecionarProduto1);

        // Referência aos elementos do Estado 2 (Produto Selecionado)
        View demonstracaoItemSelecionado = view.findViewById(R.id.View_demonstracaoItem_selecionado);
        TextView nomeProdutoSelecionado = view.findViewById(R.id.textViewNomeProdutoSelecionado);
        TextView avaliacaoProdutoSelecionado = view.findViewById(R.id.textViewAvaliacaoProdutoSelecionado);

        // Elementos do segundo produto (que serão mantidos invisíveis)
        View demonstracaoItem2 = view.findViewById(R.id.view_demonstracaoItem2);
        TextView textViewSelecionarProduto2 = view.findViewById(R.id.textViewSelecionarProduto2);


        // Adicionando um botão de teste para simular a seleção
        Button botaoTesteTransicao = view.findViewById(R.id.botao_teste_transicao);

        botaoTesteTransicao.setOnClickListener(v -> {
            // Esconde os elementos do Estado 1
            // *MUDANÇA CRUCIAL*: Usamos INVISIBLE para que o View_demonstracaoItem_1
            // mantenha o espaço e a barra de pesquisa não se mova.
            demonstracaoItem1.setVisibility(View.INVISIBLE);
            textViewSelecionarProduto1.setVisibility(View.GONE);

            // Oculta o botão de teste
            botaoTesteTransicao.setVisibility(View.GONE);

            // Mostra os elementos do Produto Selecionado
            demonstracaoItemSelecionado.setVisibility(View.VISIBLE);
            nomeProdutoSelecionado.setVisibility(View.VISIBLE);
            avaliacaoProdutoSelecionado.setVisibility(View.VISIBLE);

            // Garante que os elementos do segundo produto continuam invisíveis
            demonstracaoItem2.setVisibility(View.GONE);
            textViewSelecionarProduto2.setVisibility(View.GONE);


            // Opcional: Altera o texto do produto selecionado
            nomeProdutoSelecionado.setText("Leite desnatado");
            avaliacaoProdutoSelecionado.setText("Avaliação: 78");

            // *** BLOCO DE REPOSICIONAMENTO DE CONSTRAINT REMOVIDO ***
            // A barra de pesquisa permanece fixa pela constraint do XML,
            // que aponta para o View_demonstracaoItem_1 (agora INVISIBLE).
        });

        // Detecta toque fora do EditText para remover foco e fechar teclado
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View currentFocus = requireActivity().getCurrentFocus();
                if (currentFocus instanceof EditText) {
                    Rect outRect = new Rect();
                    currentFocus.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        currentFocus.clearFocus();
                        InputMethodManager imm = (InputMethodManager) requireActivity()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                        }
                    }
                }
            }
            return false;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}