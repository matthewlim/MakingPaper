package ten.k.studio.makingpaper;

/**
 * Created by matthewlim on 4/9/16.
 * MakingPaper
 * Copyright 2016 Cord Project Inc.
 */
public class Util {

    public static float lerp(float a, float b, float pct) {
        return a + pct * (b - a);
    }
}
