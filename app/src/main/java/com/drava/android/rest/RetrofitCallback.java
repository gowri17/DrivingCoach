package com.drava.android.rest;

import com.drava.android.base.AppConstants;
import com.drava.android.base.Log;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class RetrofitCallback<T> implements AppConstants, Callback<T> {
    private static final String TAG = RetrofitCallback.class.getSimpleName();

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        int statusCode = response.code();
        if (response.body() == null || !response.isSuccessful()) {
            String message = null;
            try {
                message = response.errorBody().string();
                Log.e(TAG, "onSuccess Retrofit failure callback response... " + message + " code ---> " + statusCode + " isSuccess ---> " + response.isSuccessful());
                if (statusCode == 404 || statusCode == 500 || message.isEmpty()) {
                    onFailureCallback(call, null, DEFAULT_ERROR, statusCode);
                    return;
                }

                JSONObject jsonObject = new JSONObject(message);
                //TODO HANGLE THE ERROR MESSAGE ACCORDING TO THE RESPONSE LIKE BELOW
                /*if (jsonObject.has("meta")) {
                    JSONObject meta = jsonObject.optJSONObject("meta");
                    int code = meta.optInt("code");
                    try {
                        JSONArray error = meta.optJSONArray("error");
                        onFailureCallback(call, null, error.get(0).toString(), code);
                    } catch (Exception e) {
                        String errorMessage = meta.optString("error");
                        onFailureCallback(call, null, errorMessage, code);
                    }
                    return;
                } else {
                    if (jsonObject.has("error_description")) {
                        message = jsonObject.optString("error_description");
                    }
                }*/

            } catch (Exception e) {
                e.printStackTrace();
            }
            onFailureCallback(call, null, message, statusCode);
            return;
        }

        String responseString;
        try

        {
            ResponseBody body = (ResponseBody) response.body();
            responseString = body.string();
            Log.e(TAG, "onSuccess Retrofit callback response... " + responseString + " code ---> " + statusCode + " isSuccess ---> " + response.isSuccessful());
            onSuccessCallback(call, responseString);
        } catch (
                IOException e
                )

        {
            e.printStackTrace();
        }

        /*try {
//            String json = new Gson().toJson(response.body());
//            Log.e(TAG,"Gson to json string... " + json);
            JSONObject responseObject = new JSONObject(response.body().toString());
            boolean status = responseObject.optBoolean("success");
            if (response.isSuccessful()) {
                if (status) {
                    onSuccessCallback(call, response);
                } else {
                    if (responseObject.has("code") && responseObject.optInt("code") == 1012) {
                        onSuccessCallback(call, response);
                    } else if (responseObject.has("errorcode")) {
                        int errorCode = responseObject.optInt("errorcode");
                        onExpired(call, errorCode);
                    } else if (responseObject.has("errormessage")) {
                        String message = responseObject.optString("errormessage");
                        onFailureCallback(call, null, message, statusCode);
                    } else {
                        String message = responseObject.optString("message");
                        onFailureCallback(call, null, message, statusCode);
                    }
                }
            } else {
                if (statusCode == 401 || statusCode == 403) {
                    if (responseObject.has("errorcode")) {
                        int errorCode = responseObject.optInt("errorcode");
                        if (errorCode == 5002) {
                            String errorMessage = responseObject.optString("errormessage");
                            onFailureCallback(call, null, errorMessage, errorCode);
                            return;
                        }
                        onExpired(call, errorCode);
                        return;
                    }
                }
                if (statusCode == 404 || statusCode == 500) {
                    onFailureCallback(call, null, DEFAULT_ERROR, statusCode);
                    return;
                }

                if (!status) {
                    String message = responseObject.optString("errormessage");
                    onFailureCallback(call, null, message, statusCode);
                    return;
                }
                onFailureCallback(call, null, response.errorBody().toString(), statusCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        String message = null;
        try {
            message = t.getMessage();
            Log.e(TAG, "onFailure Retrofit callback message... " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        onFailureCallback(call, t, message, 0);
    }

    public void onSuccessCallback(Call<T> call, String content) {
    }

    public void onFailureCallback(Call<T> call, Throwable t, String message, int code) {
    }

}
