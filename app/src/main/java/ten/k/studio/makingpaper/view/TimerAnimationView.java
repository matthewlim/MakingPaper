package ten.k.studio.makingpaper.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ten.k.studio.makingpaper.R;
import ten.k.studio.makingpaper.Util;
import ten.k.studio.makingpaper.entity.Explosion;
import ten.k.studio.makingpaper.entity.Ship;
import ten.k.studio.makingpaper.entity.Smoke;
import ten.k.studio.makingpaper.entity.Star;


/**
 * Created by Matthew Lim on 4/9/16.
 * MakingPaper
 */
public class TimerAnimationView extends FrameLayout {

    public static final float ROTATION_RANGE = 20.f;
    public static final int FOREGROUND_STAR_INTERVAL = 20;
    public static final int BACKGROUND_STAR_INTERVAL = 30;

    private TimerTask mFrameTask;
    private Timer mFrameTimer;
    private float mMinY;
    private float mMaxY;

    private static final Object sFrameLock = new Object();
    private float mLastY;

    private float mTouchSlop;
    private Ship mShip;

    private Paint mStarPaint;
    private int mForegroundStarTicker;
    private int mBackgroundStarTicker;
    private ArrayList<Star> mForegroundStars = new ArrayList<>();
    private ArrayList<Star> mBackgroundStars = new ArrayList<>();

    private ValueAnimator mTitleAnimator;
    private TextView mTitleView;
    private TextView mSubtitleView;

    private int mTitleMarginBottom;
    private State mTitleState = State.SHOWN;

    protected ArrayList<Explosion> explosions;
    protected ArrayList<Explosion> explosionsToDelete;
    protected Paint explosionPaint;

    private int mSmokeTicker;
    private ArrayList<Smoke> mSmokeParticles = new ArrayList<>();

    private final Runnable mShowTitleRunnable = new Runnable() {
        @Override
        public void run() {
            showTitle();
        }
    };

    private Runnable mHideTitleRunnable = new Runnable() {
        @Override
        public void run() {
            hideTitle();
        }
    };

    private enum State {
        HIDDEN,
        HIDING,
        SHOWING,
        SHOWN,
    }

    public TimerAnimationView(Context context) {
        this(context, null);
    }

