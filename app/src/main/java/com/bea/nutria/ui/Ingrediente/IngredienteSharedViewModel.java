package com.bea.nutria.ui.Ingrediente;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class IngredienteSharedViewModel extends ViewModel {
    private final MutableLiveData<List<IngredienteResponse>> ingredientesSelecionados = new MutableLiveData<>(new ArrayList<>());

    public void setIngredientesSelecionados(List<IngredienteResponse> ingredientes) {
        ingredientesSelecionados.setValue(new ArrayList<>(ingredientes));
    }

    public LiveData<List<IngredienteResponse>> getIngredientesSelecionados() {
        return ingredientesSelecionados;
    }

    // remover ingrediente específico
    public void removerIngrediente(IngredienteResponse ingrediente) {
        List<IngredienteResponse> atual = ingredientesSelecionados.getValue();
        if (atual != null) {
            List<IngredienteResponse> nova = new ArrayList<>(atual);
            // Remover por ID
            for (int i = 0; i < nova.size(); i++) {
                if (nova.get(i).getId().equals(ingrediente.getId())) {
                    nova.remove(i);
                    break;
                }
            }
            ingredientesSelecionados.setValue(nova);
        }
    }

    public void limparSelecao() {
        ingredientesSelecionados.setValue(new ArrayList<>());
    }
    public void limparIngredientes() {
        ingredientesSelecionados.setValue(new ArrayList<>());
    }

    public boolean temIngredientesSalvos() {
        return ingredientesSelecionados.getValue() != null && !ingredientesSelecionados.getValue().isEmpty();
    }
}