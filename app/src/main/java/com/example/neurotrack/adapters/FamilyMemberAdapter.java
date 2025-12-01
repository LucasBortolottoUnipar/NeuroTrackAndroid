package com.example.neurotrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.neurotrack.R;
import com.example.neurotrack.models.ChildSummary;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class FamilyMemberAdapter extends RecyclerView.Adapter<FamilyMemberAdapter.MemberViewHolder> {

    private Context context;
    private List<ChildSummary> members;
    private OnMemberActionListener listener;

    public interface OnMemberActionListener {
        void onEditClick(ChildSummary member);
        void onDeleteClick(ChildSummary member);
        void onAddTaskClick(ChildSummary member);
        void onViewTasksClick(ChildSummary member);
    }

    public FamilyMemberAdapter(Context context, OnMemberActionListener listener) {
        this.context = context;
        this.members = new ArrayList<>();
        this.listener = listener;
    }

    public void setMembers(List<ChildSummary> members) {
        this.members = members != null ? members : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_family_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        ChildSummary member = members.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgAvatar;
        private TextView tvNome;
        private TextView tvInfo;
        private ImageButton btnAdicionarTarefa;
        private ImageButton btnEditar;
        private ImageButton btnExcluir;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            btnAdicionarTarefa = itemView.findViewById(R.id.btnAdicionarTarefa);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }

        public void bind(ChildSummary member) {
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewTasksClick(member);
                }
            });

            tvNome.setText(member.getName());

            String info = member.getTotalPoints() + " pontos";
            if (member.getTotalTasksToday() != null && member.getTotalTasksToday() > 0) {
                info += " • " + member.getTasksCompletedToday() + "/" + member.getTotalTasksToday() + " tarefas";
            }
            tvInfo.setText(info);

            loadAvatar(member.getId());

            btnAdicionarTarefa.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddTaskClick(member);
                }
            });

            btnEditar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(member);
                }
            });

            btnExcluir.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(member);
                }
            });
        }

        private void loadAvatar(Long childId) {
            ChildSummary member = members.get(getAdapterPosition());
            if (member != null && member.getAvatar() != null && !member.getAvatar().isEmpty()) {
                Glide.with(context)
                        .load(member.getAvatar())
                        .placeholder(R.drawable.avatar_child)
                        .error(R.drawable.avatar_child)
                        .circleCrop()
                        .into(imgAvatar);
            } else {
                try {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference avatarRef = storage.getReference()
                            .child("avatars")
                            .child("child_" + childId + ".jpg");

                    avatarRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                Glide.with(context)
                                        .load(uri.toString())
                                        .placeholder(R.drawable.avatar_child)
                                        .error(R.drawable.avatar_child)
                                        .circleCrop()
                                        .into(imgAvatar);
                            })
                            .addOnFailureListener(e -> {
                                Glide.with(context)
                                        .load(R.drawable.avatar_child)
                                        .circleCrop()
                                        .into(imgAvatar);
                            });
                } catch (Exception e) {
                    Glide.with(context)
                            .load(R.drawable.avatar_child)
                            .circleCrop()
                            .into(imgAvatar);
                }
            }
        }
    }
}

