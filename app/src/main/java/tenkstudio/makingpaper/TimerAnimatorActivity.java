package tenkstudio.makingpaper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

import tenkstudio.makingpaper.view.TimerAnimationView;

/**
 * Created by matthewlim on 4/9/16.
 * MakingPaper
 * Copyright 2016 Cord Project Inc.
 */
public class TimerAnimatorActivity extends AppCompatActivity {

    private TimerAnimationView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        view = (TimerAnimationView) findViewById(R.id.timer_view);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (view != null) {
            view.onActivityResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (view != null) {
            view.onActivityPause();
        }
    }
}
