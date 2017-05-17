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

    Button getInit, getFinal, calc, next;
    //reset, logData, showLog;
    Handler h;

//    ListView listView;

    boolean isInitPressed, isFinalPressed;


    Button getDiffB;
    //Initial Values;
    private TextView a, b, c, d;

    //Final Values
    private TextView fA, fB, fC, fD;

    //Difference
    private TextView dA, dB, dC, dD;

    //Radii
    private TextView tradA, tradB, tradC, tradD;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btAdapter = BluetoothAdapter.getDefaultAdapter();        // get Bluetooth adapter
        checkBTState();

        isFinalPressed = false;
        isInitPressed = false;

        setContentView(R.layout.activity_main);

        Button log = (Button) findViewById(R.id.vLog);
        log.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, LogActivity.class);
                startActivity(i);
            }
        });


        final Intent i = new Intent(this, slipPerActivity.class);
        next = (Button) findViewById(R.id.next);
        getDiffB = (Button) findViewById(R.id.diff);
        getDiffB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                calDiff();
            }
        });



        next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle b = new Bundle();
                b.putDouble("ARAD", radA);
                b.putDouble("BRAD", radB);
                b.putDouble("CRAD", radC);
                b.putDouble("DRAD", radD);
                i.putExtras(b);
                startActivity(i);
            }
        });


//        logData = (Button) findViewById(R.id.log);
//        showLog = (Button) findViewById(R.id.showL);
//        myDB = new DBHelper(this);
//        reset = (Button) findViewById(R.id.reset);
        calc = (Button) findViewById(R.id.calc);
        distance = (EditText) findViewById(R.id.dist);

        //Init Val
        a = (TextView) findViewById(R.id.A);
        b = (TextView) findViewById(R.id.B);
        c = (TextView) findViewById(R.id.C);
        d = (TextView) findViewById(R.id.D);
        //Final Val
        fA = (TextView) findViewById(R.id.fA);
        fB = (TextView) findViewById(R.id.fB);
        fC = (TextView) findViewById(R.id.fC);
        fD = (TextView) findViewById(R.id.fD);
        //Difference
        dA = (TextView) findViewById(R.id.dA);
        dB = (TextView) findViewById(R.id.dB);
        dC = (TextView) findViewById(R.id.dC);
        dD = (TextView) findViewById(R.id.dD);


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

        getInit = (Button) findViewById(R.id.getInit);
        getFinal = (Button) findViewById(R.id.getFin);

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
                            Toast.makeText(MainActivity.this, sbprint, Toast.LENGTH_SHORT).show();
                            int m = parseWhat();
                            switch (m) {
                                case 0:
                                    if(!checkStat(a,b,c,d))
                                        parseData(sbprint, a, b, c, d);
                                    break;
                                case 1:
                                    if(!checkStat(fA,fB,fC,fD))
                                        parseData(sbprint, fA, fB, fC, fD);
                                    break;
                            }
                        }
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            }


        };


        calc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                calcVal();
            }
        });

        getInit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                isInitPressed = true;
                getInit.setEnabled(false);
                getFinal.setEnabled(true);
                isFinalPressed = false;

//                while(checkStat(a,b,c,d)==false){
//                    mConnectedThread.read();
//                }
//                mConnectedThread.read();
            }
        });

        getFinal.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                isFinalPressed = true;
                getFinal.setEnabled(false);
                getInit.setEnabled(true);
                isInitPressed = false;
//                distance.setVisibility(View.VISIBLE);
//                calc.setVisibility(View.VISIBLE);
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


    private int parseWhat() {
        if (isInitPressed)
            return 0;

        else if (isFinalPressed)
            return 1;

        else
            return -1;
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
        String aVal = dA.getText().toString();
        String bVal = dB.getText().toString();
        String cVal = dC.getText().toString();
        String dVal = dD.getText().toString();
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

//        next.setVisibility(View.VISIBLE);
//        tradA.setVisibility(View.VISIBLE);
//        tradB.setVisibility(View.VISIBLE);
//        tradC.setVisibility(View.VISIBLE);
//        tradD.setVisibility(View.VISIBLE);
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

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
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


    private void parseData(String s, TextView aA, TextView bB, TextView cC, TextView dD) {
        String value = "";
        int i = 0;
        char temp;
        if (s.charAt(0) == '<') {
            temp = s.charAt(1);
            switch (temp) {
                case 'A':
                    i = 0;
                    break;
                case 'B':
                    i = 1;
                    break;
                case 'C':
                    i = 2;
                    break;
                case 'D':
                    i = 3;
                    break;
            }

            for (int j = 2; s.charAt(j) != '>'; j++)
                value += Character.toString(s.charAt(j));
        }


        switch (i) {
            case 0:
                aA.setText(value);
                break;
            case 1:
                bB.setText(value);
                break;
            case 2:
                cC.setText(value);
                break;
            case 3:
                dD.setText(value);
                break;
        }
    }

    private boolean checkStat(TextView one, TextView two, TextView three, TextView four) {
        String curA, curB, curC, curD;
        curA = one.getText().toString();
        curB = two.getText().toString();
        curC = three.getText().toString();
        curD = four.getText().toString();
        if (!curA.isEmpty() && !curB.isEmpty() && !curC.isEmpty() && !curD.isEmpty()) {
            return true;
        }

        return false;
    }

    private void calDiff() {
        double iA, iB, iC, iD, fiA, fiB, fiC, fiD, diffA, diffB, diffC, diffD;
        iA = Double.parseDouble(a.getText().toString());
        iB = Double.parseDouble(b.getText().toString());
        iC = Double.parseDouble(c.getText().toString());
        iD = Double.parseDouble(d.getText().toString());

        fiA = Double.parseDouble(fA.getText().toString());
        fiB = Double.parseDouble(fB.getText().toString());
        fiC = Double.parseDouble(fC.getText().toString());
        fiD = Double.parseDouble(fD.getText().toString());

        diffA = fiA - iA;
        diffB = fiB - iB;
        diffC = fiC - iC;
        diffD = fiD - iD;

        dA.setText(String.valueOf(diffA));
        dB.setText(String.valueOf(diffB));
        dC.setText(String.valueOf(diffC));
        dD.setText(String.valueOf(diffD));
    }


    @Override
    public void onDestroy() {
        if (mConnectedThread!=null) {
            mConnectedThread.interrupt(); // request to terminate thread in regular way
            try{
                mConnectedThread.join(500); // wait until thread ends or timeout after 0.5 second
            }
            catch (InterruptedException e){

            }

            if (mConnectedThread.isAlive()) {
                // this is needed only when something is wrong with thread, for example hangs in ininitive loop or waits to long for lock to be released by other thread.
                Log.e(TAG, "Serious problem with thread!");
                mConnectedThread.stop();
            }
        }
        super.onDestroy();
    }
}