    public TimerAnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mStarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setWillNotDraw(false);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        addTitleViews();
        explosions = new ArrayList<>();
        explosionsToDelete = new ArrayList<>();
        explosionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void initFrameTask() {
        mFrameTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (sFrameLock) {
                    if (mShip != null) {
                        mShip.onFrame();
                        if (mShip.mY < mTitleMarginBottom &&
                                mTitleState != State.HIDING && mTitleState != State.HIDDEN) {
                            removeCallbacks(mHideTitleRunnable);
                            post(mHideTitleRunnable);
                        } else if (mShip.mY > mTitleMarginBottom &&
                                mTitleState != State.SHOWING && mTitleState != State.SHOWN){
                            removeCallbacks(mShowTitleRunnable);
                            post(mShowTitleRunnable);
                        }
                        updateStars();
                        updateSmoke();
                        updateExplosions();
                    }
                }
                postInvalidate();
            }
        };
    }

    public void onActivityResume() { // schedule the task

        if (mFrameTimer == null) {
            mFrameTimer = new Timer();
        }
        if (mFrameTask == null) {
            initFrameTask();
        } else {
            mFrameTask.cancel();
            mFrameTask = null;
            initFrameTask();
        }
        mFrameTimer.schedule(mFrameTask, 0, 16);
    }

    public void onActivityPause() { // stop the task

        if (mFrameTimer != null) {
            mFrameTimer.cancel();
            mFrameTimer.purge();
        }
        if (mFrameTask != null) {
            mFrameTask.cancel();
            mFrameTask = null;
            mFrameTimer = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (sFrameLock) {
            for (Star star : mBackgroundStars) {
                mStarPaint.setColor(star.mColor);
                canvas.drawCircle(star.mX, star.mY, star.mRadius, mStarPaint);
            }
            if (mShip != null) {
                for (Smoke smoke : mSmokeParticles) {
                    smoke.drawSmoke(canvas);
                }
                mShip.drawShip(canvas);
            }
            for (Star star : mForegroundStars) {
                mStarPaint.setColor(star.mColor);
                canvas.drawCircle(star.mX, star.mY, star.mRadius, mStarPaint);
            }
            drawExplosions(canvas);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        if (mShip == null) {
            mShip = new Ship();
            mShip.createShipBitmap(width);
            mShip.mY = height/2;
            mShip.mCenterX = width*2/5;
        }
        mMaxY = height*15/16 - mShip.getShipHeight();
        mMinY = height/16;

        mTitleMarginBottom = height/14 + mTitleView.getMeasuredHeight() +
                height/16 + mSubtitleView.getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int marginTop = getHeight()/14;
        int marginLeft = (getWidth() - mTitleView.getMeasuredWidth())/2;
        mTitleView.layout(marginLeft, marginTop, marginLeft + mTitleView.getMeasuredWidth(), marginTop + mTitleView.getMeasuredHeight());

        marginTop += getHeight()/16;
        marginLeft = (getWidth() - mSubtitleView.getMeasuredWidth())/2;
        mSubtitleView.layout(marginLeft, marginTop, marginLeft + mSubtitleView.getMeasuredWidth(), marginTop + mSubtitleView.getMeasuredHeight());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        synchronized (sFrameLock) {

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_POINTER_DOWN:

                    break;
                case MotionEvent.ACTION_DOWN:
                    mLastY = event.getY();
                    return true;

                case MotionEvent.ACTION_MOVE:

                    float deltaY = event.getY() - mLastY;
                    mLastY = event.getY();
                    if (mShip != null) {
                        mShip.mY = Math.min(mMaxY, Math.max(mMinY, Util.lerp(mShip.mY, mShip.mY + deltaY, 0.7f)));
                        if (deltaY > 0) {
                            mShip.mRotation = ROTATION_RANGE * Math.min(1.f, Math.abs(deltaY) / mTouchSlop);
                        } else if (deltaY < 0) {
                            mShip.mRotation = -ROTATION_RANGE * Math.min(1.f, Math.abs(deltaY) / mTouchSlop);
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:

                    mLastY = event.getY();

                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    private void updateSmoke() {
        if (mSmokeTicker++ == 2) {
            Smoke smoke = new Smoke(mShip.getShipHeight()/4);
            float variation = (float) (-mShip.getShipHeight()/4 + Math.random()*mShip.getShipHeight()/2);
            smoke.mY = mShip.mY + mShip.getShipHeight()/2 - smoke.mDiameter/2 + variation;
            smoke.mX = mShip.mX - smoke.mDiameter;
            mSmokeParticles.add(smoke);
            mSmokeTicker = 0;
        }
        ArrayList<Smoke> removalArray = new ArrayList<>();
        for (Smoke smoke: mSmokeParticles) {
            smoke.mRotation = Util.lerp(smoke.mRotation, 360.f, 0.05f);
            smoke.mX = Util.lerp(smoke.mX, -getWidth()/2, .05f*smoke.mRotation/360);

            if (smoke.mX <= 0.f) {
                removalArray.add(smoke);
            }
        }
        mSmokeParticles.removeAll(removalArray);
        removalArray.clear();

    }

    private void updateStars() {
        if (mForegroundStarTicker++ == FOREGROUND_STAR_INTERVAL) {
            mForegroundStarTicker = 0;
            emitStar((int) (getWidth() / 64 + (getWidth()/128)*Math.random()), 0xffffffff, getWidth() / 64, mForegroundStars);
        }

        if (mBackgroundStarTicker++ == BACKGROUND_STAR_INTERVAL) {
            mBackgroundStarTicker = 0;
            emitStar((int) (getWidth() / 128 + (getWidth()/256)*Math.random()), 0x66ffffff, getWidth() / 256, mBackgroundStars);
        }
        ArrayList<Star> removalArray = new ArrayList<>();
        for (Star star : mForegroundStars) {
            star.mX -= star.mSpeed;
            if ((star.mX + star.mRadius > mShip.mX && star.mX + star.mRadius < mShip.mX + mShip.getShipHeight()*4/3)
                    && (star.mY > mShip.mY && star.mY  < mShip.mY + mShip.getShipHeight())) {
                addExplosion(star.mX, star.mY, Util.getRandomColor());
                removalArray.add(star);
            }
            if (star.mX < 0.f) {
                removalArray.add(star);
            }

        }
        mForegroundStars.removeAll(removalArray);
        removalArray.clear();
        for (Star star : mBackgroundStars) {
            star.mX -= star.mSpeed;
            if (star.mX < 0.f) {
                removalArray.add(star);
            }
        }
        mBackgroundStars.removeAll(removalArray);
        removalArray.clear();
    }

    private void emitStar(int radius, int color, int speed, ArrayList<Star> collection) {
        Star star = new Star();
        star.mX = getWidth() + radius;
        star.mY = (float) (Math.random() * getHeight());
        star.mRadius = radius;
        star.mColor = color;
        star.mSpeed = speed;
        collection.add(star);
    }

    // Title shenanigans
    private void hideTitle() {
        if (mTitleAnimator != null) {
            mTitleAnimator.cancel();
        }
        mTitleAnimator = ValueAnimator.ofFloat(mTitleView.getAlpha(), 0.f);
        mTitleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mTitleView.setAlpha((Float) animation.getAnimatedValue());
                mSubtitleView.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        mTitleAnimator.addListener(new Animator.AnimatorListener() {

            private boolean wasCancelled;

            @Override
            public void onAnimationStart(Animator animation) {
                mTitleState = State.HIDING;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!wasCancelled) {
                    mTitleState = State.HIDDEN;
                }
                mTitleAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                wasCancelled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mTitleAnimator.start();
    }

    private void showTitle() {
        if (mTitleAnimator != null) {
            mTitleAnimator.cancel();
        }
        mTitleAnimator = ValueAnimator.ofFloat(mTitleView.getAlpha(), 1.f);
        mTitleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mTitleView.setAlpha((Float) animation.getAnimatedValue());
                mSubtitleView.setAlpha((Float) animation.getAnimatedValue());
            }
        });
        mTitleAnimator.addListener(new Animator.AnimatorListener() {
            private boolean wasCanceled;


            @Override
            public void onAnimationStart(Animator animation) {
                mTitleState = State.SHOWING;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!wasCanceled) {
                    mTitleState = State.SHOWN;
                }
                mTitleAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                wasCanceled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mTitleAnimator.start();
    }

    private void addTitleViews() {
        mTitleView = new TextView(getContext());
        mTitleView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mTitleView.setTextColor(Color.WHITE);
        mTitleView.setText(R.string.app_name);
        mTitleView.setTextSize(20);
        addView(mTitleView);

        mSubtitleView = new TextView(getContext());
        mSubtitleView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mSubtitleView.setTextColor(Color.WHITE);
        mSubtitleView.setText(R.string.twitter_handles);
        mSubtitleView.setTextSize(14);
        addView(mSubtitleView);
    }

    private void drawExplosions(Canvas canvas) {
        for (Explosion data : explosions) {
            explosionPaint.setColor(data.color);
            explosionPaint.setAlpha(data.alpha);
            canvas.drawCircle(data.x, data.y, data.radius, explosionPaint);
        }
    }

    protected void updateExplosions() {
        for (Explosion data : explosions) {
            if (data.shouldDelete) {
                explosionsToDelete.add(data);
            }
        }
        explosions.removeAll(explosionsToDelete);
        explosionsToDelete.clear();
    }

    public void addExplosion(float explosionX, float explosionY, int color) {
        final Explosion data = new Explosion();
        data.x = explosionX;
        data.y = explosionY;
        data.color = color;
        final ValueAnimator circleAnim = ValueAnimator.ofFloat(0, 1);
        circleAnim.setDuration(800);
        circleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                data.radius = (int) (getHeight() / 2 * animation.getAnimatedFraction());
                data.alpha = (int) (255 * (1 - animation.getAnimatedFraction()));

            }
        });

        circleAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                data.radius = 0;
                data.alpha = 0;
                data.shouldDelete = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        explosions.add(data);
        post(new Runnable() {
            @Override
            public void run() {
                circleAnim.start();
            }
        });

    }
}
