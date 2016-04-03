package ro.cipry.xplorers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class AutonomousModeActivity extends Activity {

    private int currentApiVersion;
    private ImageView front_arrow_layout, left_arrow_layout, right_arrow_layout,
            circle_speed_layout, battery_level_layout;
    private Chronometer chronometer_layout;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;
    private TextView start_textView, distance_textView, bottom_textView;
    private TextView battery_textView;
    private Boolean start_enabled = false, logger = true, back_button_pressed = false, stopWorker = true, connectedToDevice = false;
    private float prev_value = 0.0f;
    private String receivedString = "0";
    private byte[] readBuffer;
    private int readBufferPosition;
    private double D=0;

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_autonomous_mode);

        initial_settings();
        turnOnBT();
        // listenAndEvolve();

    }

    private void initial_settings() {
        currentApiVersion = Build.VERSION.SDK_INT;
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT)
        {

            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(flags);
                    }
                }
            });
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        front_arrow_layout = (ImageView) findViewById(R.id.id_front_arrow);
        left_arrow_layout = (ImageView) findViewById(R.id.id_left_arrow);
        right_arrow_layout = (ImageView) findViewById(R.id.id_right_arrow);
        circle_speed_layout = (ImageView) findViewById(R.id.id_circle_speed);
        battery_level_layout = (ImageView) findViewById(R.id.id_battery_level);
        chronometer_layout = (Chronometer) findViewById(R.id.id_chronometer);

        start_textView = (TextView) findViewById(R.id.id_text_start_stop);
        distance_textView = (TextView) findViewById(R.id.id_distance_text);
        bottom_textView = (TextView) findViewById(R.id.id_bottom_text);
        battery_textView = (TextView) findViewById(R.id.id_battery_text);
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/orator_std.otf");
        start_textView.setTypeface(myTypeface);
        distance_textView.setTypeface(myTypeface);
        bottom_textView.setTypeface(myTypeface);
        battery_textView.setTypeface(myTypeface);
        chronometer_layout.setTypeface(myTypeface);

        chronometer_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectedToDevice) {
                    if(!start_enabled) {
                        if (logger) Log.e("ActionStart","ActionStart chronometer_layout ON !!!");
                        startGame(start_enabled = true);
                    } else {
                        if (logger) Log.e("ActionStart","ActionStart chronometer_layout OFF !!!");
                        startGame(start_enabled = false);
                    }
                }
            }
        });

        start_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectedToDevice){
                    if(!start_enabled) {
                        if (logger) Log.e("ActionStart","ActionStart start_textView ON !!!");
                        startGame(start_enabled = true);
                    } else {
                        if (logger) Log.e("ActionStart","ActionStart start_textView OFF !!!");
                        startGame(start_enabled = false);
                    }
                }
            }
        });


    }

    private void turnOnBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getBaseContext(), "No bluetooth adapter available!", Toast.LENGTH_SHORT).show();
            //myLabel.setText("No bluetooth adapter available");
            return;
        }

        if(!mBluetoothAdapter.isEnabled()) {
            if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                mBluetoothAdapter.enable();
                Toast.makeText(getBaseContext(), "Bluetooth off! Enabling now...", Toast.LENGTH_SHORT).show();
            } else {
                //State.INTERMEDIATE_STATE;
            }
        }
    }

    @SuppressLint("NewApi")
    private void connectBTDevice() throws IOException {

        if (mBluetoothAdapter.isEnabled()) {

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals("HC-06")) {
                        mmDevice = device;
                        //Toast.makeText(getBaseContext(), "linvor Found! NOT Connected!", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }

            if(mmDevice == null){
                if (true) Log.e("Error", "Device Negasit!");
                return;
            } else {
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SerialPortService ID

                try {
                    mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
                } catch (IOException e) {
                    if (logger) Log.d("ERR", "In onResume() and socket create failed: " + e.getMessage());
                    return;
                }
                mBluetoothAdapter.cancelDiscovery();

                try {
                    mmSocket.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "Connection Failed!", Toast.LENGTH_SHORT).show();
                    try {
                        disconnectBTDevice();
                    } catch (IOException e2) {
                        if (logger) Log.d("ERR", "Unable to close socket during connection failure " + e2.getMessage());
                        return;
                    }
                    return;
                }

                if (mmSocket.isConnected()) {
                    try {
                        mmOutputStream = mmSocket.getOutputStream();
                        mmInputStream = mmSocket.getInputStream();
                        connectedToDevice = true;
                    } catch (IOException e) {
                        if (logger) Log.d("ERR", "In onResume() and output stream creation failed:" + e.getMessage());
                        connectedToDevice = false;
                        return;
                    }

                    listenAndEvolve();

                    Toast.makeText(getBaseContext(), "Connected to device!", Toast.LENGTH_SHORT).show();
                    initialState("Connected to device!");
                } else {
                    if (logger) Log.e("Error","Connection Failed!!");
                    Toast.makeText(getBaseContext(), "Connection Failed!", Toast.LENGTH_SHORT).show();
                    connectedToDevice = false;
                    disconnectBTDevice();
                    return;
                }
            }
        }
    }

    private void listenAndEvolve() {
        final Handler handler = new Handler();
        final byte delimiter = 10; // This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        Thread workerThread = new Thread(new Runnable() {
            @SuppressLint("NewApi")
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        if (mmSocket.isConnected()) {

                            connectedToDevice = true;

                            int bytesAvailable = mmInputStream.available();
                            if (bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);
                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == delimiter) {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0,	encodedBytes, 0, encodedBytes.length);
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        handler.post(new Runnable() {
                                            public void run() {
                                                receivedString = data;
                                                // if (logger) Log.d("<-- received data: ", String.valueOf(receivedString));

                                                int speedCar=1, rConditions=1, battery=1, distance=1;
                                                if (start_enabled) speedCar = 20; else speedCar=0;
                                                battery = 80;
                                                if (start_enabled) D += 0.009; else D=0;
                                                distance = (int) D;

                                                /**
                                                 * time, speed, run_conditions(forward, back, left, right, slope, stalling, lifted, metal, sensors), battery, distance
                                                 * run_conditions:
                                                 *      1 - forward
                                                 *      2 - backward
                                                 *      3 - left
                                                 *      4 - right
                                                 *      5 - slope up
                                                 *      6 - slope down
                                                 *      7 - stalling
                                                 *      8 - lifted
                                                 *      9 - metal
                                                 *      10 - sensors
                                                 *      0 - stopped
                                                 */

                                                char[] buffer = toCharacterArray(receivedString);
                                                int index = 0;
                                                int aux = 0, number = 0;
                                                int length = receivedString.length();
                                                for(int i=0;i<length;i++)
                                                {
                                                    if (buffer[i] != '#') {
                                                        if(buffer[i] != '@'){
                                                            aux=aux*10+(buffer[i]-'0');
                                                        }
                                                        else
                                                        {
                                                            number = aux;
                                                            switch(index) {
                                                                case 0: if (start_enabled) speedCar = 20;
                                                                else speedCar=0; break;
                                                                case 1: rConditions = 1; break;
                                                                case 2: battery = 80; break;
                                                                case 3: if (start_enabled) D += 0.009;
                                                                else D=0;
                                                                    distance = (int) D ; break;
                                                            }
                                                            index++; aux = 0;
                                                        }
                                                    }
                                                }

                                                Log.e("Received: ", speedCar + ", " + rConditions + ", " + battery + ", " + distance);

                                                setBatteryLevel(battery);

                                                if (runningConditions(rConditions) == 0) {
                                                    changeSpeedAnim(speedCar);
                                                    distance_textView.setText(distance + " cm");
                                                } else {
                                                    distance_textView.setText(0 + " cm");
                                                }

                                            }
                                        });
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            } else {
                                // nu se primeste caractere
                                // if (true) Log.e("Error!","nu se primeste date! ");
                                // initialState("Not receiving data!");
                            }
                        } else {
                            // if (true) Log.e("Error!"," Deconectat de la BT! ");
                            connectedToDevice = false;
                            // initialState("Disconnected!");
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                        connectedToDevice = false;
                        Log.e("Error!", ex.getMessage());
                    }
                }
            }
        });

        workerThread.start();
    }

    private char[] toCharacterArray(String s) {
        if (s == null) {
            return null;
        }
        char[] c = new char[s.length()];
        for (int i = 0; i < s.length(); i++) {
            c[i] = s.charAt(i);
        }

        return c;
    }

    private void setBatteryLevel(int battery) {
        if (battery == 0) {
            battery_level_layout.setImageResource(R.drawable.b_0);
        } else if ( battery > 0 && battery <= 25) {
            battery_level_layout.setImageResource(R.drawable.b_25);
        } else if ( battery > 25 && battery <= 50) {
            battery_level_layout.setImageResource(R.drawable.b_50);
        } else if ( battery > 50 && battery <= 75) {
            battery_level_layout.setImageResource(R.drawable.b_75);
        } else if ( battery > 75 && battery <= 100) {
            battery_level_layout.setImageResource(R.drawable.b_100);
        }
    }

    private void changeSpeedAnim(int progress) {
        AnimationSet animSet = new AnimationSet(false);
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.setFillAfter(true);
        animSet.setFillEnabled(true);

        final RotateAnimation animRotate = new RotateAnimation(prev_value, (float) 273*progress/100,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);

        animRotate.setDuration(200);
        animRotate.setFillAfter(true);
        animSet.addAnimation(animRotate);

        circle_speed_layout.startAnimation(animSet);
        prev_value = (float) 273*progress/100;
    }

    @SuppressLint("NewApi")
    private void sendData(String msg) throws IOException {
        // msg = myTextbox.getText().toString();
        // msg += "\n";
        if (connectedToDevice) {
            if (mmOutputStream != null) {
                try {
                    mmOutputStream.write(msg.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                    disconnectBTDevice();
                }
                if (logger) Log.d("--> Data Sent: ", msg);
            } else if (logger) Log.e("TAG", "Error Send data: mmOutputStream is Null");
        } else {
            if (logger) Log.e("Error", "Disconnected sau startDisabled");
        }
    }

    private void disconnectBTDevice() throws IOException {
        stopWorker = true;

        // mmOutputStream.close();
        // mmInputStream.close();
        // mmSocket.close();

        if (logger) Log.d("TAG", "Disconnecting from device...");
        if (mmOutputStream != null) {
            try {
                mmOutputStream.close();
                mmInputStream.close();
                connectedToDevice = false;
            } catch (IOException e) {
                if (logger) Log.d("TAG", "In onPause() and failed to flush output stream: "	+ e.getMessage());
                Toast.makeText(getBaseContext(), "Socket failed", Toast.LENGTH_SHORT).show();

                return;
            }
        }

        if (mmSocket != null) {
            try {
                mmSocket.close();
            } catch (IOException e2) {
                if (logger) Log.d("TAG", "In onPause() and failed to close socket." + e2.getMessage());
                Toast.makeText(getBaseContext(), "Socket failed!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        battery_level_layout.setImageResource(R.drawable.b_0);
        Toast.makeText(getBaseContext(), "Disconnected from device!", Toast.LENGTH_SHORT).show();
        initialState("Disconnected!");
    }

    private void goingForward() {
        front_arrow_layout.setImageResource(R.drawable.front_active_colour);
        left_arrow_layout.setImageResource(R.drawable.left_inactive);
        right_arrow_layout.setImageResource(R.drawable.right_inactive);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setDuration(100);

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        /*front_arrow_layout.setAnimation(animation);
        left_arrow_layout.setAnimation(animation);
        right_arrow_layout.setAnimation(animation);*/
    }

    private void goingLeft(){
        if (!back_button_pressed) front_arrow_layout.setImageResource(R.drawable.front_active_colour);
        if (!back_button_pressed) left_arrow_layout.setImageResource(R.drawable.left_active_colour);
        right_arrow_layout.setImageResource(R.drawable.right_inactive);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setDuration(100);

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        /*front_arrow_layout.setAnimation(animation);
        left_arrow_layout.setAnimation(animation);
        right_arrow_layout.setAnimation(animation);*/
    }

    private void goingRight(){
        if (!back_button_pressed) front_arrow_layout.setImageResource(R.drawable.front_active_colour);
        left_arrow_layout.setImageResource(R.drawable.left_inactive);
        if (!back_button_pressed) right_arrow_layout.setImageResource(R.drawable.right_active_colour);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setDuration(100);

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        /*front_arrow_layout.setAnimation(animation);
        left_arrow_layout.setAnimation(animation);
        right_arrow_layout.setAnimation(animation);*/
    }

    private void goingBackward(){
        front_arrow_layout.setImageResource(R.drawable.back_active_colour);
        left_arrow_layout.setImageResource(R.drawable.left_inactive);
        right_arrow_layout.setImageResource(R.drawable.right_inactive);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setDuration(100);

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        /*front_arrow_layout.setAnimation(animation);
        left_arrow_layout.setAnimation(animation);
        right_arrow_layout.setAnimation(animation);*/
    }

    private void initialState(String bottomText) {
        front_arrow_layout.setImageResource(R.drawable.front_inactive);
        left_arrow_layout.setImageResource(R.drawable.left_inactive);
        right_arrow_layout.setImageResource(R.drawable.right_inactive);
        circle_speed_layout.setImageResource(R.drawable.circle_arrow);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setDuration(100);

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        front_arrow_layout.setAnimation(animation);
        left_arrow_layout.setAnimation(animation);
        right_arrow_layout.setAnimation(animation);
        circle_speed_layout.setAnimation(animation);

        //seekBar_speed_layout.setProgress(0);

        if (!bottomText.isEmpty()) {
            if (bottomText.contains("Discon")) {
                startGame(start_enabled = false);
            }
            bottom_textView.setText(bottomText);
        } else
            bottom_textView.setText("Everything looks good!");

    }

    private int runningConditions(int condition) {
        int state = 0;
        switch (condition) {
            case 1: goingForward();
                bottom_textView.setText("Everything looks good!");
                state = 0;
                break;
            case 2: goingBackward();
                bottom_textView.setText("Everything looks good!");
                state = 0;
                break;
            case 3: goingLeft();
                bottom_textView.setText("Everything looks good!");
                state = 0;
                break;
            case 4: goingRight();
                bottom_textView.setText("Everything looks good!");
                state = 0;
                break;
            case 5: // on slope up
                bottom_textView.setText("On slope up!");
                goingForward();
                state = 0;
                break;
            case 6: // on slope down
                bottom_textView.setText("On slope down!");
                goingForward();
                state = 0;
                break;
            case 7: // stalling
                bottom_textView.setText("Stalling protection activated!");
                circle_speed_layout.setImageResource(R.drawable.circle_arrow_red);
                front_arrow_layout.setImageResource(R.drawable.front_red);
                left_arrow_layout.setImageResource(R.drawable.left_red);
                right_arrow_layout.setImageResource(R.drawable.right_red);

                startGame(start_enabled = false);
                state = 1;
                break;
            case 8: // lifted
                bottom_textView.setText("Car lifted!");
                circle_speed_layout.setImageResource(R.drawable.circle_arrow_red);
                front_arrow_layout.setImageResource(R.drawable.front_red);
                left_arrow_layout.setImageResource(R.drawable.left_red);
                right_arrow_layout.setImageResource(R.drawable.right_red);

                startGame(start_enabled = false);
                state = 1;
                break;
            case 9: // metal detected
                bottom_textView.setText("Metal detected!");
                circle_speed_layout.setImageResource(R.drawable.circle_arrow_red);
                front_arrow_layout.setImageResource(R.drawable.front_red);
                left_arrow_layout.setImageResource(R.drawable.left_red);
                right_arrow_layout.setImageResource(R.drawable.right_red);

                startGame(start_enabled = false);
                state = 1;
                break;
            case 10: // sensors problem
                bottom_textView.setText("Sensor problems!");
                circle_speed_layout.setImageResource(R.drawable.circle_arrow_red);
                front_arrow_layout.setImageResource(R.drawable.front_red);
                left_arrow_layout.setImageResource(R.drawable.left_red);
                right_arrow_layout.setImageResource(R.drawable.right_red);

                startGame(start_enabled = false);
                state = 1;
                break;
            case 0: // stop mode
                bottom_textView.setText("Car stopped!");
                front_arrow_layout.setImageResource(R.drawable.front_inactive);
                left_arrow_layout.setImageResource(R.drawable.left_inactive);
                right_arrow_layout.setImageResource(R.drawable.right_inactive);
                circle_speed_layout.setImageResource(R.drawable.circle_arrow);

                startGame(start_enabled = false);
                state = 1;
                break;
        }


        /*int imagesToShow[] = new int[] { R.drawable.circle_arrow_red, R.drawable.circle_arrow };
        animate(circle_speed_layout, imagesToShow, 0, false);

        imagesToShow = new int[] { R.drawable.front_red, R.drawable.front_inactive };
        animate(front_arrow_layout, imagesToShow, 0, false);

        imagesToShow = new int[] { R.drawable.left_red, R.drawable.left_inactive };
        animate(left_arrow_layout, imagesToShow, 0, false);

        imagesToShow = new int[] { R.drawable.right_red, R.drawable.right_inactive };
        animate(right_arrow_layout, imagesToShow, 0, false);*/

        return state;
    }

    private void animate(final ImageView imageView, final int images[], final int imageIndex, final boolean forever) {

        //imageView <-- The View which displays the images
        //images[] <-- Holds R references to the images to display
        //imageIndex <-- index of the first image to show in images[]
        //forever <-- If equals true then after the last image it starts all over again with the first image resulting in an infinite loop. You have been warned.

        int fadeInDuration = 200; // Configure time values here
        int timeBetween = 300;
        int fadeOutDuration = 200;

        imageView.setVisibility(View.VISIBLE);    //Visible or invisible by default - this will apply when the animation ends
        imageView.setImageResource(images[imageIndex]);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setDuration(fadeInDuration);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
        fadeOut.setStartOffset(fadeInDuration + timeBetween);
        fadeOut.setDuration(fadeOutDuration);

        AnimationSet animation = new AnimationSet(false); // change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setRepeatCount(1);
        imageView.setAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                if (images.length - 1 > imageIndex) {
                    animate(imageView, images, imageIndex + 1,forever); //Calls itself until it gets to the end of the array
                }
                else {
                    if (forever){
                        animate(imageView, images, 0, forever);  //Calls itself to start the animation all over again in a loop if forever = true
                    }
                }
            }
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void startGame(boolean go) {
        if (go) {
            try {
                sendData("s");
            } catch (IOException e) {
                e.printStackTrace();
            }
            chronometer_layout.setBase(SystemClock.elapsedRealtime());
            chronometer_layout.start();
            start_textView.setText("STOP");
        } else {
            try {
                sendData("p");
            } catch (IOException e) {
                e.printStackTrace();
            }
            start_textView.setText("START");
            chronometer_layout.stop();
            D=0;
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                openOptionsMenu();
            }
        }, 5);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (logger) Log.d("TAG", "...On Pause...");

        /*stopWorker = true;
        if (logger) Log.d("TAG", "Disconnecting from device...");
        if (mmOutputStream != null) {
            try {
                mmOutputStream.close();
                mmInputStream.close();
                connectedToDevice = false;
            } catch (IOException e) {
                if (logger) Log.d("TAG", "In onPause() and failed to flush output stream: "	+ e.getMessage());
                Toast.makeText(getBaseContext(), "Socket failed", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (mmSocket != null) {
            try {
                stopWorker = true;
                connectedToDevice = false;
                mmSocket.close();
            } catch (IOException e2) {
                if (logger) Log.d("TAG",
                        "In onPause() and failed to close socket." + e2.getMessage());

                return;
            }
        }

        Toast.makeText(getBaseContext(), "Disconnected from device!", Toast.LENGTH_SHORT).show();*/
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_autonomous_mode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.controlled_mode:
                Intent intent = new Intent(this, ControlledModeActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.test_mode:
                intent = new Intent(this, TestModeActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.connect_bt:
                /*Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            connectBTDevice();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
                thread.start();*/

               /* Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            connectBTDevice();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }, 500 );*/

                try {
                    connectBTDevice();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                break;
            case R.id.disconnect_bt:
                try {
                    disconnectBTDevice();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
            case R.id.finish_app:
                try {
                    disconnectBTDevice();
                } catch (IOException ex) {
                    // ex.printStackTrace();
                }
                if(mBluetoothAdapter.isEnabled())
                    mBluetoothAdapter.disable();
                Toast.makeText(getBaseContext(), "Bluetooth turned off!", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}