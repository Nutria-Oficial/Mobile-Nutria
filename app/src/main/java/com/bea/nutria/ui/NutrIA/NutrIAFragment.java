package com.bea.nutria.ui.NutrIA;

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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;
import com.bea.nutria.databinding.FragmentNutriaBinding;
import com.bea.nutria.model.Chat;
import java.util.ArrayList;

public class NutrIAFragment extends Fragment {

    private FragmentNutriaBinding binding;
    private ChatAdapter chatAdapter;
    private Chat chatAtual;
    private int indiceResposta = 0;



    // Respostas mocadas
    private final String[] respostasMocadas = {
            "Olá! Claro, estou aqui para ajudar. Qual é a sua dúvida sobre a tabela nutricional?",
            "A tabela nutricional contém informações sobre calorias, proteínas, carboidratos e gorduras.",
            "Posso te ajudar a entender melhor os valores diários recomendados!",
            "Você pode me fazer perguntas específicas sobre qualquer nutriente.",
            "Estou analisando sua pergunta... um momento!",
            "Minha embalagem tem pouco espaço, posso usar QR Code para fornecer a tabela?"
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNutriaBinding.inflate(inflater, container, false);

        inicializarChat();
        setupRecyclerView();
        setupListeners();



        return binding.getRoot();
    }
    private void inicializarChat() {
        chatAtual = new Chat();
        chatAtual.setIdUsuario(1);
        chatAtual.setIndiceChat(0);
        chatAtual.setListaUsuario(new ArrayList<>());
        chatAtual.setListaBot(new ArrayList<>());
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

    private void enviarMensagem() {
        String mensagem = binding.editTextPergunta.getText().toString().trim();

        if (!mensagem.isEmpty()) {
            // Adiciona mensagem do usuário no model
            chatAtual.getListaUsuario().add(mensagem);

            // Adiciona mensagem do usuário no adapter
            ChatMessage userMessage = new ChatMessage(mensagem, true);
            chatAdapter.addMessage(userMessage);

            binding.editTextPergunta.setText("");

            // Scroll para última mensagem
            binding.recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1);

            binding.recyclerViewChat.setVisibility(View.VISIBLE);
            binding.imgLixeira.setVisibility(View.VISIBLE);
            binding.imgBalao.setVisibility(View.GONE);
            binding.imgTria.setVisibility(View.GONE);

            responderImediatamente();
        }
    }

    private void responderImediatamente() {
        // Pega uma resposta mocada
        String resposta = respostasMocadas[indiceResposta];
        indiceResposta = (indiceResposta + 1) % respostasMocadas.length;

        // Adiciona resposta do bot no model
        chatAtual.getListaBot().add(resposta);

        // Adiciona resposta do bot no adapter
        ChatMessage botMessage = new ChatMessage(resposta, false);
        chatAdapter.addMessage(botMessage);

        // Scroll para última mensagem
        binding.recyclerViewChat.scrollToPosition(chatAdapter.getItemCount() - 1);

        // Atualiza o índice do chat
        chatAtual.setIndiceChat(chatAtual.getIndiceChat() + 1);
    }

    private void limparChat() {
        chatAdapter.clearMessages();
        inicializarChat();
        indiceResposta = 0;
    }

    private void mostrarDialogConfirmacao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Infla o layout customizado
        View dialogView = getLayoutInflater().inflate(R.layout.popup_limpar_chat, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        // Pega as views do dialog
        ImageView btnFechar = dialogView.findViewById(R.id.btnFechar);
        Button btnExcluir = dialogView.findViewById(R.id.btnExcluir);

        btnFechar.setOnClickListener(v -> dialog.dismiss());
        btnExcluir.setOnClickListener(v -> {
            limparChat();
            dialog.dismiss();
            binding.recyclerViewChat.setVisibility(View.GONE);
            binding.imgLixeira.setVisibility(View.GONE);
            binding.imgBalao.setVisibility(View.VISIBLE);
            binding.imgTria.setVisibility(View.VISIBLE);
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}