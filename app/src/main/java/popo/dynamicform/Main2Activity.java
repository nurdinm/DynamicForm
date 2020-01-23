package popo.dynamicform;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import static popo.dynamicform.PaintView.DEFAULT_COLOR;


public class Main2Activity extends AppCompatActivity implements View.OnClickListener{

    private ArrayList<FingerPath> paths = new ArrayList<>();
    PDFView pdfView;


    String uri;
    ProgressDialog pDialog;
    String tes;
    File pdfFolder;
    private BoomMenuButton bmb;
    CardView _viewcard;


    Toolbar toolbar;
    private FrameLayout frameLayout;

    static final int NONE = 0;
    static final int DRAG = 1;
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
    ScaleGestureDetector mScaleDetector;
    float mScaleFactor = 1f;
    private boolean editMode = false;
    private boolean moveMode = false;
    private boolean scrollMode = true;
    Rect mRect;

    private static float MIN_ZOOM = 1f;
    private static float MAX_ZOOM = 2f;

    String active = "no";

    private float scaleFactor = 1.f;

    //These two variables keep track of the X and Y coordinate of the finger when it first
    //touches the screen
    private float startX = 0f;
    private float startY = 0f;

    //These two variables keep track of the amount we need to translate the canvas along the X
    //and the Y coordinate
    private float translateX = 0f;
    private float translateY = 0f;

    //These two variables keep track of the amount we translated the X and Y coordinates, the last time we
    //panned.
    private float previousTranslateX = 0f;
    private float previousTranslateY = 0f;

    private boolean dragged = true;

    private CustomScrollView myScrollView;
    Button btnEdit,btnZoom,btnAlign;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        uri = "http://absensi.sstbbmpjg.com/sample.pdf";
        tes = Environment.getExternalStorageDirectory().toString();
//        pdfView = findViewById(R.id.pdfView);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAlpha(0xff);

        mRect = new Rect();
        bmb = findViewById(R.id.bmb);
        assert bmb != null;
        bmb.setButtonEnum(ButtonEnum.Ham);

        toolbar = findViewById(R.id.toolbar);
        frameLayout = findViewById(R.id.remote_pdf_root);
        btnEdit = findViewById(R.id.btn_edit);
        btnAlign = findViewById(R.id.btn_align);
        btnZoom = findViewById(R.id.btn_zoom);

        btnZoom.setOnClickListener(this);
        btnEdit.setOnClickListener(this);
        btnAlign.setOnClickListener(this);
        btnAlign.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_vertical_align_center_24dp));
        setSupportActionBar(toolbar);

        bmb.setPiecePlaceEnum(PiecePlaceEnum.HAM_2);
        bmb.setButtonPlaceEnum(ButtonPlaceEnum.HAM_2);
        bmb.setNormalColor(R.color.default_bmb_normal_color);

        HamButton.Builder builder = new HamButton.Builder()
                .normalImageRes(R.drawable.shape_oval_normal)
                .normalTextRes(R.string.app_name)
                .subNormalTextRes(R.string.app_name)
                .normalColorRes(R.color.colorAccent)
                .listener(new OnBMClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onBoomButtonClick(int index) {
                        // When the boom-button corresponding this builder is clicked.

                    }
                });
        bmb.addBuilder(builder);

        HamButton.Builder builder2 = new HamButton.Builder()
                .normalImageRes(R.drawable.ic_launcher_foreground)
                .normalTextRes(R.string.app_name)
                .subNormalTextRes(R.string.app_name)
                .normalColorRes(R.color.colorPrimaryDark)
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {

                    }
                });
        bmb.addBuilder(builder2);
        _viewcard = findViewById(R.id.cardOuter);

        _viewcard.setVisibility(View.GONE);
        pdfView = findViewById(R.id.pdfView);
        myScrollView = (CustomScrollView) findViewById(R.id.myScroll);
        myScrollView.setFillViewport(true);
//        paintView = (PaintView) findViewById(R.id.paintView);
//        paintView.setVisibility(View.GONE);

//        paintView = new PaintView(this);

        DirectoryHelper.createDirectory(this);
        pdfFolder = Environment.getExternalStorageDirectory();

        new LoadData().execute(uri);

        mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        paintView.init(displayMetrics);

