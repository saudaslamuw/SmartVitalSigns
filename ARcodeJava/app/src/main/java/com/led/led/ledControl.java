package com.led.led;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Scanner;
import java.io.OutputStream;
import java.util.ArrayList;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.jtransforms.fft.*;
import flanagan.complex.Complex;
import flanagan.control.BlackBox;
import flanagan.plot.PlotPoleZero;

public class ledControl extends Activity {
    public static double Po;
    public static int f=1;
    public static int optimalorder=0;
    public static int RRloc=0;
    public static int arorder=18;
    double[] cof=new double[arorder];
    public static int RRloc1=0;
    public static int arorder1=5;
    double[] cof1=new double[arorder1];

    final int handlerState = 0;        				 //used to identify handler message
    private static final String hostname = "192.168.8.103";//use public ip or ipv4 in ipconfig for local host
    private static final int portnumber= 7000;
    private Socket socket= null;
    private static final String debugString= "debug";
    Handler bluetoothIn;
    Button btnOn, btnOff, btnDis;
    TextView outputText,outputTextt,outputTexttt,outputTextttt;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Complex[] zerosAndPoles = new Complex[arorder];
    Complex[] zerosAndPoles1 = new Complex[arorder1];
    //PlotPoleZero ppz1 = new PlotPoleZero(null, cof);
    BlackBox ppz= new BlackBox();
    BlackBox ppz1= new BlackBox();

    private StringBuilder recDataString = new StringBuilder();
    private ConnectedThread mConnectedThread;


    private int bps = 0;
    public int bpd = 1;
    public long rr= 0;
    public float temp = 0;
    public long hr= 0;

    // Start of onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();

        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the ledControl
        setContentView(R.layout.activity_led_control);
        new Thread() {

            @Override
            public void run() {

                // rr= 15;
                // bps = rr* 9;
                // bpd = bps / 2;
                // hr= rr+ 50;
                //tem = hr- 20;


                try {

                    Log.i(debugString, "attempting to connect to server");
                    socket = new Socket(hostname, portnumber);
                    Log.i(debugString, "connection established");

                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    bw.write("This is a message from the client.");
                    bw.newLine();
                    bw.flush();

                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    System.out.println("Message from client :" + br.readLine());

                } catch (IOException e) {
                    Log.e(debugString, e.getMessage());
                }

            }

        }.start();
        //call the widgtes
        btnOn = (Button) findViewById(R.id.button2);
      //  btnOff = (Button) findViewById(R.id.button3);
        btnDis = (Button) findViewById(R.id.button4);
        outputText = (TextView) findViewById(R.id.outputText);
        outputTextt = (TextView) findViewById(R.id.outputTextt);
        outputTexttt = (TextView) findViewById(R.id.outputTexttt);
        outputTextttt = (TextView) findViewById(R.id.outputTextttt);



        new ConnectBT().execute(); //Call the class to connect

