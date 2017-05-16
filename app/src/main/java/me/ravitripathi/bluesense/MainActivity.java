package me.ravitripathi.bluesense;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "bluetooth2";

    Button start, stop, calc, next;
    //reset, logData, showLog;
    Handler h;

//    ListView listView;

    boolean isStopPressed = false;
    private TextView a, b, c, d, tradA, tradB, tradC, tradD;
    //, , sliA, sliB, sliC, sliD, sliRat;

    private Double radA = 0.0, radB = 0.0, radC = 0.0, radD = 0.0;
    private EditText distance;

    final int RECIEVE_MESSAGE = 1;        // Status  for Handler
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "98:D3:32:30:A3:46";
    private DBHelper myDB;
    String value[] = new String[4];
    Double val[] = new Double[4];
    char wheel = ' ';

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button log = (Button) findViewById(R.id.vLog);
        log.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,LogActivity.class);
                startActivity(i);
            }
        });


        final Intent i = new Intent(this, slipPerActivity.class);
        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle b = new Bundle();
                b.putDouble("ARAD", radA);
                b.putDouble("BRAD", radB);
                b.putDouble("CRAD", radC);
                b.putDouble("DRAD", radD);
                i.putExtras(b);
                h.removeCallbacks(mConnectedThread);
//                try {
//                    mConnectedThread.join();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                startActivity(i);
            }
        });

        for (int J = 0; J < 4; J++) {
            value[J] = "";
            val[J] = 0.00;
        }

//        logData = (Button) findViewById(R.id.log);
//        showLog = (Button) findViewById(R.id.showL);
//        myDB = new DBHelper(this);
//        reset = (Button) findViewById(R.id.reset);
        calc = (Button) findViewById(R.id.calc);
        distance = (EditText) findViewById(R.id.dist);
        a = (TextView) findViewById(R.id.A);
        b = (TextView) findViewById(R.id.B);
        c = (TextView) findViewById(R.id.C);
        d = (TextView) findViewById(R.id.D);
        tradA = (TextView) findViewById(R.id.radA);
        tradB = (TextView) findViewById(R.id.radB);
        tradC = (TextView) findViewById(R.id.radC);
        tradD = (TextView) findViewById(R.id.radD);
//        sliA = (TextView) findViewById(R.id.slipA);
//        sliB = (TextView) findViewById(R.id.slipB);
//        sliC = (TextView) findViewById(R.id.slipC);
//        sliD = (TextView) findViewById(R.id.slipD);
//        sliRat = (TextView) findViewById(R.id.slipRat);


//
//        reset.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                reset();
//            }
//        });
        //        listView = (ListView) findViewById(R.id.lV);
        //        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        //        listView.setAdapter(listAdapter);

        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                    // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);                    // create string from bytes array
                        sb.append(strIncom);                                                // append string
                        int endOfLineIndex = sb.indexOf("\r\n");                            // determine the end-of-line
                        if (endOfLineIndex > 0) {                                            // if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex);                // extract string
                            sb.delete(0, sb.length());
                            Toast.makeText(MainActivity.this,sbprint,Toast.LENGTH_SHORT).show();
                            // and clear
//                            list.add("Data from Arduino: " + sbprint);
                                parseData(sbprint);
//                            listAdapter.notifyDataSetChanged();
                            stop.setEnabled(true);
                            start.setEnabled(true);
                        }
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            }


        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();        // get Bluetooth adapter
        checkBTState();


        calc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                calcVal();
            }
        });

        start.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                start.setEnabled(false);
                mConnectedThread = new ConnectedThread(btSocket);
                mConnectedThread.start();
                distance.setVisibility(View.GONE);
                calc.setVisibility(View.GONE);
                next.setVisibility(View.GONE);
                tradA.setVisibility(View.GONE);
                tradB.setVisibility(View.GONE);
                tradC.setVisibility(View.GONE);
                tradD.setVisibility(View.GONE);
                isStopPressed = false;
                stop.setEnabled(true);
//                listView.setAdapter(listAdapter);
                mConnectedThread.write("1");    // Send "1" via Bluetooth
                //Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            }
        });

        stop.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                stop.setEnabled(false);
                start.setEnabled(true);
                isStopPressed = true;
                distance.setVisibility(View.VISIBLE);
                calc.setVisibility(View.VISIBLE);
//                mConnectedThread.write("0");    // Send "0" via Bluetooth
//                try {
//                    mConnectedThread.join();
//                } catch (InterruptedException e) {
//
////                }
//                list.clear();
//                listAdapter.notifyDataSetChanged();
//                listView.setAdapter(null);

                //Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double calRad(int dist, double turns) {
        double deno = 2 * 3.14 * turns;
        double numo = dist;
        double ratio = numo / deno;
        return ratio;

    }

    //


