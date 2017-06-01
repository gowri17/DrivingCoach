package com.drava.android.rest;

import android.content.Context;

import com.drava.android.base.Log;
import com.drava.android.preference.DravaPreference;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class DravaApiClient {
    private static Retrofit retrofit = null;
    private static DravaPreference mPreference;

    public DravaApiClient(Context context) {
        mPreference = new DravaPreference(context);
    }

    public static DravaApiInterface getClientInterface() {
        if (retrofit == null) {
            OkHttpClient defaultHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Interceptor.Chain chain) throws IOException {
                            if (!mPreference.isUserLoggedIn()) {
                                Log.e("retrofit", "Authorization header is already present or user not logged in....");
                                return chain.proceed(chain.request());
                            }
                            Request authorisedRequest = chain.request().newBuilder().addHeader("Authorization", "Bearer " + mPreference.getAccessToken()).build();
                            Log.e("retrofit", "Authorization header is added to the url.... "+mPreference.getAccessToken());
                            return chain.proceed(authorisedRequest);
                        }
                    })
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(DravaURL.BASE_URL)
                    .addConverterFactory(JacksonConverterFactory.create())
                    .client(defaultHttpClient)
                    .build();
        }
        return retrofit.create(DravaApiInterface.class);
    }

    private String getClientId() {
        return "";
    }

    private String getClientSecret() {
        return "";
    }
}
