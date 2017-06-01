package com.drava.android.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

import com.drava.android.BuildConfig;
import com.drava.android.R;
import com.drava.android.base.Log;

public class BitmapUtils {

    public static Bitmap getRoundedCornerBitmap(Bitmap input, int pixels, boolean roundTop,
                                                boolean roundBottom) {

        int w = input.getWidth();
        int h = input.getHeight();

        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        //final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, w, h);
        final RectF rectF = new RectF(rect);

//make sure that our rounded corner is scaled appropriately
        //final float roundPx = pixels * densityMultiplier;
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

//draw rectangles over the corners we want to be square
        if (!roundTop) {
            canvas.drawRect(0, 0, w / 2, h / 2, paint);
            canvas.drawRect(w / 2, 0, w, h / 2, paint);
        }

        if (!roundBottom) {
            canvas.drawRect(0, h / 2, w / 2, h, paint);
            canvas.drawRect(w / 2, h / 2, w, h, paint);
        }

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(input, 0, 0, paint);

        return output;
    }

    public static BitmapDrawable drawTextToBitmap(Context gContext, Bitmap bitmap, String gText) {
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text size in pixels
        paint.setTextSize((int) (12 * scale));
//        paint.setTypeface(Typef.getTypeFace(gContext, TypefaceUtils.BOLD));
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        paint.setTextAlign(Paint.Align.CENTER);

//        int radius = (gText.length()*10)+10; // radius according to the text length
        int radius = BDevice.getPixelFromDp(gContext, 15); // constant radius
//        Log.e("drawText", "x --> " + x + " y --> " + y);

        int x, y;
        if (BDevice.getDensityDpi(gContext) > 320) {
            x = (gText.length() > 2) ? ((bitmap.getWidth() - bounds.width()) / 4) + 4
                    : (gText.length() > 1) ? ((bitmap.getWidth() - bounds.width()) / 4) : ((bitmap.getWidth() - bounds.width()) / 4) +2;
            y = ((bitmap.getHeight() - bounds.height()) / 10);
        } else {
            x = (gText.length() > 2) ? ((bitmap.getWidth() - bounds.width()) / 3) + 10
                    : (gText.length() > 1) ? ((bitmap.getWidth() - bounds.width()) / 3) + 6
                    : ((bitmap.getWidth() - bounds.width()) / 3) + 4;
            y = ((bitmap.getHeight() - bounds.height()) / 6);
        }
        /* ---------- xxhdpi ----------- y+10 */
//        int x = (gText.length() > 2) ? ((bitmap.getWidth() - bounds.width()) / 4) + 4 : (gText.length() > 1) ? ((bitmap.getWidth() - bounds.width()) / 4) : ((bitmap.getWidth() - bounds.width()) / 4) - 4;
//        int y = ((bitmap.getHeight() - bounds.height()) / 6);
        /* ---------- xhdpi ------------ y+8  */
//        int x = (gText.length() > 2) ? ((bitmap.getWidth() - bounds.width()) / 3) + 10 : (gText.length() > 1) ? ((bitmap.getWidth() - bounds.width()) / 3) + 6 : ((bitmap.getWidth() - bounds.width()) / 3) + 1;
//        int y = ((bitmap.getHeight() - bounds.height()) / 4);

        Log.e("density", "BitmapUtils --- densitydpi -- > " + BDevice.getDensityDpi(gContext));
        x = BDevice.getPixelFromDp(gContext, x);
        y = BDevice.getPixelFromDp(gContext, y);

//        if (BuildConfig.IS_CUSTOMER_VERSION) {
//            circlePaint.setColor(Color.parseColor("#FDE402"));// circle color
//            paint.setColor(gContext.getResources().getColor(R.color.theme_primary));
//        } else {
            circlePaint.setColor(Color.RED);
            paint.setColor(Color.WHITE);// text color - #3D3D3D
//        }

        canvas.drawCircle(x, y, radius, circlePaint);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(5);
        circlePaint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, radius, circlePaint);
        canvas.drawText(gText, x, BDevice.getDensityDpi(gContext) > 320 ? y + 10 : y + 8, paint);

        return new BitmapDrawable(resources, bitmap);
    }

}
