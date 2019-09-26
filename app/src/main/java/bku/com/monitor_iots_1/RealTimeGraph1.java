package bku.com.monitor_iots_1;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class RealTimeGraph1 extends Fragment {
    private final String TAG = "GRAPH FIELD 2";

    Timer aTimer2 = new Timer();
    private LineGraphSeries<DataPoint> mSeries2;
    private boolean firstTime2 = true;
    private int lastEntry2 = 0,lastIndex2 = 0;

    GraphView graph2;
    public TextView mTextView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_real_time_graph, container, false);
        graph2 = (GraphView) rootView.findViewById(R.id.graph1);
        setupThingSpeakTimer2(true);


        return rootView;
    }
    //==========================================================================
     /*THIS IS TIMER*/

    @Override
    public void onPause() {
        super.onPause();
        //setupThingSpeakTimer2(false);
    }

    private void setupThingSpeakTimer2(boolean value) {
        TimerTask aTask = new TimerTask() {
            @Override
            public void run() {
                double value = (Math.random() * 5 - 2);
                sendDatatoThingSpeak2(value);
                if (firstTime2)
                    getDatatoThingSpeak2(100);
                else
                    getDatatoThingSpeak2(2);
            }
        };
        if (value){
            aTimer2.schedule(aTask, 1000, 6000);
            Log.d(TAG, "setupThingSpeakTimer 2: START");
        } else{
            aTimer2.cancel();
            Log.d(TAG, "setupThingSpeakTimer 2: PAUSE");
        }

    }




    /*THIS IS GET - SET DATA TO THINGSPEAK*/
    private  void drawGraph(DataPoint[] listData){
        mSeries2 = new LineGraphSeries<>(listData);
        mSeries2.setDrawDataPoints(true);
        mSeries2.setDataPointsRadius(5);
        mSeries2.setThickness(4);
        graph2.addSeries(mSeries2);
        graph2.setTitle("REALTIME DATA FIELD 2");
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(listData.length - 10);
        graph2.getViewport().setYAxisBoundsManual(true);
        graph2.getViewport().setMinY(-5) ;
        graph2.getViewport().setMaxY(5);
        graph2.getViewport().setScrollable(true);
    }

    private void sendDatatoThingSpeak2(final double value) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        String WRITE_API_KEY = "XE1IFXI91UZH3HYP";
        final String c = Double.toString(value);
        String LINK_WRITE = "https://api.thingspeak.com/update?api_key=";
        Request request = builder.url(LINK_WRITE + WRITE_API_KEY + "&field2=" + c).build();

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
    private void getDatatoThingSpeak2(final int value) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        String WRITE_API_KEY = "NHA22BRVQH5NC4NW";
        String c = Integer.toString(value);
        String LINK_WRITE = "https://api.thingspeak.com/channels/866836/fields/2.json?api_key=";
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
                    lastEntry2 = Integer.parseInt(channel.getString("last_entry_id"));
                    JSONArray feeds = reader.getJSONArray("feeds");
                    if(firstTime2){
                        firstTime2 = false;
                        drawGraph(generateData(feeds));
                    }else{
                        if (lastEntry2 > lastIndex2) {
                            JSONObject entry = feeds.getJSONObject(feeds.length()- 1);
                            if (!entry.getString("field2").equals("null")) {
                                DataPoint onePoint = new DataPoint(lastIndex2, Double.parseDouble(entry.getString("field2")));
                                lastIndex2+=1;
                                mSeries2.appendData(onePoint, true, 10000);
                            }
                            else{
                                // next
                            }
                        }
                    }
                    Log.d(TAG,"onGetting: TRUE | SIZE ARRAY : "+lastIndex2);
                } catch (JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());

                }
            }

            // TO DO : TÁCH CHUỖI JSON VÀ HIỂN THỊ LÊN TRÊN GRAP THEO THỜI GIAN.
        });
    }
    private DataPoint[] generateData(JSONArray feeds) throws JSONException {
        List<String> field2_data = new ArrayList<String>();
        for (int i = 0; i < feeds.length(); i++) {
            JSONObject entry = feeds.getJSONObject(i);
            String temp = entry.getString("field2");
            if (!temp.equals("null")){
                field2_data.add(temp);
            }
        }
        lastIndex2 = field2_data.size();
        DataPoint[] _field2 = new DataPoint[lastIndex2];
        for (int i =0;i < field2_data.size();i++){
            DataPoint v = new DataPoint(i, Double.parseDouble(field2_data.get(i)));
            _field2[i] = v;
        }

        return _field2;
    }
}
