package me.dawars.sandbox;

import android.app.Activity;
import android.os.Bundle;

import me.dawars.sandbox.views.BezierSplineView;


public class BezierCurveActivity extends Activity {
    private BezierSplineView mCanvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bezier_curve);

        //mCanvasView = (BezierSplineView) findViewById(R.id.activity_bezier_curve_view);

    }

}
