package com.arcsoft.arcfacedemo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.common.getIp;
import com.arcsoft.arcfacedemo.model.PersonalStutasInfo;
import com.openxu.cview.chart.barchart.BarHorizontalChart;
import com.openxu.cview.chart.bean.BarBean;
import com.openxu.utils.DensityUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackView extends AppCompatActivity {

    private List<String> studentsId = new ArrayList<String>();
    private List AttendancedStatus = new ArrayList<PersonalStutasInfo>();
    private Map map = new HashMap<String, List>();
    private String testJson = "{\n" +
            "\"Table\": [\n" +
            "{\n" +
            "\"id\": \"1620271 \",\n" +
            "\"finallyStatus\": 0,\n" +
            "\"num\": 1\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"1620272 \",\n" +
            "\"finallyStatus\": 0,\n" +
            "\"num\": 5\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"1620273 \",\n" +
            "\"finallyStatus\": 0,\n" +
            "\"num\": 5\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"1620274 \",\n" +
            "\"finallyStatus\": 0,\n" +
            "\"num\": 5\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"1620275 \",\n" +
            "\"finallyStatus\": 0,\n" +
            "\"num\": 5\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"1620276 \",\n" +
            "\"finallyStatus\": 0,\n" +
            "\"num\": 5\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"1620277 \",\n" +
            "\"finallyStatus\": 0,\n" +
            "\"num\": 5\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"1620278 \",\n" +
            "\"finallyStatus\": 0,\n" +
            "\"num\": 5\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"1620279 \",\n" +
            "\"finallyStatus\": 0,\n" +
            "\"num\": 5\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"1620280 \",\n" +
            "\"finallyStatus\": 0,\n" +
            "\"num\": 5\n" +
            "},\n" +
            "{\n" +
            "\"id\": \"1620271 \",\n" +
            "\"finallyStatus\": 3,\n" +
            "\"num\": 4\n" +
            "}\n" +
            "]\n" +
            "}";

    List<String> strXList;
    List<List<BarBean>>dataList;
    private BarHorizontalChart chart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stack_view);

        chart = (BarHorizontalChart)findViewById(R.id.chart);
        chart.setBarSpace(DensityUtil.dip2px(this, 1));  //双柱间距
        chart.setBarItemSpace(DensityUtil.dip2px(this, 20));  //柱间距
        chart.setDebug(true);
        chart.setBarNum(4);
        chart.setBarColor(new int[]{Color.parseColor("#FF3333"),Color.parseColor("#5F93E7"),Color.parseColor("#F28D02"),Color.parseColor("#5BFF33")});

        //X轴
        strXList = new ArrayList<>();
        //柱状图数据
        dataList = new ArrayList<>();
        // TODO: 19-11-25 更改为接口解析后数据，填充数据
        dataList.clear();
        strXList.clear();
        GetStatus(30);

    }

    public void SelectThirtyDay(View view){
        dataList.clear();
        strXList.clear();
        GetStatus(30);

    }
    public void SelectHalfOfYear(View view){
        dataList.clear();
        strXList.clear();
        GetStatus(30 * 6);

    }
    public void SelectOneYear(View view){
        dataList.clear();
        strXList.clear();
        GetStatus(30 * 12);

    }

    private void GetStatus(int range) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String LastUrl = "/Values/SelectAllStatusByTime?range=" + range;
        StringRequest stringRequest = new StringRequest(getIp.ip + LastUrl, new Response.Listener<String>() {
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
                        int id = Integer.parseInt(jsonObject.getString("id").trim());
                        int finallyStatus = jsonObject.getInt("finallyStatus");
                        int num = jsonObject.getInt("num");
                        List AttendancedStatus = new ArrayList<PersonalStutasInfo>();
                        AttendancedStatus.clear();
// TODO: 2019/12/1 list map model 转换问题， 改变成JSONobject 存储数据

                        PersonalStutasInfo personalStutasInfo = new PersonalStutasInfo(finallyStatus, num);
                        if (map.containsKey(id)) {
                            ArrayList<PersonalStutasInfo> a = (ArrayList<PersonalStutasInfo>) map.get(id);
                            for (int b=0; b<a.size(); b++) {
                                AttendancedStatus.add(a.get(b));
                            }

                            AttendancedStatus.add(personalStutasInfo);
                        } else {
                            AttendancedStatus.add(personalStutasInfo);
                        }

                        map.put(id, AttendancedStatus);

                    }
                    for(Object key : map.keySet()){
                        Log.e("test","Key = "+key+"value = "+map.get(key));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setStack();
                //Toast.makeText(AttendanceSituation.this, "获取成功", Toast.LENGTH_SHORT).show();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("test", error.toString());
                String errorMessage = error.getMessage();
                Toast.makeText(StackView.this, "提交失败,请检查连接网络情况" + errorMessage, Toast.LENGTH_SHORT).show();

            }
        }
        );

        requestQueue.add(stringRequest);
    }

    private void setStack() {
        dataList.clear();
        strXList.clear();
        int Absences = 0,  EarlyLeave = 0,  Late = 0,  normal = 0;
        Object[] keys = map.keySet().toArray();
        Arrays.sort(keys);
        for (Object key : keys) {

            ArrayList<PersonalStutasInfo> AttendancedList = (ArrayList<PersonalStutasInfo>)map.get(key);
            for (int i=0; i < AttendancedList.size(); i++) {
                switch (((PersonalStutasInfo)AttendancedList.get(i)).getFinallyStatus()) {
                    case 0:
                        Absences = AttendancedList.get(i).getNum();
                        break;
                    case 1:
                        EarlyLeave = AttendancedList.get(i).getNum();
                        break;
                    case 2:
                        Late = AttendancedList.get(i).getNum();
                        break;
                    case 3:
                        normal = AttendancedList.get(i).getNum();
                        break;

                }
            }
            List<BarBean> list = new ArrayList<>();
            list.add(new BarBean(Absences, "lable1"));
            list.add(new BarBean(EarlyLeave, "lable2"));
            list.add(new BarBean(Late, "lable3"));
            list.add(new BarBean(normal, "lable4"));
            dataList.add(list);
            strXList.add(key+"");


        }
        chart.setLoading(false);
        chart.setData(dataList, strXList);
        map.clear();

//        for(int i = 0; i<map.size(); i++){
//            //此集合为柱状图上一条数据，集合中包含几个实体就是几个柱子
//            List<BarBean> list = new ArrayList<>();
//            list.add(new BarBean(Absences, "lable1"));
//            list.add(new BarBean(EarlyLeave, "lable2"));
//            list.add(new BarBean(Late, "lable3"));
//            list.add(new BarBean(normal, "lable4"));
//            dataList.add(list);
//            strXList.add((i+1)+"");
//        }
//        chart.setLoading(false);
//        chart.setData(dataList, strXList);
    }
}
