package xyz.mhuy.quicktorch;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.widget.RemoteViews;
import java.util.List;


public class WgRec extends BroadcastReceiver {

    static Camera mCameraWidget;
    static Camera.Parameters paramsWidget;
    int count = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.widget_layout);

        if (StatusSet.getIsFlashOn()) {
            views.setImageViewResource(R.id.flashlight_widget_imageview,
                    R.drawable.light_on);
        } else {
            views.setImageViewResource(R.id.flashlight_widget_imageview,
                    R.drawable.light_off);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager
                .getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context,
                WgProv.class), views);

        if (StatusSet.getIsFlashOn()) {
            if (getmCameraWidget() != null) {
                flashOffWidget();
            }

            if (Main.getCameraActivity() != null) {
                flashOffApp();
                Main.fb.setBackgroundResource(R.drawable.light_on);
            }

            Main.turnMotOff();

        } else {
            try {
                setmCameraWidget(Camera.open());
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (getmCameraWidget() == null) {
            } else {
                setParamsWidget(getmCameraWidget().getParameters());

                List<String> flashModes = getParamsWidget()
                        .getSupportedFlashModes();

                if (flashModes == null) {
                    return;
                } else {
                    if (count == 0) {
                        getParamsWidget().setFlashMode(
                                Camera.Parameters.FLASH_MODE_OFF);
                        getmCameraWidget().setParameters(getParamsWidget());
                        getmCameraWidget().startPreview();
                        StatusSet.setIsFlashOn(true);
                    }

                    String flashMode = getParamsWidget().getFlashMode();

                    if (!Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {

                        if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                            getParamsWidget().setFlashMode(
                                    Camera.Parameters.FLASH_MODE_TORCH);
                            getmCameraWidget().setParameters(getParamsWidget());
                        } else {
                            getParamsWidget().setFlashMode(
                                    Camera.Parameters.FLASH_MODE_ON);

                            getmCameraWidget().setParameters(getParamsWidget());
                            try {
                                getmCameraWidget().autoFocus(
                                        new Camera.AutoFocusCallback() {
                                            public void onAutoFocus(
                                                    boolean success,
                                                    Camera camera) {
                                                count = 1;
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

            Main.turnMotorolaOn();
        }
    }

    private void flashOffApp() {
        Main.getCameraActivity().stopPreview();
        Main.getCameraActivity().release();
        Main.setCameraActivity(null);
        StatusSet.setIsFlashOn(true);
        count = 0;
    }

    private void flashOffWidget() {
        WgRec.getmCameraWidget().stopPreview();
        WgRec.getmCameraWidget().release();
        WgRec.setmCameraWidget(null);
        StatusSet.setIsFlashOn(false);
        count = 0;
    }

    public static Camera getmCameraWidget() {
        return mCameraWidget;
    }

    public static void setmCameraWidget(Camera mCameraWidget) {
        WgRec.mCameraWidget = mCameraWidget;
    }

    public static Camera.Parameters getParamsWidget() {
        return paramsWidget;
    }

    public static void setParamsWidget(Camera.Parameters paramsWidgetSet) {
        paramsWidget = paramsWidgetSet;
    }
}