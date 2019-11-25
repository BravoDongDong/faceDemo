package com.arcsoft.arcfacedemo.activity;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.common.getIp;

import java.lang.String;

import com.arcsoft.arcfacedemo.model.attendanceInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

public class AttendanceSituation extends AppCompatActivity {
    /**
     *
     */
    private String AttendancedStatistics = "/Values/AttendancedStatistics";
    private String NoStatistics = "/Values/NoStatistics";

    private static final String TAG = "AttendanceSituation";
    private ProgressBar center_loading_prgbar;
    private LinearLayout showList;
    private ListView attendanceList;
    private ListView noAttendanceList;
    private List<attendanceInfo> attendanceInfoArrayList = new ArrayList<attendanceInfo>();
    private List<String> studentsId = new ArrayList<String>();
    private List<String> attendancedId = new ArrayList<String>();

    // TODO: 19-11-11 这里要确保缓存信息在当前课程不能被删除
    // TODO: 19-11-11 或者更改为缓存信息和 请求webapi查询考勤情况 相结合
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("当前考勤情况");
        setContentView(R.layout.activity_attendance_situation);
        center_loading_prgbar = (ProgressBar) findViewById(R.id.center_loading_prgbar);
        showList = (LinearLayout) findViewById(R.id.showList);
        attendanceList = (ListView) findViewById(R.id.attendanceList);
        noAttendanceList = (ListView) findViewById(R.id.noAttendanceList);
        getStudents();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:

                    SetListView(attendancedId, true);
                    break;
                case 2:
                    // TODO: 19-11-11
                    SetListView(studentsId, false);
                    break;
            }
        }
    };

    private void getStudents() {
        center_loading_prgbar.setVisibility(View.VISIBLE);
        final Gson gson = new Gson();
        RequestQueue requestQueue = Volley.newRequestQueue(this);


        StringRequest stringRequest = new StringRequest(getIp.ip + AttendancedStatistics, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // TODO: 19-11-11 两个数组解析到两个list中


//                    JsonParser jsonParser = new JsonParser();
//                    JsonArray jsonArray = new JsonArray();
                JSONObject myJsonObject = new JSONObject();
                try {
                    myJsonObject = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    JSONArray jsonArray = myJsonObject.getJSONArray("Table");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String id = jsonObject.getString("id");
                        attendancedId.add(id);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // TODO: 2019/11/17 string 转JSONObject 再解析
//                    attendancedId = gson.fromJson(response, new TypeToken<List<String>>() {}.getType());
                //Toast.makeText(AttendanceSituation.this, "获取成功", Toast.LENGTH_SHORT).show();
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = error.getMessage();
                Toast.makeText(AttendanceSituation.this, "获取失败,请检查连接网络情况" + errorMessage, Toast.LENGTH_SHORT).show();
                //Log.e(TAG, errorMessage);
                center_loading_prgbar.setVisibility(View.INVISIBLE);

            }
        }
        );
//        try {
//            JSONArray jsonArray = response.getJSONArray("Table");
//            for (int i = 0; i < jsonArray.length(); i++) {
//                JSONObject jsonObject = jsonArray.getJSONObject(i);
//                String id = jsonObject.getString("id");
//                attendancedId.add(id);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getIp.ip + NoStatistics, new Response.Listener<JSONObject>() {
//
//            @Override
//            public void onResponse(JSONObject response) {
//
//            }
//        }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//            }
//        }
//        );


        StringRequest stringRequest1 = new StringRequest(getIp.ip + NoStatistics, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject myJsonObject = new JSONObject();
                try {
                    myJsonObject = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    JSONArray jsonArray = myJsonObject.getJSONArray("Table");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String id = jsonObject.getString("id");
                        studentsId.add(id);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Toast.makeText(AttendanceSituation.this, "获取成功", Toast.LENGTH_SHORT).show();
                Message msg = new Message();
                msg.what = 2;
                handler.sendMessage(msg);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = error.getMessage();
                Toast.makeText(AttendanceSituation.this, "提交失败,请检查连接网络情况" + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e(TAG, errorMessage);
                center_loading_prgbar.setVisibility(View.INVISIBLE);

            }
        }
        );

        requestQueue.add(stringRequest);
        requestQueue.add(stringRequest1);
    }


    //Boolean attendancedOrStudent : true 是attendancedId  false 是StudentId
    private void SetListView(List<String> IdList, Boolean attendancedOrStudent) {
        center_loading_prgbar.setVisibility(View.INVISIBLE);
        ArrayAdapter<String> ArrayAdapter;

        showList.setVisibility(View.VISIBLE);
        if (IdList.isEmpty()) {
            return;
        }

        if (attendancedOrStudent) {
            ArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, IdList);
            attendanceList.setAdapter(ArrayAdapter);

        } else {
            ArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, IdList);
            noAttendanceList.setAdapter(ArrayAdapter);

        }
    }
}
