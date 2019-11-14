package popo.dynamicform;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;

import static popo.dynamicform.PaintView.DEFAULT_COLOR;

public class PdfToImageView extends AppCompatActivity{
    ImageView image;
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    Matrix savedMatrix2 = new Matrix();

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int POINT2 = 2;
    static final int ZOOM = 3;
    int mode = NONE;
    private Paint mPaint;
    private MaskFilter mEmboss;
    private MaskFilter mBlur;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private Path mPath;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private int currentColor;
    private float startX;
    private float startY;
    private float endX;
    private float endY;
    LinearLayout mDrawingPad;
    Path path = new Path();
    private static final String TAG = "Touch";
    private static final float MIN_ZOOM = 1f,MAX_ZOOM = 1f;
    private ArrayList<FingerPath> paths = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_to_image_view2);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAlpha(0xff);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        paintView.init(metrics);

        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        currentColor = DEFAULT_COLOR;

        image = findViewById(R.id.image);
        File imagePath = new File(Environment.getExternalStorageDirectory().toString().concat("/")
                + DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/")
                + "/screenshot.jpg");

        Glide.with(this)
                .load(Uri.fromFile(imagePath))
                .into(image);

        image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchStart(x, y);
                        view.invalidate();
                        Log.e("DINAMIC", "DOWN");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        touchMove(x, y);
                        view.invalidate();
                        Log.e("DINAMIC", "MOVE");
                        break;
                    case MotionEvent.ACTION_UP:
                        touchUp();
                        view.invalidate();
                        Log.e("DINAMIC", "MOVE");
                        break;
                }
                return true;
            }
        });
    }



    private void touchStart(float x, float y) {
        mPath = new Path();
        FingerPath fp = new FingerPath(currentColor, false, false, 10, mPath);
        paths.add(fp);

        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(mX, mY);
    }






}

