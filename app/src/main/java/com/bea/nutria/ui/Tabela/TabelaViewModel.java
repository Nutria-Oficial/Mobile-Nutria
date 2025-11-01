package com.bea.nutria.ui.Tabela;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.Map;

public class TabelaViewModel extends ViewModel {
    private final MutableLiveData<Map<Integer, String>> quantidadesLivedata = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<String> unidadeMedidaLiveData = new MutableLiveData<>("");
    private final MutableLiveData<String> nomeProdutoLiveData = new MutableLiveData<>("");
    private final MutableLiveData<String> nomeTabelaLiveData = new MutableLiveData<>("");
    private final MutableLiveData<Integer> porcaoEmbalagemLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Double> porcaoLiveData = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> temDadosSalvosLiveData = new MutableLiveData<>(false);

    public LiveData<Map<Integer, String>> getQuantidades() {
        return quantidadesLivedata;
    }

    public MutableLiveData<String> getUnidadeMedidaLiveData() {
        return unidadeMedidaLiveData;
    }

    public MutableLiveData<String> getNomeProdutoLiveData() {
        return nomeProdutoLiveData;
    }

    public MutableLiveData<String> getNomeTabelaLiveData() {
        return nomeTabelaLiveData;
    }

    public MutableLiveData<Integer> getPorcaoEmbalagemLiveData() {
        return porcaoEmbalagemLiveData;
    }

    public MutableLiveData<Double> getPorcaoLiveData() {
        return porcaoLiveData;
    }

    public LiveData<Boolean> hasDadosSalvos() {
        return temDadosSalvosLiveData;
    }
    public void setQuantidade(int ingredienteId, String valor){
        Map<Integer, String> mapAtual = quantidadesLivedata.getValue();

        if (mapAtual == null) mapAtual = new HashMap<>();

        mapAtual.put(ingredienteId,valor);
        quantidadesLivedata.setValue(mapAtual);
    }
    public void setUnidadeMedida(String unidadeMedida){
        unidadeMedidaLiveData.setValue(unidadeMedida);
    }
    public void setNomeProduto(String nomeProduto){
        nomeProdutoLiveData.setValue(nomeProduto);
    }
    public void setNomeTabela(String nomeTabela){
        nomeTabelaLiveData.setValue(nomeTabela);
    }

    public void setPorcao(Double porcao){
        porcaoLiveData.setValue(porcao);
    }
    public void setPorcaoEmbalagem(Integer porcaoEmbalagem){
        porcaoEmbalagemLiveData.setValue(porcaoEmbalagem);
    }
    public void setTemDadosSalvos(boolean temDadosSalvos) {
        temDadosSalvosLiveData.setValue(temDadosSalvos);
    }
    public String getQuantidade(int ingredienteId){
        Map<Integer, String> mapAtual = quantidadesLivedata.getValue();

        if (mapAtual == null) return "0";

        return mapAtual.getOrDefault(ingredienteId, "0");
    }
    public void removerQuantidade(int ingredienteId){
        Map<Integer, String> mapAtual = quantidadesLivedata.getValue();

        if (mapAtual != null){
            mapAtual.remove(ingredienteId);
            quantidadesLivedata.setValue(mapAtual);
        }

    }
}
