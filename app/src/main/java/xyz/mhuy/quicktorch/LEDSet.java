package xyz.mhuy.quicktorch;

import android.os.IBinder;

import java.lang.reflect.Method;


public class LEDSet {

    private Object svc = null;
    private Method getFlashlightEnabled = null;
    private Method setFlashlightEnabled = null;


    @SuppressWarnings({ "unchecked", "rawtypes" })
    public LEDSet() throws Exception {
        try {
            Class sm = Class.forName("android.os.ServiceManager");
            Object hwBinder = sm.getMethod("getService", String.class).invoke(
                    null, "hardware");

            Class hwsstub = Class.forName("android.os.IHardwareService$Stub");
            Method asInterface = hwsstub.getMethod("asInterface",
                    android.os.IBinder.class);
            svc = asInterface.invoke(null, (IBinder) hwBinder);

            Class proxy = svc.getClass();

            getFlashlightEnabled = proxy.getMethod("getFlashlightEnabled");
            setFlashlightEnabled = proxy.getMethod("setFlashlightEnabled",
                    boolean.class);
        } catch (Exception e) {
            throw new Exception("LED could not be initialized");
        }
    }

    public boolean isEnabled() {
        try {
            return getFlashlightEnabled.invoke(svc).equals(true);
        } catch (Exception e) {
            return false;
        }
    }

    public void enable(boolean tf) {
        try {
            setFlashlightEnabled.invoke(svc, tf);
        } catch (Exception e) {}
    }
}