//        int height = metrics.heightPixels;
//        int width = metrics.widthPixels;
//        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        mCanvas = new Canvas(mBitmap);

        int h = 1;
        int w = 1;
        myScrollView.post(new Runnable() {
            @Override
            public void run() {
                if(myScrollView.getWidth()>0) {
                    int h = myScrollView.getChildAt(0).getHeight();
                    int w = myScrollView.getWidth();
                    mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    mCanvas = new Canvas(mBitmap);
                    Log.e("THI1S", String.valueOf(h));
                    Log.e("THIS1", String.valueOf(w));
                }
            }
        });

        Log.e("THIS", String.valueOf(h));
        Log.e("THIS", String.valueOf(w));

        currentColor = DEFAULT_COLOR;

        pdfView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (editMode){
                    myScrollView.setEnableScrolling(false);
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
//                            Log.e("DOWN","X : " +mLastTouchX + " Y :" + mLastTouchY + " scalePointX : " + scalePointX + " scalePointY : " + scalePointY );
                            break;
                        case MotionEvent.ACTION_MOVE:

                            Log.e("DINAMIC", "MscalE : "+mScaleFactor);
                            if (mScaleFactor > 1f){
                                float a = event.getX();
                                float b = event.getY();
                                touchMove(a, b);
                                Log.e("DINAMIC", "ZOME");
                            }else{
                                float a = event.getX();
                                float b = event.getY();
                                touchMove(a, b);
                                Log.e("DINAMIC", "ZOMEss");
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
                    view.invalidate();
                    return true;
                }

                if(moveMode){
                    myScrollView.setEnableScrolling(false);
//                    mRect.top = 0;
//                    mScaleDetector.onTouchEvent(event);
//                    Log.e("TES", String.valueOf(mScaleFactor));
//                    Log.e("TOUCH","RECT TOP :" + mRect.top + " RECT LEFT : " + mRect.left + " RECT RIGHT : " + mRect.right + " RECT BOTTOM :" + mRect.bottom);
//
//
//                    view.invalidate();
//                    return true;

                    switch (event.getAction() & MotionEvent.ACTION_MASK) {

                        case MotionEvent.ACTION_DOWN:
                            mode = DRAG;

                            //We assign the current X and Y coordinate of the finger to startX and startY minus the previously translated
                            //amount for each coordinates This works even when we are translating the first time because the initial
                            //values for these two variables is zero.
                            startX = event.getX() - previousTranslateX;
                            startY = event.getY() - previousTranslateY;
                            break;

                        case MotionEvent.ACTION_MOVE:
                            translateX = event.getX() - startX;
                            translateY = event.getY() - startY;

                            //We cannot use startX and startY directly because we have adjusted their values using the previous translation values.
                            //This is why we need to add those values to startX and startY so that we can get the actual coordinates of the finger.
                            double distance = Math.sqrt(Math.pow(event.getX() - (startX + previousTranslateX), 2) +
                                    Math.pow(event.getY() - (startY + previousTranslateY), 2)
                            );

                            if(distance > 0) {
                                dragged = true;
                            }

                            break;

                        case MotionEvent.ACTION_POINTER_DOWN:
                            mode = ZOOM;
                            break;

                        case MotionEvent.ACTION_UP:
                            mode = NONE;
                            dragged = false;

                            //All fingers went up, so let's save the value of translateX and translateY into previousTranslateX and
                            //previousTranslate
                            previousTranslateX = translateX;
                            previousTranslateY = translateY;
                            break;

                        case MotionEvent.ACTION_POINTER_UP:
                            mode = DRAG;

                            //This is not strictly necessary; we save the value of translateX and translateY into previousTranslateX
                            //and previousTranslateY when the second finger goes up
                            previousTranslateX = translateX;
                            previousTranslateY = translateY;
                            break;
                    }

                    mScaleDetector.onTouchEvent(event);

                    //We redraw the canvas only in the following cases:
                    //
                    // o The mode is ZOOM
                    //        OR
                    // o The mode is DRAG and the scale factor is not equal to 1 (meaning we have zoomed) and dragged is
                    //   set to true (meaning the finger has actually moved)
                    if ((mode == DRAG && scaleFactor != 1f && dragged) || mode == ZOOM) {
                        pdfView.invalidate();
                    }

                    return true;
                }

                if(scrollMode){
                    myScrollView.setEnableScrolling(true);
                    return true;
                }
                return false;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.normal:
//                paintView.normal();
//                paintView.setVisibility(View.INVISIBLE);
//                mDrawingPad.setVisibility(View.INVISIBLE);
                active = "yes";
                this.setEditMode(true);
                this.setMoveableMode(false);
                Log.e("TES",active);
                return true;
            case R.id.emboss:
//                mDrawingPad.setVisibility(View.VISIBLE);
//                paintView.setVisibility(View.VISIBLE);
                this.setEditMode(false);
                this.setMoveableMode(false);
                active = "no";
                Log.e("TES",active);
                return true;
            case R.id.blur:
//                paintView.clear();
                this.setEditMode(false);
                this.setMoveableMode(true);
                return true;
            case R.id.clear:
//                this.setEditMode(false);
//                this.setMoveableMode(false);
                new SaveFileImage().execute(myScrollView);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_edit){
            this.setEditMode(true);
            this.setMoveableMode(false);
            this.setScrollMode(false);
            btnEdit.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_edit_24dp));
            btnAlign.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_vertical_align_center_non_24dp));
            btnZoom.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_zoom_out_map_non_24dp));
        }else if(view.getId() == R.id.btn_align){
            this.setEditMode(false);
            this.setMoveableMode(false);
            this.setScrollMode(true);
            btnEdit.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_edit_non_24dp));
            btnAlign.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_vertical_align_center_24dp));
            btnZoom.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_zoom_out_map_non_24dp));
        }else if(view.getId() == R.id.btn_zoom){
            this.setEditMode(false);
            this.setMoveableMode(true);
            this.setScrollMode(false);
            btnEdit.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_edit_non_24dp));
            btnAlign.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_vertical_align_center_non_24dp));
            btnZoom.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_zoom_out_map_24dp));
        }
    }

    private class LoadData extends AsyncTask<String,String,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            System.out.println("Starting Download");
            pDialog = new ProgressDialog(Main2Activity.this);
            pDialog.setMessage("Loading... Please Wait...");
            pDialog.setCancelable(false);
            pDialog.setIndeterminate(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            int count;
            try {
                String root = Environment.getExternalStorageDirectory().toString();
                System.out.println(root);
                System.out.println("Downloading");
                URL url = new URL(strings[0]);

                URLConnection urlConnection = url.openConnection();
                urlConnection.connect();

                int lengtoffile = urlConnection.getContentLength();

                InputStream inputStream = new BufferedInputStream(url.openStream(), 8192);

                OutputStream outputStream = new FileOutputStream(root.concat("/")+DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/")+"downloadfile.pdf");
                System.out.println(DirectoryHelper.ROOT_DIRECTORY_NAME);
                byte data[] = new byte[1024];

                long total = 0;
                while ((count = inputStream.read(data)) != -1){
                    total += count;

                    outputStream.write(data,0,count);

                }

                outputStream.flush();

                outputStream.close();
                inputStream.close();
            } catch (Exception e) {
                Log.e("error: ",e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println("Downloaded");
            System.out.println(s);
            final String uri = Environment.getExternalStorageDirectory().toString();

            pdfView.fromFile(new File(uri.concat("/") + DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/") + "downloadfile.pdf"))
                    .enableSwipe(true) // allows to block changing pages using swipe
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .onError(new OnErrorListener() {
                        @Override
                        public void onError(Throwable t) {
                            Log.e("FORM",t.getMessage());
                        }
                    })
                    .onDraw(new OnDrawListener() {
                        @Override
                        public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
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
                            if(moveMode){
                                canvas.getClipBounds(mRect);
                                canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
                                canvas.save();

//                                if(mScaleDetector.isInProgress()) {
//                                    pdfView.setScaleX(mScaleFactor);
//                                    pdfView.setScaleY(mScaleFactor);
//
//                                } else
//                                    pdfView.setScaleX(mScaleFactor);
//                                    pdfView.setScaleY(mScaleFactor);
                                    pdfView.animate().scaleX(mScaleFactor).scaleY(mScaleFactor).setDuration(100).start();
                                pdfView.enableDoubletap(true);
                                Log.e("TES x : ", String.valueOf(mScaleDetector.getFocusX()));

//        mCanvas.drawColor(backgroundColor);

                                canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
                                canvas.restore();
                            }
                            if(scrollMode){
                            }
                        }
                    })
                    .onPageError(new OnPageErrorListener() {
                        @Override
                        public void onPageError(int page, Throwable t) {
                            Log.e("FORM","Halaman "+page+ " Error :"+t.getMessage());
                        }
                    })
                    .onRender(new OnRenderListener() {
                        @Override
                        public void onInitiallyRendered(int nbPages, float pageWidth, float pageHeight) {

                        }
                    })
                    .enableAnnotationRendering(true) // render annotations (such as comments, colors or forms)
                    .password(null)
                    .scrollHandle(null)
                    .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                    // spacing between pages in dp. To define spacing color, set view background
                    .spacing(20)
                    .load();
            pDialog.dismiss();


        }
    }

    private Bitmap takeScreenShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int height = myScrollView.getChildAt(0).getHeight();
        int width = metrics.widthPixels;

        Bitmap b = Bitmap.createBitmap(b1, width, height, 0, 0);
        Log.e("Screenshot", String.valueOf(statusBarHeight));
        view.destroyDrawingCache();
        Log.e("Screenshot", "taken successfully");
        return b;

    }

    public Bitmap saveBitmap2(CustomScrollView customScrollView) {
        int h = 0;
        Bitmap bitmap = null;
        //get the actual height of scrollview
        for (int i = 0; i < customScrollView.getChildCount(); i++) {
            h += customScrollView.getChildAt(i).getHeight();
            customScrollView.getChildAt(i).setBackgroundResource(R.color.color_white);
        }
        // create bitmap with target size
        bitmap = Bitmap.createBitmap(customScrollView.getWidth(), h,
                Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(bitmap);
        customScrollView.draw(canvas);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(Environment.getExternalStorageDirectory().toString().concat("/")
                    + DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/")
                    + "/screenshot.jpg");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (null != out) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                getResizedBitmap(bitmap,500);
                out.flush();
                out.close();
            }
        } catch (IOException e) {

        }
        return bitmap;

    }

    public void saveBitmap(Bitmap bitmap) {
        File imagePath = new File(Environment.getExternalStorageDirectory().toString().concat("/")
                + DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/")
                + "/screenshot.jpg");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.e("Screenshot", "saved successfully");

            fos.flush();
            fos.close();
//            paintView.clear();
        } catch (FileNotFoundException e) {
            Log.e("GREC", e.getMessage(), e);
        } catch (IOException e) {
            Log.e("GREC", e.getMessage(), e);
        }

    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private class SaveFileImage extends AsyncTask<CustomScrollView,String,String>{
        @Override
        protected String doInBackground(CustomScrollView... activities) {
            try {
//                Bitmap bitmap = takeScreenShot(activities[0]);
                saveBitmap2(activities[0]);
            }catch (Exception e){
                Log.e("ERROR2",e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Saving File Image");
            pDialog = new ProgressDialog(Main2Activity.this);
            pDialog.setMessage("Loading... Please Wait...");
            pDialog.setCancelable(false);
            pDialog.setIndeterminate(false);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println("Saved");
            System.out.println(s);

            pDialog.dismiss();
//            Intent intent = new Intent(Main2Activity.this,PdfToImageView.class);
//            startActivity(intent);

        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

//            mScaleFactor *= detector.getScaleFactor();
//            scalePointX = detector.getFocusX();
//            scalePointY = detector.getFocusY();
//            // Don't let the object get too small or too large.
//            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
//            mScaleFactor = (mScaleFactor < 1 ? 1 : mScaleFactor);
//            Log.e("ZOOM", " scale: "+scalePointX);
//            Log.e("ZOOM", " scale: "+scalePointY);
//            Log.e("ZOOM", " scalePDF: "+pdfView.getScaleX());
//            Log.e("ZOOM", " scalePDF: "+pdfView.getScaleY());

            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(MIN_ZOOM, Math.min(mScaleFactor, MAX_ZOOM));
//            pdfView.setScaleX(mScaleFactor);
//            pdfView.setScaleY(mScaleFactor);
            pdfView.moveTo(mScaleDetector.getFocusX(),mScaleDetector.getFocusY());

            pdfView.invalidate();
            return true;
        }
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

    public boolean isScrollMode() {
        return scrollMode;
    }

    public void setScrollMode(boolean scrollMode) {
        this.scrollMode = scrollMode;
    }
}

