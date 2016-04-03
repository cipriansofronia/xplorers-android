package ro.cipry.xplorers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class TestModeActivity extends Activity {

    private int currentApiVersion;
    private ImageView battery_level_layout, left_down_layout, left_up_layout, connected_layout,
            right_down_layout, right_up_layout, stop_button_layout, plus_left_layout, minus_left_layout, plus_right_layout, minus_right_layout;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;
    private TextView distance_textView, details_textView, connected_textView;
    private TextView battery_textView, pwm_textView;
    private Boolean start_enabled = false, logger = true, stopWorker = true, connectedToDevice = false;
    private String receivedString = "0";
    private byte[] readBuffer;
    private int readBufferPosition, c;
    private StringBuilder dataString = new StringBuilder();

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_test_mode);

        initial_settings();
        turnOnBT();
        // listenAndEvolve();

    }

    private void initial_settings() {
        currentApiVersion = android.os.Build.VERSION.SDK_INT;
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

        left_down_layout = (ImageView) findViewById(R.id.idImageL1);
        left_up_layout = (ImageView) findViewById(R.id.idImageL2);
        right_down_layout = (ImageView) findViewById(R.id.idImageR1);
        right_up_layout = (ImageView) findViewById(R.id.idImageR2);
        stop_button_layout = (ImageView) findViewById(R.id.idImageStop);
        battery_level_layout = (ImageView) findViewById(R.id.id_battery_level);
        plus_left_layout = (ImageView) findViewById(R.id.idPlusLeft);
        minus_left_layout = (ImageView) findViewById(R.id.idMinusLeft);
        plus_right_layout = (ImageView) findViewById(R.id.idPlusRight);
        minus_right_layout = (ImageView) findViewById(R.id.idMinusRight);
        connected_layout = (ImageView) findViewById(R.id.idConnectedImg);

        details_textView = (TextView) findViewById(R.id.idDetailsText);
        connected_textView = (TextView) findViewById(R.id.idConnectedText);
        battery_textView = (TextView) findViewById(R.id.id_battery_text);
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/orator_std.otf");
        details_textView.setTypeface(myTypeface);
        battery_textView.setTypeface(myTypeface);
        connected_textView.setTypeface(myTypeface);

        stop_button_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (true) {
                    try {
                        sendData("0");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else
                    Toast.makeText(getApplicationContext(), "NOT Connected!", Toast.LENGTH_SHORT).show();
            }
        });

        left_down_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (true) {
                    try {
                        sendData("2");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else
                    Toast.makeText(getApplicationContext(), "NOT Connected!", Toast.LENGTH_SHORT).show();
            }
        });

        left_up_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (true) {
                    try {
                        sendData("1");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else
                    Toast.makeText(getApplicationContext(), "NOT Connected!", Toast.LENGTH_SHORT).show();
            }
        });

        right_down_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (true) {
                    try {
                        sendData("8");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else
                    Toast.makeText(getApplicationContext(), "NOT Connected!", Toast.LENGTH_SHORT).show();
            }
        });

        right_up_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (true) {
                    try {
                        sendData("7");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else
                    Toast.makeText(getApplicationContext(), "NOT Connected!", Toast.LENGTH_SHORT).show();
            }
        });

        plus_left_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (true) {
                    try {
                        sendData("-");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else
                    Toast.makeText(getApplicationContext(), "NOT Connected!", Toast.LENGTH_SHORT).show();
            }
        });

        minus_left_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (true) {
                    try {
                        sendData("+");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else
                    Toast.makeText(getApplicationContext(), "NOT Connected!", Toast.LENGTH_SHORT).show();
            }
        });

        plus_right_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (true) {
                    try {
                        sendData("*");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else
                    Toast.makeText(getApplicationContext(), "NOT Connected!", Toast.LENGTH_SHORT).show();
            }
        });

        minus_right_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (true) {
                    try {
                        sendData("/");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else
                    Toast.makeText(getApplicationContext(), "NOT Connected!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void appendTextAndScroll(String text) {
        if (details_textView != null) {
            details_textView.setText(text);
            final Layout layout = details_textView.getLayout();
            if (layout != null) {
                // find the amount we need to scroll.  This works by
                // asking the TextView's internal layout for the position
                // of the final line and then subtracting the TextView's height
                final int scrollAmount = layout.getLineTop(details_textView.getLineCount()) - details_textView.getHeight();
                // if there is no need to scroll, scrollAmount will be <=0
                if (scrollAmount > 0)
                    details_textView.scrollTo(0, scrollAmount);
                else
                    details_textView.scrollTo(0, 0);
            }
        }
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

                    Toast.makeText(getBaseContext(), "Bluetooth Connected!", Toast.LENGTH_SHORT).show();
                    connected_layout.setImageResource(R.drawable.on);
                    connected_textView.setText("Connected!");
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
                                                if (logger) Log.d("<-- received data: ", String.valueOf(receivedString));


                                                if (dataString.length() > 4000) {
                                                    dataString.delete(0, dataString.length());
                                                    details_textView.setText(dataString.toString());
                                                }

                                                int u1=0, u2=0, u3=0, u4=0, rConditions=0, battery=0;
                                                /**
                                                 * sonici, run_conditions(forward, back, left, right, slope, stalling, lifted, metal, sensors), battery
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
                                                                case 0: u1 = number; break;
                                                                case 1: u2 = number; break;
                                                                case 2: u3 = number; break;
                                                                case 3: u4 = number; break;
                                                                case 4: rConditions = number; break;
                                                                case 5: battery = number; break;
                                                            }
                                                            index++; aux = 0;
                                                        }
                                                    }
                                                }

                                                Log.e("Received: ", u1 + ", " + u2 + ", " + u3 + ", " + u4 + ", "
                                                        + rConditions + ", " + (battery=80) + " | C: " + connectedToDevice);

                                                setBatteryLevel(battery);

                                                if (runningConditions(rConditions, u1, u2, u3, u4) == 0) {
                                                    dataString.append("U1: ").append(u1).append(" U2: ").append(u2)
                                                            .append(" U3: ").append(u3).append(" U4: ").append(u4).append("\n");
                                                    details_textView.setMovementMethod(new ScrollingMovementMethod());
                                                    appendTextAndScroll(dataString.toString());
                                                }

                                            }
                                        });
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            } else {
                                // nu se primeste caractere
                                // if (true) Log.e("Error!"," nu se primeste date! ");
                                // initialState("Not receiving data!");
                            }
                        } else {
                            if (true) Log.e("Error!"," Deconectat de la BT! ");
                            connectedToDevice = false;
                            // initialState("Disconnected!");
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                        connectedToDevice = false;
                        Log.e("Error!","" + ex.getMessage());
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
        } else if ( battery > 50 && battery <= 60) {
            battery_level_layout.setImageResource(R.drawable.b_75);
        } else if ( battery > 60 && battery <= 100) {
            battery_level_layout.setImageResource(R.drawable.b_100);
        }
    }

    private int runningConditions(int condition, int u1, int u2, int u3, int u4) {
        int state = 0;
        switch (condition) {
            case 1: // forward
                state = 0;
                break;
            case 2: //
                state = 0;
                break;
            case 3:
                state = 0;
                break;
            case 4:
                ;
                state = 0;
                break;
            case 5: // on slope up
                dataString.append("On slope UP!").append("\n");
                details_textView.setMovementMethod(new ScrollingMovementMethod());
                appendTextAndScroll(dataString.toString());
                state = 2;
                break;
            case 6: // on slope down
                dataString.append("On slope DOWN!").append("\n");
                details_textView.setMovementMethod(new ScrollingMovementMethod());
                appendTextAndScroll(dataString.toString());
                state = 2;
                break;
            case 7: // stalling
                dataString.append("WARNING! Stalling protection activated!").append("\n");
                details_textView.setMovementMethod(new ScrollingMovementMethod());
                appendTextAndScroll(dataString.toString());
                state = 1;
                break;
            case 8: // lifted
                dataString.append("WARNING! Car lifted!").append("\n");
                details_textView.setMovementMethod(new ScrollingMovementMethod());
                appendTextAndScroll(dataString.toString());
                state = 1;
                break;
            case 9: // metal detected
                dataString.append("WARNING! Metal detected!").append("\n");
                details_textView.setMovementMethod(new ScrollingMovementMethod());
                appendTextAndScroll(dataString.toString());
                state = 1;
                break;
            case 10: // sensors problem
                dataString.append("WARNING! Sensors problems!").append("\n").append("U1: ").append(u1).append(" U2: ").append(u2)
                        .append(" U3: ").append(u3).append(" U4: ").append(u4).append("\n");
                details_textView.setMovementMethod(new ScrollingMovementMethod());
                appendTextAndScroll(dataString.toString());
                state = 1;
                break;
            case 0: // stop mode
                dataString.append("WARNING! Car stopped!").append("\n");
                details_textView.setMovementMethod(new ScrollingMovementMethod());
                appendTextAndScroll(dataString.toString());
                state = 1;
                break;
        }
        return state;
    }

    @SuppressLint("NewApi")
    private void sendData(String msg) throws IOException {
        // msg = myTextbox.getText().toString();
        // msg += "\n";
        if (true) {
            if (mmOutputStream != null) {
                try {
                    mmOutputStream.write(msg.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                    disconnectBTDevice();
                }
                if (true) Log.d("--> Data Sent: ", msg);
            } else if (logger) Log.e("TAG", "Error Send data: mmOutputStream is Null");
        } else {
            if (logger) Log.e("Error", "connectedToDevice = false");
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
        connected_layout.setImageResource(R.drawable.off);
        battery_level_layout.setImageResource(R.drawable.b_0);
        connected_textView.setText("Disconnected!");
        Toast.makeText(getBaseContext(), "Disconnected from device!", Toast.LENGTH_SHORT).show();
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
        getMenuInflater().inflate(R.menu.menu_test_mode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.autonomous_mode:
                Intent intent = new Intent(this, AutonomousModeActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.controlled_mode:
                intent = new Intent(this, ControlledModeActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.deleteHistory:
                dataString.delete(0, dataString.length());
                details_textView.setText(dataString.toString());
                break;
            case R.id.connect_bt:

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
                    ex.printStackTrace();
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