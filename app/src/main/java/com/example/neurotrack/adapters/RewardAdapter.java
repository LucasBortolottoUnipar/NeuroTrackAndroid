package com.example.neurotrack.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neurotrack.R;
import com.example.neurotrack.models.RewardCatalog;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder> {

    private Context context;
    private List<RewardCatalog> rewards;
    private OnEditClickListener editListener;
    private OnDeleteClickListener deleteListener;
    private Set<Long> redeemedRewardIds = Collections.emptySet();

    public interface OnEditClickListener {
        void onEditClick(RewardCatalog reward);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(RewardCatalog reward);
    }

    public RewardAdapter(Context context, List<RewardCatalog> rewards,
                         OnEditClickListener editListener, OnDeleteClickListener deleteListener) {
        this.context = context;
        this.rewards = rewards;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reward, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        RewardCatalog reward = rewards.get(position);

        holder.tvRewardName.setText(reward.getName());
        holder.tvRewardDescription.setText(reward.getDescription());
        holder.tvRewardPoints.setText(reward.getCostPoints() + " pontos");

        boolean isRedeemed = redeemedRewardIds.contains(reward.getRewardId());
        CardView card = (CardView) holder.itemView;
        if (isRedeemed) {
            card.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
        } else {
            card.setCardBackgroundColor(Color.parseColor("#FAFAFA"));
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEditClick(reward);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(reward);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rewards.size();
    }

    public void setRewards(List<RewardCatalog> newRewards) {
        this.rewards = newRewards;
        notifyDataSetChanged();
    }

    public void setRedeemedRewardIds(Set<Long> redeemedIds) {
        this.redeemedRewardIds = redeemedIds != null ? redeemedIds : Collections.emptySet();
        notifyDataSetChanged();
    }

    static class RewardViewHolder extends RecyclerView.ViewHolder {
        TextView tvRewardName, tvRewardDescription, tvRewardPoints;
        ImageButton btnEdit, btnDelete;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRewardName = itemView.findViewById(R.id.tvRewardName);
            tvRewardDescription = itemView.findViewById(R.id.tvRewardDescription);
            tvRewardPoints = itemView.findViewById(R.id.tvRewardPoints);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

