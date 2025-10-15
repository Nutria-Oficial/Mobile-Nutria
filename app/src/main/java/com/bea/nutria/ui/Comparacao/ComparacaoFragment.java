package com.bea.nutria.ui.Comparacao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import androidx.fragment.app.Fragment;

import com.bea.nutria.ComparacaoFragmentParte2;
import com.bea.nutria.R;
import com.bea.nutria.databinding.FragmentComparacaoBinding;

public class ComparacaoFragment extends Fragment {

    private FragmentComparacaoBinding binding;

    // Variáveis de instância para os Views cujo estado muda
    private View demonstracaoItem1;
    private TextView textViewSelecionarProduto1;
    private Button botaoTesteTransicao;
    private View demonstracaoItemSelecionado;
    private TextView nomeProdutoSelecionado;
    private View demonstracaoItem2;
    private TextView textViewSelecionarProduto2;
    private View iconeTabela;
    private View btnEscolherTabelas; // MaterialButton ou View

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

        // 1. Armazenar referências nas variáveis de instância
        demonstracaoItem1 = view.findViewById(R.id.View_demonstracaoItem_1);
        textViewSelecionarProduto1 = view.findViewById(R.id.textViewSelecionarProduto1);
        demonstracaoItemSelecionado = view.findViewById(R.id.View_demonstracaoItem_selecionado);
        nomeProdutoSelecionado = view.findViewById(R.id.textViewNomeProdutoSelecionado);
        demonstracaoItem2 = view.findViewById(R.id.view_demonstracaoItem2);
        textViewSelecionarProduto2 = view.findViewById(R.id.textViewSelecionarProduto2);
        iconeTabela = view.findViewById(R.id.imageViewIconeTabela);
        btnEscolherTabelas = view.findViewById(R.id.btn_escolherTabelas);
        botaoTesteTransicao = view.findViewById(R.id.botao_teste_transicao);

        // 2. Lógica de SIMULAÇÃO (mantida)
        botaoTesteTransicao.setOnClickListener(v -> {
            // Estado de Produto Selecionado
            demonstracaoItem1.setVisibility(View.INVISIBLE);
            textViewSelecionarProduto1.setVisibility(View.GONE);
            botaoTesteTransicao.setVisibility(View.GONE);
            demonstracaoItemSelecionado.setVisibility(View.VISIBLE);
            nomeProdutoSelecionado.setVisibility(View.VISIBLE);
            iconeTabela.setVisibility(View.VISIBLE);
            btnEscolherTabelas.setVisibility(View.VISIBLE);
            iconeTabela.bringToFront();
            demonstracaoItem2.setVisibility(View.GONE); // Não sei se é para ser GONE ou VISIBLE aqui, mantendo o original
            textViewSelecionarProduto2.setVisibility(View.GONE); // Não sei se é para ser GONE ou VISIBLE aqui, mantendo o original
            nomeProdutoSelecionado.setText("Manteiga");
        });

        // 3. Lógica para fechar teclado (mantida)
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

        // 4. Lógica para abrir ComparacaoFragmentParte2 (mantida)
        Button botaoAbrirComparacaoParte2 = (Button) btnEscolherTabelas;
        botaoAbrirComparacaoParte2.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ComparacaoFragmentParte2.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // ESTE É O PONTO CHAVE: Chamar o método de reset aqui!
        resetEstado();
    }

    /**
     * Restaura o Fragment para o seu estado inicial ("zerado").
     * Este é o estado antes do clique em "SIMULAR SELEÇÃO".
     */
    private void resetEstado() {
        // Estado Inicial: Produto 1 é visível, Produto Selecionado/Tabela escondido
        demonstracaoItem1.setVisibility(View.VISIBLE);
        textViewSelecionarProduto1.setVisibility(View.VISIBLE);
        botaoTesteTransicao.setVisibility(View.VISIBLE);

        // Elementos do estado "Produto Selecionado" devem estar escondidos
        demonstracaoItemSelecionado.setVisibility(View.GONE);
        nomeProdutoSelecionado.setVisibility(View.GONE);
        iconeTabela.setVisibility(View.GONE);
        btnEscolherTabelas.setVisibility(View.GONE);

        // Limpa o texto (opcional, mas boa prática)
        nomeProdutoSelecionado.setText("");

        // Certifica-se que o 2º produto está no estado inicial (se você quiser que ele apareça com o 1º)
        // Pelo código de clique, assumo que eles devem estar escondidos no estado inicial
        demonstracaoItem2.setVisibility(View.GONE);
        textViewSelecionarProduto2.setVisibility(View.GONE);

        // Se houver algum texto digitado na barra de pesquisa que você quer limpar:
        EditText searchBar = binding.searchBar;
        searchBar.setText("");

        // Remove o foco da barra de pesquisa e fecha o teclado
        View currentFocus = requireActivity().getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
            InputMethodManager imm = (InputMethodManager) requireActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}