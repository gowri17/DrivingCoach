package com.drava.android.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.drava.android.R;
import com.drava.android.utils.TypefaceUtils;

public class DravaButton extends Button {

    public DravaButton(Context context) {
        super(context);
    }

    public DravaButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyTypeFace(context, attrs);
    }

    public DravaButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyTypeFace(context, attrs);
    }

    private void applyTypeFace(Context context, AttributeSet attr) {
        if (isInEditMode())
            return;

        TypedArray array = context.obtainStyledAttributes(attr, R.styleable.FontTypeface);
        int typefaceId = array.getInt(R.styleable.FontTypeface_typeface, TypefaceUtils.ROBOTO_REGULAR);
        array.recycle();

        Typeface typeFace = TypefaceUtils.get(context, typefaceId);

        if (typeFace != null) {
            setTypeface(typeFace);
        }
    }
}
