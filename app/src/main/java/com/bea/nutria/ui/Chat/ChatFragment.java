package com.bea.nutria.ui.Chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bea.nutria.R;
import com.bea.nutria.api.ChatAPIFast;
import com.bea.nutria.api.ChatAPISpring;
import com.bea.nutria.api.conexaoApi.ConexaoAPI;
import com.bea.nutria.api.conexaoApi.ConexaoFastAPI;
import com.bea.nutria.databinding.FragmentNutriaBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private FragmentNutriaBinding binding;
    private ChatAdapter chatAdapter;
    private List<String> chatAtual;
    private ConexaoAPI apiManagerSpring;
    private ConexaoFastAPI apiManagerFast;
    private ChatAPISpring chatAPISpring;
    private ChatAPIFast chatAPIFast;
    private int idUser = 0;
    private static final String URL_FAST = "https://nutria-fast-api.koyeb.app/";
    private static final String URL_SPRING = "https://api-spring-mongodb.onrender.com/";

    private int countErro = 0; // Variável não utilizada, mantida por consistência
    private int tentativas = 0; // Variável não utilizada, mantida por consistência

    private String prefsName = "nutria_prefs";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNutriaBinding.inflate(inflater, container, false);

        SharedPreferences prefs = requireActivity().getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        idUser = prefs.getInt("id", 1);

        if(idUser == 0){
            mostrarTelaVazia();
        }else{
            binding.progressBar.setVisibility(View.VISIBLE);
            inicializarChat();
            setupRecyclerView();
            setupListeners();
            carregarChat();
        }

        return binding.getRoot();
    }

    private void inicializarChat() {
        chatAtual = new ArrayList<>();

        // Spring Boot com autenticação
        apiManagerSpring = new ConexaoAPI(URL_SPRING);
        chatAPISpring = apiManagerSpring.getApi(ChatAPISpring.class);

        // FastAPI sem autenticação
        apiManagerFast = new ConexaoFastAPI(URL_FAST);
        chatAPIFast = apiManagerFast.getApi(ChatAPIFast.class);
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        binding.recyclerViewChat.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewChat.setAdapter(chatAdapter);
    }

    private void setupListeners() {
        binding.btEnviar.setOnClickListener(v -> enviarMensagem());
        binding.imgLixeira.setOnClickListener(v -> mostrarDialogConfirmacao());
    }

    private void carregarChat() {
        binding.progressBar.setVisibility(View.VISIBLE);
        apiManagerSpring.iniciarServidor(requireActivity(), () -> {
            chatAPISpring.listarChat(idUser).enqueue(new Callback<List<String>>() {
                @Override
                public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                    // **VERIFICAÇÃO DE SEGURANÇA CONTRA NullPointerException**
                    if (binding == null) {
                        return;
                    }

                    binding.progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        List<String> chatInteiro = response.body();

                        if (!chatInteiro.isEmpty()) {
                            chatAtual.clear();
                            chatAtual.addAll(chatInteiro);
                            chatAdapter.carregarChat(chatInteiro);

                            binding.recyclerViewChat.setVisibility(View.VISIBLE);
                            binding.imgLixeira.setVisibility(View.VISIBLE);
                            binding.imgNutriaChat.setVisibility(View.GONE);
                        } else {
                            mostrarTelaVazia();
                        }
                    } else {
                        mostrarTelaVazia();
                    }
                }

                @Override
                public void onFailure(Call<List<String>> call, Throwable throwable) {
                    // **VERIFICAÇÃO DE SEGURANÇA CONTRA NullPointerException**
                    if (binding == null) {
                        return;
                    }

                    binding.progressBar.setVisibility(View.GONE);
                    mostrarTelaVazia();
                    Toast.makeText(getContext(), "Erro ao carregar chat", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void enviarMensagem() {
        String mensagem = binding.editTextPergunta.getText().toString().trim();

        if (mensagem.isEmpty()) {
            return;
        }

        binding.btEnviar.setEnabled(false);
        binding.editTextPergunta.setText("");

        chatAtual.add(mensagem);
        chatAdapter.addMessage(mensagem);

        // scroll para última mensagem
        binding.recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1);

        binding.recyclerViewChat.setVisibility(View.VISIBLE);
        binding.imgLixeira.setVisibility(View.VISIBLE);
        binding.imgNutriaChat.setVisibility(View.GONE);

        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setPergunta(mensagem);
        chatRequest.setIdUser(idUser);

        enviarParaFastAPI(chatRequest, 0); // Passar contador como parâmetro
    }

    private void enviarParaFastAPI(ChatRequest chatRequest, int tentativa) {
        chatAPIFast.enviarMensagemPegarResposta(chatRequest).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                // **VERIFICAÇÃO DE SEGURANÇA CONTRA NullPointerException**
                if (binding == null) {
                    return;
                }

                binding.btEnviar.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    String respostaBot = response.body().getResposta();

                    // Adicionar resposta do bot
                    chatAtual.add(respostaBot);
                    chatAdapter.addMessage(respostaBot);

                    // scroll para última mensagem
                    binding.recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1);
                } else {
                    Toast.makeText(getContext(), "Erro ao receber resposta", Toast.LENGTH_SHORT).show();
                    // remove a mensagem do usuário que não teve resposta
                    if (!chatAtual.isEmpty()) {
                        chatAtual.remove(chatAtual.size() - 1);
                        chatAdapter.deletarMensagem();
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable throwable) {
                // **VERIFICAÇÃO DE SEGURANÇA CONTRA NullPointerException**
                if (binding == null) {
                    return;
                }

                if (tentativa < 2) { // Tenta no máximo 3 vezes (0, 1, 2)
                    enviarParaFastAPI(chatRequest, tentativa + 1);
                } else {
                    binding.btEnviar.setEnabled(true); // Re-habilitar o botão após a falha final

                    // após 3 tentativas, mostra erro e remove a mensagem que foi enviada
                    Toast.makeText(getContext(), "Não foi possível enviar essa mensagem", Toast.LENGTH_SHORT).show();

                    if (!chatAtual.isEmpty()) {
                        chatAtual.remove(chatAtual.size() - 1);
                        chatAdapter.deletarMensagem();
                    }
                }
            }
        });
    }

    private void limparChat(int tentativas) {
        int finalTentativas = tentativas;
        apiManagerSpring.iniciarServidor(requireActivity(), () -> {
            chatAPISpring.limparChat(idUser).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // **VERIFICAÇÃO DE SEGURANÇA CONTRA NullPointerException**
                    if (binding == null) {
                        return;
                    }

                    binding.imgLixeira.setEnabled(true);

                    if (response.isSuccessful()) {
                        chatAtual.clear();
                        chatAdapter.clearMessages();
                        mostrarTelaVazia();
                        carregarChat();
                    }
                    else {
                        Toast.makeText(getContext(), "Erro ao limpar chat", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable throwable) {
                    // **VERIFICAÇÃO DE SEGURANÇA CONTRA NullPointerException**
                    if (binding == null) {
                        return;
                    }

                    if (finalTentativas < 2) { // Tenta no máximo 3 vezes
                        limparChat(finalTentativas + 1);
                    } else {
                        binding.imgLixeira.setEnabled(true);
                        // após 3 tentativas, mostra erro
                        Toast.makeText(getContext(), "Não foi possível limpar o chat", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        });
    }

    private void mostrarTelaVazia() {
        if (binding == null) {
            return;
        }
        binding.recyclerViewChat.setVisibility(View.GONE);
        binding.imgLixeira.setVisibility(View.GONE);
        binding.imgNutriaChat.setVisibility(View.VISIBLE);
    }

    private void mostrarDialogConfirmacao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.popup_limpar_chat, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        ImageView btnFechar = dialogView.findViewById(R.id.btnFechar);
        Button btnExcluir = dialogView.findViewById(R.id.btnExcluir);

        btnFechar.setOnClickListener(v -> dialog.dismiss());
        btnExcluir.setOnClickListener(v -> {
            binding.imgLixeira.setEnabled(false);
            limparChat(0);
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Garante que o binding seja nullificado para evitar vazamento de memória e o NullPointerException
        binding = null;
    }
}