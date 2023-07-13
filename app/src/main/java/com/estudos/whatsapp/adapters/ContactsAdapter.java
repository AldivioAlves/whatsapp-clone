package com.estudos.whatsapp.adapters;

import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.estudos.whatsapp.R;
import com.estudos.whatsapp.models.UserModel;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder> {
    private List<UserModel> contacts = new ArrayList<>();

    public ContactsAdapter(List<UserModel> users) {
        this.contacts = users;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View contactItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_contact, parent, false);
        return new MyViewHolder(contactItem);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        UserModel user = contacts.get(position);
        boolean isHeader = TextUtils.isEmpty(user.getEmail());
        holder.email.setText(user.getEmail());
        holder.name.setText(user.getName());
        if (user.getPhoto() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(Uri.parse(user.getPhoto()))
                    .into(holder.imageProfile);
        } else {
            holder.imageProfile.setImageResource(isHeader ?
                    R.drawable.group_icon :
                    R.drawable.default_user_image);
            if(isHeader){
                holder.email.setVisibility(View.GONE);
            }
        }
    }

    public List<UserModel> getContacts(){
        return this.contacts;
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, email;
        CircleImageView imageProfile;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            imageProfile = itemView.findViewById(R.id.image_profile);
            email = itemView.findViewById(R.id.info_user);
        }
    }

}
