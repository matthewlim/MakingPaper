package ten.k.studio.makingpaper.entity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * Created by matthewlim on 4/10/16.
 * MakingPaper
 */
public class Smoke {

    public float mX, mY;
    public float mRotation;
    public float mDiameter;
    private static Bitmap sSmokeBitmap;
    private Matrix mMatrix = new Matrix();
    private Paint mBitmapPaint;

    public Smoke(int diameter) {
        this.mDiameter = diameter;
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (sSmokeBitmap == null) {
            initSmokeBitmap();
        } else if (diameter != sSmokeBitmap.getWidth()) {
            initSmokeBitmap();
        }
    }

    private void initSmokeBitmap() {
        sSmokeBitmap = Bitmap.createBitmap((int) mDiameter, (int) mDiameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(sSmokeBitmap);
        canvas.drawColor(0x88616161);
    }

    public void drawSmoke(Canvas canvas) {
        mMatrix.reset();
        mMatrix.setTranslate(mX, mY);
        mMatrix.postRotate(mRotation, mX + mDiameter / 2, mY + mDiameter / 2);
        mBitmapPaint.setAlpha((int) (0xff*(1.f - mRotation/400.f)));
        canvas.drawBitmap(sSmokeBitmap, mMatrix, mBitmapPaint);
    }
}
