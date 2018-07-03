package com.blue.bluetoothcomm;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    Button on,enable,discover,send;
    EditText messagetext;

    btConService mbtConService;
    private static final UUID MY_UUID=UUID.fromString("a5ae3adf-afd3-4182-bab6-18302ffe09c0");
    BluetoothDevice btdevice;

    BluetoothAdapter mbluetoothAdapter;
    public List<BluetoothDevice> mlist=new ArrayList<>();
    public deviceListAdapter listAdapter;
    RecyclerView recyclerView;
    devices deviceclass;
    RelativeLayout relativeLayout;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mbroadcastReceiver);
        unregisterReceiver(mbroadcastReceiver2);
        unregisterReceiver(mbroadcastReceiver3);
        unregisterReceiver(broadcastReceiver4);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mbluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        setContentView(R.layout.activity_main);
        discover=(Button)findViewById(R.id.discover);
        on=(Button)findViewById(R.id.on);
        enable=(Button)findViewById(R.id.enable);
        send=(Button)findViewById(R.id.snd);
        messagetext=(EditText) findViewById(R.id.message);
        recyclerView=(RecyclerView)findViewById(R.id.recycle);
        relativeLayout=(RelativeLayout)findViewById(R.id.relLayout);

        mbtConService=new btConService(MainActivity.this);

        recyclerView.hasFixedSize();
        IntentFilter bond = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(broadcastReceiver4,bond);
        enable.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(enable.getText()=="DISABLE")
                enable.setText("ENABLE");
            else
                enable.setText("DISABLE");
            Toast.makeText(MainActivity.this, "Discoverable for 100 sec", Toast.LENGTH_SHORT).show();

            Intent discover=new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discover.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,100);
            startActivity(discover);
            IntentFilter i=new IntentFilter(mbluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            registerReceiver(mbroadcastReceiver2,i);
            discover();
        }
    });
        on.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if(mbluetoothAdapter==null)
                Toast.makeText(MainActivity.this, "Bluetooth not available", Toast.LENGTH_SHORT).show();
            if(!mbluetoothAdapter.isEnabled())
            {
                Intent enablebt=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enablebt);
                IntentFilter bIntent=new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(mbroadcastReceiver,bIntent);
                discover();
            }
            if(mbluetoothAdapter.isEnabled())
            {
                mbluetoothAdapter.disable();
                clearRecyclerView();
                IntentFilter bIntent=new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(mbroadcastReceiver,bIntent);
            }

        }
    });
    discover.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        clearRecyclerView();
        discover();
        }
    });

    send.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            byte[] bytes=messagetext.getText().toString().getBytes(Charset.defaultCharset());
            mbtConService.write(bytes);
        }
    });

}
    public  void  startConnection(BluetoothDevice device)
    {
        btdevice=device;
        startBTConnection(btdevice,MY_UUID);
    }
    private final BroadcastReceiver mbroadcastReceiver= new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action= intent.getAction();
        if(action.equals(mbluetoothAdapter.ACTION_STATE_CHANGED))
        {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,mbluetoothAdapter.ERROR);
            switch(state)
            {
                case BluetoothAdapter.STATE_OFF:
                    Toast.makeText(MainActivity.this, "bluetooth off", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Toast.makeText(MainActivity.this, "bluetooth turning on", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothAdapter.STATE_ON:
                    Toast.makeText(MainActivity.this, "bluetooth on", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Toast.makeText(MainActivity.this, "bluetooth turning off", Toast.LENGTH_SHORT).show();
                    break;

            }
        }

    }
};
    private final BroadcastReceiver mbroadcastReceiver2= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action= intent.getAction();
            if(action.equals(mbluetoothAdapter.ACTION_STATE_CHANGED))
            {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,mbluetoothAdapter.ERROR);
                switch(state)
                {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Toast.makeText(MainActivity.this, "sDiscoverability enabled", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Toast.makeText(MainActivity.this, "Discoverability enabled\nable to recieve connections", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Toast.makeText(MainActivity.this, "Discoverability disabled", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Toast.makeText(MainActivity.this, "Connecting...", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        }
    };

    private final BroadcastReceiver mbroadcastReceiver3= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action= intent.getAction();
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,mbluetoothAdapter.ERROR);
                if(action.equals(BluetoothDevice.ACTION_FOUND))
                {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    deviceclass=new devices(device.getName(),device.getAddress());
                    BluetoothDevice mdevice = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                    mlist.add(mdevice);
                    listAdapter=new deviceListAdapter(mlist);//,clickListener);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(listAdapter);
                    listAdapter.notifyDataSetChanged();
                    Log.d("hello",device.getName()+device.getAddress()+"");
                }

        }
    };
    public final BroadcastReceiver broadcastReceiver4 =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action=intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch(device.getBondState())
                {
                    case BluetoothDevice.BOND_BONDED:
                        Toast.makeText(context, "BOND_BONDED", Toast.LENGTH_SHORT).show();
                        btdevice=device;
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Toast.makeText(context, "BOND_BONDING", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Toast.makeText(context, "BOND_NONE", Toast.LENGTH_SHORT).show();
                        break;


                }
            }
        }
    };

    public void startBTConnection(BluetoothDevice device,UUID uuid)
    {
        mbtConService.startClient(device,uuid);
    }

    public void  discover() {
        Toast.makeText(this, "Looking for devices...", Toast.LENGTH_SHORT).show();
        if(mbluetoothAdapter.isDiscovering())
        {
            mbluetoothAdapter.cancelDiscovery();
            Toast.makeText(this, "Cancelling discovery...", Toast.LENGTH_SHORT).show();
            check();
            mbluetoothAdapter.startDiscovery();
            IntentFilter d=new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mbroadcastReceiver3,d);
        }
        if(!mbluetoothAdapter.isDiscovering())
        {
            check();
            mbluetoothAdapter.startDiscovery();
            IntentFilter d=new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mbroadcastReceiver3,d);

        }
    }

   private void check() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }

            
    }

    public class deviceListAdapter extends RecyclerView.Adapter<holder> {
        private List<BluetoothDevice> devicesList;
        public deviceListAdapter(List<BluetoothDevice> devicesList){
            this.devicesList=devicesList;
        }

        @NonNull
        @Override
        public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rholder, parent, false);
            return new holder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final holder holder, final int position) {
            final BluetoothDevice device=devicesList.get(position);
            holder.name.setText(device.getName());
            holder.address.setText(device.getAddress());
            holder.mBtn.setEnabled(false);
            final boolean bonded=mlist.get(position).getBondState()==BluetoothDevice.BOND_BONDED;
            if(bonded)
            {
                holder.mBtn.setEnabled(true);
                holder.pair.setText("Paired");
                holder.pair.setEnabled(false);
            }
            holder.pair.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mbluetoothAdapter.cancelDiscovery();
                    if(bonded)
                        Toast.makeText(MainActivity.this, "Already paired", Toast.LENGTH_SHORT).show();
                    else
                    {
                        holder.pair.setText("Pair");
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                        {
                            Toast.makeText(MainActivity.this, "Pairing...", Toast.LENGTH_SHORT).show();
                            mlist.get(position).createBond();
                            mbtConService=new btConService(MainActivity.this);
                        }
                    }

                }
            });
            holder.mBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(bonded)
                     startConnection(mlist.get(position));
                    else{
                        Snackbar snackbar=Snackbar.make(relativeLayout,"Pair First",Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return devicesList.size();
        }


    }
    public void clearRecyclerView()
    {
        mlist.clear();
        listAdapter=new deviceListAdapter(mlist);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }

}
