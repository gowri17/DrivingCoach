package com.drava.android.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.drava.android.R;
import com.drava.android.activity.contacts.InviteFragment;
import com.drava.android.activity.help.HelpFragment;
import com.drava.android.activity.leftmenu.ConstructionFragment;
import com.drava.android.activity.leftmenu.LeftMenuAdapter;
import com.drava.android.activity.leftmenu.LeftMenuDrawerItem;
import com.drava.android.activity.map.HomeFragment;
import com.drava.android.activity.map.TripStates;
import com.drava.android.activity.map.services.CurrentLocationUpdateService;
import com.drava.android.activity.map.services.UpdateTripStateFragment;
import com.drava.android.activity.map.services.UpdateTripStates;
import com.drava.android.activity.mentor_mentee.FragmentChangeListener;
import com.drava.android.activity.mentor_mentee.MenteeMentorFragment;
import com.drava.android.activity.mentor_mentee.MentorListParser;
import com.drava.android.activity.mentor_mentee.PurchaseCompleteListener;
import com.drava.android.activity.my_profile.MyProfileFragment;

import com.drava.android.activity.notifications.NotificationFragment;
import com.drava.android.activity.settings.SettingsFragmentMentor;
import com.drava.android.activity.trips.TripListFragment;
import com.drava.android.base.AppConstants;
import com.drava.android.base.BaseActivity;
import com.drava.android.fcm.PushNotification;
import com.drava.android.model.EndTrip;
import com.drava.android.network.ConnectivityReceiver;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.AppLog;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.DravaLog;
import com.drava.android.utils.GpsUtils;
import com.drava.android.utils.TextUtils;
import com.drava.android.welcome.AboutFragment;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import util.*;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

