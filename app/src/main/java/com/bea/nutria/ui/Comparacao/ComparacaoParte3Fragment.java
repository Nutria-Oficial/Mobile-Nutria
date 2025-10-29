package com.bea.nutria.ui.Comparacao;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bea.nutria.R;
import com.bea.nutria.api.TabelaAPI;
import com.bea.nutria.api.conexaoApi.ConexaoAPI;
import com.bea.nutria.ui.Comparacao.ComparacaoNutrienteDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragmento responsável por exibir a comparação detalhada entre duas tabelas.
 * Recebe os IDs das tabelas como Long, compatível com GetTabelaDTO.
 */
public class ComparacaoParte3Fragment extends Fragment {

    private static final String TAG = "ComparacaoP3Fragment";

    // Chaves dos argumentos para os IDs das tabelas
    private static final String ARG_TABELA_ID_1 = "tabela_id_1";
    private static final String ARG_TABELA_ID_2 = "tabela_id_2";

    // URL base da API (Fixa aqui para uso direto com ConexaoAPI)
    private static final String BASE_API_URL = "https://api-spring-mongodb.onrender.com/";

    // Variáveis para armazenar os IDs recebidos (Tipo Long)
    private Long tabelaId1;
    private Long tabelaId2;

    // API Service
    private TabelaAPI tabelaAPI;

    // Mapeamento dos nomes de nutrientes da API para IDs de TextViews e ImageViews
    private final Map<String, Integer> nutrientTextViewMap = new HashMap<>();
    private final Map<String, Integer> nutrientIconViewMap = new HashMap<>();


