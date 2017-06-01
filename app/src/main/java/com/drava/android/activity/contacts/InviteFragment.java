package com.drava.android.activity.contacts;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drava.android.R;
import com.drava.android.activity.AcceptDeclineActivity;
import com.drava.android.activity.mentor_mentee.MetaParser;
import com.drava.android.base.BaseFragment;
import com.drava.android.parser.InviteParser;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.TextUtils;
import com.google.gson.Gson;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class InviteFragment extends BaseFragment implements InviteMentorMentee {

    private ArrayList<ContactBean> emailList;
    private ArrayList<ContactBean> contactNumberList;

    private RecyclerView rvContactList;
    private ImageButton imgMobile, imgEmail, imgCustom;
    private ContactsAdapter mAdapterEmail;
    HashMap<String, ArrayList<ContactBean>> hmap;
    private ImageView imgArrowPhone, imgArrowEmail;
    private int TAB_SELECETED = 2;
    private int EMAIL_TAB = 1;
    private int MOBILE_TAB = 2;
    private int CUSTOM_TAB = 3;
    private final int REQ_CONTACTS = 1;
    private int PREV_TAB = 2;
    private Toolbar toolbar;
    private TextView txtInviteDesc;
    private SearchView searchView;
    private String customEmail;
    private TextInputLayout txtInputEmail;
    private TextView txtNoContacts;
    private ProgressDialog progDailog;
    private RelativeLayout rlvInviteLayoutIcons, rlvInviteLayoutIconsDivider, tutorialLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_get_contacts, container, false);
        init(view);
        setupDefaults();
        setupEvent();
        return view;
    }

    private void init(View view){
        hmap = new HashMap<>();
        emailList = new ArrayList<>();
        contactNumberList = new ArrayList<>();

        toolbar = (Toolbar)view.findViewById(R.id.default_toolbar);
        rvContactList = (RecyclerView)view.findViewById(R.id.contact);
        imgMobile = (ImageButton)view.findViewById(R.id.img_mobile);
        imgEmail = (ImageButton)view.findViewById(R.id.img_email);
        imgCustom = (ImageButton)view.findViewById(R.id.img_custom);
        imgArrowPhone = (ImageView)view.findViewById(R.id.img_arrow_phone);
        imgArrowEmail = (ImageView)view.findViewById(R.id.img_arrow_email);
        searchView = (SearchView)view.findViewById(R.id.search_view);
        txtInviteDesc = (TextView)view.findViewById(R.id.txt_invite_desc);
        txtNoContacts = (TextView)view.findViewById(R.id.txt_no_contacts);
        rlvInviteLayoutIcons = (RelativeLayout)view.findViewById(R.id.rlv_invite_icons);
        rlvInviteLayoutIconsDivider = (RelativeLayout)view.findViewById(R.id.rlv_invite_icons_divider);
        tutorialLayout = (RelativeLayout)view.findViewById(R.id.tutorial_get_contacts);
        mAdapterEmail = new ContactsAdapter(getActivity(), emailList, this);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rvContactList.setLayoutManager(mLayoutManager);
        rvContactList.setHasFixedSize(true);
        rvContactList.setAdapter(mAdapterEmail);

        progDailog = new ProgressDialog(getActivity());
        progDailog.setMessage("Fetching Contacts... ");
        progDailog.setIndeterminate(false);
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDailog.setCancelable(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, REQ_CONTACTS);
            } else {
                new GetContactsTask(false).execute();
            }
        } else {
            if (emailList.size() <= 0 || contactNumberList.size() <= 0) {
                new GetContactsTask(false).execute();
            }
        }
        if (getApp().getUserPreference().getMentorOrMentee().equals(MENTOR)){
            txtInviteDesc.setText(getString(R.string.str_invite_friends));
        }else {
            txtInviteDesc.setText(getString(R.string.invite_a_mentor));
        }
    }

    private void setupDefaults(){
        setHasOptionsMenu(true);
        toolbar.setVisibility(View.GONE);
        resetDrawable(MOBILE_TAB);
        if(!getApp().getUserPreference().getIsTutInviteContactsViewed()){
            getApp().getUserPreference().setIsTutInviteContactsViewed(true);
        }else{
            tutorialLayout.setVisibility(View.GONE);
        }
    }

    private void setupEvent() {

        imgMobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TAB_SELECETED == MOBILE_TAB)
                    return;
                TAB_SELECETED = MOBILE_TAB;
                resetDrawable(MOBILE_TAB);
                if(contactNumberList.isEmpty()) {
                    new GetContactsTask(false).execute();
                }else{
                    callInviteWebService();
                }
            }
        });

        imgEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TAB_SELECETED == EMAIL_TAB)
                    return;
                TAB_SELECETED = EMAIL_TAB;
                resetDrawable(EMAIL_TAB);
                if(emailList.isEmpty()) {
                    new GetContactsTask(true).execute();
                }else{
                    callInviteWebService();
                }
            }
        });

        imgCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PREV_TAB = TAB_SELECETED;
                TAB_SELECETED = CUSTOM_TAB;
                showCustomDialog();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                DeviceUtils.hideSoftKeyboard(getActivity());
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapterEmail.getFilter().filter(newText);
                return false;
            }
        });


        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if(!queryTextFocused) {
                    rlvInviteLayoutIcons.setVisibility(View.VISIBLE);
                    rlvInviteLayoutIconsDivider.setVisibility(View.VISIBLE);
                    searchView.clearFocus();
                    searchView.setIconified(true);
                }else{
                    rlvInviteLayoutIcons.setVisibility(View.GONE);
                    rlvInviteLayoutIconsDivider.setVisibility(View.GONE);
                }
            }
        });

        tutorialLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tutorialLayout.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_pending_invite, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.item_pending_invite){
            startActivity(new Intent(getActivity(),AcceptDeclineActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQ_CONTACTS){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                new GetContactsTask(false).execute();
            }
        }else{
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.CALL_PHONE)){
                promptSettings("Read Contacts");
            } else {
                promptSettings("Read Contacts");
            }
        }
    }

    private void getEmailContactList(){
        Cursor emailCursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Email.DATA,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Email.PHOTO_URI}, null, null, ContactsContract.CommonDataKinds.Email.DISPLAY_NAME + " ASC");
        try {
            if(emailCursor.getCount()>0)
                while (emailCursor != null && emailCursor.moveToNext()) {
                    String email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    String name = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String image_uri = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.PHOTO_URI));

                    if (email != null && name != null) {
                        boolean found = false;
                        for (ContactBean singleItem : emailList) {
                            if (singleItem.email.equals(email)) {
                                found = true;
                            }
                        }
                        if (!found) {
                            emailList.add(new ContactBean(name, email, image_uri, DEFAULT_USER_STATUS, DEFAULT_USER_STATUS, false));
                        }
                    }
                }
            callInviteWebService();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(emailCursor != null && !emailCursor.isClosed()){
                emailCursor.close();
            }
        }
    }

    private void getPhoneNumberList() {
        ContentResolver cr = getActivity().getContentResolver();
        String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone._ID,
                        ContactsContract.CommonDataKinds.Phone.PHOTO_URI}, selection, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        try {
            while (phoneCursor != null && phoneCursor.moveToNext()) {
                String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String name = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                String image_uri = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

                String numberFormat = number.replaceAll(" ", "").replaceAll("\\-", "").replaceAll("\\(", "").replaceAll("\\)", "");
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

                try {
                    Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(numberFormat, "");
                    numberFormat = "" + phoneNumber.getNationalNumber();
                } catch (NumberParseException ignored) {
                }

                ArrayList<ContactBean> tempList = new ArrayList<>();
                if (hmap.containsKey(numberFormat)) {
                    for (int i = 0; i < hmap.get(numberFormat).size(); i++) {
                        tempList.add(hmap.get(numberFormat).get(i));
                    }
                    tempList.add(new ContactBean(name, number, image_uri, DEFAULT_USER_STATUS, DEFAULT_USER_STATUS, false));
                    hmap.put(numberFormat, tempList);
                } else {
                    tempList.add(new ContactBean(name, number, image_uri, DEFAULT_USER_STATUS, DEFAULT_USER_STATUS, false));
                    hmap.put(numberFormat, tempList);
                    contactNumberList.add(new ContactBean(name, number, image_uri, DEFAULT_USER_STATUS, DEFAULT_USER_STATUS, false));
                }

                for (int i = 0; i < hmap.get(numberFormat).size(); i++) {
                    Log.d("GetContactsActivity", "HashMap :Iteration : " + i + " : Key :" + numberFormat + " -->Values : " + hmap.get(numberFormat).get(i).email);
                }
            }
            callInviteWebService();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(phoneCursor != null && !phoneCursor.isClosed()){
                phoneCursor.close();
            }
        }
    }

    class GetContactsTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog progDailog;
        private boolean isEmailContact;

        public GetContactsTask(boolean isEmailContact) {
            this.isEmailContact = isEmailContact;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog = new ProgressDialog(getActivity());
            progDailog.setMessage("Fetching Data  ");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(false);
            if(null != getActivity()) {
                progDailog.show();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            if(isEmailContact)
                getEmailContactList();
            else
                getPhoneNumberList();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (null != getActivity()) {
                progDailog.dismiss();
            }
        }
    }

    private void promptSettings(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(String.format(getResources().getString(R.string.denied_title), type));
        builder.setMessage(String.format(getString(R.string.denied_msg), type));
        builder.setPositiveButton("go to Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                goToSettings();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.setCancelable(false);
        builder.show();
    }

    private void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(myAppSettings);
    }

    private void resetDrawable(int selectedTab){
        Log.e("Selected Tab :", " "+selectedTab);
        mAdapterEmail.setSelectedTab(selectedTab);
        searchView.clearFocus();
        imgEmail.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.contacts_btn_normal_bg));
        imgMobile.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.contacts_btn_normal_bg));
        imgCustom.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.contacts_btn_normal_bg));
        if(selectedTab == MOBILE_TAB) {
            imgMobile.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.contacts_btn_selected_bg));
            imgArrowPhone.setVisibility(View.VISIBLE);
            imgArrowEmail.setVisibility(View.INVISIBLE);
        }else if(selectedTab == EMAIL_TAB) {
            imgEmail.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.contacts_btn_selected_bg));
            imgArrowPhone.setVisibility(View.INVISIBLE);
            imgArrowEmail.setVisibility(View.VISIBLE);
        }else {
            imgCustom.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.contacts_btn_selected_bg));
            imgArrowPhone.setVisibility(View.INVISIBLE);
            imgArrowEmail.setVisibility(View.INVISIBLE);
        }
    }

    private void showCustomDialog(){
        View view = getActivity().getLayoutInflater().inflate(R.layout.contacts_custom_dialog, null);
        EditText edtEmail = (EditText)view.findViewById(R.id.edt_email_id);
        txtInputEmail = (TextInputLayout)view.findViewById(R.id.txt_input_email);
        txtInputEmail.setErrorEnabled(true);

        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                customEmail = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                txtInputEmail.setErrorEnabled(true);
            }
        });

        final AlertDialog.Builder dialogBuiler = new AlertDialog.Builder(getActivity());
        dialogBuiler.setTitle(getResources().getString(R.string.str_enter_user_details));
        dialogBuiler.setView(view);
        dialogBuiler.setPositiveButton("Invite", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        dialogBuiler.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogBuiler.create().cancel();
                TAB_SELECETED = PREV_TAB;
                resetDrawable(TAB_SELECETED);
            }
        });

        AlertDialog alertDialog = dialogBuiler.create();
        alertDialog.show();
        Button inviteButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        inviteButton.setOnClickListener(new CustomListener(alertDialog));
    }

    class CustomListener implements View.OnClickListener {
        private Dialog dialog;

        public CustomListener(Dialog dialog) {
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            if(userInfoValidate(customEmail)){
                dialog.dismiss();
                resetDrawable(TAB_SELECETED);
                Log.d("Value :", "Email : "+customEmail);
                callInviteMentorMenteeWebService(customEmail);
                customEmail = null;
            }
        }
    }

    private boolean userInfoValidate(String email){
        if(TextUtils.isEmpty(email)){
            txtInputEmail.setError("Enter Email Id");
            return false;
        } else if(!TextUtils.isEmpty(email) && !TextUtils.isValidEmail(email)){
            txtInputEmail.setError("Invalid Email Id");
            return false;
        }
        return true;
    }

    private void callInviteWebService(){
        ArrayList<String> listEmail = new ArrayList<>();
        final ArrayList<String> listPhone = new ArrayList<>();

        for(ContactBean object:contactNumberList){
            listPhone.add(object.getEmail().toString().trim());
        }

        for(ContactBean object:emailList){
            listEmail.add(object.getEmail().toString().trim());
        }

        JSONArray phoneJson = new JSONArray(listPhone);
        JSONArray emailJson = new JSONArray(listEmail);

        JSONObject jsonObject = new JSONObject();
        try {
            if(TAB_SELECETED == EMAIL_TAB) {
                jsonObject.accumulate("ContactEmail", emailJson);
            }else if(TAB_SELECETED == MOBILE_TAB){
                jsonObject.accumulate("ContactNumber", phoneJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e("object", jsonObject.toString());

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), (jsonObject.toString()));
        getApp().getRetrofitInterface().userInviteStatus(requestBody).enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                Log.e("UserInvite", content);
                InviteParser inviteParser = new Gson().fromJson(content, InviteParser.class);
                if(inviteParser.meta.getCode()!=200 && inviteParser.meta.getCode()!=201 ) {
                    AlertUtils.showAlert(getActivity(), getString(R.string.invite_alert), inviteParser.meta.errorMessage);
                } else {
                    if (TAB_SELECETED == MOBILE_TAB) {
                        if (inviteParser.getInviteDetails().getContactNumber().size() > 0) {
                            for (ContactBean object : contactNumberList) {
                                for (InviteParser.InviteDetails.ContactNumber contactNumber : inviteParser.getInviteDetails().getContactNumber()) {
                                    if (object.getEmail().trim().equalsIgnoreCase(contactNumber.getId())) {
                                        object.setStatus(contactNumber.getInviteStatus());
                                        object.setUserStatus(contactNumber.getUserStatus());
                                    }
                                }
                            }
                            if (contactNumberList.size() > 0) {
                                mAdapterEmail.update(contactNumberList);
                                rvContactList.scrollToPosition(0);
                                showDataView();
                            } else {
                                hideDataView();
                            }
                        }
                    } else if (TAB_SELECETED == EMAIL_TAB) {
                        if (inviteParser.getInviteDetails().getContactEmail().size() > 0) {
                            for (ContactBean object : emailList) {
                                for (InviteParser.InviteDetails.ContactEmail contactEmail : inviteParser.getInviteDetails().getContactEmail()) {
                                    if (object.getEmail().trim().equals(contactEmail.getId())) {
                                        object.setStatus(contactEmail.getInviteStatus());
                                        object.setUserStatus(contactEmail.getUserStatus());
                                    }
                                }
                            }
                            if (emailList.size() > 0) {
                                mAdapterEmail.update(emailList);
                                rvContactList.scrollToPosition(0);
                                showDataView();
                            } else {
                                hideDataView();
                            }
                        }
                    }
                }

            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
                Log.e("UserInvite", message);
            }
        });
    }

    @Override
    public void callInviteMentorMenteeWebService(final String contactId) {
        final ProgressDialog progDailog1 = new ProgressDialog(getActivity());
        progDailog1.setMessage("Inviting User  ");
        progDailog1.setIndeterminate(false);
        progDailog1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDailog1.setCancelable(false);
        progDailog1.show();

        getApp().getRetrofitInterface().inviteMentorMentee(contactId, TAB_SELECETED).enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                Log.e("Invite Mentee", content);
                if (null != getActivity() && progDailog1.isShowing() && isAdded()) {
                    progDailog1.dismiss();
                }
                MetaParser metaParser = new Gson().fromJson(content, MetaParser.class);
                if(metaParser.meta.code.equals("200")||metaParser.meta.code.equals("201")){
                    showInviteSuccessDialog();
                }else{
                    AlertUtils.showAlert(getActivity(), metaParser.meta.errorMessage);
                }

                if(TAB_SELECETED == CUSTOM_TAB){
                    TAB_SELECETED = PREV_TAB;
                    resetDrawable(TAB_SELECETED);
                }else{
                    if(TAB_SELECETED == MOBILE_TAB){
                        for(ContactBean object:contactNumberList){
                            if(object.getEmail().equalsIgnoreCase(contactId)){
                                object.setSelected(true);
                            }
                        }
                    }else if(TAB_SELECETED == EMAIL_TAB){
                        for(ContactBean object:emailList){
                            if(object.getEmail().equalsIgnoreCase(contactId)){
                                object.setSelected(true);
                            }
                        }
                    }
                    mAdapterEmail.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
                Log.e("Invite Mentee", message);
                if (null != getActivity() && progDailog1.isShowing() && isAdded()) {
                    progDailog1.dismiss();
                }
            }
        });
    }

    private void showInviteSuccessDialog(){
        View view = getActivity().getLayoutInflater().inflate(R.layout.invite_success_custom_dialog, null);
        TextView txtOk = (TextView)view.findViewById(R.id.txt_ok);
        TextView txtContent = (TextView)view.findViewById(R.id.txt_invite_popup);
        if(getApp().getUserPreference().getMentorOrMentee().equalsIgnoreCase("MENTEE")){
            txtContent.setText(getResources().getString(R.string.str_invitation_mentor_content));
        }else{
            txtContent.setText(getResources().getString(R.string.str_invitation_mentee_content));
        }
        final AlertDialog.Builder dialogBuiler = new AlertDialog.Builder(getActivity());
        dialogBuiler.setView(view);
        final AlertDialog alertDialog = dialogBuiler.create();
        alertDialog.show();
        txtOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    private void showDataView(){
        searchView.setVisibility(View.VISIBLE);
        rvContactList.setVisibility(View.VISIBLE);
        txtNoContacts.setVisibility(View.GONE);
    }

    private void hideDataView(){
        searchView.setVisibility(View.GONE);
        rvContactList.setVisibility(View.GONE);
        txtNoContacts.setVisibility(View.VISIBLE);
    }
}
