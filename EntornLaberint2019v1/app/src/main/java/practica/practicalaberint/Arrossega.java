package practica.practicalaberint;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Ramon Mas.
 * Manage dragging to move the game characters
 */

public class Arrossega implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    Arrossega(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void onSwipeUp() {
    }

    public void onSwipeDown() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeRight() {
    }

    public void onStop() {
    }

    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {


        public boolean onDown(MotionEvent e) {
            onStop(); return false;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float x1 = e1.getX();
            float y1 = e1.getY();

            float x2 = e2.getX();
            float y2 = e2.getY();

            double rad = Math.atan2(y1-y2,x2-x1) + Math.PI;
            double angle =  (rad*180/Math.PI + 180)%360;

            if(inRange(angle, 45, 135)){
                onSwipeUp();
            }
            else if(inRange(angle, 0,45) || inRange(angle, 315, 360)){
                onSwipeRight();
            }
            else if(inRange(angle, 225, 315)){
                onSwipeDown();
            }
            else {
                onSwipeLeft();
            }

            return false;
        }
        private boolean inRange(double angle, float init, float end){
            return (angle >= init) && (angle < end);
        }
    }
}
