package com.bea.nutria.ui.Comparacao;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent; // Esta importação será removida se não for usada para outros fins
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
import androidx.fragment.app.FragmentManager; // Importação essencial para transição
import androidx.fragment.app.FragmentTransaction; // Importação essencial para transição

// Note que o nome do fragmento de destino foi alterado, pois agora é um Fragment
import com.bea.nutria.ComparacaoParte2Fragment;
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
    private View btnEscolherTabelas;

    // ID do contêiner onde os fragments são exibidos na Activity principal.
    // É VITAL que este ID esteja correto. Usei um placeholder.
    private static final int FRAGMENT_CONTAINER_ID = R.id.nav_host_fragment_activity_main; // <<<< VERIFIQUE ESTE ID!

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

        // 1. Armazenar referências
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
            // ... (lógica de visibilidade mantida) ...
            demonstracaoItem1.setVisibility(View.INVISIBLE);
            textViewSelecionarProduto1.setVisibility(View.GONE);
            botaoTesteTransicao.setVisibility(View.GONE);
            demonstracaoItemSelecionado.setVisibility(View.VISIBLE);
            nomeProdutoSelecionado.setVisibility(View.VISIBLE);
            iconeTabela.setVisibility(View.VISIBLE);
            btnEscolherTabelas.setVisibility(View.VISIBLE);
            iconeTabela.bringToFront();
            demonstracaoItem2.setVisibility(View.GONE);
            textViewSelecionarProduto2.setVisibility(View.GONE);
            nomeProdutoSelecionado.setText("Manteiga");
        });

        // 3. Lógica para fechar teclado (mantida)
        view.setOnTouchListener((v, event) -> {
            // ... (lógica de toque para fechar teclado mantida) ...
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

        // 4. Lógica CORRIGIDA para abrir ComparacaoFragmentParte2
        Button botaoAbrirComparacaoParte2 = (Button) btnEscolherTabelas;
        botaoAbrirComparacaoParte2.setOnClickListener(v -> {
            // **CORREÇÃO: Usar FragmentTransaction em vez de Intent/startActivity()**
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // Adiciona a transição na pilha de retorno para permitir o botão "Voltar" do Android
            fragmentTransaction.replace(FRAGMENT_CONTAINER_ID, new ComparacaoParte2Fragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        resetEstado();
    }

    /**
     * Restaura o Fragment para o seu estado inicial.
     */
    private void resetEstado() {
        // ... (lógica de reset de estado mantida) ...
        demonstracaoItem1.setVisibility(View.VISIBLE);
        textViewSelecionarProduto1.setVisibility(View.VISIBLE);
        botaoTesteTransicao.setVisibility(View.VISIBLE);

        demonstracaoItemSelecionado.setVisibility(View.GONE);
        nomeProdutoSelecionado.setVisibility(View.GONE);
        iconeTabela.setVisibility(View.GONE);
        btnEscolherTabelas.setVisibility(View.GONE);

        nomeProdutoSelecionado.setText("");

        demonstracaoItem2.setVisibility(View.GONE);
        textViewSelecionarProduto2.setVisibility(View.GONE);

        EditText searchBar = binding.searchBar;
        searchBar.setText("");

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