package com.blue.bluetoothcomm;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import android.support.design.widget.Snackbar;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class btConService {
    private static final String TAG="btconservice";
    private static final String appName="btapp";
    private static final UUID MY_UUID=UUID.fromString("a5ae3adf-afd3-4182-bab6-18302ffe09c0");
    private AcceptThread athread;
    private final BluetoothAdapter bluetoothAdapter;
    private ConnectThread mconnectThread;
    private BluetoothDevice mdevice;
    Context mcontext;
    private UUID deviceUUID;
    ProgressDialog progressDialog;
    private ConnectedThread mConnectedThread;

    public btConService(Context mcontext) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mcontext = mcontext;
        start();
    }

    private class AcceptThread extends Thread
    {
        private final BluetoothServerSocket serverSocket;
        public AcceptThread()
        {
            BluetoothServerSocket tmp =null;
            try
            {
                tmp=bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName,MY_UUID);

            }
            catch (Exception e)
            {
                Log.d("BTService", "Error:\n" + e.getMessage());
            }
            serverSocket=tmp;

        }
        public void run()
        {
            Log.d("BTService", "Acceptthread running");
            BluetoothSocket socket=null;
            try{
                Log.d("BTService", "RFcom server start...");
                socket = serverSocket.accept();
                Log.d("BTService", "Connection accepted");
            }catch (Exception e){
                Log.d("BTService", "Exception:\n" + e.getMessage());
            }
            if(socket!=null)
                connected(socket,mdevice);

        }
        public void cancel(){
            Log.d("BTService", "Cancelling thread");
            try
            {
                serverSocket.close();
            }catch (Exception e)
            {
                Log.d("BTService", "Closing of thread socket failed\n" + e.getMessage());
            }
        }
    }
    private class ConnectThread extends Thread{
        private BluetoothSocket msocket;
        public ConnectThread(BluetoothDevice device,UUID uuid)
        {
            mdevice=device;
            deviceUUID=uuid;
        }
        public void run()
        {
            Log.d("BTService", "ConnectThread start");
            BluetoothSocket tmp=null;
            try{
                Log.d("BTService", "creating InsecureRfcommSocket");
                tmp=mdevice.createInsecureRfcommSocketToServiceRecord(deviceUUID);
            }catch (Exception e){
                Log.d("BTService", "Unable to create InsecureRfcommSocket:\n" + e.getMessage());
            }

            msocket=tmp;
            bluetoothAdapter.cancelDiscovery();
            try {
                msocket.connect();
                Log.d("BTService", "ConnectThread connected");
            } catch (IOException e)
            {
                try {
                    msocket.close();
                    Log.d("BTService", "ConnectedThread closed");

                } catch (IOException e1) {
                    Log.d("BTService", "Unable to close ConnectedThread connection:"+e.getMessage());
                }
                Log.d("BTService", "Could not connect to UUID:"+e.getMessage());
            }
            connected(msocket,mdevice);

        }
    public void cancel(){
        Log.d("BTService", "Closing client socket");
        try
        {
            msocket.close();
        }catch (Exception e)
        {
            Log.d("BTService", "Closing of socket\nin ConnectedThead failed\n" + e.getMessage());
        }
    }
}


    public synchronized void start()
    {
        Log.d("BTService", "Start");
    if(mconnectThread!=null)
    {
        mconnectThread.cancel();
        mconnectThread=null;
    }
    if(athread==null)
    {
        athread=new AcceptThread();
        athread.start();
    }
}
    public void startClient(BluetoothDevice device, UUID uuid)
    {
        Log.d("BTService", "client Started");
     progressDialog = ProgressDialog.show(mcontext,"Connecting Bluetooth","Please Wait",true);
     mconnectThread=new ConnectThread(device,uuid);
     mconnectThread.start();
 }
    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket msocket;
        private final InputStream minputStream;
        private final OutputStream moutputStream;

        private ConnectedThread(BluetoothSocket msocket) {
            Log.d("BTService", "ConnectedThread Starting");
            this.msocket = msocket;
            InputStream in =null;
            OutputStream out =null;
            progressDialog.dismiss();
            try {
                in=msocket.getInputStream();
                out=msocket.getOutputStream();
            } catch (IOException e) {

            }
            minputStream=in;
            moutputStream=out;
        }
        public void run()
        {
            byte[] buffer =new byte[1024];
            int bytes;
            while(true)
            {
                try {
                    bytes=minputStream.read(buffer);
                    String incomingMessage = new String(buffer,0,bytes);
                    Log.d("BTService", "" + incomingMessage);
                }catch (Exception e)
                {
                    Log.d("BTService", "Error reading input:"+e.getMessage());
                   break;
                }
            }
        }
        public void write(byte[] bytes)
        {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d("BTService", "writing output:\n" + text);
            try {
                moutputStream.write(bytes);
            }catch (IOException e){
                Log.d("BTService", "Writing error to output "+e.getMessage());
            }
        }
        public void cancel()
        {
            try{
                msocket.close();
            }catch(IOException e){}
        }
    }
    private void connected(BluetoothSocket msocket, BluetoothDevice mdevice) {
        Log.d("BTService", "Connected method starting");
        mConnectedThread = new ConnectedThread(msocket);
        mConnectedThread.start();
    }

    public void write(byte[] out) {
        ConnectedThread r;
        mConnectedThread.write(out);
    }
}
