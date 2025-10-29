package com.bea.nutria.ui.Chat;

import android.content.Context;
import android.content.SharedPreferences;

import android.app.AlertDialog;
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
import com.bea.nutria.api.ChatAPI;
import com.bea.nutria.api.conexaoApi.ConexaoAPI;
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
    private ConexaoAPI apiManager;
    private ChatAPI chatAPI;
    private int idUser = 0;
//    private static final String url = "https://nutria-fast-api.koyeb.app/";
    private static final String url = "https://api-spring-mongodb.onrender.com/";

    private String prefsName = "nutria_prefs";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNutriaBinding.inflate(inflater, container, false);

//        SharedPreferences prefs = requireActivity().getSharedPreferences(prefsName, Context.MODE_PRIVATE);
//        idUser = prefs.getInt("id", 1);
        idUser = 999;

        if(idUser == 0){
            mostrarTelaVazia();
        }else{
            inicializarChat();
            setupRecyclerView();
            setupListeners();
            carregarChatDoServidor();
        }

        return binding.getRoot();
    }

    private void inicializarChat() {
        chatAtual = new ArrayList<>();
        apiManager = new ConexaoAPI(url);
        chatAPI = apiManager.getApi(ChatAPI.class);
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        binding.recyclerViewChat.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewChat.setAdapter(chatAdapter);
    }

    private void setupListeners() {
//        binding.btEnviar.setOnClickListener(v -> enviarMensagem());
        binding.imgLixeira.setOnClickListener(v -> mostrarDialogConfirmacao());
    }

    private void carregarChatDoServidor() {
        apiManager.iniciarServidor(requireActivity(), () -> {
            chatAPI.listarChat(idUser).enqueue(new Callback<List<String>>() {
                @Override
                public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<String> chatInteiro = response.body();

                        if (!chatInteiro.isEmpty()) {
                            chatAtual.clear();
                            chatAtual.addAll(chatInteiro);
                            chatAdapter.carregarChat(chatInteiro);

                            binding.recyclerViewChat.setVisibility(View.VISIBLE);
                            binding.imgLixeira.setVisibility(View.VISIBLE);
                            binding.imgBalao.setVisibility(View.GONE);
                            binding.imgTria.setVisibility(View.GONE);
                        } else {
                            mostrarTelaVazia();
                        }
                    } else {
                        mostrarTelaVazia();
                    }
                }

                @Override
                public void onFailure(Call<List<String>> call, Throwable throwable) {
                    mostrarTelaVazia();
                    Toast.makeText(getContext(), "Erro ao carregar chat", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

//    private void enviarMensagem() {
//        String mensagem = binding.editTextPergunta.getText().toString().trim();
//
//        if (!mensagem.isEmpty()) {
//            // Adiciona mensagem do usuário localmente
//            chatAtual.add(mensagem);
//            chatAdapter.addMessage(mensagem);
//            binding.editTextPergunta.setText("");
//
//            // Scroll para última mensagem
//            binding.recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1);
//
//            // Mostrar o chat
//            binding.recyclerViewChat.setVisibility(View.VISIBLE);
//            binding.imgLixeira.setVisibility(View.VISIBLE);
//            binding.imgBalao.setVisibility(View.GONE);
//            binding.imgTria.setVisibility(View.GONE);
//
//            // Enviar para o backend e receber resposta
//            enviarParaBackend(mensagem);
//        }
//    }

//    private void enviarParaBackend(String pergunta) {
//        ChatRequest request = new ChatRequest(pergunta, idUser);
//
//        chatAPI.enviarMensagem(request).enqueue(new Callback<ChatResponse>() {
//            @Override
//            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    String respostaBot = response.body().getResposta();
//
//                    // Adiciona resposta do bot
//                    chatAtual.add(respostaBot);
//                    chatAdapter.addMessage(respostaBot);
//
//                    // Scroll para última mensagem
//                    binding.recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1);
//                } else {
//                    Toast.makeText(getContext(), "Erro ao receber resposta", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ChatResponse> call, Throwable throwable) {
//                Toast.makeText(getContext(), "Falha na comunicação: " + throwable.getMessage(),
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void limparChat() {
        chatAdapter.clearMessages();
        chatAtual.clear();
        mostrarTelaVazia();
    }

    private void mostrarTelaVazia() {
        binding.recyclerViewChat.setVisibility(View.GONE);
        binding.imgLixeira.setVisibility(View.GONE);
        binding.imgBalao.setVisibility(View.VISIBLE);
        binding.imgTria.setVisibility(View.VISIBLE);
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
            limparChat();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}