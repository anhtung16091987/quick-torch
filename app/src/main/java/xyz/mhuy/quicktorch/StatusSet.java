package xyz.mhuy.quicktorch;
import android.app.Application;

public class StatusSet extends Application {
    static Boolean isFlashOn = false;
    static Boolean isOff = false;

    public static Boolean getIsFlashOn() { return isFlashOn; }
    public static void setIsFlashOn(Boolean isOn) { isFlashOn = isOn; }
}
