package com.drava.android.activity.notifications;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.drava.android.R;
import com.drava.android.parser.NotificationListParser;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.DateConversion;
import com.drava.android.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

class NotficationAdapter extends RecyclerView.Adapter<NotficationAdapter.NotificationHolder> {

    private ArrayList<NotificationListParser.NotificationTrackingList> notificationTrackingLists;
    private Context context;
    private NotificationDeleteListener notificationDeleteListener;

    public NotficationAdapter(Context context, ArrayList<NotificationListParser.NotificationTrackingList> notificationTrackingLists){
        this.context = context;
        this.notificationTrackingLists = notificationTrackingLists;
    }

    @Override
    public NotificationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list_item, parent, false);
        return new NotificationHolder(view);
    }

    @Override
    public void onBindViewHolder(NotificationHolder holder, final int position) {
        holder.notificationText.setText(notificationTrackingLists.get(position).Message);
        String localtime = DateConversion.getLocalTimeFromGMT(notificationTrackingLists.get(position).DateCreated);
        Date notificationDate = DateConversion.stringToDate(localtime, "yyyy-MM-dd HH:mm:ss");
//        String dateText = TimeUtils.converToTimeForRecentActivity(context, notificationDate.getTime());
        Date currentDate = new Date();
        String dateText;
        if(notificationDate.getYear()== currentDate.getYear() && notificationDate.getMonth() == currentDate.getMonth() && notificationDate.getDate() == currentDate.getDate()){
            long difference = TimeUnit.MILLISECONDS.toSeconds(currentDate.getTime() - notificationDate.getTime());
            long mins = difference/60;
            if(mins >= 60){
                dateText = (mins/60)+" Hours ago";
            }else{
                dateText = mins+" Min ago";
            }

        }else{
            String prefix = DateConversion.formatDate(notificationTrackingLists.get(position).DateCreated, "yyyy-MM-dd hh:mm:ss", "EEEE, MMM dd yyyy");
            String suffix = DateConversion.formatDate(notificationTrackingLists.get(position).DateCreated, "yyyy-MM-dd hh:mm:ss", "hh.mma");
            dateText = prefix+" at "+suffix;
        }
        holder.notificationTime.setText(dateText);
        int notificationImage = R.drawable.ic_mail;
        if(notificationTrackingLists.get(position).Type.equalsIgnoreCase("2")){
            notificationImage = R.drawable.ic_gps;
        } else if(notificationTrackingLists.get(position).Type.equalsIgnoreCase("3")){
            notificationImage = R.drawable.ic_car;
        } else if(notificationTrackingLists.get(position).Type.equalsIgnoreCase("4")){
            notificationImage = R.drawable.ic_phone;
        }
        holder.notificationIcon.setImageResource(notificationImage);

        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertUtils.showAlert(context, context.getString(R.string.str_confirm_alert), context.getString(R.string.str_delelte_notifications), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean status = notificationDeleteListener.deleteNotification(notificationTrackingLists.get(position).NotificationId);
                        notificationTrackingLists.remove(position);
                        notifyDataSetChanged();
                        dialogInterface.dismiss();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }, false);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationTrackingLists.size();
    }

    public void updateNotificaitonList(ArrayList<NotificationListParser.NotificationTrackingList> notificationTrackingListItems){
        this.notificationTrackingLists = notificationTrackingListItems;
        notifyDataSetChanged();
    }

    class NotificationHolder extends RecyclerView.ViewHolder {
        com.drava.android.ui.DravaTextView notificationText, notificationTime;
        ImageView notificationIcon;
        public View view;

        NotificationHolder(View view) {
            super(view);
            this.view = view;
            notificationIcon = (ImageView) view.findViewById(R.id.notification_icon);
            notificationText = (com.drava.android.ui.DravaTextView) view.findViewById(R.id.notification_text);
            notificationTime = (com.drava.android.ui.DravaTextView) view.findViewById(R.id.notification_time);
        }
    }

    public void setNotificationDeleteListener(NotificationDeleteListener notificationDeleteListener){
        this.notificationDeleteListener = notificationDeleteListener;
    }
}
