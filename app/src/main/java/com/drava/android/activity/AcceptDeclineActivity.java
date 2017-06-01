package com.drava.android.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.drava.android.R;
import com.drava.android.activity.mentor_mentee.MetaParser;
import com.drava.android.base.BaseActivity;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.ui.DravaTextView;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.DividerItemDecoration;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcceptDeclineActivity extends BaseActivity {
    private  String ACCEPT="1",DECLINE="2";
    private RecyclerView recyclerView;
    private TextView txtEmptyView;
    private List<InviteListParser.InviteList> inviteLists = new ArrayList<>();
    private InviteListAdapter inviteListAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_decline);
        init();
        setUpDefaults();
        setUpEvents();
    }

    private void init() {
        recyclerView = (RecyclerView) findViewById(R.id.invite_list);
        txtEmptyView = (DravaTextView) findViewById(R.id.empty_view);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.setCancelable(false);
    }

    private void setUpDefaults() {
        setToolbar("Pending List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setStatusBarColor();

        inviteListAdapter = new InviteListAdapter(this,inviteLists);
        recyclerView.setAdapter(inviteListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Drawable drawable = ContextCompat.getDrawable(this,R.drawable.cl_recycler_view_divider);
        recyclerView.addItemDecoration(new DividerItemDecoration(drawable));
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.theme_color));
        getInviteList(true);
    }

    private void setUpEvents(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getInviteList(false);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void acceptDeclineWebservice(InviteListParser.InviteList inviteList, String type){
        mProgressDialog.show();
        getApp().getRetrofitInterface().acceptDecline(inviteList.InviteId, type, inviteList.UserId).enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                getInviteList(false);
                MetaParser metaParser = new Gson().fromJson(content, MetaParser.class);
                if(metaParser.meta.code.equals("201")||(metaParser.meta.code.equals("200"))){
                }else{
                    AlertUtils.showAlert(AcceptDeclineActivity.this, getString(R.string.str_invite_accept_decline));
                }
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
            }
        });
    }

    private void getInviteList(boolean show){
        if (show) {
            mProgressDialog.show();
        }
        getApp().getRetrofitInterface().inviteList().enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                super.onResponse(call, response);
                if (mProgressDialog.isShowing()){
                    mProgressDialog.dismiss();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                super.onFailure(call, t);
                if (mProgressDialog.isShowing()){
                    mProgressDialog.dismiss();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                if (mProgressDialog.isShowing()){
                    mProgressDialog.dismiss();
                }
                swipeRefreshLayout.setRefreshing(false);
                InviteListParser inviteListParser = new Gson().fromJson(content,InviteListParser.class);
                if (inviteListParser.meta.code.equals("200")){
                    recyclerView.setVisibility(View.VISIBLE);
                    txtEmptyView.setVisibility(View.GONE);
                    inviteListAdapter.updateList(inviteListParser.InviteList);
                }else {
                    recyclerView.setVisibility(View.GONE);
                    txtEmptyView.setVisibility(View.VISIBLE);
                    txtEmptyView.setText(inviteListParser.meta.errorMessage);
                }
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
                if (mProgressDialog.isShowing()){
                    mProgressDialog.dismiss();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public class InviteListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private List<InviteListParser.InviteList> inviteLists;
        private Context mContext;

        public InviteListAdapter(Context context,List<InviteListParser.InviteList> inviteLists) {
            this.inviteLists = inviteLists;
            mContext = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.invite_list_item,parent,false);
            return new InviteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            InviteViewHolder inviteViewHolder = (InviteViewHolder) holder;
            inviteViewHolder.txtUserName.setText(inviteLists.get(position).FirstName+" "+inviteLists.get(position).LastName);
            inviteViewHolder.txtEmail.setText(inviteLists.get(position).Email);
            inviteViewHolder.txtAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    acceptDeclineWebservice(inviteLists.get(position),ACCEPT);
                }
            });
            inviteViewHolder.txtDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    acceptDeclineWebservice(inviteLists.get(position),DECLINE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return inviteLists.size();
        }

        public void updateList(List<InviteListParser.InviteList> inviteLists){
            this.inviteLists = inviteLists;
            notifyDataSetChanged();
        }

        private class InviteViewHolder extends RecyclerView.ViewHolder{
            TextView txtUserName,txtEmail,txtAccept,txtDecline;
            ImageView imgAccept,imgDecline;

            public InviteViewHolder(View itemView) {
                super(itemView);
                txtUserName = (TextView) itemView.findViewById(R.id.txt_user_name);
                txtEmail = (TextView) itemView.findViewById(R.id.txt_email);
                txtAccept = (TextView) itemView.findViewById(R.id.accept);
                txtDecline = (TextView) itemView.findViewById(R.id.decline);
            }
        }
    }
}
