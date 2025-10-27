package com.bea.nutria.ui.Ingrediente;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bea.nutria.ui.Ingrediente.IngredienteResponse;

import java.util.ArrayList;
import java.util.List;

public class IngredienteSharedViewModel extends ViewModel {
    private final MutableLiveData<List<IngredienteResponse>> ingredientesSelecionados = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<IngredienteResponse> novoIngredienteAdicionado = new MutableLiveData<>();

    public void setIngredientesSelecionados(List<IngredienteResponse> ingredientes) {
        ingredientesSelecionados.setValue(new ArrayList<>(ingredientes));
    }

    public LiveData<List<IngredienteResponse>> getIngredientesSelecionados() {
        return ingredientesSelecionados;
    }

    // notificar quando um novo ingrediente é criado
    public void setNovoIngredienteAdicionado(IngredienteResponse ingrediente) {
        novoIngredienteAdicionado.setValue(ingrediente);
    }

    public LiveData<IngredienteResponse> getNovoIngredienteAdicionado() {
        return novoIngredienteAdicionado;
    }

    public void removerIngrediente(IngredienteResponse ingrediente) {
        List<IngredienteResponse> atual = ingredientesSelecionados.getValue();
        if (atual != null) {
            List<IngredienteResponse> nova = new ArrayList<>(atual);
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
}