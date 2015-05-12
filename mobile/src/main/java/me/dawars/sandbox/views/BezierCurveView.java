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
public class BezierCurveView extends View {
    private static final String VERBOSE_TAG = "BezierCurveView";

    private static final float STROKE_WIDTH = 5f;

    private ArrayList<PointF> pathPoints = new ArrayList<>();

    private Paint paint = new Paint();
    private Paint paintPoints = new Paint();
    private int current = -1;
    private Path path = new Path();

    public BezierCurveView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(STROKE_WIDTH);

        paintPoints.setAntiAlias(true);
        paintPoints.setColor(Color.RED);
        paintPoints.setStyle(Paint.Style.STROKE);
        paintPoints.setStrokeJoin(Paint.Join.ROUND);
        paintPoints.setStrokeWidth(STROKE_WIDTH * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (pathPoints.size() >= 4) {
            path.reset();
            path.moveTo(pathPoints.get(0).x, pathPoints.get(0).y);

            float drawSteps = 100;
            for (int i = 0; i < drawSteps; i++) {
                // Calculate the Bezier (x, y) coordinate for this step.
                float t = ((float) i) / drawSteps;
                float tt = t * t;
                float ttt = tt * t;
                float u = 1 - t;
                float uu = u * u;
                float uuu = uu * u;

                float x = uuu * pathPoints.get(0).x;
                x += 3 * uu * t * pathPoints.get(1).x;
                x += 3 * u * tt * pathPoints.get(2).x;
                x += ttt * pathPoints.get(3).x;

                float y = uuu * pathPoints.get(0).y;
                y += 3 * uu * t * pathPoints.get(1).y;
                y += 3 * u * tt * pathPoints.get(2).y;
                y += ttt * pathPoints.get(3).y;

                // Set the incremental stroke width and draw.
                path.lineTo(x, y);
            }
            path.lineTo(pathPoints.get(3).x, pathPoints.get(3).y);

            canvas.drawPath(path, paint);
        }
        for (PointF point : pathPoints) {
            canvas.drawPoint(point.x, point.y, paintPoints);
        }

        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (this.pathPoints.size() < 4) {
                    pathPoints.add(new PointF(eventX, eventY));
                } else {
                    if (this.current == -1) {//grab new

                        float xDist, yDist;
                        PointF point;
                        for (int i = 0; i < pathPoints.size(); i++) {

                            point = pathPoints.get(i);
                            xDist = point.x - eventX;
                            yDist = point.y - eventY;
                            if ((xDist * xDist + yDist * yDist) < 20 * STROKE_WIDTH * STROKE_WIDTH) {
                                current = i;
                                pathPoints.get(i).set(eventX, eventY);

                                break;
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:

                this.current = -1;
                break;
            case MotionEvent.ACTION_MOVE:

                if (current > -1) {
                    pathPoints.get(current).set(eventX, eventY);
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
        this.pathPoints.clear();
        invalidate();
    }
}