        //commands to be sent to bluetooth
        btnOn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                try {
                    RRRate();
                    //HeartRate();      //method to turn on
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });


        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect(); //close connection
            }
        });


    }//end On create









    private void Disconnect() {
        if (btSocket != null) //If the btSocket is busy
        {
            try {
                btSocket.close(); //close connection
            } catch (IOException e) {
                msg("Error");
            }
        }
        finish(); //return to the first layout

    }








    private void RRRate() throws IOException
    {outputTexttt.setText("Keep Your finger on the sensor...");
        bluetoothIn = new Handler() {
            int m=80;
            int m1=50;
            public void handleMessage(android.os.Message msg) {


                if (msg.what == handlerState) {
                    outputTexttt.setText("You can remove your finger");
                    //if message is what we want
                    String readMessage = (String) msg.obj;               // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);                //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");

                    if (endOfLineIndex > 0) {
                       // outputTexttt.setText("cal2");
                        float[] adcValue1 = new float[250];
                        float[] adcValue = new float[400];
                        String sensor0 = recDataString.substring(0, endOfLineIndex);
                        String[] valu = sensor0.split("\\r?\\n");
                        temp=Float.parseFloat(valu[400]);

                        int k = 5;
                        int l = 0;
                        for (int i = 0; i < 250; i++) {
                            if (k == 5) {
                                k = 0;
                                adcValue1[l] = Float.parseFloat(valu[i]);
                                l++;
                            }
                            k++;
                        }
                        int s = 5;
                        int q = 0;
                        for (int i = 0; i < 400; i++) {
                            if (s == 5) {
                                s = 0;
                                adcValue[q] = Float.parseFloat(valu[i]);
                                q++;
                            }
                            s++;
                        }
                        double[] arr = new double[m];
                        double[] arr1 = new double[m1];
                        for (int i = 0; i < m; i++) {
                            arr[i] = (double) (adcValue[i]);


                        }
                        for (int i = 0; i < m1; i++) {
                            arr1[i] = (double) (adcValue1[i]);


                        }
                        outputTextttt.setText(String.valueOf(temp));
                       // outputTexttt.setText((int) temp);
                        recDataString.delete(0, recDataString.length());

                        // DataInputStream textFileStream = new DataInputStream(getAssets().open(String.format("HRR6.txt")));
                        // Scanner inFile1 = new Scanner(new File("C:/Users/Saud/Documents/MATLAB/4res.txt"));
                        //      Scanner inFile1 = new Scanner(textFileStream);
                        //   List<Double> temps = new ArrayList<Double>();
                        // int k=50;
                        // while loop
                        // while (inFile1.hasNext())
                        //{
                        // find next line
                        //  double token1 = inFile1.nextDouble();
                        //if(k==50){
                        //  temps.add(token1);
                        //k=0;}
                        //k++;
                        // }
                        //inFile1.close();
                        //    double[] arr = new double[m];
                        //      Double[] tempsArray = temps.toArray(new Double[0]);

                        // for (int i=0;i<m;i++)
                        //{
//            arr[i]=((tempsArray[i]));


                        // }


                        Po = AutoCorr(arr);
                        cof = AR(arr, arorder);
                        cof1 = AR(arr1, arorder1);
                        // System.out.println();
                        // System.out.print("coff:"+"\t");
                        // for (int i=0;i<=arorder;i++)
                        //  System.out.print(cof[i]+"\t");
                        // System.out.println(Po+"\t");
                        //System.out.println();
                        // System.out.println("optimalorder:"+ "\t"+optimalorder);

                        ppz.setZdenom(cof);
                        zerosAndPoles = ppz.getPolesZ();
                        double[] RRnl = new double[arorder / 2];
                        int[] loc = new int[arorder / 2];
                        ppz1.setZdenom(cof1);
                        zerosAndPoles1 = ppz1.getPolesZ();
                        double[] RRnl1 = new double[arorder1 / 2];
                        int[] loc1 = new int[arorder1 / 2];


                        int n = 0;
                        for (int i = 0; i < arorder1; i++) {

                            if (zerosAndPoles1[i].getReal() < 0 && zerosAndPoles1[i].getImag() > 0) {
                                RRnl1[n] = zerosAndPoles1[i].abs();
                                loc1[n] = i;
                                n++;

                            }
                        }

                        //System.out.println();
                        RRloc = min(RRnl1, n);
hr=(Math.round(zerosAndPoles1[loc1[RRloc]].argRad() * 38.2));
                        // System.out.print("RR:"+"\t");
                        // outputText.setText(String.valueOf( Math.round(zerosAndPoles[loc[RRloc]].argRad())*38.2));
                        outputTextt.setText(String.valueOf(Math.round(zerosAndPoles1[loc1[RRloc]].argRad() * 38.2)));


                        // System.out.print((zerosAndPoles[1][loc[RRloc]].argRad())*38.2+"\t");

                        n = 0;
                        for (int i = 0; i < arorder; i++) {

                            if (zerosAndPoles[i].getReal() > 0 && zerosAndPoles[i].getImag() > 0) {
                                if (zerosAndPoles[i].argRad()<=1){
                                RRnl[n] = zerosAndPoles[i].abs();
                                loc[n] = i;
                                n++;}

                            }
                        }

                        //System.out.println();
                        RRloc = min(RRnl, n);
                        rr=(Math.round(zerosAndPoles[loc[RRloc]].argRad() * 38.2));
                        // System.out.print("RR:"+"\t");
                        // outputText.setText(String.valueOf( Math.round(zerosAndPoles[loc[RRloc]].argRad())*38.2));
                        outputText.setText(String.valueOf(Math.round(zerosAndPoles[loc[RRloc]].argRad() * 38.2)));


                        // System.out.print((zerosAndPoles[1][loc[RRloc]].argRad())*38.2+"\t");


                    }
                }
            }
        };
    }

    private void HeartRate() throws IOException {

        int  m= 1460  ;
        float[] x = new float[m/2];


        outputText.setText("Good Job");
        DataInputStream textFileStream = new DataInputStream(getAssets().open(String.format("HRR6.txt")));
        Scanner inFile1 = new Scanner(textFileStream);
        outputText.setText("Good Joba");


        List<Float> temps = new ArrayList<Float>();

        // while loop
        while (inFile1.hasNext()) {
            // find next line
            float token1 = inFile1.nextFloat();
            temps.add(token1);
        }
        inFile1.close();
        float[] input = new float[m];
        Float[] tempsArray = temps.toArray(new Float[0]);

        for (int i=0;i<m;i++) {

            input[i]= (float) filterLow(tempsArray[i]);

            i++;

        }

        performfft(input,m);



    }//end Heart Rate



    public static double[] AR(double[] input, int order){

        int length = input.length;
        double[] eb = new double[length+1];
        double[] ef = new double[length+1];
        double[] coefficient = new double[order+1];
        double[][] autoreg = new double[order+1][order+1];
        double[] pn=new double[order+1];
        double temp1,temp2;
        int k;
        double[] temp3 = new double[order+1];
        double[] a = new double[order+2];



        for (k=1;k<=order;k++) {


            int j;
            int j1 = length - k;
            double num = 0.0;
            double den = 0.0;

            for (j=0;j<j1;j++) {
                temp1 = input[j+k] + ef[j];
                temp2 = input[j] + eb[j];
                num -=  temp1 * temp2;
                den += (temp1 * temp1) + (temp2 * temp2);
            }

            a[k] = 2*num / den;
            temp1 = a[k];
            //  System.out.println(t1+" ");
            //  pn[0]=Po;
            //	pn[f]=(1-(t1*t1))*pn[f-1];///Variance Power
            // new	System.out.println(pn[f]+" "+t1);
            //	f++;



            if (k != 1) {
                for (j=1;j<k;j++){
                    temp3 [j] = a [j] + temp1 * a [k - j];
                }
                for (j=1;j<k;j++){
                    a[j] = temp3[j];
                }
                j1--;
            }

            for (j=0;j<j1;j++) {
                eb [j] += temp1 * ef [j] + temp1 * input [j + k];
                ef [j] = ef [j + 1] + temp1 * eb [j + 1] + temp1 * input [j + 1];
            }

            for (j = 0; j < k; j++)
                autoreg [k][j] = a [j + 1];
        }
        coefficient[0]=1;
        for (int i=1;i<=order;i++){
            coefficient[i] = autoreg[order][i-1];
            // System.out.println(coef[i]+"\t");
        }
        //System.out.println(Po+"\t");
        mdl(pn,length,order);
        return coefficient;

    }//endAR

    public void max(float arr[], int n) {

        double maxV = 0;
        int a = 0;
        for (int i = 5; i < 100; i++) {
            if (arr[i] > maxV) {
                maxV = arr[i];
                a = i;
            }

        }

        outputText.setText(String.valueOf( Math.round(((a)*60/12.86))));

    }// end Max

    public static int min(double MinMdl[],int n)
    {

        double minV =1000;
        int a=0;
        for (int i = 0; i < n; i++)
        {
            if (MinMdl[i] < minV)
            {
                minV = MinMdl[i];
                a=i;


            }

        }

        return a;

    }//end min


    public static void mdl(double[] estvp,int len,int order){
        double[] mdll = new double[order];

        for (int i=0;i<order;i++)
        {
            mdll[i]=len*Math.log(estvp[i])+(i*Math.log(len));
        }
        optimalorder=min(mdll,order);
        // new for (int i=0;i<order;i++){
        // System.out.print(mdll[i]+"\t");}
    }//end mdl

    public static double AutoCorr(double[] inputs)
    {
        int length = inputs.length;
        double sum=0;
        for (int i=0;i<length;i++)
        {
            sum=sum+inputs[i]*inputs[i];
        }


        return sum/length;
    }//end Autocorr
    // Filter Code Low Pass
    private final int Nl = 64;
    private final double[] hl =
            {
                    5.199501680259073E-4,
                    5.337301664976084E-4,
                    7.946826074437926E-4,
                    0.0011274562747372295,
                    0.0015414226608979223,
                    0.00204694277696368,
                    0.002651028670094147,
                    0.0033616391683155998,
                    0.004184590477375452,
                    0.005123418795780208,
                    0.006181076910513048,
                    0.007356413078765654,
                    0.008646943707220056,
                    0.010046839411163048,
                    0.011547604788052678,
                    0.013138137589119574,
                    0.014804159832060838,
                    0.016528872658926774,
                    0.01829314238123631,
                    0.020075537665836498,
                    0.021853342601119763,
                    0.02360219257200764,
                    0.02529710858051712,
                    0.026912719568762487,
                    0.028423937341981766,
                    0.029806761650269357,
                    0.031038640740262774,
                    0.03209915507731296,
                    0.032970439241882185,
                    0.033637503368964414,
                    0.034088841188049994,
                    0.0343164950541179,
                    0.0343164950541179,
                    0.034088841188049994,
                    0.033637503368964414,
                    0.032970439241882185,
                    0.03209915507731296,
                    0.031038640740262774,
                    0.029806761650269357,
                    0.028423937341981766,
                    0.026912719568762487,
                    0.02529710858051712,
                    0.02360219257200764,
                    0.021853342601119763,
                    0.020075537665836498,
                    0.01829314238123631,
                    0.016528872658926774,
                    0.014804159832060838,
                    0.013138137589119574,
                    0.011547604788052678,
                    0.010046839411163048,
                    0.008646943707220056,
                    0.007356413078765654,
                    0.006181076910513048,
                    0.005123418795780208,
                    0.004184590477375452,
                    0.0033616391683155998,
                    0.002651028670094147,
                    0.00204694277696368,
                    0.0015414226608979223,
                    0.0011274562747372295,
                    7.946826074437926E-4,
                    5.337301664976084E-4,
                    5.199501680259073E-4,
            };

    private int iWritel = 0;
    private int iReadl= 0;

    private float[] jl = new float[Nl];

    public double filterLow(float x_in)
    {
        double y = 0.0;

//Store the current input, overwriting the oldest input
        jl[iWritel] = x_in;
        iReadl = iWritel;

//Multiply the filter coefficients by the previous inputs and sum
        for (int i=0; i<Nl; i++)
        {
            y += hl[i] * jl[iReadl];
            iReadl++;
            if(iReadl == jl.length){
                iReadl = 0;
            }
        }

//Increment the input buffer index to the next location
        iWritel--;
        if(iWritel < 0){
            iWritel = jl.length-1;
        }

        return y;
    }

