package com.bea.nutria.ui.NutrIA;

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

    private List<ChatMessage> messages = new ArrayList<>();

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void clearMessages() {
        messages.clear();
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
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
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

        public void bind(ChatMessage message) {
            if (message.isUser()) {
                // Mostrar mensagem do usuário
                layoutUserMessage.setVisibility(View.VISIBLE);
                layoutBotMessage.setVisibility(View.GONE);
                txtUserMessage.setText(message.getMessage());
            } else {
                // Mostrar mensagem do bot
                layoutUserMessage.setVisibility(View.GONE);
                layoutBotMessage.setVisibility(View.VISIBLE);
                txtBotMessage.setText(message.getMessage());
            }
        }
    }
}