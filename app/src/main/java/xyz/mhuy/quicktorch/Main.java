package xyz.mhuy.quicktorch;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.List;

public class Main extends Activity implements SurfaceHolder.Callback, View.OnClickListener {

    //protected PowerManager.WakeLock wl;
    private int c = 0;
    private static Camera ca;
    private Camera.Parameters pa;
    private SurfaceView sv;
    private SurfaceHolder sh;

    static Button fb;
    private Button ib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //this.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

//        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        this.wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
//        this.wl.acquire();

        sv = (SurfaceView) this.findViewById(R.id.preview);
        sh = sv.getHolder();

        if (StatusSet.getIsFlashOn()) {
            if (WgRec.getmCameraWidget() != null) {
                OffWidget();
                setWidgetTo(R.drawable.light_on);
            }
            if (getCameraActivity() != null) {
                try {
                    OffApp();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                setWidgetTo(R.drawable.light_on);
            }
            
            Main.this.c = 0;
            turnMotOff();
        }

        try {
            setCameraActivity(Camera.open());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Main.this.fb = (Button) this.findViewById(R.id.flashlight_button);
        Main.this.ib = (Button) this.findViewById(R.id.info_button);
        Main.this.fb.setOnClickListener(this);
        Main.this.ib.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.flashlight_button:
                if (StatusSet.getIsFlashOn()) {
                    processOffClick();
                } else {
                    processOnClick();
                }
                break;
            case R.id.info_button:
                startActivity(new Intent(Main.this, DevInfo.class));
                overridePendingTransition(0, 0);
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        sh = holder;
        sh.addCallback(this);

        if (getCameraActivity() != null) {

            try {
                getCameraActivity().setPreviewDisplay(sh);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        sh = holder;
        sh.addCallback(this);

        if (getCameraActivity() != null) {
            try {
                getCameraActivity().setPreviewDisplay(sh);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        sh = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!StatusSet.getIsFlashOn()) {
            processOffClick();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        processOffClick();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (StatusSet.getIsFlashOn()) {
            if (WgRec.getmCameraWidget() != null) {
                OffWidget();
                setWidgetTo(R.drawable.light_on);
            }

            if (getCameraActivity() != null) {
                try {
                    OffApp();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                setWidgetTo(R.drawable.light_on);
            }

            Main.this.c = 0;
            turnMotOff();

        }

        if (StatusSet.getIsFlashOn()) {
            fb.setBackgroundResource(R.drawable.light_off);
        } else if (!StatusSet.getIsFlashOn()) {
            fb.setBackgroundResource(R.drawable.light_on);
        }
    }

    @Override
    protected void onDestroy() {
        //this.wl.release();
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    private void processOnClick() {

        fb.setBackgroundResource(R.drawable.light_off);
        setWidgetTo(R.drawable.light_off);

        if (getCameraActivity() == null) {
            try {
                sh.addCallback(this);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                }
                setCameraActivity(Camera.open());
                try {
                    if (sh != null) {
                        getCameraActivity().setPreviewDisplay(sh);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (getCameraActivity() != null) {
            flashOnApp();
        }

        if (WgRec.getmCameraWidget() != null) {
            flashOnWidget();
        }

        turnMotorolaOn();
    }

    private void flashOnApp() {
        setParams(getCameraActivity().getParameters());

        List<String> flashModes = getParams().getSupportedFlashModes();

        if (flashModes == null) {
            return;
        } else {
            if (Main.this.c == 0) {
                getParams().setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                getCameraActivity().setParameters(getParams());
                sv = (SurfaceView) findViewById(R.id.preview);
                sh = sv.getHolder();
                sh.addCallback(this);

                try {
                    getCameraActivity().startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                StatusSet.setIsFlashOn(true);
            }

            String flashMode = getParams().getFlashMode();

            if (!Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {

                if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                    getParams().setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    getCameraActivity().setParameters(getParams());
                } else {
                    getParams().setFlashMode(Camera.Parameters.FLASH_MODE_ON);

                    getCameraActivity().setParameters(getParams());
                    try {
                        getCameraActivity().autoFocus(new Camera.AutoFocusCallback() {
                            public void onAutoFocus(boolean success, Camera camera) {
                                Main.this.c = 1;
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                StatusSet.setIsFlashOn(true);
            }
        }
    }

    private void flashOnWidget() {
        WgRec.setParamsWidget(WgRec
                .getmCameraWidget().getParameters());

        List<String> flashModes = WgRec.getParamsWidget()
                .getSupportedFlashModes();

        if (flashModes == null) {
            return;
        } else {
            if (Main.this.c == 0) {
                WgRec.getParamsWidget().setFlashMode(
                        Camera.Parameters.FLASH_MODE_OFF);
                WgRec.getmCameraWidget().setParameters(
                        WgRec.getParamsWidget());

                try {
                    WgRec.getmCameraWidget().startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                StatusSet.setIsFlashOn(true);
            }

            String flashMode = WgRec.getParamsWidget().getFlashMode();

            if (!Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {

                if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                    WgRec.getParamsWidget().setFlashMode(
                            Camera.Parameters.FLASH_MODE_TORCH);
                    WgRec.getmCameraWidget().setParameters(
                            WgRec.getParamsWidget());
                } else {
                    WgRec.getParamsWidget().setFlashMode(
                            Camera.Parameters.FLASH_MODE_ON);

                    WgRec.getmCameraWidget().setParameters(
                            WgRec.getParamsWidget());
                    try {
                        WgRec.getmCameraWidget().autoFocus(
                                new Camera.AutoFocusCallback() {
                                    public void onAutoFocus(boolean success, Camera camera) {
                                        Main.this.c = 1;
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                StatusSet.setIsFlashOn(true);

            }
        }
    }

    static void turnMotorolaOn() {
        LEDSet led;
        try {
            led = new LEDSet();
            led.enable(true);
            StatusSet.setIsFlashOn(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processOffClick() {

        fb.setBackgroundResource(R.drawable.light_on);
        setWidgetTo(R.drawable.light_on);

        if (getCameraActivity() != null) {
            Main.this.c = 0;
            OffApp();
        }

        if (WgRec.getmCameraWidget() != null) {
            Main.this.c = 0;
            OffWidget();
        }

        turnMotOff();

    }

    private void OffApp() {
        getCameraActivity().stopPreview();
        getCameraActivity().release();
        setCameraActivity(null);
        StatusSet.setIsFlashOn(false);
    }

    private void OffWidget() {
        WgRec.getmCameraWidget().stopPreview();
        WgRec.getmCameraWidget().release();
        WgRec.setmCameraWidget(null);
        StatusSet.setIsFlashOn(false);

    }

    static void turnMotOff() {
        LEDSet led;
        try {
            led = new LEDSet();
            led.enable(false);
            StatusSet.setIsFlashOn(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setWidgetTo(int drawable) {
        Context context = this;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        ComponentName thisWidget = new ComponentName(context, WgProv.class);
        remoteViews.setImageViewResource(R.id.flashlight_widget_imageview, drawable);
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }

    public static Camera getCameraActivity() {
        return ca;
    }

    public static void setCameraActivity(Camera mCameraActivity) {
        Main.ca = mCameraActivity;
    }

    public Camera.Parameters getParams() {
        return pa;
    }

    public void setParams(Camera.Parameters p) {
        this.pa = p;
    }
}