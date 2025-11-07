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
        
        // Set priority with color
        String priority = task.getPriority();
        holder.tvPriority.setText(priority);
        if (priority != null) {
            switch (priority.toUpperCase()) {
                case "HIGH":
                    holder.tvPriority.setBackgroundTintList(holder.itemView.getContext().getColorStateList(R.color.priority_high));
                    break;
                case "MEDIUM":
                    holder.tvPriority.setBackgroundTintList(holder.itemView.getContext().getColorStateList(R.color.priority_medium));
                    break;
                case "LOW":
                    holder.tvPriority.setBackgroundTintList(holder.itemView.getContext().getColorStateList(R.color.priority_low));
                    break;
            }
        }
        
        // Set status with color
        String status = task.getStatus();
        holder.tvStatus.setText(status);
        if (status != null) {
            switch (status.toUpperCase()) {
                case "DONE":
                    holder.tvStatus.setBackgroundTintList(holder.itemView.getContext().getColorStateList(R.color.status_done));
                    break;
                case "IN_PROGRESS":
                    holder.tvStatus.setBackgroundTintList(holder.itemView.getContext().getColorStateList(R.color.status_in_progress));
                    break;
                case "TODO":
                default:
                    holder.tvStatus.setBackgroundTintList(holder.itemView.getContext().getColorStateList(R.color.status_todo));
                    break;
            }
        }
        
        if (task.getAssignedName() != null && !task.getAssignedName().isEmpty()) {
            holder.tvAssignedTo.setText("Giao cho: " + task.getAssignedName());
        } else {
            holder.tvAssignedTo.setText("Chưa giao cho ai");
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

