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

public class MembersSelectedAdapter extends RecyclerView.Adapter<MembersSelectedAdapter.MyViewHolder>{

    private List<UserModel> membersSelected = new ArrayList<>();

    public MembersSelectedAdapter(List<UserModel> membersSelected) {
        this.membersSelected = membersSelected;
    }

    @NonNull
    @Override
    public MembersSelectedAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View contactItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_members_selected, parent, false);
        return new MembersSelectedAdapter.MyViewHolder(contactItem);
    }

    @Override
    public void onBindViewHolder(@NonNull MembersSelectedAdapter.MyViewHolder holder, int position) {
        UserModel user = membersSelected.get(position);
        holder.name.setText(user.getName());
        if (user.getPhoto() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(Uri.parse(user.getPhoto()))
                    .into(holder.imageProfile);
        } else {
            holder.imageProfile.setImageResource(R.drawable.default_user_image);
        }
    }

    @Override
    public int getItemCount() {
        return membersSelected.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        CircleImageView imageProfile;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_profile_member_selected);
            imageProfile = itemView.findViewById(R.id.image_profile_member_selected);
        }
    }
}
