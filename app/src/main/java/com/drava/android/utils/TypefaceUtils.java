package com.drava.android.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import java.util.Hashtable;

public class TypefaceUtils {
    public static final int ROBOTO_REGULAR = 0;

    public static final String S_ROBOTO_REGULAR = "roboto_regular.ttf";

    private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

    public static Typeface get(Context c, int tf) {
        synchronized (cache) {
            String assetPath = null;
            switch (tf) {

                case ROBOTO_REGULAR:
                    assetPath = S_ROBOTO_REGULAR;
                    break;

                default:
                    //TODO assign the default font here
                    assetPath = S_ROBOTO_REGULAR;
                    break;
            }
            if (!cache.containsKey(assetPath)) {
                try {
                    Log.e("Typeface", "create from asset typeface -- assetPath --> " + assetPath);
                    Typeface t = Typeface.createFromAsset(c.getAssets(), "fonts/" + assetPath);
                    cache.put(assetPath, t);
                } catch (Exception e) {
                    Log.e("Typeface", "Could not get typeface '" + assetPath + "' because " + e.getMessage());
                    return null;
                }
            }
            return cache.get(assetPath);
        }
    }
}