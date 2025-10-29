package com.bea.nutria.ui.Ingrediente;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.Map;

public class QuantidadeViewModel extends ViewModel {
    private final MutableLiveData<Map<Integer, String>> quantidadesLivedata = new MutableLiveData<>(new HashMap<>());

    public LiveData<Map<Integer, String>> getQuantidades() {
        return quantidadesLivedata;
    }
    public void setQuantidade(int ingredienteId, String valor){
        Map<Integer, String> mapAtual = quantidadesLivedata.getValue();

        if (mapAtual == null) mapAtual = new HashMap<>();

        mapAtual.put(ingredienteId,valor);
        quantidadesLivedata.setValue(mapAtual);
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
