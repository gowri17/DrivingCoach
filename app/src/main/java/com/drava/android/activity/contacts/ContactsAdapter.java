package com.drava.android.activity.contacts;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.drava.android.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.CustomViewHolder> implements Filterable{
    Context context;
    ArrayList<ContactBean> emailList, originalList;
    View itemView;
    InviteMentorMentee inviteMentorMentee;
    private int selectedTab;

    public ContactsAdapter(Context context, ArrayList<ContactBean> list, InviteMentorMentee inviteMentorMentee) {
        this.context = context;
        this.emailList = list;
        this.originalList = list;
        this.inviteMentorMentee = inviteMentorMentee;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new CustomViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ContactsAdapter.CustomViewHolder holder, final int position) {

        holder.txtEmail.setText(emailList.get(position).email);
        holder.txtName.setText(emailList.get(position).nameEmail);
        switch(emailList.get(position).getInviteStatus()){
            case 0:
                holder.btnInvite.setVisibility(View.VISIBLE);
                holder.txtInviteStatus.setVisibility(View.GONE);
                if(emailList.get(position).isSelected){
                    holder.btnInvite.setText(context.getResources().getString(R.string.str_invited));
                    holder.btnInvite.setEnabled(false);
                    holder.btnInvite.setClickable(false);
                }else{
                    holder.btnInvite.setText(context.getResources().getString(R.string.str_invite));
                    holder.btnInvite.setEnabled(true);
                    holder.btnInvite.setClickable(true);
                }
                break;
            case 1:
                //invited
                holder.btnInvite.setVisibility(View.VISIBLE);
                holder.txtInviteStatus.setVisibility(View.GONE);
                holder.btnInvite.setText(context.getResources().getString(R.string.str_invited));
                holder.btnInvite.setEnabled(false);
                holder.btnInvite.setClickable(false);
                break;
            case 2:
                holder.btnInvite.setVisibility(View.INVISIBLE);
                holder.txtInviteStatus.setVisibility(View.GONE);
                break;
            case 3:
                holder.btnInvite.setVisibility(View.VISIBLE);
                holder.txtInviteStatus.setVisibility(View.GONE);
                if(emailList.get(position).isSelected){
                    holder.btnInvite.setText(context.getResources().getString(R.string.str_reinvite));
                    holder.btnInvite.setEnabled(false);
                    holder.btnInvite.setClickable(false);
                }else{
                    holder.btnInvite.setText(context.getResources().getString(R.string.str_reinvite));
                    holder.btnInvite.setEnabled(true);
                    holder.btnInvite.setClickable(true);
                }
                break;
            case 4:
                holder.btnInvite.setVisibility(View.VISIBLE);
                holder.txtInviteStatus.setVisibility(View.VISIBLE);
                holder.txtInviteStatus.setText(context.getResources().getString(R.string.str_rejected));
                if(emailList.get(position).isSelected){
                    holder.btnInvite.setText(context.getResources().getString(R.string.str_reinvite));
                    holder.btnInvite.setEnabled(false);
                    holder.btnInvite.setClickable(false);
                }else{
                    holder.btnInvite.setText(context.getResources().getString(R.string.str_reinvite));
                    holder.btnInvite.setEnabled(true);
                    holder.btnInvite.setClickable(true);
                }
                break;
            case 5:
                holder.btnInvite.setVisibility(View.GONE);
                holder.txtInviteStatus.setVisibility(View.VISIBLE);
                holder.txtInviteStatus.setText(context.getResources().getString(R.string.str_rejected));
                break;
        }


        holder.txtUserStatus.setVisibility(View.VISIBLE);
        switch (emailList.get(position).getUserStatus()) {
            case 0:
                holder.txtUserStatus.setText("");
                holder.txtUserStatus.setVisibility(View.GONE);
                break;
            case 1:
                holder.txtUserStatus.setText(context.getResources().getString(R.string.str_already_mentor));
                break;
            case 2:
                holder.txtUserStatus.setText(context.getResources().getString(R.string.str_already_mentee));
                break;
            case 3:
                holder.txtUserStatus.setText(context.getResources().getString(R.string.str_my_mentor));
                break;
            case 4:
                holder.txtUserStatus.setText(context.getResources().getString(R.string.str_my_mentee));
                break;
        }

        holder.btnInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inviteMentorMentee.callInviteMentorMenteeWebService(emailList.get(position).getEmail());
                holder.btnInvite.setText(context.getResources().getString(R.string.str_invited));
            }
        });

        String uri = emailList.get(position).profileImage;
        if (!TextUtils.isEmpty(uri)) {
            holder.profileImage.setImageURI(Uri.parse(uri));
        } else {
            holder.profileImage.setImageResource(R.drawable.user);
        }
    }

    @Override
    public int getItemCount() {
        return emailList.size();
    }

    public void update(ArrayList<ContactBean> emailList) {
        this.emailList = emailList;
        this.originalList = emailList;
        notifyDataSetChanged();
    }

    public void updateSearch(ArrayList<ContactBean> filteredList){
        this.emailList = filteredList;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new ContactFilter(ContactsAdapter.this, originalList);
    }

    public void setSelectedTab(int selectedTab){
        this.selectedTab = selectedTab;
    }

    private class ContactFilter extends Filter{
        private ContactsAdapter contactsAdapter;
        private final ArrayList<ContactBean> originalList;
        private ArrayList<ContactBean> filteredList;

        public ContactFilter(ContactsAdapter contactsAdapter, ArrayList<ContactBean> originalList){
            this.contactsAdapter = contactsAdapter;
            this.originalList = originalList;
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            filteredList.clear();
            FilterResults filterResults = new FilterResults();
            if(charSequence.length() == 0){
                filteredList = originalList;
            }else{
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for(ContactBean object : originalList) {
                    if(selectedTab == 1) {
                        if (object.email.toLowerCase().contains(filterPattern.toLowerCase())) {
                            filteredList.add(object);
                        }
                    }else if(selectedTab == 2){
                        if (object.nameEmail.toLowerCase().contains(filterPattern.toLowerCase())) {
                            filteredList.add(object);
                        }
                    }
                }
            }
            filterResults.values = filteredList;
            filterResults.count = filteredList.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            if(filterResults != null){
                contactsAdapter.updateSearch((ArrayList<ContactBean>) filterResults.values);
            }
        }
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtEmail, txtUserStatus, txtInviteStatus;
        CircleImageView profileImage;
        Button btnInvite;

        public CustomViewHolder(View itemView) {
            super(itemView);
            txtName = (TextView) itemView.findViewById(R.id.name);
            txtEmail = (TextView) itemView.findViewById(R.id.data);
            txtUserStatus = (TextView) itemView.findViewById(R.id.txt_user_status);
            profileImage = (CircleImageView) itemView.findViewById(R.id.profile_image);
            btnInvite = (Button) itemView.findViewById(R.id.btn_invite);
            txtInviteStatus = (TextView) itemView.findViewById(R.id.txt_invite);
        }
    }
}
