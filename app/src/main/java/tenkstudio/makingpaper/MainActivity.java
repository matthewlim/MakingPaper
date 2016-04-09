package tenkstudio.makingpaper;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import tenkstudio.makingpaper.view.TimerAnimationView;

public class MainActivity extends AppCompatActivity {

    private TimerAnimationView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
