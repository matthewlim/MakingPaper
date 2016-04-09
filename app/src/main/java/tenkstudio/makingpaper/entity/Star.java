package tenkstudio.makingpaper.entity;

/**
 * Created by matthewlim on 4/9/16.
 * MakingPaper
 * Copyright 2016 Cord Project Inc.
 */
public class Star {

    public float x;
    public float y;
    public int color;
    public int radius;
    public float speed;
    private int screenWidth;


    public Star(int screenWidth) {
        this.screenWidth = screenWidth;
    }
}