public class HomeActivity extends BaseActivity implements DrawerLayout.DrawerListener, LeftMenuAdapter.OnMenuClickListener,
        UpdateTripStates, FragmentChangeListener, IabBroadcastReceiver.IabBroadcastListener {

    public DrawerLayout mDrawerLayout;
    private ImageView imgProfileImage;
    private TextView txtUserName, txtUserEmail;
    private ListView menuList;
    private LeftMenuAdapter leftMenuAdapter;
    private LeftMenuDrawerItem.Type mCurrentFragment;
    private boolean mIsConnListenerRegistered;
    //    private SyncHelper syncHelper;
    private Handler handler;
    UpdateTripStateFragment updateTripStateFragment;
    public TripStates tripStates;
    public ArrayList<HomeFragment.MyMapPoint> mPoints = new ArrayList<HomeFragment.MyMapPoint>();
    private boolean isPassenger = false;
    private Intent mIntent;
    private PurchaseCompleteListener purchaseCompleteListener;
    private int selectedMenteePosition;
    private boolean isPaused=false;

    //  private IInAppBillingService mService;
    private final String TAG = HomeActivity.class.getSimpleName();

    // SKUs for our products: view_mentee (Consumable Managed Products)
    private final String SKU_VIEW_MENTEE_TOKEN = "com.test.locatementee";   //view_mentee

    // (arbitrary) request code for the purchase flow
    static final int VIEW_MENTEE_REQ_CODE = 10001;

    // How many tokens (5 tokens for each purchase).
    static final int MAX_TOKEN = 5;

    //  Number of token available for the user
    private int availableToken;

    // The helper object
    IabHelper mHelper;

    // Provides purchase notification while this app is running
    IabBroadcastReceiver mBroadcastReceiver;

    protected BroadcastReceiver gpsOffBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(GPS_DISABLED)){
                if(tripStates != null && tripStates.getisTripStarted()){
                    tripStates.callEndTripService();
                    if (updateTripStateFragment != null) {
                        tripStates.currentSpeed = 0;
                        updateTripStateFragment.updateView();     // wil call HomeFragment.java's updateView()
                    }
                }
            }
        }
    };

    private static final char[] symbols = new char[36];

    static {
        for (int idx = 0; idx < 10; ++idx)
            symbols[idx] = (char) ('0' + idx);
        for (int idx = 10; idx < 36; ++idx)
            symbols[idx] = (char) ('a' + idx - 10);
    }

    public class RandomString {

        /*
         * static { for (int idx = 0; idx < 10; ++idx) symbols[idx] = (char)
         * ('0' + idx); for (int idx = 10; idx < 36; ++idx) symbols[idx] =
         * (char) ('a' + idx - 10); }
         */
//        http://stackoverflow.com/questions/17196562/token-that-identify-the-user/17205999#17205999

        private final Random random = new Random();
        private final char[] buf;
        public RandomString(int length) {
            if (length < 1)
                throw new IllegalArgumentException("length < 1: " + length);
            buf = new char[length];
        }

        public String nextString() {
            for (int idx = 0; idx < buf.length; ++idx)
                buf[idx] = symbols[random.nextInt(symbols.length)];
            return new String(buf);
        }
    }

    public final class SessionIdentifierGenerator {
        private SecureRandom random = new SecureRandom();
        public String nextSessionId() {
            return new BigInteger(130, random).toString(32);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();
        initInAppPurchaseBilling();
        setDefaults();
        setUpEvents();

    }

    //For In-app purchase
    private void initInAppPurchaseBilling(){

//        String base64EncodedPublicKey = AppConstants.INAPP_BILLING_KEY;
        String base64EncodedPublicKey = getResources().getString(R.string.inapp_billing_api_key);

        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.enableDebugLogging(true, TAG);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

//                iabresult: unable to buy item (response: 7:item already owned) Consume the item manually
//                try {
//                    mHelper.queryInventoryAsync(mGotInventoryListener);
//                } catch (IabHelper.IabAsyncInProgressException e) {
//                    complain("Error querying inventory. Another async operation in progress.");
//                }

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().

                mBroadcastReceiver = new IabBroadcastReceiver(HomeActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                AppLog.print(HomeActivity.this, "In app purchase : Setup successfull....");
//                try {     //R.L v1.1
//                    mHelper.queryInventoryAsync(mGotInventoryListener);
//                } catch (IabHelper.IabAsyncInProgressException e) {
//                    complain("Error querying inventory. Another async operation in progress.");
//                }
            }
        });
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                AppLog.print(HomeActivity.this, "In app purchase : Querying inventory failed....");
                return;
            }

            Log.d(TAG, "Query inventory was successful.");
            AppLog.print(HomeActivity.this, "In app purchase : Querying inventory Success....");

            // Check for token delivery -- if we have token, we should update the immediately
            Purchase tokenPurchase = inventory.getPurchase(SKU_VIEW_MENTEE_TOKEN);
            if (tokenPurchase != null && verifyDeveloperPayload(tokenPurchase)) {
                Log.d(TAG, "We have remaining token. Consuming it.");
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_VIEW_MENTEE_TOKEN), mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error in consuming token. Another async operation in progress.");
                }
                return;
            }

            setWaitScreen(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    public void manualConsume(){
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject("{\"packageName\":\"com.drava.android\",\"orderId\":\"GPA.3377-4049-6620-80117\",\"productId\":\"com.test.locatementee\",\"developerPayload\":\"7cimry6w2df4eq63hfdoyst00748gpwdo4d7\",\"purchaseTime\":1487340834352,\"purchaseState\":0,\"purchaseToken\":\"akppibeablbakmllpohkipda.AO-J1Owkv5-TyexZYBMpgOjIgWxBM6EyV-jfOy6vW1dPzSIUD5Cjlafo0i7o9PqSowfB8f1Cgx9foES0P3T2a7ymHJ86fSQfXS3bQDAhk3OpnLJE7bl4WD4XffppnZ-3Q1u3iDD1gT68\"}");
            Log.e(TAG, jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Purchase purchase;
        try {
            purchase = new Purchase("inapp", jsonObject.toString(), "");
            mHelper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {

                @Override
                public void onConsumeFinished(Purchase purchase, IabResult result) {
                    Log.d("TAG", "Result: " + result);
                    Log.e(TAG, "Result: " + result);
                }
            });
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error on consuming view mentee token. Another async operation in progress.");
            return;
        } catch (JSONException e){
            complain("Error on consuming view mentee token. Json Exception");
            return;
        }
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            AppLog.print(HomeActivity.this, "In app purchase : Purchase entry started....");

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                AppLog.print(HomeActivity.this, "In app purchase : Purchase entry failed....");
                setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                AppLog.print(HomeActivity.this, "In app purchase : purchase failed on Payload verification....");
                setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");
            AppLog.print(HomeActivity.this, "In app purchase : purchase successfull....");

            if (purchase.getSku().equals(SKU_VIEW_MENTEE_TOKEN)) {
                // bought view mentee token. So consume it.
                Log.d(TAG, "Purchase is view mentee. Start to use view mentee token.");
                AppLog.print(HomeActivity.this, "In app purchase : Start Consuming....");
                try {
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                    availableToken = MAX_TOKEN;
                    getApp().getRetrofitInterface().savePurchasePoints(purchase.getOrderId(), purchase.getToken(), "1 MYR", "android").enqueue(new RetrofitCallback<ResponseBody>(){
                        @Override
                        public void onSuccessCallback(Call<ResponseBody> call, String content) {
                            super.onSuccessCallback(call, content);
                            AppLog.print(HomeActivity.this, "In app purchase : Updated Purchase information to database");
                        }

                        @Override
                        public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                            super.onFailureCallback(call, t, message, code);
                            AppLog.print(HomeActivity.this, "In app purchase : Failed to update Purchase information to database");
                        }
                    });
                    Log.d(TAG, "Consume Success. Redirecting to view mentee token.");
                    purchaseCompleteListener.onPurchaseCompleteListener();
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error on consuming view mentee token. Another async operation in progress.");
                    setWaitScreen(false);
                    return;
                }
            }
        }
    };


    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);
            AppLog.print(HomeActivity.this, "In app purchase : Consumption started....");

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");
                AppLog.print(HomeActivity.this, "In app purchase : Consumption success....");
                availableToken = availableToken == MAX_TOKEN ? MAX_TOKEN : availableToken + 1;
                alert("You viewed the mentee location. Your have remaining " + String.valueOf(availableToken) + " Token!");
            }
            else {
                complain("Error while consuming: " + result);
                AppLog.print(HomeActivity.this, "In app purchase : Consumption failure....");
            }
            setWaitScreen(false);
            Log.d(TAG, "End consumption flow.");
            AppLog.print(HomeActivity.this, "In app purchase : Consumption flow....");
        }
    };

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        AppLog.print(HomeActivity.this, "In app purchase : Received broadcast notification");
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error querying inventory. Another async operation in progress.");
        }
    }

    public void setOnPurchaseCompleteListener(PurchaseCompleteListener purchaseCompleteListener){
        this.purchaseCompleteListener = purchaseCompleteListener;
    }

    // User clicked the "View Mentee" button
    public void purchaseViewMenteeToken(int position) {
        selectedMenteePosition = position;
        Log.d(TAG, "Purchase View mentee Token started ......");
        AppLog.print(HomeActivity.this, "In app purchase : Purchase view mentee token started....");

        if ( availableToken >= MAX_TOKEN) {
//            complain("You have enough token to view mentees. Use Token to view mentee!");
            return;
        }

        // launch the gas purchase UI flow.
        // We will be notified of completion via mPurchaseFinishedListener
        setWaitScreen(true);
        Log.d(TAG, "Launching purchase flow for view mentee.");

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
//        http://stackoverflow.com/questions/17196562/token-that-identify-the-user/17205999#17205999

        RandomString randomString = new RandomString(36);
        String payload = randomString.nextString();

        try {
            mHelper.launchPurchaseFlow(this, SKU_VIEW_MENTEE_TOKEN, VIEW_MENTEE_REQ_CODE, mPurchaseFinishedListener, payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error launching purchase flow. Another async operation in progress.");
            setWaitScreen(false);
        }
    }

    // Enables or disables the "please wait" screen.
    void setWaitScreen(boolean set) {
//        findViewById(R.id.screen_main).setVisibility(set ? View.GONE : View.VISIBLE);
//        findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
    }

    void complain(String message) {
        Log.e(TAG, "**** DraVA Purchase Error: " + message);
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }

    private void setUpEvents() {
        switchPassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SwitchCompat switchBtn = (SwitchCompat) view;
                isPassenger = switchBtn.isChecked();

                tripStates.isPassenger = isPassenger ? "1" : "0";
            }
        });
        switchPassenger.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isPassenger = b;

                tripStates.isPassenger = isPassenger ? "1" : "0";
            }
        });
    }

    @Override
    public void clearMap() {        //This method called from TripStates.java using interface
        mPoints.clear();
        if (updateTripStateFragment != null) {
            updateTripStateFragment.clearMap();     // wil call HomeFragment.java's clearMap()
        }
    }

    @Override
    public void updateViolatedPoint() {     //This method called from TripStates.java using interface
        boolean voilate = !isPassenger;
        mPoints.add(new HomeFragment.MyMapPoint((mPoints.size() + 1), tripStates.mCurrentLatLng, voilate));
        if (updateTripStateFragment != null) {
            updateTripStateFragment.updateViolatedPoint();     // wil call HomeFragment.java's updateViolatedPoint()
        }
    }

    @Override
    public void setButtonState(boolean state) {     //This method called from TripStates.java using interface
        if (state) {
            switchPassenger.setClickable(false);

        } else {
            switchPassenger.setClickable(true);
        }

        if (updateTripStateFragment != null) {
            updateTripStateFragment.setButtonState(state);     // wil call HomeFragment.java's setButtonState()
        }
    }

    @Override
    public void updateView() {      //This method called from TripStates.java using interface
        if(tripStates.isTripAutoStarted || tripStates.isTripManualStarted)  //R.L
            mPoints.add(new HomeFragment.MyMapPoint((mPoints.size() + 1), tripStates.mCurrentLatLng, false));
        if (updateTripStateFragment != null) {
            updateTripStateFragment.updateView();     // wil call HomeFragment.java's updateView()
        }
    }

    public void setUpdateTripStateFragment(UpdateTripStateFragment updateTripStateFragment) {
        this.updateTripStateFragment = updateTripStateFragment;
    }

    private void callLocationService() {

        if (!GpsUtils.isGpsEnabled(this)) {
            showGPSAlert();
            return;
        }

//        if (DeviceUtils.isInternetConnected(getActivity())) {
        if(getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {       //R.L v1.1
            AppLog.print(HomeActivity.this, "---------------------------Inside CallLocationService only for Mentee-------------------------");
            tripStates.initLocationClient();
        }

//        } else {
//            AlertUtils.showAlert(getActivity(), getResources().getString(R.string.check_your_internet_connection), new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    getActivity().finish();
//                }
//            }, false);
//        }

    }

    private void initIntentForPushNotificaiton(){
        mIntent = getIntent();
        if(DeviceUtils.isInternetConnected(HomeActivity.this)) {
            handleIntentFromPushNotifications(mIntent);
        }
    }

    private void handleIntentFromPushNotifications(Intent intent){
        if(intent == null || !intent.hasExtra(PUSH_MESSAGE)){
            Log.e(TAG, "New Intent is Null (or) not having push message data ");
            mIntent = null;
            return;
        }

        PushNotification pushNotification = (PushNotification) intent.getSerializableExtra(PUSH_MESSAGE);
        if(pushNotification.getType() == 1){
            setPassengerToogle(false);
            leftMenuAdapter.setMenuType(mCurrentFragment);
//            setToolbar(getResources().getString(R.string.str_invite));
//            addFragment(new InviteFragment());
//            mCurrentFragment = LeftMenuDrawerItem.Type.INVITE;
//            leftMenuAdapter.setMenuType(mCurrentFragment);
            Intent notificationIntent = new Intent(HomeActivity.this, AcceptDeclineActivity.class); //"Invite Action"
            startActivity(notificationIntent);
        }else if(pushNotification.getType() == 2){
            AlertUtils.showAlert(HomeActivity.this, pushNotification.getMessage()); //"GPS "
        }else if(pushNotification.getType() == 3){
            AlertUtils.showAlert(HomeActivity.this, pushNotification.getMessage()); //"Violation "
        }else if(pushNotification.getType() == 4){
            AlertUtils.showAlert(HomeActivity.this, pushNotification.getMessage()); //"Switch Off "
        }else if(pushNotification.getType() == 5){
            AlertUtils.showAlert(HomeActivity.this, pushNotification.getMessage()); //"Force Quit "
        }
        mIntent = null;
    }


    private void init() {
        setToolbar(getResources().getString(R.string.str_home));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.menu));

