package com.example.carrie.carrie_test1;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CheckBeacon extends Service {
    String memberid;
    private Handler handler = new Handler( );
    private Runnable runnable;
    // 定義WifiManager對象
    public static WifiManager mWifiManager;
    // 定義WifiInfo對象
    private WifiInfo mWifiInfo;
    private int status = 1 ;
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SEARCH_TIMEOUT = 10000;
    private static List<BluetoothDevice> mBleDevices = new ArrayList<BluetoothDevice>();
    private ArrayList<ArrayList<String>> bringBeacon = new ArrayList<ArrayList<String>>();
    private ArrayList<String> needBeacon = new ArrayList<String>();
    private ArrayList<String> storeAPBSSID = new ArrayList<String>();
    private int beaconNum = 0;
    private int APnum = 0 ;
    RequestQueue requestQueue;
    String getm_BeaconUrl = "http://54.65.194.253/Beacon/getm_Beacon.php";
    String getAP = "http://54.65.194.253/Beacon/getAP.php";
    private int UUIDnum = 0 ;
    private int SSIDnum = 0 ;


    @Override
    public IBinder onBind(Intent arg0) {
// TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
// TODO Auto-generated method stub
        super.onCreate();
//        Bundle bundle = getIntent().getExtras();
//        memberid=bundle.getString("memberid");

        checkWifi();
    }


    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this, "Service start", Toast.LENGTH_SHORT).show();
        getAP();
        getbeacon();
        handler.postDelayed(runnable, 5000);
    }
    public void onDestroy(){
        super.onDestroy();
        Toast.makeText(this, "Service stop", Toast.LENGTH_SHORT).show();
    }
    public void checkWifi(){
        runnable=new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                //要做的事情
                WifiAdmin(getApplicationContext());
                getbeacon();
                getAP();
                Log.d("UUIDnum",Integer.toString(UUIDnum));
                Log.d("SSIDnum",Integer.toString(SSIDnum));
                if(UUIDnum != 0 && SSIDnum != 0){
                    String SSID= mWifiInfo.getSSID() ;//Wi-Fi名稱
                    int NETWORKID= mWifiInfo.getNetworkId() ;//Wi-Fi連線ID
                    int LinkSpeed= mWifiInfo.getLinkSpeed() ;
                    int Rssi= mWifiInfo.getRssi() ;
                    String BSSID= mWifiInfo.getBSSID() ;
                    String MacAddress= mWifiInfo.getMacAddress() ;
                    int IPADRRESS= mWifiInfo.getIpAddress() ;//Wi-Fi連線位置
                    String IP= String.format("%d.%d.%d.%d", (IPADRRESS & 0xff), (IPADRRESS >> 8 & 0xff), (IPADRRESS >> 16 & 0xff),( IPADRRESS >> 24 & 0xff)) ;//Wi-Fi IP位置
                    Log.d("qq","[SSID="+SSID+"],[NetworkID="+Integer.toString(NETWORKID)+"],[LinkSpeed="+Integer.toString(LinkSpeed)+"],[Rssi="+Integer.toString(Rssi)+"],[BSSID="+BSSID+"],[MacAddress="+MacAddress+"],[IPAdrress="+IP+"]");
                    Log.d("storeAPBSSIDsize",Integer.toString(storeAPBSSID.size()));
                    int countAP = 0 ;
                    for(int i = 0; i < storeAPBSSID.size(); i++){
                        if(BSSID.equals(storeAPBSSID.get(i))){
                            countAP ++ ;
                        }
                    }
                    if(status == 1 && countAP != 0){
                        status = 2 ;
                        Log.d("qq",Integer.toString(status));
                        checkBeacon();
                    }else if(status == 3 && countAP == 0){
                        status = 4 ;
                        Log.d("qq",Integer.toString(status));
                        checkBeacon();
                    }
                }
                handler.postDelayed(this, 5000);
            }
        };
    }
    public void WifiAdmin(Context context) {
        // 取得WifiManager對象
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // 取得WifiInfo對象
        mWifiInfo = mWifiManager.getConnectionInfo();
    }
    public void checkBeacon(){
        if(status==2){
            Log.d("qq","333");
//            handler.removeCallbacks(runnable);
            InitBLE ();
            SearchForBLEDevices();
            status = 3 ;
        }else if (status == 4){
            Log.d("qq","333");
//            handler.removeCallbacks(runnable);
            InitBLE ();
            SearchForBLEDevices();
            status = 1 ;

        }
//        for(int i = 0 ; i < bringBeacon.length ; i ++){
//
//        }
    }
    private void InitBLE() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) == false) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (mBluetoothAdapter.isEnabled() == false) {
            Log.d("qq","444");
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
    }

    private void SearchForBLEDevices () {
        new Thread() {
            @Override
            public void run() {
                mBluetoothAdapter.startLeScan(mBleScanCallback);
                try {
                    Thread.sleep(SEARCH_TIMEOUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mBluetoothAdapter.stopLeScan(mBleScanCallback);
                int countBeacon = 0 ;
                Log.d("needBeaconSize", Integer.toString(needBeacon.size()));
                Log.d("bringBeaconSize", Integer.toString(bringBeacon.size()));
                for(int i = 0; i < bringBeacon.size(); i++){
                    for(int j = 0; j < needBeacon.size(); j++ ){
                        if(bringBeacon.get(i).get(0).equals(needBeacon.get(j))){
                            countBeacon ++ ;
                        }else{

                        }
                    }

                }
                if(countBeacon!=needBeacon.size()){
                    Log.d("aaa", "沒帶Beacon!!!!!!!!!!");
                    
                }
                needBeacon.clear();
                bringBeacon.clear();
                Log.d("needBeaconSize", Integer.toString(needBeacon.size()));
                Log.d("bringBeaconSize", Integer.toString(bringBeacon.size()));
            }
        }.start();
    }

    public BluetoothAdapter.LeScanCallback mBleScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            try{
                if (device != null) {
                    if (mBleDevices.indexOf(device) == -1) //only add new devices
                    {
                        mBleDevices.add(device);
                        for (int i=0; i<1; i++) {
                            String  uuid = "" ;
                            if(scanRecord.length > 30) {
                                //從scanRecord 分辦是固定封包是6byte還是9ybge。
                                //if((scanRecord[5]  == (byte)0x4c) && (scanRecord[6] == (byte)0x00) && (scanRecord[7]  == (byte)0x02) && (scanRecord[8] == (byte)0x15)) {
                                Log.v("test1", "123+"+IntToHex2(scanRecord[5] & 0xff)+"+321");
                                Log.v("test1", "123+"+IntToHex2(scanRecord[6] & 0xff)+"+321");
                                Log.v("test1", "123+"+IntToHex2(scanRecord[7] & 0xff)+"+321");
                                Log.v("test1", "123+"+IntToHex2(scanRecord[8] & 0xff)+"+321");
                                Log.v("test1", "123+"+IntToHex2(scanRecord[9] & 0xff)+"+321");
                                Log.v("test1", "123+"+IntToHex2(scanRecord[10] & 0xff)+"+321");
                                uuid = IntToHex2(scanRecord[5] & 0xff)  +  IntToHex2(scanRecord[6] & 0xff)
                                        +  IntToHex2(scanRecord[7] & 0xff) + IntToHex2(scanRecord[8] & 0xff)
                                        +  IntToHex2(scanRecord[9] & 0xff) +  IntToHex2(scanRecord[10] & 0xff) +  "-"
                                        +  IntToHex2(scanRecord[9] & 0xff) +  IntToHex2(scanRecord[10] & 0xff)
                                        +  IntToHex2(scanRecord[11] & 0xff) +  IntToHex2(scanRecord[12] & 0xff) +  "-"
                                        +  IntToHex2(scanRecord[13] & 0xff) +  IntToHex2(scanRecord[14] & 0xff) +  "-"
                                        +  IntToHex2(scanRecord[15] & 0xff) +  IntToHex2(scanRecord[16] & 0xff) +  "-"
                                        +  IntToHex2(scanRecord[17] & 0xff) +  IntToHex2(scanRecord[18] & 0xff) +  "-"
                                        +  IntToHex2(scanRecord[19] & 0xff) +  IntToHex2(scanRecord[20] & 0xff)
                                        +  IntToHex2(scanRecord[21] & 0xff) +  IntToHex2(scanRecord[22] & 0xff)
                                        +  IntToHex2(scanRecord[23] & 0xff) +  IntToHex2(scanRecord[24] & 0xff);
                            }
                            Log.d("qq","[Name="+device.getName()+"],[Address="+device.getAddress()+"],[UUID="+uuid+"],[RSSI="+Integer.toString(rssi)+"]");
                            ArrayList<String> beaconInfo = new ArrayList<String>();
                            beaconInfo.add(0,uuid);
                            beaconInfo.add(1,Integer.toString(rssi));
                            bringBeacon.add(beaconNum,beaconInfo);
                            Log.d("qqqq",bringBeacon.toString());
                        }
                    }
                }
            }catch (Exception e){
                Log.d("aaa",e.toString());
            }
        }
    };
    //int整數和hex16進位轉換
    public String IntToHex2(int i) {
        char hex_2[] = {Character.forDigit((i >> 4) & 0x0f, 16), Character.forDigit(i & 0x0f, 16)};
        String hex_2_str = new String(hex_2);
        return hex_2_str.toUpperCase();
    }
    public void finish() {
        throw new RuntimeException("Stub!");
    }
    public void startActivityForResult(Intent intent, int requestCode) {
        throw new RuntimeException("Stub!");
    }
    public void getbeacon(){
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        final StringRequest drugrequest = new StringRequest(Request.Method.POST, getm_BeaconUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("nn1111",response);
                try {
                    JSONArray jarray = new JSONArray(response);
                    UUIDnum = jarray.length() ;
                    needBeacon.clear();
                    for (int i=0;i<jarray.length();i++){
                        JSONObject obj = jarray.getJSONObject(i);
                        String UUID = obj.getString("UUID");
                        needBeacon.add(i,UUID);
                        Log.d("needBeacon",needBeacon.get(i));
                    }
                } catch (JSONException e) {
                    Log.d("nn1111",e.toString());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("nn1111", error.toString());
                Toast.makeText(getApplicationContext(), "Error read getm_Beacon.php!!!", Toast.LENGTH_LONG).show();
            }
        })
        {
            protected Map<String, String> getParams() throws AuthFailureError {//把值丟到php
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("member_id","6");
                Log.d("nn1111",parameters.toString());

                return parameters;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(drugrequest);
    }

    public void getAP(){
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        final StringRequest drugrequest = new StringRequest(Request.Method.POST, getAP, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("nn1122",response);
                try {
                    JSONArray jarray = new JSONArray(response);
                    final String[] SSIDarray=new String[jarray.length()];
                    final String[] BSSIDarray=new String[jarray.length()];
                    storeAPBSSID.clear();
                    SSIDnum = jarray.length();
                    for (int i=0;i<jarray.length();i++){
                        JSONObject obj = jarray.getJSONObject(i);
                        String SSID = obj.getString("SSID");
                        String BSSID = obj.getString("BSSID");
                        SSIDarray[i]=SSID;
                        storeAPBSSID.add(i,BSSID);
                        Log.d("nn11",BSSID);
                    }
                } catch (JSONException e) {
                    Log.d("nn1122",e.toString());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("nn1122", error.toString());
                Toast.makeText(getApplicationContext(), "Error read getm_AP.php!!!", Toast.LENGTH_LONG).show();
            }
        })
        {
            protected Map<String, String> getParams() throws AuthFailureError {//把值丟到php
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("member_id","6");
                Log.d("nn1122",parameters.toString());

                return parameters;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(drugrequest);
    }
}