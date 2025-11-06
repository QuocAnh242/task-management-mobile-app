package com.prm392.taskmanaapp.ui.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.taskmanaapp.R;
import com.prm392.taskmanaapp.data.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskLongClick(Task task);
    }

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.tvTitle.setText(task.getTitle());
        holder.tvDescription.setText(task.getDescription());
        holder.tvPriority.setText("Priority: " + task.getPriority());
        holder.tvStatus.setText("Status: " + task.getStatus());
        if (task.getAssignedName() != null && !task.getAssignedName().isEmpty()) {
            holder.tvAssignedTo.setText("Assigned to: " + task.getAssignedName());
        } else {
            holder.tvAssignedTo.setText("Unassigned");
        }
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    public void updateTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDescription;
        TextView tvPriority;
        TextView tvStatus;
        TextView tvAssignedTo;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvPriority = itemView.findViewById(R.id.tvTaskPriority);
            tvStatus = itemView.findViewById(R.id.tvTaskStatus);
            tvAssignedTo = itemView.findViewById(R.id.tvTaskAssignedTo);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onTaskClick(tasks.get(getAdapterPosition()));
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onTaskLongClick(tasks.get(getAdapterPosition()));
                    return true;
                }
                return false;
            });
        }
    }
}