//        syncHelper = new SyncHelper(this);
        AppLog.print(HomeActivity.this, " ------------------------  Creating SyncHelper Object -------------------");

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        menuList = (ListView) findViewById(R.id.menu_list);
        imgProfileImage = (ImageView) findViewById(R.id.profile_imageview);
        txtUserName = (TextView) findViewById(R.id.txt_user_name);
        txtUserEmail = (TextView) findViewById(R.id.txt_user_email);

        leftMenuAdapter = new LeftMenuAdapter(this, this);
        leftMenuAdapter.getDynamicMenuItems();
        menuList.setAdapter(leftMenuAdapter);
        DeviceUtils.setSystemUiVisibility(menuList);
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.color_gray_light_bg));
        mDrawerLayout.setDrawerListener(this);
        mCurrentFragment = LeftMenuDrawerItem.Type.HOME;
        addFragment(new HomeFragment());
        leftMenuAdapter.setMenuType(mCurrentFragment);
    }


    private void setDefaults() {
        isPaused = false;
        setStatusBarColor();
        getApp().getUserPreference().setIsHomePageShown(true);
        Log.e("Page Status", "Visited Home Screen");
        if (getApp().getUserPreference().getMentorOrMentee().equalsIgnoreCase(MENTOR)) {
            AppLog.print(HomeActivity.this, " ------------------------  Logged in as Mentorrrrrrrr -------------------");
            if (!TextUtils.isNullOrEmpty(getApp().getUserPreference().getPhoto())) {
                Picasso.with(this).load(getApp().getUserPreference().getPhoto())
                        .placeholder(ContextCompat.getDrawable(this, R.drawable.mentor)).into(imgProfileImage);

            } else {
                imgProfileImage.setImageResource(R.drawable.mentor);
            }
            if(!GpsUtils.isGpsEnabled(HomeActivity.this)){
                showGPSAlert();
            }else{
                updateCurrentLocationUpdateService();
            }
        } else if (getApp().getUserPreference().getMentorOrMentee().equalsIgnoreCase(MENTEE)){
            AppLog.print(HomeActivity.this, " ------------------------  Logged in as Menteeeeeeeee -------------------");
            LocalBroadcastManager.getInstance(this).registerReceiver(gpsOffBroadCastReceiver, new IntentFilter(GPS_DISABLED));
            setPassengerToogle(true);
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {

                    if (msg.what == 1) {
                        Log.e("Network", " Offline Data Sync Called");

                        AppLog.print(HomeActivity.this, "HomeActivity==> Offline Data Sync Called");
                        AppLog.print(HomeActivity.this, "HomeActivity==> ---------------------End from 4----------------------- Offline Data Sync Called");
                        DravaLog.print("---------------------End from 4-----------------------");
                        if (DeviceUtils.isInternetConnected(getApplicationContext())) {
//                            if (syncHelper.syncRunning) {
                            if (getApp().getSyncHelper().syncRunning) {
                                return;
                            } else {
//                            new java.util.Timer().schedule(
//                                    new java.util.TimerTask(){
//
//                                        @Override
//                                        public void run() {
                                getApp().getSyncHelper().startOfflineDataSync();
//                                        }
//                                    }, 300);
//
                            }
                        }
                    }
                }
            };
            registerNetworkReceiver();
            tripStates = new TripStates(this);
            tripStates.setUpdateTripState(this);
            callLocationService();

            if (!TextUtils.isNullOrEmpty(getApp().getUserPreference().getPhoto())) {
                Picasso.with(this).load(getApp().getUserPreference().getPhoto())
                        .placeholder(ContextCompat.getDrawable(this, R.drawable.mentee)).into(imgProfileImage);

            } else {
                imgProfileImage.setImageResource(R.drawable.mentee);
            }
            updateCurrentLocationUpdateService();
        }
        txtUserEmail.setText(getApp().getUserPreference().getEmail());
        txtUserName.setText(getApp().getUserPreference().getFirstName() + " " + getApp().getUserPreference().getLastName());
        initIntentForPushNotificaiton();
    }

    private void updateCurrentLocationUpdateService(){
        Intent currentLocationIntent = new Intent(HomeActivity.this, CurrentLocationUpdateService.class);
        startService(currentLocationIntent);
    }


    private final ConnectivityReceiver mConnectionListener = new ConnectivityReceiver() {
        @Override
        public void onStateChange(boolean isConnected) {
            super.onStateChange(isConnected);
            if (isConnected) {
                Log.e("Network", " Network Connected");
                AppLog.print(HomeActivity.this, "HomeActivity==>Network Connected");
                handler.removeMessages(1);
                handler.sendEmptyMessageDelayed(1, 1000);
            } else {
                Log.e("Network", " Network Not Connected");
                AppLog.print(HomeActivity.this, "HomeActivity==> Network Not Connected");
            }
        }
    };

    private void registerNetworkReceiver() {
        /**
         * check wifi
         */
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mIsConnListenerRegistered = true;
        registerReceiver(mConnectionListener, intentFilter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG, "on New Intent has called");
        handleIntentFromPushNotifications(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            hideKeyboardonToggle();                     //R.L
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);

            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }

        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void externalOnOptionsItemSelected(){
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);

        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
