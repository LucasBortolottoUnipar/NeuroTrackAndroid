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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChildRewardAdapter extends RecyclerView.Adapter<ChildRewardAdapter.ChildRewardViewHolder> {

    public interface OnRewardClickListener {
        void onRewardClick(RewardCatalog reward);
    }

    private final Context context;
    private final OnRewardClickListener listener;
    private List<RewardCatalog> rewards;
    private Set<Long> redeemedRewardIds = Collections.emptySet();

    public ChildRewardAdapter(Context context, OnRewardClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.rewards = new ArrayList<>();
    }

    public void setRewards(List<RewardCatalog> rewards) {
        this.rewards = rewards != null ? rewards : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setRedeemedRewardIds(Set<Long> redeemedIds) {
        this.redeemedRewardIds = redeemedIds != null ? redeemedIds : Collections.emptySet();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChildRewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reward, parent, false);
        return new ChildRewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildRewardViewHolder holder, int position) {
        RewardCatalog reward = rewards.get(position);

        holder.tvRewardName.setText(reward.getName());
        holder.tvRewardDescription.setText(reward.getDescription());
        holder.tvRewardPoints.setText(reward.getCostPoints() + " pontos");

        boolean isRedeemed = redeemedRewardIds.contains(reward.getRewardId());
        CardView card = (CardView) holder.itemView;
        if (isRedeemed) {
            card.setCardBackgroundColor(Color.parseColor("#E8F5E9")); // verde claro
        } else {
            card.setCardBackgroundColor(Color.parseColor("#FAFAFA")); // cor padrão do layout
        }

        holder.btnEdit.setVisibility(View.GONE);
        holder.btnDelete.setVisibility(View.GONE);

        if (isRedeemed) {
            holder.itemView.setOnClickListener(null);
        } else {
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRewardClick(reward);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return rewards != null ? rewards.size() : 0;
    }

    static class ChildRewardViewHolder extends RecyclerView.ViewHolder {
        TextView tvRewardName, tvRewardDescription, tvRewardPoints;
        ImageButton btnEdit, btnDelete;

        public ChildRewardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRewardName = itemView.findViewById(R.id.tvRewardName);
            tvRewardDescription = itemView.findViewById(R.id.tvRewardDescription);
            tvRewardPoints = itemView.findViewById(R.id.tvRewardPoints);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

