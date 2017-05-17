package me.ravitripathi.bluesense;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class slipPerActivity extends AppCompatActivity {

    Button getInit, getFinal;
    Handler h;

    boolean isInitPressed, isFinalPressed;
    private ConnectedThread mConnectedThread;

    Button getDiffB;
    //Initial Values;
    private TextView a, b, c, d;

    //Final Values
    private TextView fA, fB, fC, fD;

    //Difference
    private TextView dA, dB, dC, dD;

    private EditText distance;

    final int RECIEVE_MESSAGE = 1;        // Status  for Handler
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "98:D3:32:30:A3:46";


////////////////////////////////////////////////////////////////////////////


    private ScrollView scrollView;
    private DBHelper myDB;


    String TAG = "slipPerActivity";

    TextView TslipA, TslipB, TslipC, TslipD, TsliRat, TsliRat2;

    Double radA, radB, radC, radD;
    Double slipA = 0.0, slipB = 0.0, slipC = 0.0, slipD = 0.0;
    Double slipRat = 0.0, slipRat2 = 0.0 ;

    Button calc, log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slip_per);
        Intent i = getIntent();
        Bundle bun = i.getExtras();

        myDB = new DBHelper(this);

        getInit = (Button) findViewById(R.id.getInit);
        getFinal = (Button) findViewById(R.id.getFin);


        radA = bun.getDouble("ARAD");
        radB = bun.getDouble("BRAD");
        radC = bun.getDouble("CRAD");
        radD = bun.getDouble("DRAD");

        btAdapter = BluetoothAdapter.getDefaultAdapter();        // get Bluetooth adapter
        checkBTState();

        isFinalPressed = false;
        isInitPressed = false;


        getDiffB = (Button) findViewById(R.id.diff);
        getDiffB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calDiff();
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


        TslipA = (TextView) findViewById(R.id.slipA);
        TslipB = (TextView) findViewById(R.id.slipB);
        TslipC = (TextView) findViewById(R.id.slipC);
        TslipD = (TextView) findViewById(R.id.slipD);
        TsliRat = (TextView) findViewById(R.id.SLIPRAT);
        TsliRat2 = (TextView) findViewById(R.id.SLIPRAT2);

        log = (Button) findViewById(R.id.log);
        scrollView = (ScrollView) findViewById(R.id.scroll);


        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                (SNO integer primary key AUTOINCREMENT, ASLIP text,BSLIP text,CSLIP text, DSLIP text,SLIPRAT text
                myDB.insertData(slipA.toString(), slipB.toString(), slipC.toString(), slipD.toString(), slipRat.toString());
                Toast.makeText(slipPerActivity.this, "Data Logged Successfully", Toast.LENGTH_SHORT).show();
            }
        });

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
                            Toast.makeText(slipPerActivity.this, sbprint, Toast.LENGTH_SHORT).show();
                            int m = parseWhat();
                            switch (m) {
                                case 0:
                                    if (!checkStat(a, b, c, d))
                                        parseData(sbprint, a, b, c, d);
                                    break;
                                case 1:
                                    if (!checkStat(fA, fB, fC, fD))
                                        parseData(sbprint, fA, fB, fC, fD);
                                    break;
                            }
                        }
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            }


        };


        calc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calnSet();
            }
        });

        getInit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isInitPressed = true;
                getInit.setEnabled(false);
                getFinal.setEnabled(true);
                isFinalPressed = false;
            }
        });

        getFinal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isFinalPressed = true;
                getFinal.setEnabled(false);
                getInit.setEnabled(true);
                isInitPressed = false;
            }
        });

    }

    private int parseWhat() {
        if (isInitPressed)
            return 0;

        else if (isFinalPressed)
            return 1;

        else
            return -1;
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


    private double slipPer(int dist, double rad, double turns) {

        double a = 2 * 3.14 * rad * turns;
        double b = a - dist;
        double c = b / dist;
        return c * 100;
//        return ((((2 * 3.14 * rad) * turns) - dist) / dist) * 100;
    }

    private void calnSet() {
        Double Aturns = 0.0, Bturns = 0.0, Cturns = 0.0, Dturns = 0.0;
        int dist = 0;
        try {
            String aW = dA.getText().toString();
            String bW = dB.getText().toString();
            String cW = dC.getText().toString();
            String dW = dD.getText().toString();
            String dis = distance.getText().toString();

            if (!aW.isEmpty())
                Aturns = Double.parseDouble(aW);
            if (!bW.isEmpty())
                Bturns = Double.parseDouble(bW);
            if (!cW.isEmpty())
                Cturns = Double.parseDouble(cW);
            if (!dW.isEmpty())
                Dturns = Double.parseDouble(dW);

            if (!dis.isEmpty())
                dist = Integer.parseInt(dis);

            slipA = slipPer(dist, radA, Aturns);
            slipB = slipPer(dist, radB, Bturns);
            slipC = slipPer(dist, radC, Cturns);
            slipD = slipPer(dist, radD, Dturns);
            TslipA.setText("Slip% for A: " + slipA.toString());
            TslipB.setText("Slip% for B: " + slipB.toString());
            TslipC.setText("Slip% for C: " + slipC.toString());
            TslipD.setText("Slip% for D: " + slipD.toString());

            //1:a, 2:c
            //radA=R1, radD=R2, Aturns = N1, Dturns = N2
            if (radA != 0 && radC != 0 && Aturns != 0) {
                Double r1N1 = radA * Aturns;
                Double r2N2 = radC * Cturns;
                slipRat = (r1N1 - r2N2) / r1N1;
                TsliRat.setText("Slip Ratio (a,c) :" + slipRat.toString());
            }

            //1:b, 2:d
            if (radB != 0 && radD != 0 && Bturns != 0) {
                Double r1N1 = radB * Bturns;
                Double r2N2 = radD * Dturns;
                slipRat2 = (r1N1 - r2N2) / r1N1;
                TsliRat2.setText("Slip Ratio (b,d) :" + slipRat2.toString());
            }


        } catch (NumberFormatException e) {
            Toast.makeText(this, "Either the provided distance or number of turns is in incorrect format", Toast.LENGTH_LONG).show();
        }
        scrollView.setVisibility(View.VISIBLE);
    }
}
