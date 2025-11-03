package com.bea.nutria.ui.Comparacao;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.widget.NestedScrollView;

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

public class ComparacaoParte3Fragment extends Fragment {

    private static final String TAG = "ComparacaoP3Fragment";

    private static final String ARG_TABELA_ID_1 = "tabela_id_1";
    private static final String ARG_TABELA_ID_2 = "tabela_id_2";
    private static final String ARG_TABELA_NOME_1 = "tabela_nome_1";
    private static final String ARG_TABELA_NOME_2 = "tabela_nome_2";

    private static final String BASE_API_URL = "https://api-spring-mongodb.onrender.com/";

    private Long tabelaId1;
    private Long tabelaId2;
    private String tabelaNome1;
    private String tabelaNome2;

    private TabelaAPI tabelaAPI;

    private ProgressBar progressBarLoading;
    private NestedScrollView scrollViewBlocoTabelas;
    private TextView tvNomeP1Header;
    private TextView tvNomeP2Header;
    private TextView tvDiffTitle;
    private ImageView ivInverter;
    private ImageView ivVoltar;

    private List<ComparacaoNutrienteDTO> originalComparisonData;
    private boolean isFlipped = false;

    private final Map<String, Boolean> expandedState = new HashMap<>();

    private final Map<String, Integer> nutrientComparacaoTextViewMap = new HashMap<>();
    private final Map<String, Integer> nutrientIconViewMap = new HashMap<>();
    private final Map<String, Integer> nutrientExpansionIconMap = new HashMap<>();
    private final Map<String, Integer> nutrientLineViewMap = new HashMap<>();
    private final Map<String, Integer> nutrientDetailViewMap = new HashMap<>();
    private final Map<String, Integer> detailNameP1Map = new HashMap<>();
    private final Map<String, Integer> detailValueP1Map = new HashMap<>();
    private final Map<String, Integer> detailNameP2Map = new HashMap<>();
    private final Map<String, Integer> detailValueP2Map = new HashMap<>();

    private static final Pattern UNIT_PATTERN = Pattern.compile("\\(([^)]+)\\)");

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

