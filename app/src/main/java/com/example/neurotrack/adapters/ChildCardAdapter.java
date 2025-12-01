package com.example.neurotrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.neurotrack.R;
import com.example.neurotrack.models.ChildSummary;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ChildCardAdapter extends RecyclerView.Adapter<ChildCardAdapter.ChildViewHolder> {

    private List<ChildSummary> children;
    private OnChildClickListener listener;

    public interface OnChildClickListener {
        void onChildClick(ChildSummary child);
    }

    public ChildCardAdapter(List<ChildSummary> children, OnChildClickListener listener) {
        this.children = children;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_child_card, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        ChildSummary child = children.get(position);
        holder.bind(child, listener);
    }

    @Override
    public int getItemCount() {
        return children != null ? children.size() : 0;
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewChildAvatar;
        TextView textViewChildName;
        TextView textViewChildPoints;
        TextView textViewTaskProgress;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewChildAvatar = itemView.findViewById(R.id.imageViewChildAvatar);
            textViewChildName = itemView.findViewById(R.id.textViewChildName);
            textViewChildPoints = itemView.findViewById(R.id.textViewChildPoints);
            textViewTaskProgress = itemView.findViewById(R.id.textViewTaskProgress);
        }

        public void bind(ChildSummary child, OnChildClickListener listener) {
            textViewChildName.setText(child.getName());

            String avatarUrl = child.getAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(avatarUrl);
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Glide.with(itemView.getContext())
                            .load(uri)
                            .placeholder(R.drawable.avatar_child)
                            .error(R.drawable.avatar_child)
                            .into(imageViewChildAvatar);
                }).addOnFailureListener(e -> {
                    Glide.with(itemView.getContext())
                            .load(R.drawable.avatar_child)
                            .into(imageViewChildAvatar);
                });
            } else {
                Glide.with(itemView.getContext())
                        .load(R.drawable.avatar_child)
                        .into(imageViewChildAvatar);
            }

            textViewChildPoints.setText("⭐ " + child.getTotalPoints() + " pts");

            textViewTaskProgress.setText("✓ " + child.getTasksCompletedToday() + "/" + child.getTotalTasksToday());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChildClick(child);
                }
            });
        }
    }
}

