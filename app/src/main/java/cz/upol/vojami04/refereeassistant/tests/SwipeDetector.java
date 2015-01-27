package cz.upol.vojami04.refereeassistant.tests;

import android.view.MotionEvent;
import android.view.View;

public abstract class SwipeDetector implements View.OnTouchListener {
    static final int MIN_DISTANCE = 100;
//    abstract void onUpSwipe();
//    abstract void onDownSwipe();
    abstract void onRightSwipe();
    abstract void onLeftSwipe();
    abstract void onClick();
    private float downX;
    private float downY;

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                return true;
            }
            case MotionEvent.ACTION_UP: {
                float upX = event.getX();
                float upY = event.getY();
                float deltaX = downX - upX;
                float deltaY = downY - upY;
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (Math.abs(deltaX) > MIN_DISTANCE) {
                        if (deltaX < 0) {
                            this.onRightSwipe();
                            return true;
                        }
                        if (deltaX > 0) {
                            this.onLeftSwipe();
                            return true;
                        }
                    } else {
                        this.onClick();
                        return true;
                    }
                } else {
                    if (Math.abs(deltaY) > MIN_DISTANCE) {
                        if (deltaY < 0) {
//                            this.onDownSwipe();
                            return true;
                        } else if (deltaY > 0) {
//                            this.onUpSwipe();
                            return true;
                        }
                    } else {
                        this.onClick();
                        return true;
                    }
                }
                return true;
            }
            default:
                return true;
        }
    }
}