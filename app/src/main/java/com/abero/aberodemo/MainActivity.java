package com.abero.aberodemo;

import android.animation.ObjectAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testMap();

    }


    private void testMap()
    {
     /*   TestMapView view = findViewById(R.id.map);
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "degreeY", 0, -45);
        animator.setDuration(1000 * 8);
        animator.setStartDelay(500);
        animator.start();*/

    }
}
