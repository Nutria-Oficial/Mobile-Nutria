package com.bea.nutria.ui.Ingrediente;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;
import com.bea.nutria.api.IngredienteAPI;
import com.bea.nutria.api.conexaoApi.ConexaoAPI;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IngredienteAdapter extends RecyclerView.Adapter<IngredienteAdapter.ViewHolder> {

    private static final String TAG = "IngredienteAdapter";
    private static final String url = "https://api-spring-mongodb.onrender.com";

    private List<IngredienteResponse> lista;
    private Context context;
    private List<IngredienteResponse> ingredientesSelecionados;
    private OnIngredienteChangeListener listener;
    private IngredienteAPI ingredienteApi;
    private int itemExpandidoAtual = -1;

    public void adicionarIngrediente(IngredienteResponse ingredienteCriado) {
        lista.add(ingredienteCriado);
        notifyDataSetChanged();
    }

    public interface OnIngredienteChangeListener {
        void onIngredienteAdicionado(IngredienteResponse ingrediente);

        void onIngredienteRemovido(IngredienteResponse ingrediente);
    }

    public IngredienteAdapter(Context context, List<IngredienteResponse> lista) {
        this.lista = lista != null ? lista : new ArrayList<>();
        this.context = context;
        this.ingredientesSelecionados = new ArrayList<>();

        ConexaoAPI apiManager = new ConexaoAPI(url);
        this.ingredienteApi = apiManager.getApi(IngredienteAPI.class);
    }

    public void setOnIngredienteChangeListener(OnIngredienteChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingrediente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 0 || position >= lista.size()) {
            return;
        }

        IngredienteResponse ingrediente = lista.get(position);
        holder.bind(ingrediente, position);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    private boolean estaNaListaSelecionados(IngredienteResponse ingrediente) {
        for (IngredienteResponse selecionado : ingredientesSelecionados) {
            if (selecionado.getId().equals(ingrediente.getId())) {
                return true;
            }
        }
        return false;
    }

    private void removerDaSelecao(IngredienteResponse ingrediente) {
        for (int i = 0; i < ingredientesSelecionados.size(); i++) {
            if (ingredientesSelecionados.get(i).getId().equals(ingrediente.getId())) {
                ingredientesSelecionados.remove(i);
                break;
            }
        }
    }

    private void configurarBotao(Button botao, boolean selecionado) {
        if (selecionado) {
            botao.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.light_gray));
            botao.setTextColor(ContextCompat.getColor(context, R.color.gray));
            botao.setText("Adicionado");
        } else {
            botao.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.orange));
            botao.setTextColor(ContextCompat.getColor(context, R.color.white));
            botao.setText("Adicionar");
        }
    }

    public void atualizarLista(List<IngredienteResponse> novaLista) {
        this.lista = novaLista != null ? novaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void restaurarSelecao(List<IngredienteResponse> selecionados) {
        this.ingredientesSelecionados.clear();
        if (selecionados != null) {
            this.ingredientesSelecionados.addAll(selecionados);
        }
        notifyDataSetChanged();
    }

    public List<IngredienteResponse> getListaSelecionados() {
        return new ArrayList<>(ingredientesSelecionados);
    }

    public Bundle getIngredientesSelecionados() {
        Bundle bundle = new Bundle();
        for (int i = 0; i < ingredientesSelecionados.size(); i++) {
            bundle.putSerializable("ingrediente_" + i, ingredientesSelecionados.get(i));
        }
        bundle.putInt("total", ingredientesSelecionados.size());
        return bundle;
    }

    public void limparSelecao() {
        ingredientesSelecionados.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        Button btAddIngrediente;
        TextView txtNomeIngrediente;
        LinearLayout layoutHeader;
        ImageView iconeDropdown;
        MaterialCardView cardTabelaNutricional;
        ProgressBar progressBar;
        LinearLayout containerTabela;
        TextView tvTabelaTitulo;
        TextView tvPorcaoEmbalagemColuna;
        TextView tvPorcaoColuna;
        TableLayout tableLayout;

        boolean isExpanded = false;
        boolean dadosCarregados = false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btAddIngrediente = itemView.findViewById(R.id.btAddIngrediente);
            txtNomeIngrediente = itemView.findViewById(R.id.txtNomeIngrediente);
            layoutHeader = itemView.findViewById(R.id.layoutHeader);
            iconeDropdown = itemView.findViewById(R.id.iconeDropdown);
            cardTabelaNutricional = itemView.findViewById(R.id.cardTabelaNutricional);
            progressBar = itemView.findViewById(R.id.progressBar);
            containerTabela = itemView.findViewById(R.id.containerTabela);
            tvTabelaTitulo = itemView.findViewById(R.id.tvTabelaTitulo);
            tableLayout = itemView.findViewById(R.id.tableLayout);
        }

        public void bind(IngredienteResponse ingrediente, int position) {
            txtNomeIngrediente.setText(ingrediente.getNomeIngrediente());

            boolean estaSelecionado = estaNaListaSelecionados(ingrediente);
            configurarBotao(btAddIngrediente, estaSelecionado);

            // resetar estado expandido se não for o item atual
            if (position != itemExpandidoAtual) {
                isExpanded = false;
                cardTabelaNutricional.setVisibility(View.GONE);
                iconeDropdown.setRotation(0f);
                dadosCarregados = false;
            }

            // click no header para expandir/colapsar
            layoutHeader.setOnClickListener(v -> {
                if (isExpanded) {
                    // colapsar
                    colapsar();
                    if (itemExpandidoAtual == position) {
                        itemExpandidoAtual = -1;
                    }
                } else {
                    // colapsar item anterior se houver
                    if (itemExpandidoAtual != -1 && itemExpandidoAtual != position) {
                        notifyItemChanged(itemExpandidoAtual);
                    }

                    // expandir este item
                    expandir(ingrediente);
                    itemExpandidoAtual = position;
                }
            });

            // click no botão adicionar/remover
            btAddIngrediente.setOnClickListener(v -> {
                if (estaSelecionado) {
                    removerDaSelecao(ingrediente);
                    configurarBotao(btAddIngrediente, false);

                    if (listener != null) {
                        listener.onIngredienteRemovido(ingrediente);
                    }
                } else {
                    ingredientesSelecionados.add(ingrediente);
                    configurarBotao(btAddIngrediente, true);

                    if (listener != null) {
                        listener.onIngredienteAdicionado(ingrediente);
                    }
                }
            });
        }

        private void expandir(IngredienteResponse ingrediente) {
            isExpanded = true;
            cardTabelaNutricional.setVisibility(View.VISIBLE);

            // animar seta
            ObjectAnimator.ofFloat(iconeDropdown, "rotation", 0f, 180f)
                    .setDuration(300)
                    .start();

            // carregar dados se ainda não foram carregados
            if (!dadosCarregados) {
                carregarDadosTabela(ingrediente);
            }
        }

        private void colapsar() {
            isExpanded = false;
            cardTabelaNutricional.setVisibility(View.GONE);

            // animar seta
            ObjectAnimator.ofFloat(iconeDropdown, "rotation", 180f, 0f)
                    .setDuration(300)
                    .start();
        }

        private void carregarDadosTabela(IngredienteResponse ingrediente) {
            progressBar.setVisibility(View.VISIBLE);
            containerTabela.setVisibility(View.GONE);

            ingredienteApi.getIngredienteById(ingrediente.getId()).enqueue(new Callback<Ingrediente>() {
                @Override
                public void onResponse(Call<Ingrediente> call, Response<Ingrediente> response) {
                    progressBar.setVisibility(View.GONE);
                    containerTabela.setVisibility(View.VISIBLE);

                    if (response.isSuccessful() && response.body() != null) {
                        Ingrediente dadosCompletos = response.body();
                        preencherDadosTabela(dadosCompletos);
                        dadosCarregados = true;
                    } else {
                        Log.e(TAG, "Erro ao carregar ingrediente: " + response.code());
                        tvTabelaTitulo.setText("Erro ao carregar dados");
                    }
                }

                @Override
                public void onFailure(Call<Ingrediente> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    containerTabela.setVisibility(View.VISIBLE);
                    tvTabelaTitulo.setText("Erro de conexão");
                    Log.e(TAG, "Falha na requisição", t);
                }
            });
        }

        private void preencherDadosTabela(Ingrediente ingrediente) {
            tableLayout.removeAllViews();

            // atualizar título e porções
            tvTabelaTitulo.setText("Tabela Nutricional");

            int padding = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8,
                    context.getResources().getDisplayMetrics()
            );

            // cabeçalho da tabela
            TableRow nomeColuna = new TableRow(context);
            nomeColuna.setPadding(padding, padding, padding, padding);

            TextView coluna1 = criarTextViewColuna("Nutriente", true);
            TextView coluna2 = criarTextViewColuna("Valor", true);

            nomeColuna.addView(coluna1);
            nomeColuna.addView(coluna2);
            tableLayout.addView(nomeColuna);

            adicionarDivisor();

            // adicionar nutrientes principais
            if (ingrediente.getCaloria() > 0) {
                adicionarLinhaNutriente("Calorias",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f kcal", ingrediente.getCaloria()));
            }

            if (ingrediente.getProteina() > 0) {
                adicionarLinhaNutriente("Proteínas",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f g", ingrediente.getProteina()));
            }

            if (ingrediente.getCarboidrato() > 0) {
                adicionarLinhaNutriente("Carboidratos",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f g", ingrediente.getCarboidrato()));
            }

            if (ingrediente.getAcucar() > 0) {
                adicionarLinhaNutriente("  Açúcar",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f g", ingrediente.getAcucar()));
            }

            if (ingrediente.getGorduraTotal() > 0) {
                adicionarLinhaNutriente("Gorduras Totais",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f g", ingrediente.getGorduraTotal()));
            }

            if (ingrediente.getGorduraSaturada() > 0) {
                adicionarLinhaNutriente("  Gordura Saturada",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f g", ingrediente.getGorduraSaturada()));
            }

            if (ingrediente.getGorduraMonoinsaturada() > 0) {
                adicionarLinhaNutriente("  Gordura Monoinsaturada",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f g", ingrediente.getGorduraMonoinsaturada()));
            }

            if (ingrediente.getGorduraPoliinsaturada() > 0) {
                adicionarLinhaNutriente("  Gordura Poliinsaturada",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f g", ingrediente.getGorduraPoliinsaturada()));
            }

            if (ingrediente.getFibra() > 0) {
                adicionarLinhaNutriente("Fibras",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f g", ingrediente.getFibra()));
            }

            if (ingrediente.getSodio() > 0) {
                adicionarLinhaNutriente("Sódio",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getSodio()));
            }

            if (ingrediente.getColesterol() > 0) {
                adicionarLinhaNutriente("Colesterol",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getColesterol()));
            }

            if (ingrediente.getAgua() > 0) {
                adicionarLinhaNutriente("Água",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f g", ingrediente.getAgua()));
            }

            if (ingrediente.getCalcio() > 0) {
                adicionarLinhaNutriente("Cálcio",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getCalcio()));
            }

            if (ingrediente.getFerro() > 0) {
                adicionarLinhaNutriente("Ferro",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getFerro()));
            }

            if (ingrediente.getMagnesio() > 0) {
                adicionarLinhaNutriente("Magnésio",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getMagnesio()));
            }

            if (ingrediente.getFosforo() > 0) {
                adicionarLinhaNutriente("Fósforo",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getFosforo()));
            }

            if (ingrediente.getPotassio() > 0) {
                adicionarLinhaNutriente("Potássio",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getPotassio()));
            }

            if (ingrediente.getZinco() > 0) {
                adicionarLinhaNutriente("Zinco",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getZinco()));
            }

            if (ingrediente.getCobre() > 0) {
                adicionarLinhaNutriente("Cobre",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getCobre()));
            }

            if (ingrediente.getSelenio() > 0) {
                adicionarLinhaNutriente("Selênio",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mcg", ingrediente.getSelenio()));
            }

            if (ingrediente.getVitaminaC() > 0) {
                adicionarLinhaNutriente("Vitamina C",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getVitaminaC()));
            }

            if (ingrediente.getVitaminaD() > 0) {
                adicionarLinhaNutriente("Vitamina D",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mcg", ingrediente.getVitaminaD()));
            }

            if (ingrediente.getVitaminaE() > 0) {
                adicionarLinhaNutriente("Vitamina E",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getVitaminaE()));
            }

            if (ingrediente.getVitaminaK() > 0) {
                adicionarLinhaNutriente("Vitamina K",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mcg", ingrediente.getVitaminaK()));
            }

            if (ingrediente.getVitaminaB6() > 0) {
                adicionarLinhaNutriente("Vitamina B6",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getVitaminaB6()));
            }

            if (ingrediente.getVitaminaB12() > 0) {
                adicionarLinhaNutriente("Vitamina B12",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mcg", ingrediente.getVitaminaB12()));
            }

            if (ingrediente.getTiamina() > 0) {
                adicionarLinhaNutriente("Tiamina (B1)",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getTiamina()));
            }

            if (ingrediente.getRiboflavina() > 0) {
                adicionarLinhaNutriente("Riboflavina (B2)",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getRiboflavina()));
            }

            if (ingrediente.getNiacina() > 0) {
                adicionarLinhaNutriente("Niacina (B3)",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getNiacina()));
            }

            if (ingrediente.getFolato() > 0) {
                adicionarLinhaNutriente("Folato",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mcg", ingrediente.getFolato()));
            }

            if (ingrediente.getRetinol() > 0) {
                adicionarLinhaNutriente("Retinol (Vitamina A)",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mcg", ingrediente.getRetinol()));
            }

            if (ingrediente.getColina() > 0) {
                adicionarLinhaNutriente("Colina",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getColina()));
            }

            if (ingrediente.getCafeina() > 0) {
                adicionarLinhaNutriente("Cafeína",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getCafeina()));
            }

            if (ingrediente.getTeobromina() > 0) {
                adicionarLinhaNutriente("Teobromina",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f mg", ingrediente.getTeobromina()));
            }

            if (ingrediente.getAlcool() > 0) {
                adicionarLinhaNutriente("Álcool",
                        String.format(Locale.forLanguageTag("pt-BR"), "%.2f g", ingrediente.getAlcool()));
            }
        }

        private TextView criarTextViewColuna(String texto, boolean isCabecalho) {
            TextView textView = new TextView(context);
            textView.setText(texto);
            textView.setTextColor(Color.parseColor("#0B0B0B"));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, isCabecalho ? 14 : 13);

            if (isCabecalho) {
                textView.setTypeface(null, android.graphics.Typeface.BOLD);
            }

            return textView;
        }

        private void adicionarLinhaNutriente(String nutriente, String valor) {
            int padding = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8,
                    context.getResources().getDisplayMetrics()
            );

            TableRow row = new TableRow(context);
            row.setPadding(padding, padding, padding, padding);

            TextView tvNutriente = criarTextViewColuna(nutriente, false);
            TextView tvValor = criarTextViewColuna(valor, false);

            row.addView(tvNutriente);
            row.addView(tvValor);

            tableLayout.addView(row);
            adicionarDivisor();
        }

        private void adicionarDivisor() {
            View linha = new View(context);
            TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, 1
            );
            linha.setLayoutParams(params);
            linha.setBackgroundColor(Color.parseColor("#E0E0E0"));
            tableLayout.addView(linha);
        }
    }
}