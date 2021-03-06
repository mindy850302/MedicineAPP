package com.example.carrie.carrie_test1;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by filipp on 9/16/2016.
 */
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private Context context;
    private List<MyMonitorData> my_data;
    String deletemonitorUrl = "http://54.65.194.253/Monitor/deleteMonitor.php";
    RequestQueue requestQueue;
    String objectArray;
    String objectDetailArray;
    private Dialog dialog;

    public CustomAdapter(Context context, List<MyMonitorData> my_data) {
        this.context = context;
        this.my_data = my_data;

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardmonitor, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.monitorName.setText(my_data.get(position).getName());
        holder.monitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, "Click positioin" + position, Toast.LENGTH_SHORT).show();
                dialog = ProgressDialog.show(context,
                        "讀取中", "請等待5秒...",true);
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try{
                            getMonitorPillsRecord(String.valueOf(my_data.get(position).getId()));
                            getMonitorPillsRecordTime(String.valueOf(my_data.get(position).getId()));
                            Thread.sleep(3000);
                            Log.d("customadapter", " 1");
                            Intent it = new Intent(context, SwipePlot.class);

                            Log.d("customadapter", "2");
                            Bundle bundle = new Bundle();
                            Log.d("customadapter", "3");
                            bundle.putInt("monitor_who", my_data.get(position).getId());
                            bundle.putString("userid", MonitorActivity.my_id);
                            bundle.putString("googleid", MonitorActivity.my_google_id);
                            bundle.putString("my_supervise_id", MonitorActivity.my_mon_id);
                            bundle.putString("objectArray", objectArray);
                            bundle.putString("objectDetailArray", objectDetailArray);
                            Log.d("4445", "supervised_id" + my_data.get(position).getId());
                            Log.d("customadapter", "4");
                            Log.d("customadapter", objectArray);
                            Log.d("customadapter", String.valueOf(my_data.get(position).getId()));
                            it.putExtras(bundle);
                            Log.d("customadapter", "5");
                            context.startActivity(it);
                            Log.d("customadapter", "6");
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                        finally{
                            dialog.dismiss();
                        }
                    }
                }).start();

            }
        });
        //Glide.with(context).load(my_data.get(position).getImage_link()).into(holder.imageView);
        holder.deleteMonitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMonitor(my_data.get(position).getId());
                Intent it = new Intent(context,MonitorActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("my_id", MonitorActivity.my_id);
                bundle.putString("my_google_id", MonitorActivity.my_google_id);
                bundle.putString("my_supervise_id", MonitorActivity.my_mon_id);
                it.putExtras(bundle);
                context.startActivity(it);
            }
        });


    }

    @Override
    public int getItemCount() {

        return my_data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView monitorName;
        public Button monitorButton;
        public Button deleteMonitorButton;

        public ViewHolder(View itemView) {
            super(itemView);
            //chineseName = (TextView) itemView.findViewById(R.id.chineseName);
            //imageView = (ImageView) itemView.findViewById(R.id.image);
            //indication = (TextView) itemView.findViewById(R.id.indication);
            monitorName = (TextView) itemView.findViewById(R.id.monitorName);
            imageView = (ImageView) itemView.findViewById(R.id.monitor_image);
            monitorButton = (Button) itemView.findViewById(R.id.monitorButton);
            deleteMonitorButton = (Button) itemView.findViewById(R.id.deletMonitorButton);

        }
    }
    public void deleteMonitor(final int monitorid){
        requestQueue = Volley.newRequestQueue(context);

        final StringRequest request = new StringRequest(Request.Method.POST, deletemonitorUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("rrr111", error.toString());
                Toast.makeText(context, "Error read deleteMonitor.php!!!", Toast.LENGTH_LONG).show();
            }
        })
        {
            protected Map<String, String> getParams() throws AuthFailureError {//把值丟到php
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("monitor_id", String.valueOf(monitorid));
                parameters.put("supervised_id", MonitorActivity.my_mon_id);
                Log.d("deletemonitorid", parameters.toString());
                Log.d("deletemonitorid","checck!!!");
                return parameters;

            }
        }
                ;
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request);
    }
    public void getMonitorPillsRecord(String memberid) {//取得被監視者的用藥紀錄
        requestQueue = Volley.newRequestQueue(context);

        String getMeasureInformationURL = "http://54.65.194.253/Monitor/getMonitorPillsRecord.php?member_id="+memberid;

        Map<String, String> params = new HashMap();



        //params.put("member_id", memberid);
        //Log.d("measureInfor",params.toString());
        //JSONObject parameters = new JSONObject(params);
        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, getMeasureInformationURL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
//                Log.d("getMonitorPillsRecord", response.toString());

                if (response.toString().contains("nodata")) {
                    Log.d("getMonitorPillsRecord", "nodata");
                    objectArray = "nodata";
                } else {
                    Log.d("getMonitorPillsRecord", response.toString());
                        objectArray = response.toString();
                    Log.d("getMonitorPillsRecord", objectArray);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("getMonitorPillsRecord", error.toString());
                Toast.makeText(context, "Error read getMonitorPillsRecord.php!!!", Toast.LENGTH_LONG).show();
//                refreshNormalDialogEvent();
            }
        });
        Volley.newRequestQueue(context).add(jsonArrayRequest);
    }
    public void getMonitorPillsRecordTime(String memberid) {//取得被監視者的用藥紀錄
        requestQueue = Volley.newRequestQueue(context);

        String getMeasureInformationURL = "http://54.65.194.253/Monitor/getPillRecordTime.php?member_id="+memberid;

        Map<String, String> params = new HashMap();



        //params.put("member_id", memberid);
        //Log.d("measureInfor",params.toString());
        //JSONObject parameters = new JSONObject(params);
        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, getMeasureInformationURL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
//                Log.d("getMonitorPillsRecord", response.toString());

                if (response.toString().contains("nodata")) {
                    Log.d("getMonitorPillsRecord", "nodata");
                    objectDetailArray = "nodata";
                } else {
                    Log.d("getMonitorPillsRecord", response.toString());
                    objectDetailArray = response.toString();
                    Log.d("getMonitorPillsRecord 2", objectDetailArray);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("getMonitorPillsRecord", error.toString());
                Toast.makeText(context, "Error read getPillRecordTime.php!!!", Toast.LENGTH_LONG).show();
//                refreshNormalDialogEvent();
            }
        });
        Volley.newRequestQueue(context).add(jsonArrayRequest);
    }

}
