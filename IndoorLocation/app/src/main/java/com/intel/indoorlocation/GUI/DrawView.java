package com.intel.indoorlocation.GUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by lpq on 17-6-1.
 * For test.
 */
public class DrawView extends View {
    private float x;
    private float y;

    public DrawView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint p = new Paint();
        p.setColor(Color.RED);
        p.setAntiAlias(true);
        canvas.drawCircle(x, y, 25, p);
    }

    public void setPosition(float x, float y)
    {
        this.x = x;
        this.y = y;
        postInvalidate();
    }
}
