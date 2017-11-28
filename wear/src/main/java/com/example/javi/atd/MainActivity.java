package com.example.javi.atd;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public class MainActivity extends Activity implements SensorEventListener {
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private TextView mTextView;
    private SensorManager mSensorManager;
    public Sensor mSensor=null;
    private TextView myText = null;
    public float x = 0;
    public float y = 0;
    public float z = 0;
    private float count = 0;
    private ArrayList<Float> accValues = new ArrayList<Float>(); //Save x values while handshake
    private boolean stop = false; //Stop recording accelerometer data after handshake
    private int minSize = 10; //minimum size to send Accelerometer data
    private long timestamp; //Time when the event happened
    private ArrayList<Float> timeValues = new ArrayList<Float>(); //Save time in nanoseconds for all the Accelerometer values
    private float max=0; //Max value of accValues
    private float min=0; //Min values of accValues
    private float maxAccel = 5; //Top acceleration to trigger Shake event
    private float minAccel = -15; //Minimum acceleration to trigger Shake event
    private boolean record = false;
    private EditText fname;
    private FileWriter writer;
    private String filename;
    private int period=50000; //Sampling period in us //TODO: Period not working
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("accvalues","aaaaa");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); //Vamos a usar el acelerómetro

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }


    public void onToggleClicked(View view) throws IOException {
        if(((ToggleButton) view).isChecked()) {
           Start(view);
        } else {
            Stop(view);
        }
    }

    public void Start(View v) {
        Log.d("Start","True");
        record= true;
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Files"); //Files Directory
            if (!root.exists()) { //Make dir if it doesn't exist
                boolean made = root.mkdirs(); //TODO: Ask for permission
            }
            fname = (EditText) findViewById(R.id.filename); //Get EditText text to generate file
            filename = fname.getText().toString();
            File file = new File(root, filename+".dat"); //New file with desired title
            writer = new FileWriter(file);
            //writer.append("time;"+"x;"+"y;"+"z;"+"\n"); //insert column names
            //writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Stop(View v) throws IOException {
        Log.d("Stop","True");
        record= false;
        writer.close();
        Context context = getApplicationContext();
        CharSequence text = "Success writing "+filename+".dat";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }


    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }



    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


    public final void onSensorChanged(SensorEvent event) {
        if(record==true) {
            try {
                mSensor = null;
                timestamp = event.timestamp/1000000;
                //Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                //cal.setTimeInMillis(timestamp);
                //String date = DateFormat.format("hh:mm:ss:SSS", cal).toString();
                x = event.values[0]; //Get x value from Accelerometer
                y = event.values[1]; //Get y value from Accelerometer
                z = event.values[2]; //Get z value from Accelerometer
                Log.d("Writing",Float.toString(x));
                writer.append(timestamp+", "+Float.toString(x)+", "+Float.toString(y)+", "+Float.toString(z)+",\r");
                writer.flush();
                //accValues.add(x);
                //timeValues.add(date);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); //Dejamos preparado el acelerómetro para después?
        }

            /*if (stop==true){
                int c=0;

                //Check for max and min values to see if it is a handshake
                max = Collections.max(accValues);
                min = Collections.min(accValues);

                if(max>maxAccel && min<minAccel){ //If true send shake data
                    Log.i("Info","You have shaken hands");
                }

                //Log.i("max", Float.toString(max));
                //Log.i("min",Float.toString(min));
                accValues.clear();
                timeValues.clear();
                stop=false;
            }*/
        //Log.d("Accelerometer Values", "x: " + Float.toString(x) + "y: " + Float.toString(y) + "z: " + Float.toString(z) );
    }
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, period);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}