//    private void reset() {
//        tradA.setVisibility(View.GONE);
//        tradB.setVisibility(View.GONE);
//        tradC.setVisibility(View.GONE);
//        tradD.setVisibility(View.GONE);
//        sliA.setVisibility(View.GONE);
//        sliB.setVisibility(View.GONE);
//        sliC.setVisibility(View.GONE);
//        sliD.setVisibility(View.GONE);
//        distance.setVisibility(View.GONE);
//        calc.setVisibility(View.GONE);
//        sliRat.setVisibility(View.GONE);
//        a.setText("");
//        b.setText("");
//        c.setText("");
//        d.setText("");
//    }

//    private void store() {
//
//    }

    private void calcVal() {
        String aVal = a.getText().toString();
        String bVal = b.getText().toString();
        String cVal = c.getText().toString();
        String dVal = d.getText().toString();
        Double A = 0.0, B = 0.0, C = 0.0, D = 0.0;
        if (!aVal.isEmpty())
            A = Double.parseDouble(aVal);
        if (!bVal.isEmpty())
            B = Double.parseDouble(bVal);
        if (!cVal.isEmpty())
            C = Double.parseDouble(cVal);
        if (!dVal.isEmpty())
            D = Double.parseDouble(dVal);

        String di = distance.getText().toString();
        if (!di.isEmpty()) {
            int disa = Integer.parseInt(di);
            radA = calRad(disa, A);
            radB = calRad(disa, B);
            radC = calRad(disa, C);
            radD = calRad(disa, D);

//            slipA = slipPer(disa, radA, A);
//            slipB = slipPer(disa, radB, B);
//            slipC = slipPer(disa, radC, C);
//            slipD = slipPer(disa, radD, D);
//
//            if (A != 0.0 && radA != 0.0)
//                slipRat = slipRAT(radA, radC, A, C);
        }

        if (!aVal.isEmpty()) {
            tradA.setText("Radius of wheel A: " + radA.toString());
//            sliA.setText("Slip Percentage of A: " + slipA.toString());
        }

        if (!bVal.isEmpty()) {
            tradB.setText("Radius of wheel B: " + radB.toString());

//            sliB.setText("Slip Percentage of B: " + slipB.toString());
        }

        if (!cVal.isEmpty()) {
            tradC.setText("Radius of wheel C: " + radC.toString());

//            sliC.setText("Slip Percentage of C: " + slipC.toString());
        }

        if (!dVal.isEmpty()) {
            tradD.setText("Radius of wheel D: " + radD.toString());
//            sliD.setText("Slip Percentage of D: " + slipD.toString());
        }

//        if (A != 0.0 && radA != 0.0) {
//            sliRat.setText("Slip Ratio : " + slipRat.toString());
//        }

        next.setVisibility(View.VISIBLE);
        tradA.setVisibility(View.VISIBLE);
        tradB.setVisibility(View.VISIBLE);
        tradC.setVisibility(View.VISIBLE);
        tradD.setVisibility(View.VISIBLE);
//        sliA.setVisibility(View.VISIBLE);
//        sliB.setVisibility(View.VISIBLE);
//        sliC.setVisibility(View.VISIBLE);
//        sliD.setVisibility(View.VISIBLE);
//        sliRat.setVisibility(View.VISIBLE);


    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
            return (BluetoothSocket) m.invoke(device, MY_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }


        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "....Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");
//
//        mConnectedThread = new ConnectedThread(btSocket);
//        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();        // Send to message queue Handler
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "...Data to send: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }
    }

    private void parseData(String s) {
        int i = 0;

        if (s.charAt(0) == '<') {
            if (s.charAt(1) == 'A' || s.charAt(1) == 'B' || s.charAt(1) == 'C' || s.charAt(1) == 'D') {
                switch (s.charAt(1)) {
                    case 'A':
                        wheel = 'A';
                        i = 0;
                        break;
                    case 'B':
                        wheel = 'B';
                        i = 1;
                        break;
                    case 'C':
                        wheel = 'C';
                        i = 2;
                        break;
                    case 'D':
                        wheel = 'D';
                        i = 3;
                        break;
                }

                for (int j = 2; s.charAt(j) != '>'; j++)
                    if (Character.toString(s.charAt(j)) != null)
                        value[i] += Character.toString(s.charAt(j));
            }

            try {
                val[i] = Double.parseDouble(value[i]);
            } catch (Exception e) {

            }
        }
        String W = Character.toString(wheel);
        System.out.println(W + val[i]);

        switch (i) {
            case 0:
                a.setText(String.valueOf(val[i]));
                break;
            case 1:
                b.setText(String.valueOf(val[i]));
                break;
            case 2:
                c.setText(String.valueOf(val[i]));
                break;
            case 3:
                d.setText(String.valueOf(val[i]));
                break;
        }
    }

}
