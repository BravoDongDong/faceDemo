package com.arcsoft.arcfacedemo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.arcsoft.arcfacedemo.R;
import com.openxu.cview.chart.barchart.BarHorizontalChart;
import com.openxu.cview.chart.bean.BarBean;
import com.openxu.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

public class StackView extends AppCompatActivity {

    List<String> strXList;
    List<List<BarBean>> dataList;
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
        for(int i = 0; i<10; i++){
            //此集合为柱状图上一条数据，集合中包含几个实体就是几个柱子
            List<BarBean> list = new ArrayList<>();
            list.add(new BarBean(1, "lable1"));
            list.add(new BarBean(2, "lable2"));
            list.add(new BarBean(3, "lable3"));
            list.add(new BarBean(100, "lable4"));
            dataList.add(list);
            strXList.add((i+1)+"");
        }
        chart.setLoading(false);
        chart.setData(dataList, strXList);

    }

    public void SelectThirtyDay(View view){
//        startActivity(new Intent(this, StackView.class));
        dataList.clear();
        strXList.clear();
        for(int i = 0; i<10; i++){
            //此集合为柱状图上一条数据，集合中包含几个实体就是几个柱子
            List<BarBean> list = new ArrayList<>();
            list.add(new BarBean(0, "lable1"));
            list.add(new BarBean(0, "lable2"));
            list.add(new BarBean(0, "lable3"));
            list.add(new BarBean(1, "lable4"));

            dataList.add(list);
            strXList.add((i+1)+"");
        }
        chart.setLoading(false);
        chart.setData(dataList, strXList);
    }
    public void SelectHalfOfYear(View view){
        dataList.clear();
        strXList.clear();
        for(int i = 0; i<10; i++){
            //此集合为柱状图上一条数据，集合中包含几个实体就是几个柱子
            List<BarBean> list = new ArrayList<>();
            list.add(new BarBean(1, "lable1"));
            list.add(new BarBean(2, "lable2"));
            list.add(new BarBean(3, "lable3"));
            list.add(new BarBean(100, "lable4"));
            dataList.add(list);
            strXList.add((i+1)+"");
        }
        chart.setLoading(false);
        chart.setData(dataList, strXList);
    }
    public void SelectOneYear(View view){
        dataList.clear();
        strXList.clear();
        for(int i = 0; i<10; i++){
            //此集合为柱状图上一条数据，集合中包含几个实体就是几个柱子
            List<BarBean> list = new ArrayList<>();
            list.add(new BarBean(1, "lable1"));
            list.add(new BarBean(2, "lable2"));
            list.add(new BarBean(3, "lable3"));
            list.add(new BarBean(100, "lable4"));
            dataList.add(list);
            strXList.add((i+1)+"");
        }
        chart.setLoading(false);
        chart.setData(dataList, strXList);
    }
}
