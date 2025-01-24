package macro.hd.wallpapers.ExclusiveService.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RotationSensor implements SensorEventListener {
    private int sampleRate;
    private Callback callback;
    private SensorManager sensorManager;
    private Sensor sensor;
    private float[] initialRotation;
    private boolean listenerRegistered = false;

    public RotationSensor(Context context, Callback callback, int sampleRate) {
        this.sampleRate = sampleRate;
        this.callback = callback;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//        if (sensor == null)
//            Toast.makeText(context, context.getText(R.string.toast_sensor_error), Toast
//                    .LENGTH_LONG).show();
    }

    public void register() {
        try {
            if (listenerRegistered) return;
            if(sensorManager!=null)
                sensorManager.registerListener(this, sensor, 1000000 / sampleRate);
            listenerRegistered = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregister() {
        if (!listenerRegistered) return;
        if(sensorManager!=null)
            sensorManager.unregisterListener(this);
        listenerRegistered = false;
        initialRotation = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            float[] r = new float[9];
            SensorManager.getRotationMatrixFromVector(r, event.values);
            if (initialRotation == null) {
                initialRotation = r;
                return;
            }
            float[] change = new float[3];
            SensorManager.getAngleChange(change, r, initialRotation);
            callback.onSensorChanged(change);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface Callback {
        void onSensorChanged(float[] angle);
    }
}