        if (getActivity() != null) {
            ConexaoAPI conexaoAPI = new ConexaoAPI(BASE_API_URL);
            tabelaAPI = conexaoAPI.getApi(TabelaAPI.class);

            if (tabelaId1 > 0 && tabelaId2 > 0) {
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

        progressBarLoading = view.findViewById(R.id.progress_bar_loading3);
        scrollViewBlocoTabelas = view.findViewById(R.id.scroll_view_comparacao);
        tvNomeP1Header = view.findViewById(R.id.textViewTitulo2);
        tvNomeP2Header = view.findViewById(R.id.textViewTitulo4);
        tvDiffTitle = view.findViewById(R.id.tvTabelaTitulo1);
        ivInverter = view.findViewById(R.id.imageView2);
        ivVoltar = view.findViewById(R.id.voltar);

        if (progressBarLoading != null) {
            progressBarLoading.setVisibility(View.VISIBLE);
        }
        if (scrollViewBlocoTabelas != null) {
            scrollViewBlocoTabelas.setVisibility(View.GONE);
        }

        setupNutrientMaps();

        displayTableNames();

        if (ivVoltar != null) {
            ivVoltar.setOnClickListener(v -> requireActivity().onBackPressed());
        }

        if (ivInverter != null) {
            ivInverter.setOnClickListener(v -> handleFlip());
        } else {
            Log.w(TAG, "ImageView com ID 'imageView2' não encontrado para o botão de inversão.");
        }

        setupExpansionListeners(view);

        if (tabelaId1.equals(-1L) || tabelaId2.equals(-1L)) {
            if (progressBarLoading != null) {
                progressBarLoading.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        progressBarLoading = null;
        scrollViewBlocoTabelas = null;
        tvNomeP1Header = null;
        tvNomeP2Header = null;
        tvDiffTitle = null;
        ivInverter = null;
        ivVoltar = null;
    }

    private void setupNutrientMaps() {
        String[] nutrientNames = {
                "Valor Calórico (kcal)", "Proteína (g)", "Carboidrato (g)",
                "Açúcar Total (g)", "Gordura Total (g)", "Gordura Saturada (g)", "Sódio (mg)"
        };

        String nameCalorico = nutrientNames[0];
        nutrientComparacaoTextViewMap.put(nameCalorico, R.id.tv_valor_calorico_t1);
        nutrientIconViewMap.put(nameCalorico, R.id.iv_valor_calorico_subtracao);
        nutrientExpansionIconMap.put(nameCalorico, R.id.iv_abertura_detalhes_calorico);
        nutrientLineViewMap.put(nameCalorico, R.id.row_valor_calorico);
        nutrientDetailViewMap.put(nameCalorico, R.id.detail_expansion_calorico);
        detailNameP1Map.put(nameCalorico, R.id.tv_nome_produto1_calorico);
        detailValueP1Map.put(nameCalorico, R.id.tv_valor_produto1_calorico);
        detailNameP2Map.put(nameCalorico, R.id.tv_nome_produto2_calorico);
        detailValueP2Map.put(nameCalorico, R.id.tv_valor_produto2_calorico);

        String nameProteina = nutrientNames[1];
        nutrientComparacaoTextViewMap.put(nameProteina, R.id.tv_proteina_t1);
        nutrientIconViewMap.put(nameProteina, R.id.iv_proteina_subtracao);
        nutrientExpansionIconMap.put(nameProteina, R.id.iv_abertura_detalhes_proteina);
        nutrientLineViewMap.put(nameProteina, R.id.row_proteina);
        nutrientDetailViewMap.put(nameProteina, R.id.detail_expansion_proteina);
        detailNameP1Map.put(nameProteina, R.id.tv_nome_produto1_proteina);
        detailValueP1Map.put(nameProteina, R.id.tv_valor_produto1_proteina);
        detailNameP2Map.put(nameProteina, R.id.tv_nome_produto2_proteina);
        detailValueP2Map.put(nameProteina, R.id.tv_valor_produto2_proteina);

        String nameCarboidrato = nutrientNames[2];
        nutrientComparacaoTextViewMap.put(nameCarboidrato, R.id.tv_carboidrato_t1);
        nutrientIconViewMap.put(nameCarboidrato, R.id.iv_carboidrato_subtracao);
        nutrientExpansionIconMap.put(nameCarboidrato, R.id.iv_abertura_detalhes_carboidrato);
        nutrientLineViewMap.put(nameCarboidrato, R.id.row_carboidrato);
        nutrientDetailViewMap.put(nameCarboidrato, R.id.detail_expansion_carboidrato);
        detailNameP1Map.put(nameCarboidrato, R.id.tv_nome_produto1_carboidrato);
        detailValueP1Map.put(nameCarboidrato, R.id.tv_valor_produto1_carboidrato);
        detailNameP2Map.put(nameCarboidrato, R.id.tv_nome_produto2_carboidrato);
        detailValueP2Map.put(nameCarboidrato, R.id.tv_valor_produto2_carboidrato);

        String nameAcucar = nutrientNames[3];
        nutrientComparacaoTextViewMap.put(nameAcucar, R.id.tv_acucar_total_t1);
        nutrientIconViewMap.put(nameAcucar, R.id.iv_acucar_total_subtracao);
        nutrientExpansionIconMap.put(nameAcucar, R.id.iv_abertura_detalhes_acucar_total);
        nutrientLineViewMap.put(nameAcucar, R.id.row_acucar_total);
        nutrientDetailViewMap.put(nameAcucar, R.id.detail_expansion_acucar_total);
        detailNameP1Map.put(nameAcucar, R.id.tv_nome_produto1_acucar_total);
        detailValueP1Map.put(nameAcucar, R.id.tv_valor_produto1_acucar_total);
        detailNameP2Map.put(nameAcucar, R.id.tv_nome_produto2_acucar_total);
        detailValueP2Map.put(nameAcucar, R.id.tv_valor_produto2_acucar_total);

        String nameGorduraTotal = nutrientNames[4];
        nutrientComparacaoTextViewMap.put(nameGorduraTotal, R.id.tv_gordura_total_t1);
        nutrientIconViewMap.put(nameGorduraTotal, R.id.iv_gordura_total_subtracao);
        nutrientExpansionIconMap.put(nameGorduraTotal, R.id.iv_abertura_detalhes_gordura_total);
        nutrientLineViewMap.put(nameGorduraTotal, R.id.row_gordura_total);
        nutrientDetailViewMap.put(nameGorduraTotal, R.id.detail_expansion_gordura_total);
        detailNameP1Map.put(nameGorduraTotal, R.id.tv_nome_produto1_gordura_total);
        detailValueP1Map.put(nameGorduraTotal, R.id.tv_valor_produto1_gordura_total);
        detailNameP2Map.put(nameGorduraTotal, R.id.tv_nome_produto2_gordura_total);
        detailValueP2Map.put(nameGorduraTotal, R.id.tv_valor_produto2_gordura_total);

        String nameGorduraSaturada = nutrientNames[5];
        nutrientComparacaoTextViewMap.put(nameGorduraSaturada, R.id.tv_gordura_saturada_t1);
        nutrientIconViewMap.put(nameGorduraSaturada, R.id.iv_gordura_saturada_subtracao);
        nutrientExpansionIconMap.put(nameGorduraSaturada, R.id.iv_abertura_detalhes_gordura_saturada);
        nutrientLineViewMap.put(nameGorduraSaturada, R.id.row_gordura_saturada);
        nutrientDetailViewMap.put(nameGorduraSaturada, R.id.detail_expansion_gordura_saturada);
        detailNameP1Map.put(nameGorduraSaturada, R.id.tv_nome_produto1_gordura_saturada);
        detailValueP1Map.put(nameGorduraSaturada, R.id.tv_valor_produto1_gordura_saturada);
        detailNameP2Map.put(nameGorduraSaturada, R.id.tv_nome_produto2_gordura_saturada);
        detailValueP2Map.put(nameGorduraSaturada, R.id.tv_valor_produto2_gordura_saturada);

        String nameSodio = nutrientNames[6];
        nutrientComparacaoTextViewMap.put(nameSodio, R.id.tv_sodio_t1);
        nutrientIconViewMap.put(nameSodio, R.id.iv_sodio_subtracao);
        nutrientExpansionIconMap.put(nameSodio, R.id.iv_abertura_detalhes_sodio);
        nutrientLineViewMap.put(nameSodio, R.id.row_sodio);
        nutrientDetailViewMap.put(nameSodio, R.id.detail_expansion_sodio);
        detailNameP1Map.put(nameSodio, R.id.tv_nome_produto1_sodio);
        detailValueP1Map.put(nameSodio, R.id.tv_valor_produto1_sodio);
        detailNameP2Map.put(nameSodio, R.id.tv_nome_produto2_sodio);
        detailValueP2Map.put(nameSodio, R.id.tv_valor_produto2_sodio);

        for (String key : nutrientNames) {
            expandedState.put(key, false);
        }
    }

    private void displayTableNames() {
        if (tvNomeP1Header != null) {
            tvNomeP1Header.setText(isFlipped ? tabelaNome2 : tabelaNome1);
        }
        if (tvNomeP2Header != null) {
            tvNomeP2Header.setText(isFlipped ? tabelaNome1 : tabelaNome2);
        }
    }

    private void fetchComparisonData(Integer idTabela1, Integer idTabela2) {
        if (tabelaAPI == null) {
            Toast.makeText(getContext(), "Serviço de API não inicializado.", Toast.LENGTH_SHORT).show();
            if (progressBarLoading != null) {
                progressBarLoading.setVisibility(View.GONE);
            }
            return;
        }

        tabelaAPI.compararTabelas(idTabela1, idTabela2).enqueue(new Callback<List<ComparacaoNutrienteDTO>>() {
            @Override
            public void onResponse(Call<List<ComparacaoNutrienteDTO>> call, Response<List<ComparacaoNutrienteDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        Toast.makeText(getContext(), "Nenhum dado de comparação encontrado.", Toast.LENGTH_LONG).show();
                        if (scrollViewBlocoTabelas != null)
                            scrollViewBlocoTabelas.setVisibility(View.GONE);
                        if (progressBarLoading != null) progressBarLoading.setVisibility(View.GONE);
                        return;
                    }

                    originalComparisonData = response.body();
                    displayComparisonData(originalComparisonData, isFlipped);

                    if (scrollViewBlocoTabelas != null)
                        scrollViewBlocoTabelas.setVisibility(View.VISIBLE);

                } else {
                    Toast.makeText(getContext(), "Erro ao carregar dados da comparação. Código: " + response.code(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Erro na resposta: " + response.code() + " - " + response.message());

                    if (scrollViewBlocoTabelas != null)
                        scrollViewBlocoTabelas.setVisibility(View.GONE);
                }

                if (progressBarLoading != null) {
                    progressBarLoading.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<ComparacaoNutrienteDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Falha na comunicação com a API.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Falha de rede: " + t.getMessage());

                if (scrollViewBlocoTabelas != null) scrollViewBlocoTabelas.setVisibility(View.GONE);
                if (progressBarLoading != null) {
                    progressBarLoading.setVisibility(View.GONE);
                }
            }
        });
    }

    private void handleFlip() {
        if (originalComparisonData == null || getContext() == null) {
            Toast.makeText(getContext(), "Dados de comparação ainda não carregados.", Toast.LENGTH_SHORT).show();
            return;
        }

        isFlipped = !isFlipped;

        displayTableNames();

        displayComparisonData(originalComparisonData, isFlipped);

        updateAllExpansionVisuals();
    }

    private void setupExpansionListeners(@NonNull View view) {
        for (Map.Entry<String, Integer> lineEntry : nutrientLineViewMap.entrySet()) {
            final String nutrientName = lineEntry.getKey();
            Integer lineResId = lineEntry.getValue();
            Integer detailResId = nutrientDetailViewMap.get(nutrientName);
            Integer expansionIconResId = nutrientExpansionIconMap.get(nutrientName);

            final View lineView = view.findViewById(lineResId);
            final View detailView = detailResId != null ? view.findViewById(detailResId) : null;
            final ImageView expansionIconView = expansionIconResId != null ? (ImageView) view.findViewById(expansionIconResId) : null;

            if (lineView != null && detailView != null && expansionIconView != null) {
                detailView.setVisibility(View.GONE);

                lineView.setOnClickListener(v ->
                        handleExpansionClick(nutrientName, expansionIconView, detailView)
                );
            } else {
                Log.w(TAG, String.format(Locale.getDefault(), "Listener de expansão não configurado para '%s'. Verifique os IDs: Linha (%d), Detalhe (%d), Ícone Expansão (%d)",
                        nutrientName, lineResId, detailResId, expansionIconResId));
            }
        }
    }

    private void handleExpansionClick(String nutrientName, ImageView expansionIconView, View detailView) {
        if (!expandedState.containsKey(nutrientName)) return;

        boolean isExpanded = expandedState.get(nutrientName);

        if (isExpanded) {
            detailView.setVisibility(View.GONE);
            expandedState.put(nutrientName, false);
            animateIcon(expansionIconView, 0f);
        } else {
            displayNutrientDetails(nutrientName);
            detailView.setVisibility(View.VISIBLE);
            expandedState.put(nutrientName, true);
            animateIcon(expansionIconView, 90f);
        }
    }

    private void animateIcon(ImageView imageView, float endRotation) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation", imageView.getRotation(), endRotation);
        animator.setDuration(200);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    private void updateAllExpansionVisuals() {
        if (getView() == null) return;
        View rootView = getView();

        for (Map.Entry<String, Boolean> entry : expandedState.entrySet()) {
            String nutrientName = entry.getKey();
            boolean isExpanded = entry.getValue();

            Integer detailResId = nutrientDetailViewMap.get(nutrientName);
            Integer expansionIconResId = nutrientExpansionIconMap.get(nutrientName);

            if (detailResId != null && expansionIconResId != null) {
                View detailView = rootView.findViewById(detailResId);
                ImageView expansionIconView = (ImageView) rootView.findViewById(expansionIconResId);

                if (detailView != null && expansionIconView != null) {
                    detailView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                    expansionIconView.setRotation(isExpanded ? 90f : 0f);

                    if (isExpanded) {
                        displayNutrientDetails(nutrientName);
                    }
                }
            }
        }
    }

    private void displayNutrientDetails(String nutrientName) {
        if (originalComparisonData == null || getView() == null) return;

        ComparacaoNutrienteDTO item = originalComparisonData.stream()
                .filter(dto -> nutrientName.equals(dto.getNomeNutriente()))
                .findFirst()
                .orElse(null);

        if (item == null || item.getPorcaoPorTabela() == null) {
            Log.e(TAG, "Detalhes do nutriente ou mapa de porção não encontrados para: " + nutrientName);
            return;
        }

        Map<String, Double> porcaoMap = item.getPorcaoPorTabela();
        Double valorTabela1 = porcaoMap.get(tabelaNome1);
        Double valorTabela2 = porcaoMap.get(tabelaNome2);

        String nomeP1;
        Double valorP1;
        String nomeP2;
        Double valorP2;

        if (isFlipped) {
            nomeP1 = tabelaNome2;
            valorP1 = valorTabela2;
            nomeP2 = tabelaNome1;
            valorP2 = valorTabela1;
        } else {
            nomeP1 = tabelaNome1;
            valorP1 = valorTabela1;
            nomeP2 = tabelaNome2;
            valorP2 = valorTabela2;
        }

        Integer nameP1Id = detailNameP1Map.get(nutrientName);
        Integer valueP1Id = detailValueP1Map.get(nutrientName);
        Integer nameP2Id = detailNameP2Map.get(nutrientName);
        Integer valueP2Id = detailValueP2Map.get(nutrientName);

        if (nameP1Id == null || valueP1Id == null || nameP2Id == null || valueP2Id == null) {
            Log.e(TAG, "IDs de detalhes incompletos para: " + nutrientName);
            return;
        }

        View rootView = getView();

        TextView tvNameP1 = rootView.findViewById(nameP1Id);
        TextView tvValueP1 = rootView.findViewById(valueP1Id);
        TextView tvNameP2 = rootView.findViewById(nameP2Id);
        TextView tvValueP2 = rootView.findViewById(valueP2Id);

        if (tvNameP1 != null) tvNameP1.setText(nomeP1);
        if (tvValueP1 != null) tvValueP1.setText(formatNutrientValueDetails(nutrientName, valorP1));

        if (tvNameP2 != null) tvNameP2.setText(nomeP2);
        if (tvValueP2 != null) tvValueP2.setText(formatNutrientValueDetails(nutrientName, valorP2));
    }

    private void displayComparisonData(List<ComparacaoNutrienteDTO> comparacoes, boolean flipped) {
        if (getContext() == null) return;
        View rootView = getView();
        if (rootView == null) return;

        String nomeTabela1 = flipped ? tabelaNome2 : tabelaNome1;
        String nomeTabela2 = flipped ? tabelaNome1 : tabelaNome2;
        if (tvDiffTitle != null) {
            tvDiffTitle.setText(String.format(Locale.getDefault(), "Diferença Nutricional \n %s - %s", nomeTabela1, nomeTabela2));
        }

        for (ComparacaoNutrienteDTO item : comparacoes) {
            String nutrientName = item.getNomeNutriente();
            Double originalValorComparacao = item.getValorComparacao();

            if (originalValorComparacao == null) continue;

            double valorComparacaoAjustado = flipped ? -originalValorComparacao : originalValorComparacao;

            Integer diffResId = nutrientComparacaoTextViewMap.get(nutrientName);
            if (diffResId != null) {
                TextView tvDiff = rootView.findViewById(diffResId);
                if (tvDiff != null) {
                    tvDiff.setText(formatNutrientValueDifference(nutrientName, valorComparacaoAjustado));
                }
            }

            Integer iconResId = nutrientIconViewMap.get(nutrientName);
            if (iconResId != null) {
                ImageView ivIndicator = rootView.findViewById(iconResId);
                if (ivIndicator != null) {
                    updateComparisonIcon(ivIndicator, valorComparacaoAjustado);
                }
            }
        }
    }

    private String formatNutrientValueDifference(String nutrientName, Double value) {
        if (value == null) return "-";

        Matcher matcher = UNIT_PATTERN.matcher(nutrientName);
        String unit = "";
        if (matcher.find()) {
            unit = " " + matcher.group(1);
        }

        final double INT_TOLERANCE = 0.01;
        String formatString;

        if (Math.abs(value - Math.round(value)) < INT_TOLERANCE) {
            formatString = "%.0f";
        } else if (nutrientName.contains("(kcal)") || nutrientName.contains("(mg)")) {
            formatString = "%.1f";
        } else {
            formatString = "%.2f";
        }

        String formattedValue = String.format(Locale.getDefault(), formatString, value);
        return formattedValue + unit;
    }

    private String formatNutrientValueDetails(String nutrientName, Double value) {

        Matcher matcher = UNIT_PATTERN.matcher(nutrientName);
        String unit = "";
        if (matcher.find()) {
            unit = " " + matcher.group(1);
        }

        if (value == null) {
            return "0" + unit;
        }

        String formatString;
        final double INT_TOLERANCE = 0.01;

        if (Math.abs(value - Math.round(value)) < INT_TOLERANCE) {
            formatString = "%.0f";
        } else if (nutrientName.contains("(kcal)") || nutrientName.contains("(mg)")) {
            formatString = "%.1f";
        } else {
            formatString = "%.2f";
        }

        String formattedValue = String.format(Locale.getDefault(), formatString, Math.abs(value));

        return formattedValue + unit;
    }

    private void updateComparisonIcon(ImageView imageView, double valorComparacao) {
        if (getContext() == null) return;

        final double TOLERANCE = 0.01;

        if (valorComparacao > TOLERANCE) {
            imageView.setImageResource(R.drawable.ic_soma);
        } else if (valorComparacao < -TOLERANCE) {
            imageView.setImageResource(R.drawable.ic_subtracao);
        } else {
            imageView.setImageResource(R.drawable.ic_igual);
        }

        imageView.clearColorFilter();
    }
}