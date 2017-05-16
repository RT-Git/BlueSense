package me.ravitripathi.bluesense;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
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

    private Boolean isStopPressed = false;

    private ScrollView scrollView;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();

    private DBHelper myDB;


    String TAG = "slipPerActivity";

    TextView a, b, c, d, TslipA, TslipB, TslipC, TslipD, TsliRat;

    final int RECIEVE_MESSAGE = 1;        // Status  for Handler
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ConnectedThread mConnectedThread;
    Handler h;

    Double radA, radB, radC, radD;
    Double slipA=0.0, slipB=0.0, slipC=0.0, slipD=0.0;
    Double slipRat = 0.0;

    EditText distance;
    Button calc, log;

    private static String address = "98:D3:32:30:A3:46";
    Button start, stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slip_per);
        Intent i = getIntent();
        Bundle bun = i.getExtras();

        myDB = new DBHelper(this);
        radA = bun.getDouble("ARAD");
        radB = bun.getDouble("BRAD");
        radC = bun.getDouble("CRAD");
        radD = bun.getDouble("DRAD");

        TextView radiusA = (TextView) findViewById(R.id.rA);
        TextView radiusB = (TextView) findViewById(R.id.rB);
        TextView radiusC = (TextView) findViewById(R.id.rC);
        TextView radiusD = (TextView) findViewById(R.id.rD);

        a = (TextView) findViewById(R.id.As);
        b = (TextView) findViewById(R.id.Bs);
        c = (TextView) findViewById(R.id.Cs);
        d = (TextView) findViewById(R.id.Ds);

        TslipA = (TextView) findViewById(R.id.slipA);
        TslipB = (TextView) findViewById(R.id.slipB);
        TslipC = (TextView) findViewById(R.id.slipC);
        TslipD = (TextView) findViewById(R.id.slipD);
        TsliRat = (TextView) findViewById(R.id.SLIPRAT);

        log = (Button) findViewById(R.id.log);
        scrollView = (ScrollView) findViewById(R.id.scroll);


        distance = (EditText) findViewById(R.id.dist);
        calc = (Button) findViewById(R.id.calc);

        radiusA.setText("A Ref Radius : " + radA.toString());
        radiusB.setText("B Ref Radius : " + radB.toString());
        radiusC.setText("C Ref Radius : " + radC.toString());
        radiusD.setText("D Ref Radius : " + radD.toString());

        start = (Button) findViewById(R.id.start);                    // button LED ON
        stop = (Button) findViewById(R.id.stop);                // button LED OFF

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
                            Toast.makeText(slipPerActivity.this,sbprint,Toast.LENGTH_SHORT).show();
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


        calc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calnSet();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                start.setEnabled(false);
                mConnectedThread = new ConnectedThread(btSocket);
                mConnectedThread.start();
                scrollView.setVisibility(View.VISIBLE);
                isStopPressed = false;
                stop.setEnabled(true);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stop.setEnabled(false);
                start.setEnabled(true);
                isStopPressed = true;

            }
        });

        btAdapter = BluetoothAdapter.getDefaultAdapter();        // get Bluetooth adapter
        checkBTState();


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

    }

    private void parseData(String s) {

        int i = 0;
        String value = "";
        Double val = 0.0;
        if (s.charAt(0) == '<') {
            if (s.charAt(1) == 'A' || s.charAt(1) == 'B' || s.charAt(1) == 'C' || s.charAt(1) == 'D') {
                switch (s.charAt(1)) {
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
                    if (Character.toString(s.charAt(j)) != null)
                        value += Character.toString(s.charAt(j));
            }

            try {
                val = Double.parseDouble(value);
            } catch (Exception e) {

            }
        }


        switch (i) {
            case 0:
                a.setText(value);
                break;
            case 1:
                b.setText(value);
                break;
            case 2:
                c.setText(value);
                break;
            case 3:
                d.setText(value);
                break;
        }
    }

    private double slipPer(int dist, double rad, double turns) {

        double a = 2 * 3.14 * rad * turns;
        double b = a - dist;
        double c = b / dist;
        return c * 100;
//        return ((((2 * 3.14 * rad) * turns) - dist) / dist) * 100;
    }

    private void calnSet() {
        Double Aturns=0.0, Bturns=0.0, Cturns=0.0, Dturns=0.0;
        int dist = 0;
        try {
            String aW = a.getText().toString();
            String bW = b.getText().toString();
            String cW = c.getText().toString();
            String dW = d.getText().toString();
            String dis = distance.getText().toString();

            if(!aW.isEmpty())
                Aturns = Double.parseDouble(aW);
            if(!bW.isEmpty())
                Bturns = Double.parseDouble(bW);
            if(!cW.isEmpty())
                Cturns = Double.parseDouble(cW);
            if(!dW.isEmpty())
                Dturns = Double.parseDouble(dW);

            if(!dis.isEmpty())
                dist = Integer.parseInt(dis);

            slipA = slipPer(dist, radA, Aturns);
            slipB = slipPer(dist, radB, Bturns);
            slipC = slipPer(dist, radC, Cturns);
            slipD = slipPer(dist, radD, Dturns);
            TslipA.setText("Slip% for A: "+slipA.toString());
            TslipB.setText("Slip% for B: "+slipB.toString());
            TslipC.setText("Slip% for C: "+slipC.toString());
            TslipD.setText("Slip% for D: "+slipD.toString());

            //radA=R1, radD=R2, Aturns = N1, Dturns = N2
            if(radA!=0&&radD!=0&&Aturns!=0){
                Double r1N1 = radA*Aturns;
                Double r2N2 = radD*Dturns;
                slipRat = (r1N1-r2N2)/r1N1;
                TsliRat.setText("Slip Ratio: "+slipRat.toString());
            }


        }
        catch (NumberFormatException e){
            Toast.makeText(this, "Either the provided distance or number of turns is in incorrect format",Toast.LENGTH_LONG).show();
        }

        TslipA.setVisibility(View.VISIBLE);
        TslipB.setVisibility(View.VISIBLE);
        TslipC.setVisibility(View.VISIBLE);
        TslipD.setVisibility(View.VISIBLE);
        TsliRat.setVisibility(View.VISIBLE);
        log.setVisibility(View.VISIBLE);
    }

}
