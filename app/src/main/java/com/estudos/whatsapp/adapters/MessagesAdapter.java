package com.estudos.whatsapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.estudos.whatsapp.R;
import com.estudos.whatsapp.models.MessageModel;
import com.estudos.whatsapp.utils.UserUtils;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MyViewHoder> {
    private Context context;
    private List<MessageModel> messages;
    private int TYPE_RECEIVER = 0;
    private int TYPE_USER = 1;
    private int viewType;

    public MessagesAdapter(List<MessageModel> messages, Context context) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public MyViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = null;
        this.viewType = viewType;
        if (viewType == TYPE_RECEIVER) {

            item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_receiver_message_adapter, parent, false);

        } else if (viewType == TYPE_USER) {

            item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_message_adapter, parent, false);
        }
        return new MyViewHoder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHoder holder, int position) {
        MessageModel messageModel = messages.get(position);
        String msg = messageModel.getMessage();
        String image = messageModel.getImage();
        String name = messageModel.getName();
        if (!TextUtils.isEmpty(name)) {
            holder.name.setText(name);
        } else {
            holder.name.setVisibility(View.GONE);
        }
        if (image != null) {
            Uri uri = Uri.parse(image);
            Glide.with(context)
                    .load(uri)
                    .into(holder.image);
            holder.message.setVisibility(View.GONE);
        } else {
            holder.message.setText(msg);
            holder.image.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) { // retorna o tipo da visualização
        ///para cada elemento da lista temos uma posicão
        MessageModel message = messages.get(position);
        String userId = UserUtils.getUserId();
        boolean own = userId.equals(message.getUserId());
        if (own) {
            return TYPE_USER;
        }
        return TYPE_RECEIVER;
    }

    public class MyViewHoder extends RecyclerView.ViewHolder {
        TextView message, name;
        ImageView image;

        public MyViewHoder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.text_message);
            image = itemView.findViewById(R.id.image_message);
            if (viewType == TYPE_RECEIVER) {
                name = itemView.findViewById(R.id.message_user_received_name);
            } else if (viewType == TYPE_USER) {
                name = itemView.findViewById(R.id.message_user_name);
            }
        }
    }

}
