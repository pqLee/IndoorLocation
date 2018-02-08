package com.intel.indoorlocation.GUI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.intel.indoorlocation.R;

/**
 * Painter
 * For test.
 * Created by lpq on 17-3-14.
 */
public class Drawer extends View {
    private float x, y;
    private Bitmap bg;
    private Rect srcRect, dstRect;
    //private float[][] position = new float[5][2];

    public Drawer(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public Drawer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, -1);
        bg = BitmapFactory.decodeResource(context.getResources(), R.drawable.park);
        srcRect = new Rect(0, 0, bg.getWidth(), bg.getHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getMeasuredWidth();
        int height = (int)(width * 440 / 434f);

        dstRect = new Rect(0, 0, width, height);

        setMeasuredDimension(width, height);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        Paint p = new Paint();
        canvas.drawBitmap(bg, srcRect, dstRect, p);

        p.setColor(Color.RED);

        p.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, 30, p);
    }

    public void setPosition(float x, float y)
    {
        this.x = x;
        this.y = y;

        invalidate();
    }
}
