package ten.k.studio.makingpaper.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ten.k.studio.makingpaper.Util;
import ten.k.studio.makingpaper.entity.Ship;
import ten.k.studio.makingpaper.entity.Star;


/**
 * Created by Matthew Lim on 4/9/16.
 * MakingPaper
 */
public class TimerAnimationView extends FrameLayout {

    public static final float ROTATION_RANGE = 20.f;
    private final Runnable showRunnable = new Runnable() {

        @Override
        public void run() {
            showTitle();
        }
    };
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

    private State titleState = State.SHOWN;


    private enum State {
        HIDDEN,
        HIDING,
        SHOWING,
        SHOWN,
    }

    private ValueAnimator titleAnimator;

    private ArrayList<Star> foregroundStars = new ArrayList<>();
    private ArrayList<Star> backgroundStars = new ArrayList<>();

    private TextView titleView;
    private TextView twitterHandles;

    private int titleMarginBottom;

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

        addTitleViews();
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

    private Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            hideTitle();
        }
    };

    public void initFrameTask() {
        frameTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (frameLock){
                    if (ship != null) {
                        ship.onFrame();
                        if (ship.y < titleMarginBottom &&
                                titleState != State.HIDING && titleState != State.HIDDEN) {
                            removeCallbacks(hideRunnable);
                            post(hideRunnable);
                        } else if (ship.y > titleMarginBottom &&
                                titleState != State.SHOWING && titleState != State.SHOWN){
                            removeCallbacks(showRunnable);
                            post(showRunnable);
                        }
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

        if (ship == null) {
            ship = new Ship();
            ship.createShipBitmap(width);
            ship.y = height/2;
            ship.centerX = width*2/5;
        }
        yMax = height*15/16 - ship.getShipHeight();
        yMin = height/16;

        titleMarginBottom = height/14 + titleView.getMeasuredHeight() + height/16 + twitterHandles.getMeasuredHeight();

        hasMeasured = true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int marginTop = getHeight()/14;
        int marginLeft = (getWidth() - titleView.getMeasuredWidth())/2;
        titleView.layout(marginLeft, marginTop, marginLeft + titleView.getMeasuredWidth(), marginTop + titleView.getMeasuredHeight());

        marginTop += getHeight()/16;
        marginLeft = (getWidth() - twitterHandles.getMeasuredWidth())/2;
        twitterHandles.layout(marginLeft, marginTop, marginLeft + twitterHandles.getMeasuredWidth(), marginTop + twitterHandles.getMeasuredHeight());
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

    private void hideTitle() {
        if (titleAnimator != null) {
            titleAnimator.cancel();
        }
        titleAnimator = ValueAnimator.ofFloat(titleView.getAlpha(), 0.f);
        titleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                titleView.setAlpha((Float) animation.getAnimatedValue());
                twitterHandles.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        titleAnimator.addListener(new Animator.AnimatorListener() {

            private boolean wasCancelled;

            @Override
            public void onAnimationStart(Animator animation) {
                titleState = State.HIDING;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!wasCancelled){
                    titleState = State.HIDDEN;
                }
                titleAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                wasCancelled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        titleAnimator.start();
    }

    private void showTitle() {
        if (titleAnimator != null) {
            titleAnimator.cancel();
        }
        titleAnimator = ValueAnimator.ofFloat(titleView.getAlpha(), 1.f);
        titleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                titleView.setAlpha((Float) animation.getAnimatedValue());
                twitterHandles.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        titleAnimator.addListener(new Animator.AnimatorListener() {
            private boolean wasCanceled;


            @Override
            public void onAnimationStart(Animator animation) {
                titleState = State.SHOWING;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!wasCanceled) {
                    titleState = State.SHOWN;
                }
                titleAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                wasCanceled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        titleAnimator.start();
    }

    private void addTitleViews() {
        titleView = new TextView(getContext());
        titleView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        titleView.setTextColor(Color.WHITE);
        titleView.setText("Making Paper");
        titleView.setTextSize(20);
        addView(titleView);

        twitterHandles = new TextView(getContext());
        twitterHandles.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        twitterHandles.setTextColor(Color.WHITE);
        twitterHandles.setText("@matthewylim @kimchibooty");
        twitterHandles.setTextSize(14);
        addView(twitterHandles);
    }
}
