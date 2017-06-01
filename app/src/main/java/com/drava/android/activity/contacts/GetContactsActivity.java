package com.drava.android.activity.contacts;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drava.android.R;
import com.drava.android.activity.HomeActivity;
import com.drava.android.base.AppConstants;
import com.drava.android.base.BaseActivity;
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

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;


public class GetContactsActivity extends BaseActivity implements InviteMentorMentee, AppConstants{

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
    private Menu menu;

    private TextView txtInviteDesc;
    private int PREV_TAB = 2;

    private SearchView searchView;
    private String customPhone, customEmail;
    private TextInputLayout txtInputEmail;
    private TextView txtNoContacts;
    private ProgressDialog progDailog;
    private RelativeLayout rlvInviteLayoutIcons, rlvInviteLayoutIconsDivider, tutorialLayout;
    private boolean isPhoneContactEmpty = true;
    private boolean isEmailContactEmpty = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_contacts);
        init();
        setupDefaults();
        setupEvent();
    }

    private void init() {

        setToolbar(getString(R.string.invite));
        hmap = new HashMap<>();

        emailList = new ArrayList<>();
        contactNumberList = new ArrayList<>();

        rvContactList = (RecyclerView) findViewById(R.id.contact);
        imgMobile = (ImageButton)findViewById(R.id.img_mobile);
        imgEmail = (ImageButton)findViewById(R.id.img_email);
        imgCustom = (ImageButton)findViewById(R.id.img_custom);
        imgArrowPhone = (ImageView)findViewById(R.id.img_arrow_phone);
        imgArrowEmail = (ImageView)findViewById(R.id.img_arrow_email);
        searchView = (SearchView)findViewById(R.id.search_view);

        mAdapterEmail = new ContactsAdapter(this, emailList, this);
        txtInviteDesc = (TextView)findViewById(R.id.txt_invite_desc);
        txtNoContacts = (TextView)findViewById(R.id.txt_no_contacts);
        tutorialLayout = (RelativeLayout)findViewById(R.id.tutorial_get_contacts);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvContactList.setLayoutManager(mLayoutManager);
        rvContactList.setHasFixedSize(true);
        rvContactList.setAdapter(mAdapterEmail);
        rlvInviteLayoutIcons = (RelativeLayout)findViewById(R.id.rlv_invite_icons);
        rlvInviteLayoutIconsDivider = (RelativeLayout)findViewById(R.id.rlv_invite_icons_divider);

        progDailog = new ProgressDialog(GetContactsActivity.this);
        progDailog.setMessage("Fetching Contacts... ");
        progDailog.setIndeterminate(false);
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDailog.setCancelable(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(GetContactsActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(GetContactsActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, REQ_CONTACTS);
            } else {
                new GetContactsTask(GetContactsActivity.this).execute();
            }
        } else {
            if (emailList.size() <= 0 || contactNumberList.size() <= 0)
                new GetContactsTask(GetContactsActivity.this).execute();
        }
        if (getApp().getUserPreference().getMentorOrMentee().equals(MENTOR)){
            txtInviteDesc.setText(getString(R.string.str_invite_friends));
        }else {
            txtInviteDesc.setText(getString(R.string.invite_a_mentor));
        }
    }

    private void setupDefaults(){
        getApp().getUserPreference().setIsContactPageShown(true);
        tutorialLayout.setVisibility(View.GONE);
        Log.e("Page Status", "Visited Get Contacts Screen");
        resetDrawable(MOBILE_TAB);
        mAdapterEmail.update(contactNumberList);
        setStatusBarColor();
        searchView.setQueryHint(getResources().getString(R.string.str_search_user));
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
                    new GetContactsTask(GetContactsActivity.this).execute();
                }else{
                    callInviteWebService();
                }
                /*if(isPhoneContactEmpty){
                    rvContactList.setVisibility(View.GONE);
                    txtNoContacts.setVisibility(View.VISIBLE);
                }else{
                    rvContactList.setVisibility(View.VISIBLE);
                    txtNoContacts.setVisibility(View.GONE);
                }*/
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
                    new GetContactsTask(GetContactsActivity.this).execute();
                }else{
                    callInviteWebService();
                }
                /*if(isEmailContactEmpty){
                    rvContactList.setVisibility(View.GONE);
                    txtNoContacts.setVisibility(View.VISIBLE);
                }else{
                    rvContactList.setVisibility(View.VISIBLE);
                    txtNoContacts.setVisibility(View.GONE);
                }*/
            }
        });

        imgCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PREV_TAB = TAB_SELECETED;
                TAB_SELECETED = CUSTOM_TAB;
                resetDrawable(CUSTOM_TAB);
                showCustomDialog();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                DeviceUtils.hideSoftKeyboard(GetContactsActivity.this);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_skip, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.item_skip){
            Intent intent = new Intent(GetContactsActivity.this, HomeActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQ_CONTACTS){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                new GetContactsTask(GetContactsActivity.this).execute();
            }
        }else{
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.CALL_PHONE)){
                promptSettings("Read Contacts");
            } else {
                promptSettings("Read Contacts");
            }
        }
    }

    private void getContactDetails() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        assert cur != null;
        if (cur.getCount() > 0) {
            try {
                while (cur.moveToNext()) {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor cur1 = cr.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + "= ?",
                            new String[]{id}, null);

                    Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?", new String[]{id}, null);

                    try {
                        while (phones != null && phones.moveToNext()) {
                            String number = null;
                            number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String name = phones.getString(phones.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                            int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                            String image_uri = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Email.PHOTO_URI));

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

                        while (cur1 != null && cur1.moveToNext()) {
                            String email = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)).trim();
                            String name = cur1.getString(cur1.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)).trim();
                            String image_uri = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.PHOTO_URI));

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
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if(cur1 != null && !cur1.isClosed()){
                            cur1.close();
                        }
                        if(phones != null && !phones.isClosed()){
                            phones.close();
                        }
                    }
                    if(!emailList.isEmpty()) isEmailContactEmpty = false;
                    if(!contactNumberList.isEmpty()) isPhoneContactEmpty = false;
                }
                callInviteWebService();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(cur != null && !cur.isClosed()){
                    cur.close();
                }
            }
        }
    }


    class GetContactsTask extends AsyncTask<String, Void, String> {
        private Context context;

        public GetContactsTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!progDailog.isShowing()) {
                progDailog.show();
            }
        }

        @Override
        protected String doInBackground(String... str) {
            getContactDetails();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (null != GetContactsActivity.this) {
                progDailog.dismiss();
            }
        }
    }

    private void promptSettings(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + this.getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(myAppSettings);
    }

    @Override
    public void onBackPressed() {
        if (TAB_SELECETED == MOBILE_TAB) {
            contactNumberList.clear();
            mAdapterEmail.update(contactNumberList);
            rvContactList.scrollToPosition(0);
        } else {
            emailList.clear();
            mAdapterEmail.update(emailList);
            rvContactList.scrollToPosition(0);
        }
        finishAffinity();
        super.onBackPressed();
    }

    private void resetDrawable(int selectedTab){
        mAdapterEmail.setSelectedTab(selectedTab);
        imgEmail.setBackground(ContextCompat.getDrawable(GetContactsActivity.this, R.drawable.contacts_btn_normal_bg));
        imgMobile.setBackground(ContextCompat.getDrawable(GetContactsActivity.this, R.drawable.contacts_btn_normal_bg));
        imgCustom.setBackground(ContextCompat.getDrawable(GetContactsActivity.this, R.drawable.contacts_btn_normal_bg));
        if(selectedTab == MOBILE_TAB) {
            imgMobile.setBackground(ContextCompat.getDrawable(GetContactsActivity.this, R.drawable.contacts_btn_selected_bg));
            imgArrowPhone.setVisibility(View.VISIBLE);
            imgArrowEmail.setVisibility(View.INVISIBLE);
        }else if(selectedTab == EMAIL_TAB) {
            imgEmail.setBackground(ContextCompat.getDrawable(GetContactsActivity.this, R.drawable.contacts_btn_selected_bg));
            imgArrowPhone.setVisibility(View.INVISIBLE);
            imgArrowEmail.setVisibility(View.VISIBLE);
        }else {
            imgCustom.setBackground(ContextCompat.getDrawable(GetContactsActivity.this, R.drawable.contacts_btn_selected_bg));
            imgArrowPhone.setVisibility(View.INVISIBLE);
            imgArrowEmail.setVisibility(View.INVISIBLE);
        }
    }

    private void showCustomDialog(){
        View view = getLayoutInflater().inflate(R.layout.contacts_custom_dialog, null);
//        EditText edtPhone = (EditText)view.findViewById(R.id.edt_phone_no);
        EditText edtEmail = (EditText)view.findViewById(R.id.edt_email_id);
//        edtPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
//        txtInputPhone = (TextInputLayout)view.findViewById(R.id.txt_input_phone);
        txtInputEmail = (TextInputLayout)view.findViewById(R.id.txt_input_email);
        txtInputEmail.setErrorEnabled(true);
//        txtInputPhone.setErrorEnabled(true);

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

//        edtPhone.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                customPhone = charSequence.toString();
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                txtInputPhone.setErrorEnabled(true);
//            }
//        });
        final AlertDialog.Builder dialogBuiler = new AlertDialog.Builder(GetContactsActivity.this);
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
                Log.d("Value :"," Email : "+customEmail);
                callInviteMentorMenteeWebService(customEmail);
                customEmail = null;
            }
        }
    }

    private boolean userInfoValidate(String email){
//        txtInputPhone.setErrorEnabled(false);
//        txtInputEmail.setErrorEnabled(false);
//        if(TextUtils.isEmpty(phone) && TextUtils.isEmpty(email) ){
//            AlertUtils.showAlert(GetContactsActivity.this, "Provide user information");
//            return false;
//        } else if(TextUtils.isEmpty(phone)){
//            txtInputPhone.setError("Enter Phone Number");
//            return false;
//        } else if(!TextUtils.isEmpty(phone) && phone.length()<9){
//        txtInputPhone.setError("Invalid Phone Number");
//        return false;
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
        /*Log.e("object", phoneJson.toString());
        Log.e("object", emailJson.toString());*/

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

//        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), (jsonObject.toString()));
//        getApp().getRetrofitInterface().userInviteStatus(requestBody).enqueue(new RetrofitCallback<ResponseBody>() {
//            @Override
//            public void onSuccessCallback(Call<ResponseBody> call, String content) {
//                super.onSuccessCallback(call, content);
//                Log.e("UserInvite", content);
//                InviteParser inviteParser = new Gson().fromJson(content, InviteParser.class);
//                if(TAB_SELECETED == MOBILE_TAB){
//                    if(inviteParser.getInviteDetails().getContactNumber().size()>0){
//                        for(ContactBean object: contactNumberList){
//                            for(InviteParser.InviteDetails.ContactNumber contactNumber : inviteParser.getInviteDetails().getContactNumber()) {
//                                if(object.getEmail().trim().equalsIgnoreCase(contactNumber.getId())){
//                                    if(contactNumber.getInviteStatus() > 0){
//                                        object.setStatus(contactNumber.getInviteStatus());
//                                        object.setUserStatus(contactNumber.getUserStatus());
//                                    }
//                                }
//                            }
//                        }
//                        mAdapterEmail.update(contactNumberList);
//                        rvContactList.scrollToPosition(0);
//                    }
//                }else if(TAB_SELECETED == EMAIL_TAB){
//                    if(inviteParser.getInviteDetails().getContactEmail().size()>0){
//                        for(ContactBean object: emailList){
//                            for(InviteParser.InviteDetails.ContactEmail contactEmail : inviteParser.getInviteDetails().getContactEmail()) {
//                                if(object.getEmail().trim().equalsIgnoreCase(contactEmail.getId())){
//                                    if(contactEmail.getInviteStatus() > 0){
//                                        object.setStatus(contactEmail.getInviteStatus());
//                                        object.setUserStatus(contactEmail.getUserStatus());
//                                    }
//                                }
//                            }
//                        }
//                        mAdapterEmail.update(emailList);
//                        rvContactList.scrollToPosition(0);
//                    }
//                }
//            }
//
//            @Override
//            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
//                super.onFailureCallback(call, t, message, code);
//                Log.e("UserInvite", message);
//            }
//        });

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), (jsonObject.toString()));
        getApp().getRetrofitInterface().userInviteStatus(requestBody).enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                Log.e("UserInvite", content);

                if (progDailog.isShowing()) {
                    progDailog.dismiss();
                }

                InviteParser inviteParser = new Gson().fromJson(content, InviteParser.class);
                if(inviteParser.meta.getCode()!=200 && inviteParser.meta.getCode()!=201 ) {
                    AlertUtils.showAlert(GetContactsActivity.this, getString(R.string.invite_alert), inviteParser.meta.errorMessage);
                } else {
                    if (TAB_SELECETED == MOBILE_TAB) {
                        if (inviteParser.getInviteDetails().getContactNumber().size() > 0) {
                            searchView.setVisibility(View.VISIBLE);
                            rvContactList.setVisibility(View.VISIBLE);
                            txtNoContacts.setVisibility(View.GONE);
                            for (ContactBean object : contactNumberList) {
                                for (InviteParser.InviteDetails.ContactNumber contactNumber : inviteParser.getInviteDetails().getContactNumber()) {
                                    if (object.getEmail().trim().equalsIgnoreCase(contactNumber.getId())) {
//                                        if (contactNumber.getInviteStatus() > 0) {
                                        object.setStatus(contactNumber.getInviteStatus());
                                        object.setUserStatus(contactNumber.getUserStatus());
//                                        }
                                    }
                                }
                            }
                            mAdapterEmail.update(contactNumberList);
                            rvContactList.scrollToPosition(0);
                        }else{
                            searchView.setVisibility(View.GONE);
                            rvContactList.setVisibility(View.GONE);
                            txtNoContacts.setVisibility(View.VISIBLE);
                        }
                    } else if (TAB_SELECETED == EMAIL_TAB) {
                        if (inviteParser.getInviteDetails().getContactEmail().size() > 0) {
                            searchView.setVisibility(View.VISIBLE);
                            rvContactList.setVisibility(View.VISIBLE);
                            txtNoContacts.setVisibility(View.GONE);
                            for (ContactBean object : emailList) {
                                for (InviteParser.InviteDetails.ContactEmail contactEmail : inviteParser.getInviteDetails().getContactEmail()) {
                                    if (object.getEmail().trim().equals(contactEmail.getId())) {
//                                        if (contactEmail.getInviteStatus() > 0) {
                                        object.setStatus(contactEmail.getInviteStatus());
                                        object.setUserStatus(contactEmail.getUserStatus());
//                                        }
                                    }
                                }
                            }
                            mAdapterEmail.update(emailList);
                            rvContactList.scrollToPosition(0);
                        }else{
                            searchView.setVisibility(View.GONE);
                            rvContactList.setVisibility(View.GONE);
                            txtNoContacts.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
                if (progDailog.isShowing()) {
                    progDailog.dismiss();
                }
                Log.e("UserInvite", message);
            }
        });
    }

    @Override
    public void callInviteMentorMenteeWebService(final String contactId) {

        getApp().getRetrofitInterface().inviteMentorMentee(contactId, TAB_SELECETED).enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                Log.e("Invite Mentee", content);
                showInviteSuccessDialog();
                changeMenuItem();
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
            }
        });
    }

    private void showInviteSuccessDialog(){
        View view = getLayoutInflater().inflate(R.layout.invite_success_custom_dialog, null);
        TextView txtOk = (TextView)view.findViewById(R.id.txt_ok);
        TextView txtContent = (TextView)view.findViewById(R.id.txt_invite_popup);
        if(getApp().getUserPreference().getMentorOrMentee().equalsIgnoreCase("MENTEE")){
            txtContent.setText(getResources().getString(R.string.str_invitation_mentor_content));
        }else{
            txtContent.setText(getResources().getString(R.string.str_invitation_mentee_content));
        }
        final AlertDialog.Builder dialogBuiler = new AlertDialog.Builder(GetContactsActivity.this);
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

    private void changeMenuItem(){
        MenuItem menuItem = menu.findItem(R.id.item_skip);
        menuItem.setTitle(getResources().getString(R.string.str_done));
    }
}
