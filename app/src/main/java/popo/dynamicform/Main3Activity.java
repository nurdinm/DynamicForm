package popo.dynamicform;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.github.barteksc.pdfviewer.PDFView;
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


public class Main3Activity extends AppCompatActivity{

    PDFView pdfView;
    String uri;
    ProgressDialog pDialog;
    String tes;
    File pdfFolder;
    private BoomMenuButton bmb;
    CardView _viewcard;

    private int pageIndex;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor parcelFileDescriptor;
    private static final String FILENAME = Environment.getExternalStorageDirectory().toString().concat("/")+DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/")+"downloadfile.pdf";
    private ImageView imageViewPdf;
    private PaintView paintView;
    Toolbar toolbar;
    FrameLayout remote_pdf_root;

    DrawingView dv ;
    private Paint mPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        dv = new DrawingView(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
        uri = "http://absensi.sstbbmpjg.com/sample.pdf";
        tes = Environment.getExternalStorageDirectory().toString();
//        pdfView = findViewById(R.id.pdfView);

        _viewcard = findViewById(R.id.cardOuter);
        remote_pdf_root = findViewById(R.id.remote_pdf_root);

        remote_pdf_root.addView(dv);
        _viewcard.setVisibility(View.GONE);
        pdfView = findViewById(R.id.pdfView);
//        paintView = (PaintView) findViewById(R.id.paintView);


        DirectoryHelper.createDirectory(this);
        pdfFolder = Environment.getExternalStorageDirectory();

        new LoadData().execute(uri);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        paintView.init(metrics);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.normal:
//                paintView.normal();
                pdfView.bringToFront();
                return true;
            case R.id.emboss:
//                paintView.emboss();
                dv.bringToFront();
                return true;
            case R.id.blur:
                paintView.blur();
                return true;
            case R.id.clear:
                paintView.clear();
                new SaveFileImage().execute(Main3Activity.this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoadData extends AsyncTask<String,String,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Starting Download");
            pDialog = new ProgressDialog(Main3Activity.this);
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
                    .enableAnnotationRendering(true)
                    .load();
            pDialog.dismiss();

        }
    }

    private static Bitmap takeScreenShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay()
                .getHeight();

        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height);
        Log.e("Screenshot", String.valueOf(statusBarHeight));
        view.destroyDrawingCache();
        Log.e("Screenshot", "taken successfully");
        return b;

    }

    public void saveBitmap(Bitmap bitmap) {
        File imagePath = new File(Environment.getExternalStorageDirectory().toString().concat("/")
                + DirectoryHelper.ROOT_DIRECTORY_NAME.concat("/")
                + "/screenshot.png");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.e("Screenshot", "saved successfully");

            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e("GREC", e.getMessage(), e);
        } catch (IOException e) {
            Log.e("GREC", e.getMessage(), e);
        }

    }

    private class SaveFileImage extends AsyncTask<Activity,String,String>{
        @Override
        protected String doInBackground(Activity... activities) {
            try {
                Bitmap bitmap = takeScreenShot(activities[0]);
                saveBitmap(bitmap);
            }catch (Exception e){
                Log.e("ERROR",e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Saving File Image");
            pDialog = new ProgressDialog(Main3Activity.this);
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

        }
    }


    private class DrawingView extends View{
        public int width;
        public  int height;
        private Bitmap  mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint   mBitmapPaint;
        Context context;
        private Paint circlePaint;
        private Path circlePath;

        public DrawingView(Context c) {
            super(c);
            context=c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLUE);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(4f);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath( mPath,  mPaint);
            canvas.drawPath( circlePath,  circlePaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;

                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            circlePath.reset();
            // commit the path to our offscreen
            mCanvas.drawPath(mPath,  mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }
}

