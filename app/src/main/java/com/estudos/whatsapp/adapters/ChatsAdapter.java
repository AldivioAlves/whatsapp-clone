package com.estudos.whatsapp.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.estudos.whatsapp.R;
import com.estudos.whatsapp.models.ChatModel;
import com.estudos.whatsapp.models.GroupModel;
import com.estudos.whatsapp.models.UserModel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.MyViewHolder> {

    private List<ChatModel> chats;

    public ChatsAdapter(List<ChatModel> chats) {
        this.chats = chats;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_contact, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ChatModel chat = chats.get(position);
        UserModel user = chat.getUserToShowInChat();
        holder.lastMessage.setText(chat.getLastMessage());

        if (chat.getIsGroup().equals("true")) {
            GroupModel group = chat.getGroup();
            holder.name.setText( group.getName());
            if (group.getPhoto() != null) {
                Glide.with(holder.itemView.getContext())
                        .load(Uri.parse(group.getPhoto()))
                        .into(holder.imageProfile);
            } else {
                holder.imageProfile.setImageResource(R.drawable.default_user_image);
            }

        } else {
            if(user!=null){
                holder.name.setText(user.getName());
                if (user.getPhoto() != null) {
                    Glide.with(holder.itemView.getContext())
                            .load(Uri.parse(user.getPhoto()))
                            .into(holder.imageProfile);
                } else {
                    holder.imageProfile.setImageResource(R.drawable.default_user_image);
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name, lastMessage;
        CircleImageView imageProfile;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            lastMessage = itemView.findViewById(R.id.info_user);
            imageProfile = itemView.findViewById(R.id.image_profile);
        }
    }

    public List<ChatModel> getChats(){
        return this.chats;
    }

}
