package tenkstudio.makingpaper.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import tenkstudio.makingpaper.Util;
import tenkstudio.makingpaper.entity.Ship;
import tenkstudio.makingpaper.entity.Star;

/**
 * Created by Matthew Lim on 4/9/16.
 * MakingPaper
 */
public class TimerAnimationView extends FrameLayout {

    public static final float ROTATION_RANGE = 20.f;
    private TimerTask frameTask;
    private Timer frameTimer;
    private float yMin;
    private float yMax;

    private static final Object frameLock = new Object();
    private float lastY;
    private float downY;
    private float lastX;
    private float downX;
    private float deltaY;
    private float deltaX;

    private float touchSlop;
    private int foregroundStarTicker;
    private int backgroundStarTicker;
    private Ship ship;
    private Paint starPaint;
    private boolean hasMeasured;

    private ArrayList<Star> foregroundStars = new ArrayList<>();
    private ArrayList<Star> backgroundStars = new ArrayList<>();

    public TimerAnimationView(Context context) {
        this(context, null);
    }

    public TimerAnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        setWillNotDraw(false);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void onActivityResume() { // schedule the task

        if (frameTimer == null) {
            frameTimer = new Timer();
        }
        if (frameTask == null) {
            initFrameTask();
        } else {
            frameTask.cancel();
            frameTask = null;
            initFrameTask();
        }
        frameTimer.schedule(frameTask, 0, 16);
    }

    public void initFrameTask() {
        frameTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (frameLock){
                    if (ship != null) {
                        ship.onFrame();
                    }
                    if (hasMeasured) {
                        updateStars();
                    }
                }
                postInvalidate();
            }
        };
    }

    private void updateStars() {
        if (foregroundStarTicker++ == 40) {
            foregroundStarTicker = 0;

            Star star = new Star(getWidth());
            star.x = getWidth();
            star.y = (float) (Math.random() * getHeight());
            star.radius = getWidth()/64;
            star.color = 0xffffffff;
            star.speed = getWidth()/64;

            foregroundStars.add(star);
        }

        if (backgroundStarTicker++ == 60) {
            backgroundStarTicker = 0;

            Star star = new Star(getWidth());
            star.x = getWidth();
            star.y = (float) (Math.random() * getHeight());
            star.radius = getWidth()/128;
            star.color = 0x88ffffff;
            star.speed = getWidth()/256;
            backgroundStars.add(star);
        }
        ArrayList<Star> removalArray = new ArrayList<>();
        for (Star star : foregroundStars) {
            star.x -= star.speed;
            if (star.x < 0.f) {
                removalArray.add(star);
            }

        }
        foregroundStars.removeAll(removalArray);
        removalArray.clear();
        for (Star star : backgroundStars) {
            star.x -= star.speed;
            if (star.x < 0.f) {
                removalArray.add(star);
            }
        }
        backgroundStars.removeAll(removalArray);
        removalArray.clear();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (frameLock) {
            for (Star star : backgroundStars) {
                starPaint.setColor(star.color);
                canvas.drawCircle(star.x, star.y, star.radius, starPaint);
            }
            if (ship != null) {
                ship.drawShip(canvas);
            }
            for (Star star : foregroundStars) {
                starPaint.setColor(star.color);
                canvas.drawCircle(star.x, star.y, star.radius, starPaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        yMax = height*7/8;
        yMin = height/8;
        if (ship == null) {
            ship = new Ship();
            ship.createShipBitmap(width);
            ship.y = height/2;
            ship.centerX = width*2/5;
        }
        hasMeasured = true;
    }

    public void onActivityPause() { // stop the task

        if (frameTimer != null) {
            frameTimer.cancel();
            frameTimer.purge();
        }
        if (frameTask != null) {
            frameTask.cancel();
            frameTask = null;
            frameTimer = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        synchronized (frameLock) {

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_POINTER_DOWN:

                    break;
                case MotionEvent.ACTION_DOWN:

                    lastY = downY = event.getY();
                    lastX = downX = event.getX();

                    return true;

                case MotionEvent.ACTION_MOVE:

                    deltaY = event.getY() - lastY;
                    deltaX = event.getX() - lastX;

                    lastX = event.getX();
                    lastY = event.getY();
                    if (ship != null) {
                        ship.y = Math.min(yMax, Math.max(yMin, Util.lerp(ship.y, ship.y + deltaY, 0.7f)));
                        if (deltaY > 0) {
                            ship.rotation = ROTATION_RANGE * Math.min(1.f, Math.abs(deltaY) / touchSlop);
                        } else if (deltaY < 0) {
                            ship.rotation = -ROTATION_RANGE * Math.min(1.f, Math.abs(deltaY) / touchSlop);
                        }

                    }

                    break;
                case MotionEvent.ACTION_CANCEL:

                    break;
                case MotionEvent.ACTION_POINTER_UP:

                    break;
                case MotionEvent.ACTION_UP:

                    deltaX = event.getX() - lastX;
                    deltaY = event.getY() - lastY;
                    lastX = event.getX();
                    lastY = event.getY();

                    break;
            }
        }
        return super.onTouchEvent(event);
    }


}
