package com.bea.nutria.ui.Chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bea.nutria.R;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<String> mensagens = new ArrayList<>();

    public void clearMessages() {
        mensagens.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        boolean isUserMessage = (position % 2 == 0);
        holder.bind(mensagens.get(position), isUserMessage);
    }

    public void enviarMensagem(String mensagem) {
        mensagens.add(mensagem);
        notifyDataSetChanged();
    }

    public void deletarMensagem() {
        if (!mensagens.isEmpty()) {
            int ultimaPosicao = mensagens.size() - 1;
            mensagens.remove(ultimaPosicao);
            notifyItemRemoved(ultimaPosicao);
        }
    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

    public void carregarChat(List<String> mensagens) {
        this.mensagens.clear();
        this.mensagens.addAll(mensagens);
        notifyDataSetChanged();
    }

    public void addMessage(String mensagem) {
        mensagens.add(mensagem);
        notifyItemInserted(mensagens.size() - 1);
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layoutUserMessage;
        private LinearLayout layoutBotMessage;
        private TextView txtUserMessage;
        private TextView txtBotMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutUserMessage = itemView.findViewById(R.id.layoutUserMessage);
            layoutBotMessage = itemView.findViewById(R.id.layoutBotMessage);
            txtUserMessage = itemView.findViewById(R.id.txtUserMessage);
            txtBotMessage = itemView.findViewById(R.id.txtBotMessage);
        }

        public void bind(String message, boolean isUserMessage) {
            if (isUserMessage) {
                // mostrar mensagem do usuário
                layoutUserMessage.setVisibility(View.VISIBLE);
                layoutBotMessage.setVisibility(View.GONE);
                txtUserMessage.setText(message);
            } else {
                // mostrar mensagem do bot
                layoutUserMessage.setVisibility(View.GONE);
                layoutBotMessage.setVisibility(View.VISIBLE);
                txtBotMessage.setText(message);
            }
        }
    }
}