package com.drava.android.rest;


import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DravaApiInterface {
    @FormUrlEncoded
    @POST(DravaURL.SIGN_UP)
    Call<ResponseBody> authenticateUser(@Field("FirstName") String firstName,
                                        @Field("LastName") String lastName,
                                        @Field("Email") String email,
                                        @Field("FBId") String fbId,
                                        @Field("LinkedInId") String linkedinId,
                                        @Field("GooglePlusId") String googleplusId,
                                        @Field("InstagramId") String instagramId,
                                        @Field("Platform") String platform,
                                        @Field("DeviceToken") String deviceToken,
                                        @Field("Token") String gcmeToken);

    @Multipart
    @POST(DravaURL.SIGN_UP)
    Call<ResponseBody> registerUser(
            @PartMap() Map<String, RequestBody> partMap,
            @Part MultipartBody.Part file);

    @GET(DravaURL.INSTAGRAM_SIGNIN)
    Call<ResponseBody> getInstagramSelfUserInfo(@Query("access_token") String accessToken);

    @POST(DravaURL.INVITES)
    Call<ResponseBody> userInviteStatus(@Body RequestBody contactInfo);     //request object contains Json so we are using @Body param

    @GET(DravaURL.MENTOR_LIST)
    Call<ResponseBody> getMentorList(@Query("Start") String Start);

    @GET(DravaURL.MENTEE_LIST)
    Call<ResponseBody> getMenteeList(@Query("Start") String Start);

    @FormUrlEncoded
    @POST(DravaURL.INVITE_MENTOR_MENTEE)
    Call<ResponseBody> inviteMentorMentee(@Field("ContactId") String contactId,
                                          @Field("InviteType") int inviteType);

    @GET(DravaURL.USER_DETAILS)
    Call<ResponseBody> getUserInformation();

    @FormUrlEncoded
    @HTTP(method = "DELETE", path = DravaURL.DELETE_MENTOR, hasBody = true)
    Call<ResponseBody> deleteMentor(@Field("MentorId") String id);

    @FormUrlEncoded
    @HTTP(method = "DELETE", path = DravaURL.DELETE_MENTEE, hasBody = true)
    Call<ResponseBody> deleteMentee(@Field("MenteeId") String id);

    @POST(DravaURL.ACCEPT_DECLINE)
    Call<ResponseBody> acceptDecline(@Path("id") String id,
                                     @Query("Type") String Type,
                                     @Query("UserId") String UserId);

    @GET(DravaURL.INVITE_LIST)
    Call<ResponseBody> inviteList();

    @FormUrlEncoded
    @POST(DravaURL.TRIP_CREATE)
    Call<ResponseBody> tripCreate(@Field("StartTime") String StartTime,
                                  @Field("StartLatitude") String StartLatitude,
                                  @Field("StartLongitude") String StartLongitude,
                                  @Field("TripType") String TripType,
                                  @Field("TripDate") String TripDate,
                                  @Field("IsPassenger") String isPassenger);

    @POST(DravaURL.TRIP_VOILATE)                                                         //request object contains value to append in path of url so we are using @Path param
    Call<ResponseBody> tripVoilate(@Path("tripid") String tripId,
                                   @Query("RoadSpeed") String roadSpeed,
                                   @Query("VechileSpeed") String vehicleSpeed,
                                   @Query("Latitude") String latitude,
                                   @Query("Longitude") String longitude);

    @PUT(DravaURL.TRIP_END)
    Call<ResponseBody> endTrip(@Path("Tripid") String Tripid, @Body /*EndTrip*/ RequestBody endTrip);


    @GET(DravaURL.TRIP_LIST)
    Call<ResponseBody> getTripList(@Path("menteeId") String menteeId, @Query("Start") String Start,@Query("IsPassenger") String IsPassenger);

    @POST(DravaURL.TRIP_TRACK_PATH)
    Call<ResponseBody> tripTrackingDetails(@Body RequestBody contactInfo);


    @GET(DravaURL.TRIP_DETAILS)
    Call<ResponseBody> getTripDetails(@Path("Tripid") String tripId);

    @GET(DravaURL.TRIP_VIOLATION_DETAILS)
    Call<ResponseBody> getTripViolationDetails(@Path("tripid") String tripId);

    @GET(DravaURL.LOCATION_TRACKING)
    Call<ResponseBody> getLocationTracking(@Query("TripId") String TripId, @Query("Start") String Start, @Query("UserId") String userId);

    @POST(DravaURL.LOCATION_TRACKING)
    Call<ResponseBody> setCurrentLocation(@Query("Latitude") String latitude, @Query("Longitude") String longitude);//, @Query("TripId") String tripId, @Query("IsViolation") String isVoiltaion

    @GET(DravaURL.HOME)
    Call<ResponseBody> getMenteesCurrentLocation(@Query("Start") String start);


    @GET(DravaURL.REPORT_TRIP)
    Call<ResponseBody> getTripReport(@Path("menteeId") String menteeId, @Query("Start") String Start, @Query("CurrentMonth") String CurrentMonth);

    @GET(DravaURL.CONTENTS)
    Call<ResponseBody> getSettings();

    @POST(DravaURL.NOTIFICATION)
    Call<ResponseBody> updateGPSStatus(@Query("IsGpsOff") String gpsStatus,
                                       @Query("IsSwitchOff") String switchOnStatus,
                                       @Query("IsForceQuit") String forceQuit,
                                       @Query("Type") String type);

    @GET(DravaURL.NOTIFICATION_LIST)
    Call<ResponseBody> getNotificationList(@Query("Start") String start);

    @PUT(DravaURL.SETTINGS)
    Call<ResponseBody> changeSettings(@Body RequestBody settingInfo);   //request object contains Json so we are using @Body param

    @GET(DravaURL.LOCATEMENTEE)
    Call<ResponseBody> authorizeToLocateMentee(@Query("MenteeId") String menteeId);

    @POST(DravaURL.PURCHASE_POINTS)
    Call<ResponseBody> savePurchasePoints(@Query("TransactionReceiptId") String transactionReceiptId,
                                          @Query("TransactionReceipt") String transactionReceipt,
                                          @Query("PackagePrice") String packagePrice,
                                          @Query("Platform") String platform);

    @DELETE(DravaURL.DELETE_NOTIFICATION)
    Call<ResponseBody> deleteNotification(@Path("notificationId") String notificationId);
}