///// End of Java Source Code Implementation of Low Pass Filter ////






    ////////Perform FFT

    public void performfft(float data[],int num)
    {
        float[] x = new float[num/2];
        FloatFFT_1D fftDo = new FloatFFT_1D(data.length);
        float[] fft = new float[data.length * 2];
        System.arraycopy(data, 0, fft, 0, data.length);
        fftDo.realForwardFull(fft);
        for (int i = 0; i < num; i = i + 2)
        {
            x[i / 2] = (float)Math.sqrt((fft[i] * fft[i]) + (fft[i + 1] * fft[i + 1]));
        }
        max(x, num);

    }
//////End PerformFFT

    public void sendvalues(View view) {


        BufferedWriter bw = null;

        try {
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bw.write("Systolic Blood Pressure --> "+bps);
            bw.newLine();
            bw.flush();
            bw.write("Diastolic Blood Pressure --> "+bpd);
            bw.newLine();
            bw.flush();
            bw.write("Respiratory Rate --> "+rr);
            bw.newLine();
            bw.flush();
            bw.write("Heart Rate --> "+hr);
            bw.newLine();
            bw.flush();
            bw.write("Body Temperature --> "+temp);
            bw.newLine();
            bw.flush();

        } catch (IOException e) {
            Log.e(debugString, e.getMessage());


        }
    }
    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                    mConnectedThread = new ConnectedThread(btSocket);
                    mConnectedThread.start();
                }
            } catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }



        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }






    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }


}
//compile files ('libs/flanagan.jar')