    public static ComparacaoParte3Fragment newInstance(Long tabelaId1, Long tabelaId2) {
        ComparacaoParte3Fragment fragment = new ComparacaoParte3Fragment();
        Bundle args = new Bundle();
        args.putLong(ARG_TABELA_ID_1, tabelaId1);
        args.putLong(ARG_TABELA_ID_2, tabelaId2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            tabelaId1 = getArguments().getLong(ARG_TABELA_ID_1, -1L);
            tabelaId2 = getArguments().getLong(ARG_TABELA_ID_2, -1L);

            if (tabelaId1.equals(-1L) || tabelaId2.equals(-1L)) {
                Log.e(TAG, "IDs de Tabela não encontrados nos argumentos!");
            } else {
                Log.d(TAG, "Tabelas Recebidas: ID 1 = " + tabelaId1 + ", ID 2 = " + tabelaId2);
            }
        }

        // Inicializa a conexão com a API
        if (getActivity() != null) {
            ConexaoAPI conexaoAPI = new ConexaoAPI(BASE_API_URL);
            tabelaAPI = conexaoAPI.getApi(TabelaAPI.class);
            // Inicia o servidor se necessário e, em seguida, busca os dados
            conexaoAPI.iniciarServidor(getActivity(), () -> {
                // Executado na UI thread após a tentativa de wake-up
                if (tabelaId1 > 0 && tabelaId2 > 0) {
                    fetchComparisonData(tabelaId1.intValue(), tabelaId2.intValue());
                } else {
                    Toast.makeText(getContext(), "IDs de tabela inválidos para comparação.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Assume-se que este layout contém os IDs de TextViews e ImageViews mapeados
        return inflater.inflate(R.layout.activity_comparacao_fragment_parte3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Mapeamento dos componentes de UI para nomes de nutrientes da API
        setupNutrientMaps();
    }

    // --- Lógica de Mapeamento e Preenchimento ---

    private void setupNutrientMaps() {
        // Mapeia o nome do nutriente da API para o TextView de VALOR da Tabela 1 (Teste1)
        nutrientTextViewMap.put("Valor Calórico (kcal)", R.id.tv_valor_calorico_t1);
        nutrientTextViewMap.put("Proteína (g)", R.id.tv_proteina_t1);
        nutrientTextViewMap.put("Carboidrato (g)", R.id.tv_carboidrato_t1);
        nutrientTextViewMap.put("Açúcar Total (g)", R.id.tv_acucar_total_t1);
        nutrientTextViewMap.put("Gordura Total (g)", R.id.tv_gordura_total_t1);
        nutrientTextViewMap.put("Gordura Saturada (g)", R.id.tv_gordura_saturada_t1);
        nutrientTextViewMap.put("Sódio (mg)", R.id.tv_sodio_t1); // Adicionei Sódio para cobrir o retorno

        // Mapeia o nome do nutriente da API para o ImageView de COMPARACAO
        // É CRUCIAL que você tenha um ImageView em seu layout chamado iv_..._subtracao
        nutrientIconViewMap.put("Valor Calórico (kcal)", R.id.iv_valor_calorico_subtracao);
        nutrientIconViewMap.put("Proteína (g)", R.id.iv_proteina_subtracao);
        nutrientIconViewMap.put("Carboidrato (g)", R.id.iv_carboidrato_subtracao);
        nutrientIconViewMap.put("Açúcar Total (g)", R.id.iv_acucar_total_subtracao);
        nutrientIconViewMap.put("Gordura Total (g)", R.id.iv_gordura_total_subtracao);
        nutrientIconViewMap.put("Gordura Saturada (g)", R.id.iv_gordura_saturada_subtracao);
        nutrientIconViewMap.put("Sódio (mg)", R.id.iv_sodio_subtracao); // Adicionei Sódio
    }


    private void fetchComparisonData(Integer idTabela1, Integer idTabela2) {
        if (tabelaAPI == null) {
            Toast.makeText(getContext(), "Serviço de API não inicializado.", Toast.LENGTH_SHORT).show();
            return;
        }

        tabelaAPI.compararTabelas(idTabela1, idTabela2).enqueue(new Callback<List<ComparacaoNutrienteDTO>>() {
            @Override
            public void onResponse(Call<List<ComparacaoNutrienteDTO>> call, Response<List<ComparacaoNutrienteDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayComparisonData(response.body());
                } else {
                    Toast.makeText(getContext(), "Erro ao carregar dados da comparação.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro na resposta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ComparacaoNutrienteDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Falha na comunicação com a API.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Falha de rede: " + t.getMessage());
            }
        });
    }

    private void displayComparisonData(List<ComparacaoNutrienteDTO> comparacoes) {
        if (getView() == null || getContext() == null) return;

        for (ComparacaoNutrienteDTO item : comparacoes) {
            String nutrientName = item.getNomeNutriente();

            // 1. Preencher a coluna do Produto 1 (Teste1)
            // Assumimos que a coluna Tabela 1 é preenchida com o valor real da porção
            if (item.getPorcaoPorTabela().containsKey("Teste1")) {
                Double valorT1 = item.getPorcaoPorTabela().get("Teste1");
                TextView tvT1 = findTextView(nutrientName, nutrientTextViewMap);
                if (tvT1 != null) {
                    tvT1.setText(String.format(Locale.getDefault(), "%.2f", valorT1));
                }
            }

            // 2. Preencher a coluna de Comparação (Ícone)
            Double valorComparacao = item.getValorComparacao();
            ImageView ivSubtracao = findImageView(nutrientName, nutrientIconViewMap);

            if (ivSubtracao != null && valorComparacao != null) {
                updateComparisonIcon(ivSubtracao, valorComparacao);
            }
        }
    }

    // Lógica para encontrar o TextView baseado no nome do nutriente
    private TextView findTextView(String nutrientName, Map<String, Integer> map) {
        Integer resId = map.get(nutrientName);
        if (resId != null && getView() != null) {
            return getView().findViewById(resId);
        }
        return null;
    }

    // Lógica para encontrar o ImageView baseado no nome do nutriente
    private ImageView findImageView(String nutrientName, Map<String, Integer> map) {
        Integer resId = map.get(nutrientName);
        if (resId != null && getView() != null) {
            return getView().findViewById(resId);
        }
        return null;
    }

    /**
     * Lógica para definir o ícone com base no valor de comparação (Tabela 1 - Tabela 2).
     *
     * Se T1 > T2 (Valor Positivo) -> ic_soma (mais)
     * Se T1 < T2 (Valor Negativo) -> ic_subtracao (menos)
     * Se T1 ≈ T2 (Valor Zero) -> ic_igual (igual)
     *
     * @param imageView O ImageView a ser atualizado.
     * @param valorComparacao O valor da comparação (Tabela 1 - Tabela 2).
     */
    private void updateComparisonIcon(ImageView imageView, double valorComparacao) {
        if (getContext() == null) return;

        // Se a diferença for positiva, Tabela 1 tem mais (Usa ic_soma)
        if (valorComparacao > 0.0) {
            imageView.setImageResource(R.drawable.ic_soma);
            // Cor removida, mantendo apenas a troca do ícone.
        }
        // Se a diferença for negativa, Tabela 1 tem menos (Usa ic_subtracao)
        else if (valorComparacao < 0.0) {
            imageView.setImageResource(R.drawable.ic_subtracao);
            // Cor removida, mantendo apenas a troca do ícone.
        }
        // Se for zero ou muito próximo
        else {
            // Indicando que os valores são praticamente iguais
            imageView.setImageResource(R.drawable.ic_igual);
            // Cor removida, mantendo apenas a troca do ícone.
        }

        // *Importante*: Se o ImageView tinha um filtro de cor aplicado antes (no XML ou em outro momento),
        // pode ser necessário removê-lo explicitamente para garantir que o ícone original apareça com sua cor padrão.
        // A linha abaixo garante isso:
        imageView.clearColorFilter();
    }
}
