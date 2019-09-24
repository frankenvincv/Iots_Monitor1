package bku.com.monitor_iots_1;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;




public class RealTimeGraph extends Fragment {
    private final String TAG = "IOT-SERVER ALER : ";

    private LineGraphSeries<DataPoint> mSeries;
    private int lastEntry=0;
    private boolean firstTime = true;
    DataPoint[] listData =null;


    GraphView graph;
    public TextView mTextView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_real_time_graph, container, false);
        //mTextView = (TextView) rootView.findViewById(R.id.jsonText);
        graph = (GraphView) rootView.findViewById(R.id.graph1);
        setupThingSpeakTimer();


        return rootView;
    }
    //==========================================================================
     /*THIS IS TIMER*/

    private void setupThingSpeakTimer() {
        Timer aTimer = new Timer();
            TimerTask aTask = new TimerTask() {
                @Override
                public void run() {
                    double value = (Math.random() * 11 - 5);
                    sendDatatoThingSpeak(value);
                    if (firstTime)
                        getDatatoThingSpeak(1000);
                    else
                        getDatatoThingSpeak(2);
                }
            };
            aTimer.schedule(aTask, 1000, 6000);
    }




    /*THIS IS GET - SET DATA TO THINGSPEAK*/
    private  void drawGraph(){
        mSeries = new LineGraphSeries<>(listData);
        mSeries.setDrawDataPoints(true);
        mSeries.setDataPointsRadius(6);
        mSeries.setThickness(5);
        graph.addSeries(mSeries);
        graph.setTitle("REALTIME DATA FIELD 1");
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(80);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-7);
        graph.getViewport().setMaxY(7);
        graph.getViewport().setScrollable(true);
    }

    private void sendDatatoThingSpeak(final double value) {
        Log.d(TAG,"sendDatatoThingSpeak : "+value);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        String WRITE_API_KEY = "XE1IFXI91UZH3HYP";
        final String c = Double.toString(value);
        String LINK_WRITE = "https://api.thingspeak.com/update?api_key=";
        Request request = builder.url(LINK_WRITE + WRITE_API_KEY + "&field1=" + c).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                //CASE FAIL
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String jsonString = response.body().string();
                Log.d(TAG,jsonString);
            }
        });
    }

    private void getDatatoThingSpeak(final int value) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        String WRITE_API_KEY = "NHA22BRVQH5NC4NW";
        String c = Integer.toString(value);
        String LINK_WRITE = "https://api.thingspeak.com/channels/866836/fields/1.json?api_key=";
        Request request = builder.url(LINK_WRITE + WRITE_API_KEY + "&results=" + c).build(); //&results= ? - Đọc bao nhiêu data

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                //CASE FAIL

            }

            @Override
            public void onResponse(Response response) throws IOException {
                String jsonString = response.body().string();
                Log.d(TAG, "onResponse: "+jsonString);
                try {
                    JSONObject reader = new JSONObject(jsonString);
                    JSONObject channel = reader.getJSONObject("channel");
                    lastEntry = Integer.parseInt(channel.getString("last_entry_id"));
                    JSONArray feeds = reader.getJSONArray("feeds");
                    if(firstTime){
                        listData = new DataPoint[lastEntry];
                        firstTime = false;
                        for (int i = 0; i < feeds.length(); i++) {
                            JSONObject entry = feeds.getJSONObject(i);
                            String field1 = entry.getString("field1");
                            DataPoint v = new DataPoint(i, Double.parseDouble(field1));
                            listData[i] = v;
                            Log.d(TAG, "Data[" + i + "] = " + field1);
                        }
                        drawGraph();
                    }else{
                        if (lastEntry > listData.length) {
                            JSONObject entry = feeds.getJSONObject(feeds.length()- 1);
                            DataPoint onePoint = new DataPoint(lastEntry,Double.parseDouble(entry.getString("field1")));
                            mSeries.appendData(onePoint, true, 1000);
                            Log.d(TAG,"ListData.length = "+listData.length);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());

                }
            }

                // TO DO : TÁCH CHUỖI JSON VÀ HIỂN THỊ LÊN TRÊN GRAP THEO THỜI GIAN.
        });
    }

}
