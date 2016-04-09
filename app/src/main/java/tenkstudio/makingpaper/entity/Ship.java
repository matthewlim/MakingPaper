package tenkstudio.makingpaper.entity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

import tenkstudio.makingpaper.Util;

/**
 * Created by Matthew Lim on 4/9/16.
 * MakingPaper
 */
public class Ship {

    public static final int JET_TICKER_MAX = 20;

    public float centerX;
    public float x;
    public float y;
    public float rotation;

    private Matrix shipMatrix;
    private Bitmap shipBitmap;
    private Path shipPath;
    private int shipHeight;
    private int shipLength;
    private Paint shipPaint;
    private Paint jetPaint;
    private float jetTicker;
    private Path jetPath;
    private Direction jetDirection;
    private Direction driftDirection;
    private int driftRange;
    private float currentDrift;


    private enum Direction {
        OUTWARD,
        INWARD
    }

    public Ship() {
        shipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shipPaint.setColor(Color.RED);
        shipMatrix = new Matrix();
        jetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        jetPaint.setColor(0x8800cece);
        jetPath = new Path();
        jetDirection = Direction.OUTWARD;
        driftDirection = Direction.OUTWARD;
    }

    public void createShipBitmap(int screenWidth) {

        shipLength = screenWidth/8;
        shipHeight = shipLength *3/4;

        shipPath = new Path();
        shipPath.moveTo(0, 0);
        shipPath.lineTo(shipLength, shipHeight / 2);
        shipPath.lineTo(0, shipHeight);
        shipPath.close();

        shipBitmap = Bitmap.createBitmap(shipLength, shipHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(shipBitmap);
        canvas.drawPath(shipPath, shipPaint);

        jetPath.moveTo(0, shipHeight / 6);
        jetPath.lineTo(-shipLength / 2, shipHeight / 2);
        jetPath.lineTo(0, shipHeight * 5 / 6);
        jetPath.close();
        driftRange = shipLength/3;
    }

    private void updateJetPath() {
        jetPath.reset();
        jetPath.moveTo(0, shipHeight / 6);
        jetPath.lineTo(-shipLength / 2*(jetTicker/ JET_TICKER_MAX), shipHeight / 2);
        jetPath.lineTo(0, shipHeight*5/6);
        jetPath.close();
    }

    public void drawShip(Canvas canvas) {
        if (shipBitmap != null) {
            shipMatrix.reset();
            shipMatrix.setTranslate(x, y);
            shipMatrix.postRotate(rotation, x + shipLength / 2, y + shipHeight / 2);
            canvas.drawBitmap(shipBitmap, shipMatrix, null);
            updateJetPath();
            jetPath.transform(shipMatrix);
            canvas.drawPath(jetPath, jetPaint);
        }
    }

    public void onFrame() {
        rotation = Util.lerp(rotation, 0.f, 0.1f);
        if (jetDirection == Direction.OUTWARD) {
            if (jetTicker++ == JET_TICKER_MAX) {
                jetDirection = Direction.INWARD;
            }
        } else {
            if (jetTicker-- == 0) {
                jetDirection = Direction.OUTWARD;
            }
        }
        if (driftDirection == Direction.OUTWARD) {
            currentDrift = Util.lerp(currentDrift, driftRange, 0.0125f);
            if (Math.abs(currentDrift - driftRange) < 0.1f) {
                driftDirection = Direction.INWARD;
            }

        } else {
            currentDrift = Util.lerp(currentDrift, 0, 0.0125f);
            if (currentDrift < 0.1f) {
                driftDirection = Direction.OUTWARD;
            }
        }
        x = centerX + currentDrift;
    }
}
