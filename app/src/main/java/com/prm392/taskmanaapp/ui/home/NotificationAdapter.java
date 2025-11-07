package com.prm392.taskmanaapp.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.taskmanaapp.R;
import com.prm392.taskmanaapp.data.Notification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications;
    private OnNotificationClickListener listener;
    private OnNotificationActionListener actionListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public interface OnNotificationActionListener {
        void onAcceptInvite(Notification notification);
        void onDeclineInvite(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    public void setActionListener(OnNotificationActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.tvTitle.setText(notification.getTitle());
        holder.tvContent.setText(notification.getContent());
        holder.tvTime.setText(formatTime(notification.getCreatedAt()));

        // Show indicator only for unread notifications
        if ("UNREAD".equals(notification.getStatus())) {
            holder.indicator.setVisibility(View.VISIBLE);
        } else {
            holder.indicator.setVisibility(View.GONE);
        }

        // Show action buttons for PROJECT_INVITE notifications that are unread
        if ("PROJECT_INVITE".equals(notification.getType()) && "UNREAD".equals(notification.getStatus())) {
            holder.actionButtonsLayout.setVisibility(View.VISIBLE);
            holder.btnAccept.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onAcceptInvite(notification);
                }
            });
            holder.btnDecline.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onDeclineInvite(notification);
                }
            });
        } else {
            holder.actionButtonsLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    public void updateNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    private String formatTime(String createdAt) {
        if (createdAt == null || createdAt.isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(createdAt);
            if (date == null) return "";

            long diff = System.currentTimeMillis() - date.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) {
                return days + " ngày trước";
            } else if (hours > 0) {
                return hours + " giờ trước";
            } else if (minutes > 0) {
                return minutes + " phút trước";
            } else {
                return "Vừa xong";
            }
        } catch (ParseException e) {
            return createdAt;
        }
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvContent;
        TextView tvTime;
        View indicator;
        LinearLayout actionButtonsLayout;
        Button btnAccept;
        Button btnDecline;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvContent = itemView.findViewById(R.id.tvNotificationContent);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
            indicator = itemView.findViewById(R.id.notificationIndicator);
            actionButtonsLayout = itemView.findViewById(R.id.actionButtonsLayout);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onNotificationClick(notifications.get(getAdapterPosition()));
                }
            });
        }
    }
}

