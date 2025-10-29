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
import androidx.fragment.app.Fragment;

import com.bea.nutria.R;
import com.bea.nutria.api.TabelaAPI;
import com.bea.nutria.api.conexaoApi.ConexaoAPI;
import com.bea.nutria.model.ComparacaoNutrienteDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragmento responsável por exibir a comparação detalhada entre duas tabelas.
 * Recebe os IDs das tabelas (agora como Integer/Long) e nomes para busca e exibição.
 */
public class ComparacaoParte3Fragment extends Fragment {

    private static final String TAG = "ComparacaoP3Fragment";

    // Chaves dos argumentos para os IDs das tabelas
    private static final String ARG_TABELA_ID_1 = "tabela_id_1";
    private static final String ARG_TABELA_ID_2 = "tabela_id_2";
    // Chaves dos argumentos para os nomes das tabelas
    private static final String ARG_TABELA_NOME_1 = "tabela_nome_1";
    private static final String ARG_TABELA_NOME_2 = "tabela_nome_2";

    // URL base da API (Fixa aqui para uso direto com ConexaoAPI)
    private static final String BASE_API_URL = "https://api-spring-mongodb.onrender.com/";

    // Variáveis para armazenar os IDs recebidos (Tipo Long - compatível com o Fragmento de origem)
    private Long tabelaId1;
    private Long tabelaId2;
    // Variáveis para armazenar os nomes recebidos (Tipo String)
    private String tabelaNome1;
    private String tabelaNome2;

    // API Service
    private TabelaAPI tabelaAPI;

    // Mapeamento dos nomes de nutrientes da API para IDs de TextViews e ImageViews
    // CHAVE: Nome do Nutriente da API
    // VALOR: ID do TextView do VALOR da TABELA 1
    private final Map<String, Integer> nutrientTabela1TextViewMap = new HashMap<>();
    // VALOR: ID do TextView do VALOR da TABELA 2
    private final Map<String, Integer> nutrientTabela2TextViewMap = new HashMap<>();
    // NOVO MAPA: ID do TextView do VALOR DA DIFERENÇA (valorComparacao)
    private final Map<String, Integer> nutrientComparacaoTextViewMap = new HashMap<>();
    // VALOR: ID do ImageView da setinha de comparação
    private final Map<String, Integer> nutrientIconViewMap = new HashMap<>();

    // Padrão Regex para extrair a unidade (e.g., (kcal), (g), (mg)) do nome do nutriente.
    private static final Pattern UNIT_PATTERN = Pattern.compile("\\(([^)]+)\\)");

