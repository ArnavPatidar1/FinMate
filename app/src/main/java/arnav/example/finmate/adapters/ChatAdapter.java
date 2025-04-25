package arnav.example.finmate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import arnav.example.finmate.R;
import arnav.example.finmate.model.ChatMessage;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 0;
    private static final int TYPE_BOT = 1;

    private List<ChatMessage> messageList;

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).isUser() ? TYPE_USER : TYPE_BOT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_message, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bot_message, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).message.setText(message.getMessage());
        } else {
            ((BotViewHolder) holder).message.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        UserViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.user_message);
        }
    }

    class BotViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        BotViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.bot_message);
        }
    }
}

