package me.dawars.sandbox.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Dawars on 06.05.2015.
 */
public class BezierSplineView extends View {
    private static final String VERBOSE_TAG = "BezierSplineView";

    private static final float STROKE_WIDTH = 5f;

    private ArrayList<PointF> knots = new ArrayList<>();
    private ArrayList<PointF[]> controlPoints = new ArrayList<>();
    private Path curve = new Path();

    private Paint paint = new Paint();
    private Paint paintKnots = new Paint();
    private Paint paintControls = new Paint();

    private int current = -1;
    private int splineResoluiton = 10;
    private boolean control = false;
    private boolean knot = false;

    public BezierSplineView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(STROKE_WIDTH);

        paintKnots.setAntiAlias(true);
        paintKnots.setColor(Color.RED);
        paintKnots.setStyle(Paint.Style.STROKE);
        paintKnots.setStrokeJoin(Paint.Join.ROUND);
        paintKnots.setStrokeWidth(STROKE_WIDTH * 2);

        paintControls.setAntiAlias(true);
        paintControls.setColor(Color.BLUE);
        paintControls.setStyle(Paint.Style.STROKE);
        paintControls.setStrokeJoin(Paint.Join.ROUND);
        paintControls.setStrokeWidth(STROKE_WIDTH * 2);
    }

    /// <summary>
    /// Solves a tridiagonal system for one of coordinates (x or y)
    /// of first Bezier control points.
    /// </summary>
    /// <param name="rhs">Right hand side vector.</param>
    /// <returns>Solution vector.</returns>
    private static float[] GetFirstControlPoints(float[] rhs) {
        int n = rhs.length;
        float[] x = new float[n]; // Solution vector.
        float[] tmp = new float[n]; // Temp workspace.

        float b = 2.0F;
        x[0] = rhs[0] / b;
        for (int i = 1; i < n; i++) // Decomposition and forward substitution.
        {
            tmp[i] = 1 / b;
            b = (i < n - 1 ? 4.0F : 3.5F) - tmp[i];
            x[i] = (rhs[i] - x[i - 1]) / b;
        }
        for (int i = 1; i < n; i++)
            x[n - i - 1] -= tmp[n - i] * x[n - i]; // Backsubstitution.

        return x;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        updatePoints();

        canvas.drawPath(curve, paint);

        if (this.knot) {
            for (PointF point : knots) {
                canvas.drawPoint(point.x, point.y, paintKnots);
            }
        }

        if (this.control) {
            for (PointF[] point : controlPoints) {
                canvas.drawPoint(point[0].x, point[0].y, paintControls);
                canvas.drawPoint(point[1].x, point[1].y, paintControls);
            }
        }
        super.onDraw(canvas);
    }

    private void updatePoints() {
        curve.reset();
        controlPoints.clear();

        int n = knots.size() - 1;

        if (n > 1) {
            curve.moveTo(knots.get(0).x, knots.get(0).y);

            if (n == 1) { // Special case: Bezier curve should be a straight line.
                controlPoints.add(0, new PointF[2]);
                // 3P1 = 2P0 + P3
                controlPoints.get(0)[0].x = (2 * knots.get(0).x + knots.get(1).x) / 3;
                controlPoints.get(0)[0].y = (2 * knots.get(0).y + knots.get(1).y) / 3;

                // P2 = 2P1 – P0
                controlPoints.get(0)[1].x = 2 *
                        controlPoints.get(0)[0].x - knots.get(0).x;
                controlPoints.get(1)[0].y = 2 *
                        controlPoints.get(0)[0].y - knots.get(0).y;
                return;
            }

            //start

            // Calculate first Bezier control points
            // Right hand side vector
            float[] rhs = new float[n];

            // Set right hand side X values
            for (int i = 1; i < n - 1; ++i)
                rhs[i] = 4 * knots.get(i).x + 2 * knots.get(i + 1).x;
            rhs[0] = knots.get(0).x + 2 * knots.get(1).x;
            rhs[n - 1] = (8 * knots.get(n - 1).x + knots.get(n).x) / 2.0F;
            // Get first control points X-values
            float[] x = GetFirstControlPoints(rhs);

            // Set right hand side Y values
            for (int i = 1; i < n - 1; ++i)
                rhs[i] = 4 * knots.get(i).y + 2 * knots.get(i + 1).y;
            rhs[0] = knots.get(0).y + 2 * knots.get(1).y;
            rhs[n - 1] = (8 * knots.get(n - 1).y + knots.get(n).y) / 2.0F;
            // Get first control points Y-values
            float[] y = GetFirstControlPoints(rhs);

            for (int i = 0; i < n; ++i) {
                // Fill output arrays.
                controlPoints.add(i, new PointF[2]);
                // First control point
                controlPoints.get(i)[0] = new PointF(x[i], y[i]);
                // Second control point
                if (i < n - 1)
                    controlPoints.get(i)[1] = new PointF(
                            2 * knots.get(i + 1).x - x[i + 1],
                            2 * knots.get(i + 1).y - y[i + 1]);
                else
                    controlPoints.get(i)[1] = new PointF(
                            (knots.get(n).x + x[n - 1]) / 2,
                            (knots.get(n).y + y[n - 1]) / 2);
            }

            //end

            //render points

            for (int j = 0; j < knots.size() - 1; j++) {
                for (int i = 0; i < splineResoluiton; i++) {
                    // Calculate the Bezier (x, y) coordinate for this step.
                    float t = ((float) i) / splineResoluiton;
                    float tt = t * t;
                    float ttt = tt * t;
                    float u = 1 - t;
                    float uu = u * u;
                    float uuu = uu * u;

                    float px = uuu * knots.get(j).x;
                    px += 3 * uu * t * controlPoints.get(j)[0].x;
                    px += 3 * u * tt * controlPoints.get(j)[1].x;
                    px += ttt * knots.get(j + 1).x;

                    float py = uuu * knots.get(j).y;
                    py += 3 * uu * t * controlPoints.get(j)[0].y;
                    py += 3 * u * tt * controlPoints.get(j)[1].y;
                    py += ttt * knots.get(j + 1).y;

                    curve.lineTo(px, py);
                }
            }
            curve.lineTo(knots.get(knots.size() - 1).x, knots.get(knots.size() - 1).y);


        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float xDist, yDist;
                PointF point;

                for (int i = 0; i < knots.size(); i++) {

                    point = knots.get(i);
                    xDist = point.x - eventX;
                    yDist = point.y - eventY;
                    if ((xDist * xDist + yDist * yDist) < 20 * STROKE_WIDTH * STROKE_WIDTH) {
                        current = i;
                        break;
                    }
                }

                if (current == -1) {
                    knots.add(new PointF(eventX, eventY));
                }

                break;
            case MotionEvent.ACTION_UP:
                this.current = -1;
                break;
            case MotionEvent.ACTION_MOVE:
                if (current > -1) {
                    knots.get(current).set(eventX, eventY);
                }

                break;
            default:
                Log.d(VERBOSE_TAG, "Ignored touch event: " + event.toString());
                return false;
        }

        invalidate();
        return true;
    }

    public void clearPathPoints() {
        knots.clear();
        curve.reset();
        controlPoints.clear();

        invalidate();
    }

    public void setSplineResoluiton(int splineResoluiton) {
        this.splineResoluiton = splineResoluiton;

        invalidate();
    }

    public void setControl(boolean control) {
        this.control = control;

        invalidate();
    }

    public void setKnot(boolean knot) {
        this.knot = knot;

        invalidate();
    }
}