    /**
     * Método newInstance com nomes das tabelas incluídos.
     */
    public static ComparacaoParte3Fragment newInstance(Long tabelaId1, Long tabelaId2, String tabelaNome1, String tabelaNome2) {
        ComparacaoParte3Fragment fragment = new ComparacaoParte3Fragment();
        Bundle args = new Bundle();
        args.putLong(ARG_TABELA_ID_1, tabelaId1);
        args.putLong(ARG_TABELA_ID_2, tabelaId2);
        args.putString(ARG_TABELA_NOME_1, tabelaNome1);
        args.putString(ARG_TABELA_NOME_2, tabelaNome2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            tabelaId1 = getArguments().getLong(ARG_TABELA_ID_1, -1L);
            tabelaId2 = getArguments().getLong(ARG_TABELA_ID_2, -1L);
            tabelaNome1 = getArguments().getString(ARG_TABELA_NOME_1, "Produto 1");
            tabelaNome2 = getArguments().getString(ARG_TABELA_NOME_2, "Produto 2");

            if (tabelaId1.equals(-1L) || tabelaId2.equals(-1L)) {
                Log.e(TAG, "IDs de Tabela não encontrados nos argumentos!");
            }
        }

        // Inicializa a conexão com a API
        if (getActivity() != null) {
            ConexaoAPI conexaoAPI = new ConexaoAPI(BASE_API_URL);
            tabelaAPI = conexaoAPI.getApi(TabelaAPI.class);

            // Aqui, convertemos Long para Integer, pois a API espera Integer.
            if (tabelaId1 > 0 && tabelaId2 > 0) {
                // Inicia o servidor e, em seguida, busca os dados
                conexaoAPI.iniciarServidor(getActivity(), () ->
                        fetchComparisonData(tabelaId1.intValue(), tabelaId2.intValue())
                );
            } else {
                Toast.makeText(getContext(), "IDs de tabela inválidos para comparação.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_comparacao_fragment_parte3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Mapeamento dos componentes de UI para nomes de nutrientes da API
        setupNutrientMaps();

        // Exibir os nomes das tabelas no cabeçalho
        displayTableNames(view);
    }

    // --- Lógica de Mapeamento e Preenchimento ---

    /**
     * Mapeia os nomes dos nutrientes da API para os IDs dos TextViews no layout.
     */
    private void setupNutrientMaps() {
        // Mapeamento para Tabela 1 (Valores)
        nutrientTabela1TextViewMap.put("Valor Calórico (kcal)", R.id.tv_valor_calorico_t1);
        nutrientTabela1TextViewMap.put("Proteína (g)", R.id.tv_proteina_t1);
        nutrientTabela1TextViewMap.put("Carboidrato (g)", R.id.tv_carboidrato_t1);
        nutrientTabela1TextViewMap.put("Açúcar Total (g)", R.id.tv_acucar_total_t1);
        nutrientTabela1TextViewMap.put("Gordura Total (g)", R.id.tv_gordura_total_t1);
        nutrientTabela1TextViewMap.put("Gordura Saturada (g)", R.id.tv_gordura_saturada_t1);
        nutrientTabela1TextViewMap.put("Sódio (mg)", R.id.tv_sodio_t1);

        // Mapeamento para Tabela 2 (Valores)
        nutrientTabela2TextViewMap.put("Valor Calórico (kcal)", R.id.tv_valor_calorico_t2);
        nutrientTabela2TextViewMap.put("Proteína (g)", R.id.tv_proteina_t2);
        nutrientTabela2TextViewMap.put("Carboidrato (g)", R.id.tv_carboidrato_t2);
        nutrientTabela2TextViewMap.put("Açúcar Total (g)", R.id.tv_acucar_total_t2);
        nutrientTabela2TextViewMap.put("Gordura Total (g)", R.id.tv_gordura_total_t2);
        nutrientTabela2TextViewMap.put("Gordura Saturada (g)", R.id.tv_gordura_saturada_t2);
        nutrientTabela2TextViewMap.put("Sódio (mg)", R.id.tv_sodio_t2);

        // NOVO MAPA: Mapeamento para o VALOR DA COMPARAÇÃO (DIFERENÇA)
        // ATENÇÃO: Verifique se estes IDs existem no seu layout!
        nutrientComparacaoTextViewMap.put("Valor Calórico (kcal)", R.id.tv_valor_calorico_t1);
        nutrientComparacaoTextViewMap.put("Proteína (g)", R.id.tv_proteina_t1);
        nutrientComparacaoTextViewMap.put("Carboidrato (g)", R.id.tv_carboidrato_t1);
        nutrientComparacaoTextViewMap.put("Açúcar Total (g)", R.id.tv_acucar_total_t1);
        nutrientComparacaoTextViewMap.put("Gordura Total (g)", R.id.tv_gordura_total_t1);
        nutrientComparacaoTextViewMap.put("Gordura Saturada (g)", R.id.tv_gordura_saturada_t1);
        nutrientComparacaoTextViewMap.put("Sódio (mg)", R.id.tv_sodio_t1);

        // Mapeamento para o ícone de comparação
        nutrientIconViewMap.put("Valor Calórico (kcal)", R.id.iv_valor_calorico_subtracao);
        nutrientIconViewMap.put("Proteína (g)", R.id.iv_proteina_subtracao);
        nutrientIconViewMap.put("Carboidrato (g)", R.id.iv_carboidrato_subtracao);
        nutrientIconViewMap.put("Açúcar Total (g)", R.id.iv_acucar_total_subtracao);
        nutrientIconViewMap.put("Gordura Total (g)", R.id.iv_gordura_total_subtracao);
        nutrientIconViewMap.put("Gordura Saturada (g)", R.id.iv_gordura_saturada_subtracao);
        nutrientIconViewMap.put("Sódio (mg)", R.id.iv_sodio_subtracao);
    }

    /**
     * Exibe os nomes dos produtos/tabelas nos TextViews de cabeçalho.
     */
    private void displayTableNames(@NonNull View view) {
        TextView tvNomeTabela1 = view.findViewById(R.id.textViewTitulo2);
        if (tvNomeTabela1 != null) {
            tvNomeTabela1.setText(tabelaNome1);
        } else {
            Log.w(TAG, "TextView para nome da Tabela 1 não encontrado (ID R.id.textViewTitulo2).");
        }

        TextView tvNomeTabela2 = view.findViewById(R.id.textViewTitulo4);
        if (tvNomeTabela2 != null) {
            tvNomeTabela2.setText(tabelaNome2);
        } else {
            Log.w(TAG, "TextView para nome da Tabela 2 não encontrado (ID R.id.textViewTitulo4).");
        }

        // Configura o botão de voltar, se existir.
        View backButton = view.findViewById(R.id.voltar);
        if (backButton != null) {
            backButton.setOnClickListener(v -> requireActivity().onBackPressed());
        }
    }


    /**
     * Realiza a chamada à API para buscar os dados de comparação.
     * @param idTabela1 ID da primeira tabela (como Integer para a API).
     * @param idTabela2 ID da segunda tabela (como Integer para a API).
     */
    private void fetchComparisonData(Integer idTabela1, Integer idTabela2) {
        if (tabelaAPI == null) {
            Toast.makeText(getContext(), "Serviço de API não inicializado.", Toast.LENGTH_SHORT).show();
            return;
        }

        tabelaAPI.compararTabelas(idTabela1, idTabela2).enqueue(new Callback<List<ComparacaoNutrienteDTO>>() {
            @Override
            public void onResponse(Call<List<ComparacaoNutrienteDTO>> call, Response<List<ComparacaoNutrienteDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        Toast.makeText(getContext(), "Nenhum dado de comparação encontrado.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    displayComparisonData(response.body());
                } else {
                    Toast.makeText(getContext(), "Erro ao carregar dados da comparação. Código: " + response.code(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Erro na resposta: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<ComparacaoNutrienteDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Falha na comunicação com a API.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Falha de rede: " + t.getMessage());
            }
        });
    }

    /**
     * Preenche a UI com os dados de comparação recebidos da API.
     */
    private void displayComparisonData(List<ComparacaoNutrienteDTO> comparacoes) {
        if (getView() == null || getContext() == null) return;

        // As chaves no JSON são os nomes das tabelas (Ex: "Teste1", "Teste6")
        final String nomeChave1 = tabelaNome1;
        final String nomeChave2 = tabelaNome2;

        for (ComparacaoNutrienteDTO item : comparacoes) {
            String nutrientName = item.getNomeNutriente();

            // Certifica-se de que o nome do nutriente existe no mapeamento.
            if (!nutrientTabela1TextViewMap.containsKey(nutrientName)) {
                Log.w(TAG, "Nutriente não mapeado no layout: " + nutrientName);
                continue;
            }

            // Acessa os valores da porção por tabela
            Map<String, Double> porcaoPorTabela = item.getPorcaoPorTabela();

            // 1. Preencher a coluna da Tabela 1 (Valor)
            if (porcaoPorTabela != null && porcaoPorTabela.containsKey(nomeChave1)) {
                Double valorT1 = porcaoPorTabela.get(nomeChave1);
                TextView tvT1 = findTextView(nutrientName, nutrientTabela1TextViewMap);
                if (tvT1 != null) {
                    tvT1.setText(formatNutrientValue(nutrientName, valorT1));
                }
            }

            // 2. Preencher a coluna da Tabela 2 (Valor)
            if (porcaoPorTabela != null && porcaoPorTabela.containsKey(nomeChave2)) {
                Double valorT2 = porcaoPorTabela.get(nomeChave2);
                TextView tvT2 = findTextView(nutrientName, nutrientTabela2TextViewMap);
                if (tvT2 != null) {
                    tvT2.setText(formatNutrientValue(nutrientName, valorT2));
                }
            }

            // 3. Preencher a coluna de Comparação (Ícone)
            Double valorComparacao = item.getValorComparacao();
            ImageView ivSubtracao = findImageView(nutrientName, nutrientIconViewMap);

            if (ivSubtracao != null && valorComparacao != null) {
                updateComparisonIcon(ivSubtracao, valorComparacao);
            }

            // 4. NOVO: Preencher o TextView com o valor da diferença (valorComparacao)
            TextView tvDiff = findTextView(nutrientName, nutrientComparacaoTextViewMap);
            if (tvDiff != null && valorComparacao != null) {
                // Usa o mesmo formatador, que já trata o sinal de negativo e a unidade.
                tvDiff.setText(formatNutrientValue(nutrientName, valorComparacao));
            }
        }
    }

    /**
     * Formata o valor do nutriente (Double) para uma String com formatação decimal
     * e a unidade correta, extraindo a unidade do nome do nutriente.
     * @param nutrientName O nome do nutriente (ex: "Proteína (g)").
     * @param value O valor a ser formatado (Pode ser o valor da tabela ou a diferença).
     * @return O valor formatado com a unidade (ex: "5.5 g" ou "-46.3 kcal").
     */
    private String formatNutrientValue(String nutrientName, Double value) {
        if (value == null) return "-";

        // 1. Extrair a unidade (o que está entre parênteses)
        Matcher matcher = UNIT_PATTERN.matcher(nutrientName);
        String unit = "";
        if (matcher.find()) {
            unit = " " + matcher.group(1); // Ex: " g" ou " kcal"
        }

        // 2. Definir o formato decimal
        String formatString;

        // Se o valor for inteiro (ex: 120.0), formata para 120.
        // Usamos uma tolerância (0.01) para números que são quase inteiros devido à precisão do Double.
        if (Math.abs(value - Math.round(value)) < 0.01) {
            formatString = "%.0f";
        } else if (nutrientName.contains("(kcal)") || nutrientName.contains("(mg)")) {
            // Para kcal e mg, uma casa decimal é suficiente para não arredondar demais.
            formatString = "%.1f";
        }
        else {
            // Para 'g' (gramas) e outros, usamos duas casas decimais para maior precisão, mas mantendo a leitura.
            formatString = "%.2f";
        }

        // 3. Formatar o valor numérico
        // O Locale.getDefault() garante que a formatação (vírgula/ponto) seja correta.
        String formattedValue = String.format(Locale.getDefault(), formatString, value);

        // 4. Concatenar o valor formatado com a unidade
        return formattedValue + unit;
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

        // Usa uma pequena tolerância para considerar valores próximos de zero como iguais.
        final double TOLERANCE = 0.01;

        // Se a diferença for positiva, Tabela 1 tem mais (Usa ic_soma)
        if (valorComparacao > TOLERANCE) {
            imageView.setImageResource(R.drawable.ic_soma);
        }
        // Se a diferença for negativa, Tabela 1 tem menos (Usa ic_subtracao)
        else if (valorComparacao < -TOLERANCE) {
            imageView.setImageResource(R.drawable.ic_subtracao);
        }
        // Se for zero ou muito próximo
        else {
            // Indicando que os valores são praticamente iguais
            imageView.setImageResource(R.drawable.ic_igual);
        }

        // Garante que qualquer filtro de cor anterior seja removido.
        imageView.clearColorFilter();
    }
}
