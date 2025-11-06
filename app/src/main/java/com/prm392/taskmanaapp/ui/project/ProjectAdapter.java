package com.prm392.taskmanaapp.ui.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.taskmanaapp.R;
import com.prm392.taskmanaapp.data.Project;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<Project> projects;
    private OnProjectClickListener listener;

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
        void onProjectLongClick(Project project);
        void onInviteUserClick(Project project);
    }

    public ProjectAdapter(List<Project> projects, OnProjectClickListener listener) {
        this.projects = projects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);
        holder.tvTitle.setText(project.getTitle());
        holder.tvDescription.setText(project.getDescription());
        if (project.getLeaderName() != null) {
            holder.tvLeader.setText("Leader: " + project.getLeaderName());
        }
    }

    @Override
    public int getItemCount() {
        return projects != null ? projects.size() : 0;
    }

    public void updateProjects(List<Project> projects) {
        this.projects = projects;
        notifyDataSetChanged();
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDescription;
        TextView tvLeader;
        Button btnInviteUser;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvProjectTitle);
            tvDescription = itemView.findViewById(R.id.tvProjectDescription);
            tvLeader = itemView.findViewById(R.id.tvProjectLeader);
            btnInviteUser = itemView.findViewById(R.id.btnInviteUser);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onProjectClick(projects.get(getAdapterPosition()));
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onProjectLongClick(projects.get(getAdapterPosition()));
                    return true;
                }
                return false;
            });

            btnInviteUser.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onInviteUserClick(projects.get(getAdapterPosition()));
                }
            });
        }
    }
}

