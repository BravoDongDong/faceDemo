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
import com.arcsoft.arcfacedemo.model.attendanceInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AttendanceSituation extends AppCompatActivity {
    /**
     *
     */
    private String simple_api = "/Values/getStudent";
    private String complication_api = "/Values/getStudent";

    private static final String TAG = "AttendanceSituation";
    private ProgressBar center_loading_prgbar;
    private LinearLayout showList;
    private ListView attendanceList;
    private ListView noAttendanceList;
    private List<attendanceInfo> attendanceInfoArrayList = new ArrayList<attendanceInfo>();
    private List<String> StudentsId = new ArrayList<String>();
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

    private  Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    for (attendanceInfo attendanceInfo : attendanceInfoArrayList) {
                        attendancedId.add(attendanceInfo.getId());
                    }

                    for (String id : StudentsId) {
                        if (attendancedId.contains(id)) {
                            StudentsId.remove(id);
                        }
                    }
                    SetListView(attendancedId, StudentsId);
                    break;
                case 2:
                    // TODO: 19-11-11  
                    break;
            }
        }
    };

    private void getStudents() {
        attendanceInfoArrayList = RegisterAndRecognizeActivity.getInstance().getAttendanceInfoArrayList(getApplicationContext());
        center_loading_prgbar.setVisibility(View.VISIBLE);
        final Gson gson = new Gson();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest jsonObjectRequest;
        if (attendanceInfoArrayList.isEmpty()) {
            jsonObjectRequest = new StringRequest(Request.Method.POST, getIp.ip + complication_api, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // TODO: 19-11-11 两个数组解析到两个list中 
                    StudentsId = gson.fromJson(response, new TypeToken<List<String>>() {
                    }.getType());

                    Toast.makeText(AttendanceSituation.this, "获取成功", Toast.LENGTH_SHORT).show();
                    Message msg = new Message();
                    msg.what = 2;
                    handler.sendMessage(msg);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    String errorMessage = error.getMessage();
                    Toast.makeText(AttendanceSituation.this, "获取失败,请检查连接网络情况" + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, errorMessage);
                    center_loading_prgbar.setVisibility(View.INVISIBLE);

                }
            }
            );
        } else {
            jsonObjectRequest = new StringRequest(Request.Method.POST, getIp.ip + simple_api, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    StudentsId = gson.fromJson(response, new TypeToken<List<String>>() {
                    }.getType());
                    Toast.makeText(AttendanceSituation.this, "获取成功", Toast.LENGTH_SHORT).show();
                    Message msg = new Message();
                    msg.what = 1;
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
        }

        requestQueue.add(jsonObjectRequest);

    }

    private void SetListView(List<String> attendancedId, List<String> StudentsId) {
        center_loading_prgbar.setVisibility(View.INVISIBLE);

        ArrayAdapter<String> attendancedIdArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, attendancedId);
        ArrayAdapter<String> StudentsIdArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, StudentsId);
        showList.setVisibility(View.VISIBLE);

        attendanceList.setAdapter(attendancedIdArrayAdapter);
        noAttendanceList.setAdapter(StudentsIdArrayAdapter);
    }
}
