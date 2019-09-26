package bku.com.monitor_iots_1;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
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
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;




public class RealTimeGraph extends Fragment {
    private final String TAG = "GRAPH FIELD 1";

    Timer aTimer = new Timer();
    private LineGraphSeries<DataPoint> mSeries;
    private boolean firstTime = true;
    private int lastEntry = 0,lastIndex = 0;

    GraphView graph;
    public TextView mTextView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_real_time_graph, container, false);
        graph = (GraphView) rootView.findViewById(R.id.graph1);
        setupThingSpeakTimer(true);


        return rootView;
    }
    //==========================================================================
     /*THIS IS TIMER*/

    @Override
    public void onPause() {
        super.onPause();
        //setupThingSpeakTimer(false);
    }

    private void setupThingSpeakTimer(boolean value) {
            TimerTask aTask = new TimerTask() {
                @Override
                public void run() {
                    double value = (Math.random() * 11 - 5);
                    sendDatatoThingSpeak(value);
                    if (firstTime)
                        getDatatoThingSpeak(100);
                    else
                        getDatatoThingSpeak(2);
                }
            };
        if (value){
            aTimer.schedule(aTask, 1000, 6000);
            Log.d(TAG, "setupThingSpeakTimer: START");
        } else{
            aTimer.cancel();
            Log.d(TAG, "setupThingSpeakTimer: PAUSE");
        }

    }




    /*THIS IS GET - SET DATA TO THINGSPEAK*/
    private  void drawGraph(DataPoint[] listData){
        mSeries = new LineGraphSeries<>(listData);
        mSeries.setDrawDataPoints(true);
        mSeries.setDataPointsRadius(5);
        mSeries.setThickness(4);
        graph.addSeries(mSeries);
        graph.setTitle("REALTIME DATA FIELD 1");
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(listData.length - 10);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-8);
        graph.getViewport().setMaxY(8);
        graph.getViewport().setScrollable(true);
    }

    private void sendDatatoThingSpeak(final double value) {
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
                Log.d(TAG,"onSending: "+value+ " | send Status : "+jsonString);
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
                try {
                    JSONObject reader = new JSONObject(jsonString);
                    JSONObject channel = reader.getJSONObject("channel");
                    lastEntry = Integer.parseInt(channel.getString("last_entry_id"));
                    JSONArray feeds = reader.getJSONArray("feeds");
                    if(firstTime){
                        firstTime = false;
                        drawGraph(generateData(feeds));
                    }else{
                       if (lastEntry > lastIndex) {
                            JSONObject entry = feeds.getJSONObject(feeds.length()- 1);
                            if (!entry.getString("field1").equals("null")) {
                                DataPoint onePoint = new DataPoint(lastIndex, Double.parseDouble(entry.getString("field1")));
                                lastIndex+=1;
                                mSeries.appendData(onePoint, true, 10000);
                            }
                            else{
                                // next
                            }
                        }
                    }
                    Log.d(TAG,"onGetting: TRUE | SIZE ARRAY : "+lastIndex);
                } catch (JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());

                }
            }

                // TO DO : TÁCH CHUỖI JSON VÀ HIỂN THỊ LÊN TRÊN GRAP THEO THỜI GIAN.
        });
    }
    private DataPoint[] generateData(JSONArray feeds) throws JSONException {
        List<String> field1_data = new ArrayList<String>();
        for (int i = 0; i < feeds.length(); i++) {
            JSONObject entry = feeds.getJSONObject(i);
            String temp = entry.getString("field1");
            if (!temp.equals("null")){
                field1_data.add(temp);
            }
        }
        lastIndex = field1_data.size();
        DataPoint[] _field1 = new DataPoint[lastIndex];
        for (int i =0;i < field1_data.size();i++){
            DataPoint v = new DataPoint(i, Double.parseDouble(field1_data.get(i)));
            _field1[i] = v;
        }

        return _field1;
    }
}
