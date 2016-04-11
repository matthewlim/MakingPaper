package ten.k.studio.makingpaper;

import android.graphics.Color;

/**
 * Created by matthewlim on 4/9/16.
 * MakingPaper
 * Copyright 2016 Cord Project Inc.
 */
public class Util {

    public static float lerp(float a, float b, float pct) {
        return a + pct * (b - a);
    }

    public static int getRandomColor() {
        int color = Color.WHITE;
        switch((int) (Math.random() * 6)) {
            case 0:
                color = 0xe51c23;
                break;
            case 1:
                color = 0x5677fc;
                break;
            case 2:
                color = 0x40c4ff;
                break;
            case 3:
                color = 0xffab40;
                break;
            case 4:
                color = 0xffeb3b;
                break;
            case 5:
                color = 0xff6e40;
                break;
            case 6:
                color = 0x673ab7;
                break;
        }
        return color;
    }
}
