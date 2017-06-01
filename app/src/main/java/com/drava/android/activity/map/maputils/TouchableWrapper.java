package com.drava.android.activity.map.maputils;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class TouchableWrapper extends FrameLayout {

    public TouchableWrapper(Context context) {
        super(context);
    }

    public void setTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    private OnTouchListener onTouchListener;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(onTouchListener != null) {   //R.L V1.1
                    onTouchListener.onTouch();
                }
                break;
            case MotionEvent.ACTION_UP:
                if(onTouchListener != null) {   //R.L V1.1
                    onTouchListener.onRelease();
                }
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    public interface OnTouchListener {
        public void onTouch();
        public void onRelease();
    }
}
