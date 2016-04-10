package ten.k.studio.makingpaper;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ten.k.studio.makingpaper.view.TimerAnimationView;

public class MainActivity extends AppCompatActivity {

    private TimerAnimationView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mView = (TimerAnimationView) findViewById(R.id.timer_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mView != null) {
            mView.onActivityResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mView != null) {
            mView.onActivityPause();
        }
    }
}
