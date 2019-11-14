package popo.dynamicform;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class PaintView extends PDFView {
    public static int BRUSH_SIZE = 10;
    public static final int DEFAULT_COLOR = Color.RED;
    public static final int DEFAULT_BG_COLOR = Color.TRANSPARENT;
    private static final float TOUCH_TOLERANCE = 4;
    private static final float TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private ArrayList<FingerPath> paths = new ArrayList<>();
    private Path path = new Path();
    private Paint paint = new Paint();
    private int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int strokeWidth;
    private boolean emboss;
    private boolean blur;
    private MaskFilter mEmboss;
    private MaskFilter mBlur;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint();
    private boolean editMode = false;
    private boolean moveMode = false;
    PDFView pdfView;
    ScaleGestureDetector mScaleDetector;
    float mScaleFactor = 1f;
    float scalePointX = 0f;
    float scalePointY = 0f;
    Rect mRect;
    float mLastTouchX;
    float mLastTouchY;
    private int mode = NONE;
    private static int NONE = 0;
    private static int DRAG = 1;
    private static int ZOOM = 2;
    private static float MIN_ZOOM = 0.2f;
    private static float MAX_ZOOM = 2f;

    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mRect = new Rect();
        mPath = new Path();

        mEmboss = new EmbossMaskFilter(new float[] {1, 1, 1}, 0.4f, 6, 3.5f);
        mBlur = new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL);
    }



    public void init(DisplayMetrics metrics) {
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;
    }

    public void normal() {
        emboss = false;
        blur = false;
    }

    public void emboss() {
        emboss = true;
        blur = false;
    }

    public void blur() {
        emboss = false;
        blur = true;
    }

    public void clear() {
//        backgroundColor = DEFAULT_BG_COLOR;
        mCanvas.setBitmap(mBitmap);
        mBitmap.eraseColor(Color.TRANSPARENT);
        Log.e("TES","BSDFJSKDJFLSDK");
        paths.clear();
        normal();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(moveMode){
            canvas.getClipBounds(mRect);
            canvas.save();

            canvas.scale(mScaleFactor, mScaleFactor, scalePointX, scalePointY);
            canvas.translate(mRect.left,mRect.top);
//        mCanvas.drawColor(backgroundColor);

            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            canvas.restore();
        }
        if(editMode){
            canvas.getClipBounds(mRect);
            canvas.save();
//        canvas.scale(mScaleFactor, mScaleFactor, scalePointX, scalePointY);
//        canvas.translate(mRect.top,mRect.left);
//        mCanvas.drawColor(backgroundColor);

            for (FingerPath fp : paths) {
                mPaint.setColor(fp.color);
                mPaint.setStrokeWidth(fp.strokeWidth);
                mPaint.setMaskFilter(null);

                if (fp.emboss)
                    mPaint.setMaskFilter(mEmboss);
                else if (fp.blur)
                    mPaint.setMaskFilter(mBlur);

                mCanvas.drawPath(fp.path, mPaint);

            }

            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
            canvas.restore();
        }

    }


    private void touchStart(float x, float y) {
        mPath = new Path();
        FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth, mPath);
        paths.add(fp);

        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        if (mode != ZOOM) {
            float dx = Math.abs(x - mX / mScaleFactor);
            float dy = Math.abs(y - mY / mScaleFactor);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
            Log.e("ZOOM", "YES");
            Log.e("ZOOM", " x: "+x + " , mX: "+mX);
            Log.e("ZOOM", " y: "+y + " , my: "+mY);
            Log.e("ZOOM", " scale: "+mScaleFactor);
            Log.e("ZOOM", " scale: "+dx);
            Log.e("ZOOM", " scale: "+dy);
        }else{
            float dx = Math.abs(x - mX / mScaleFactor);
            float dy = Math.abs(y - mY / mScaleFactor);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
            Log.e("ZOOM", "NO");
        }

    }

    private void touchUp() {
        mPath.lineTo(mX, mY);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (editMode){

            Log.e("TES", String.valueOf(mScaleFactor));
            Log.e("TOUCH","RECT TOP :" + mRect.top + " RECT LEFT : " + mRect.left + " RECT RIGHT : " + mRect.right + " RECT BOTTOM :" + mRect.bottom);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mode = DRAG;
                    float x = event.getX();
                    float y = event.getY();
                    touchStart(x, y);
    //                invalidate();
                    Log.e("DINAMIC", "DOWN");
                    Log.e("DOWN","X : " +mLastTouchX + " Y :" + mLastTouchY + " scalePointX : " + scalePointX + " scalePointY : " + scalePointY );
                    break;
                case MotionEvent.ACTION_MOVE:

                    if (mode != ZOOM) {
                        float a = event.getX() + mScaleFactor / mRect.right;
                        float b = event.getY() + mScaleFactor / mRect.bottom;
                        touchMove(a, b);
                    }
    //                invalidate();
                    Log.e("DINAMIC", "MOVE");
                    break;
                case MotionEvent.ACTION_UP:
                    touchUp();
                    mode = NONE;
                    Log.e("DINAMIC", "MOVE");
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mode = ZOOM;
                    Log.e("ZOOM", "MODE");
                    break;
            }
            invalidate();
            return true;
        }

        if(moveMode){
            mScaleDetector.onTouchEvent(event);
            Log.e("TES", String.valueOf(mScaleFactor));
            Log.e("TOUCH","RECT TOP :" + mRect.top + " RECT LEFT : " + mRect.left + " RECT RIGHT : " + mRect.right + " RECT BOTTOM :" + mRect.bottom);


            invalidate();
            return true;
        }
        return false;
    }

    /**
     * @return the editMode
     */
    public boolean isEditMode() {
        return editMode;
    }

    /**
     * @param editMode the editMode to set
     */
    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    /**
     * @return the editMode
     */
    public boolean isMoveable() {
        return moveMode;
    }

    /**
     * @param moveMode the editMode to set
     */
    public void setMoveableMode(boolean moveMode) {
        this.moveMode = moveMode;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            mScaleFactor *= detector.getScaleFactor();
            scalePointX = detector.getFocusX();
            scalePointY = detector.getFocusY();
            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
            mScaleFactor = (mScaleFactor < 1 ? 1 : mScaleFactor);
            invalidate();
//            mScaleFactor *= detector.getScaleFactor();
//            mScaleFactor = Math.max(MIN_ZOOM, Math.min(mScaleFactor, MAX_ZOOM));
            return true;
        }
    }

}
