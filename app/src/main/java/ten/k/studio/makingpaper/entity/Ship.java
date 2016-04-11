package ten.k.studio.makingpaper.entity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import ten.k.studio.makingpaper.Util;


/**
 * Created by Matthew Lim on 4/9/16.
 * MakingPaper
 */
public class Ship {

    public static final int JET_TICKER_MAX = 10;

    public float mCenterX;
    public float mX;
    public float mY;
    public float mRotation;

    private Matrix mShipMatrix;
    private Bitmap mShipBitmap;
    private int mShipHeight;
    private int mShipLength;
    private Paint mShipPaint;
    private Paint mJetPaint;
    private float mJetTicker;
    private Path mJetPath;
    private Direction mJetDirection;
    private Direction mDriftDirection;
    private int mDriftRange;
    private float mCurrentDrift;
    private RectF mShipRect;


    private enum Direction {
        OUTWARD,
        INWARD
    }

    public Ship() {
        mShipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShipPaint.setColor(0xffb94a39);
        mShipMatrix = new Matrix();
        mJetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mJetPaint.setColor(0x8800cece);
        mJetPath = new Path();
        mJetDirection = Direction.OUTWARD;
        mDriftDirection = Direction.OUTWARD;
        mShipRect = new RectF(0, 0, mShipLength, mShipHeight);
        mShipMatrix.mapRect(mShipRect);
    }

    public RectF getShipRect() {
        return mShipRect;
    }

    public int getShipHeight() {
        return mShipHeight;
    }

    public void createShipBitmap(int screenWidth) {
        // create a bitmap for the ship, since we know it's not going to change.
        mShipLength = screenWidth/8;
        mShipHeight = mShipLength *3/4;

        Path mShipPath = new Path();
        mShipPath.moveTo(0, 0);
        mShipPath.lineTo(mShipLength, mShipHeight / 2);
        mShipPath.lineTo(0, mShipHeight);
        mShipPath.close();

        mShipBitmap = Bitmap.createBitmap(mShipLength, mShipHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mShipBitmap);
        canvas.drawPath(mShipPath, mShipPaint);

        mJetPath.moveTo(0, mShipHeight / 6);
        mJetPath.lineTo(-mShipLength / 2, mShipHeight / 2);
        mJetPath.lineTo(0, mShipHeight * 5 / 6);
        mJetPath.close();
        mDriftRange = mShipLength /3;
    }

    private void updateJetPath() {
        mJetPath.reset();
        mJetPath.moveTo(0, mShipHeight / 6);
        mJetPath.lineTo((-mShipLength / 2) * (mJetTicker / JET_TICKER_MAX), mShipHeight / 2);
        mJetPath.lineTo(0, mShipHeight * 5 / 6);
        mJetPath.close();
    }

    public void drawShip(Canvas canvas) {
        if (mShipBitmap != null) {
            mShipMatrix.reset();
            mShipMatrix.setTranslate(mX, mY);
            mShipMatrix.postRotate(mRotation, mX + mShipLength / 2, mY + mShipHeight / 2);
            canvas.drawBitmap(mShipBitmap, mShipMatrix, null);
            updateJetPath();
            mJetPath.transform(mShipMatrix);
            canvas.drawPath(mJetPath, mJetPaint);
        }
    }

    public void onFrame() {
        mRotation = Util.lerp(mRotation, 0.f, 0.1f);
        if (mJetDirection == Direction.OUTWARD) {
            if (mJetTicker++ == JET_TICKER_MAX) {
                mJetDirection = Direction.INWARD;
            }
        } else {
            if (mJetTicker-- == 0) {
                mJetDirection = Direction.OUTWARD;
            }
        }
        if (mDriftDirection == Direction.OUTWARD) {
            mCurrentDrift = Util.lerp(mCurrentDrift, mDriftRange, 0.0125f);
            if (Math.abs(mCurrentDrift - mDriftRange) < 0.1f) {
                mDriftDirection = Direction.INWARD;
            }

        } else {
            mCurrentDrift = Util.lerp(mCurrentDrift, 0, 0.0125f);
            if (mCurrentDrift < 0.1f) {
                mDriftDirection = Direction.OUTWARD;
            }
        }
        mX = mCenterX + mCurrentDrift;
    }
}
