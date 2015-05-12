package me.dawars.sandbox;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import me.dawars.sandbox.views.BezierSplineView;


public class BezierSplineActivity extends Activity {

    private BezierSplineView view;
    private CheckBox mCheckControl;
    private CheckBox mCheckKnot;
    private SeekBar mSeekRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bezier_spline);

        view = (BezierSplineView) findViewById(R.id.activity_bezier_spline_view);

        mCheckControl = (CheckBox) findViewById(R.id.activity_bezier_spline_check_control);
        mCheckControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                view.setControl(isChecked);
            }
        });

        mCheckKnot = (CheckBox) findViewById(R.id.activity_bezier_spline_check_knot);
        mCheckKnot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                view.setKnot(isChecked);
            }
        });

        mSeekRes = (SeekBar) findViewById(R.id.activity_bezier_spline_res);
        mSeekRes.setMax(20);
        mSeekRes.setProgress(10);
        mSeekRes.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                view.setSplineResoluiton(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bezier_spline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_clear) {
            view.clearPathPoints();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