//            finishAffinity();
            moveTaskToBack(true);
//            super.onBackPressed();
        }
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {

    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    @Override
    public void onMenuClick(LeftMenuDrawerItem view, LeftMenuDrawerItem.Type type) {
        mDrawerLayout.closeDrawer(Gravity.LEFT);

        if (type == mCurrentFragment) {
            return;
        }

        if (type != LeftMenuDrawerItem.Type.SIGNOUT) {   //R.L      (Just return on signout, makes updateTripStateFragment to null, so app mis behaves)
            updateTripStateFragment = null;
        }

        setPassengerToogle(false);
        setPassengerImage(View.GONE);
        if (type != LeftMenuDrawerItem.Type.MY_PROFILE) {
            getSupportActionBar().show();
        }
        if (type == LeftMenuDrawerItem.Type.HOME) {

            if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {
                setPassengerToogle(true);
            }
            setToolbar(getResources().getString(R.string.str_home));
//            HomeFragment homeFragment = new HomeFragment().newInstance(mPoints);
            addFragment(new HomeFragment());
            mCurrentFragment = type;
            leftMenuAdapter.setMenuType(mCurrentFragment);
        } else if (type == LeftMenuDrawerItem.Type.TRIPS) {
            setPassengerImage(View.VISIBLE);
            setToolbar(getResources().getString(R.string.str_trips));
            addFragment(new TripListFragment());
            mCurrentFragment = type;
            leftMenuAdapter.setMenuType(mCurrentFragment);
        } else if (type == LeftMenuDrawerItem.Type.VIOLATION) {
            setToolbar(getResources().getString(R.string.str_violation));
            addFragment(new ConstructionFragment());
            mCurrentFragment = type;
            leftMenuAdapter.setMenuType(mCurrentFragment);
        } else if (type == LeftMenuDrawerItem.Type.MY_MENTEES) {
            setToolbar(getResources().getString(R.string.str_my_mentees));
            addFragment(new MenteeMentorFragment());
            mCurrentFragment = type;
            leftMenuAdapter.setMenuType(mCurrentFragment);
        } else if (type == LeftMenuDrawerItem.Type.MY_MENTOR) {
            setToolbar(getResources().getString(R.string.str_mentor));
            addFragment(new MenteeMentorFragment());
            mCurrentFragment = type;
            leftMenuAdapter.setMenuType(mCurrentFragment);
        } else if (type == LeftMenuDrawerItem.Type.INVITE) {
            setToolbar(getResources().getString(R.string.str_invite));
            addFragment(new InviteFragment());
            mCurrentFragment = type;
            leftMenuAdapter.setMenuType(mCurrentFragment);
        } else if (type == LeftMenuDrawerItem.Type.SETTINGS) {
            setToolbar(getResources().getString(R.string.str_settings));
            addFragment(new SettingsFragmentMentor());
            mCurrentFragment = type;
            leftMenuAdapter.setMenuType(mCurrentFragment);
        }else if(type == LeftMenuDrawerItem.Type.HELP){
            setToolbar(getResources().getString(R.string.help));
            addFragment(new HelpFragment());
            mCurrentFragment = type;
            leftMenuAdapter.setMenuType(mCurrentFragment);
        }else if (type == LeftMenuDrawerItem.Type.TERMS_OF_SERVICES) {
            setToolbar(getResources().getString(R.string.str_terms));
            addFragment(new WebViewFragment().newInstance(getApp().getUserPreference().getTermsConditions()));
            mCurrentFragment = type;
            leftMenuAdapter.setMenuType(mCurrentFragment);
        } else if (type == LeftMenuDrawerItem.Type.SIGNOUT) {   //R.L
            if(getApp().getUserPreference().getMentorOrMentee().equalsIgnoreCase(MENTEE)){
                if(tripStates.isTripAutoStarted || tripStates.isTripManualStarted){
                    Toast.makeText(HomeActivity.this, "Please Wait for Trip to End", Toast.LENGTH_LONG).show();
                    return;
                }
                if (getApp().getDBSQLite().getFirstTripId() != 0) {
                    AlertUtils.showAlert(HomeActivity.this, "Signout Alert", getString(R.string.str_signout_again));
                    if(getApp().getSyncHelper().syncRunning){
                        return;
                    }

                    EndTrip endTrip = getApp().getUserPreference().getEndTripInfo();
                    getApp().getDBSQLite().updateEndTrip(getApp().getDBSQLite().getFirstTripId(),
                            endTrip.EndTime,
                            endTrip.EndLatitude,
                            endTrip.EndLongitude,
                            endTrip.Distance,
                            endTrip.MinSpeed,
                            endTrip.MaxSpeed);
                    getApp().getSyncHelper().startOfflineDataSync();

                }else{
                    signoutProcess();
                }
            }else{
                signoutProcess();
            }

        } else if (type == LeftMenuDrawerItem.Type.ABOUT) {
            setToolbar(getResources().getString(R.string.str_about));
            addFragment(new AboutFragment());
            mCurrentFragment = type;
            leftMenuAdapter.setMenuType(mCurrentFragment);
        } else if (type == LeftMenuDrawerItem.Type.NOTIFICATION) {
            setToolbar(getResources().getString(R.string.str_notification));
            addFragment(new NotificationFragment());
            mCurrentFragment = type;
            leftMenuAdapter.setMenuType(mCurrentFragment);
        } else if (type == LeftMenuDrawerItem.Type.PRIVACY) {
            setToolbar(getResources().getString(R.string.str_privacy));
            addFragment(new WebViewFragment().newInstance(getApp().getUserPreference().getPrivacyPolicy()));
            mCurrentFragment = type;
            leftMenuAdapter.setMenuType(mCurrentFragment);
        } else if (type == LeftMenuDrawerItem.Type.MY_PROFILE) {
            getSupportActionBar().hide();
            addFragment(new MyProfileFragment());
            mCurrentFragment = type;
            leftMenuAdapter.setMenuType(mCurrentFragment);
        }
    }

    private void addFragment(Fragment fragment) {
//        FragmentTransaction removeTransaction = getSupportFragmentManager().beginTransaction();   //R.L
//        removeTransaction.remove(getSupportFragmentManager().findFragmentById(R.id.content_view)).commit();
//        hideKeyboardonToggle();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_view, fragment).commitAllowingStateLoss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        //Pass on the activity result to the helper for handling
        if(!mHelper.handleActivityResult(requestCode, resultCode, data)){
            super.onActivityResult(requestCode, resultCode, data);
        }
//        if (requestCode == VIEW_MENTEE_REQ_CODE) {
//            Log.e(TAG, "Purchase information "+data.toString());
//        }

        if(requestCode==GPS_LOCATION) {     //R.L v1.2
            if (!GpsUtils.isGpsEnabled(this)) {
                showGPSAlert();
            } else {
                if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {
                    callLocationService();
                }else if(getApp().getUserPreference().getMentorOrMentee().equalsIgnoreCase(MENTOR)){
                    updateCurrentLocationUpdateService();
                }
            }
        }
    }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {
            registerNetworkReceiver();
        }
        if(isPaused && !GpsUtils.isGpsEnabled(HomeActivity.this)){           //R.L v1.2
            showGPSAlert();
            isPaused = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    protected void onDestroy() {
        AppLog.print(this, "==>onDestroy==>");
        if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {
            getApp().appDestroyed();
        }
        super.onDestroy();
        if(tripStates != null){
            tripStates.removeListener();
        }

        if(mBroadcastReceiver != null){
            unregisterReceiver(mBroadcastReceiver);
        }

        if(gpsOffBroadCastReceiver != null)
        {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(gpsOffBroadCastReceiver);
        }

        //For In-app purchase
        if(mHelper != null)
            try {
                mHelper.disposeWhenFinished();
                mHelper = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        mHelper = null;

//        if(mService != null)
//            unbindService(mServiceConnection);
    }


    @Override
    public void replaceFragment() {
        setToolbar(getResources().getString(R.string.str_home));
        addFragment(new HomeFragment());
        mCurrentFragment = LeftMenuDrawerItem.Type.HOME;
        leftMenuAdapter.setMenuType(mCurrentFragment);
    }

    private class UnRegisterLocationListener implements Runnable{

        @Override
        public void run() {
            Log.e(TAG, "HomeActivity.this====>unregisterReceiver Called");
            DravaLog.print("HomeActivity.this====>unregisterReceiver Called====>mConnectionListener");
            if(tripStates != null) {
                tripStates.removeListener();
                tripStates.trackLatlanPathExecutor.shutdownNow();
                tripStates.scheduledExecutorService.shutdownNow();
            }
            if (mConnectionListener != null) {
                try {
                    if (mIsConnListenerRegistered) {
                        unregisterReceiver(mConnectionListener);
                        AppLog.print(HomeActivity.this, "====>unregisterReceiver====>mConnectionListener");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showGPSAlert() {
        GpsUtils.showGpsAlert(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_LOCATION);
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if (!GpsUtils.isGpsEnabled(HomeActivity.this)) {
                    showGPSAlert();
                }
            }
        });
    }

    private void hideKeyboardonToggle(){
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = this.getCurrentFocus();
        if (v != null)
            inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void signoutProcess(){
        AppLog.print(this, "==>Signout Process==>");
        if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {
            new Thread(new UnRegisterLocationListener()).start();
        }
        getApp().getUserPreference().clearPreference();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        }
        Intent intent = new Intent(HomeActivity.this, AuthorizationActivity.class);
        startActivity(intent);

        Intent currentLocationIntent = new Intent(HomeActivity.this, CurrentLocationUpdateService.class);
        stopService(currentLocationIntent);
    }

    public void setAvailableToken(int availableToken) {
        this.availableToken = availableToken;
    }

}
