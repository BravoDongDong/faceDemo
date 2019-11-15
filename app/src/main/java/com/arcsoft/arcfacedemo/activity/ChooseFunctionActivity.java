package com.arcsoft.arcfacedemo.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.common.Constants;
import com.arcsoft.arcfacedemo.model.attendanceInfo;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class ChooseFunctionActivity extends AppCompatActivity {
    private static final String TAG = "ChooseFunctionActivity";
    private Toast toast = null;
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE
    };
    private FaceEngine faceEngine = new FaceEngine();
    private SharedPreferences Countpreferences;
    private Button activeEngine ;
    private ProgressBar help_center_loading_prgbar;

    private String urls = "http://127.0.0.1:54715/api/attendance/insert";
    // TODO: 19-11-11 测试地址
//    private String urls = "https://www.baidu.com";

    private boolean isRequest = false;
    /**
     * 考勤信息list
     */
    private List<attendanceInfo> attendanceInfoArrayList = new ArrayList<attendanceInfo>();

    /**
     * 请求
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_function);
        initView();
    }

    private void initView() {

        Countpreferences = getSharedPreferences("count", MODE_PRIVATE);
        final int[] count = {Countpreferences.getInt("count", 0)};
        activeEngine = (Button) findViewById(R.id.activeEngine);
        help_center_loading_prgbar = (ProgressBar) findViewById(R.id.help_center_loading_prgbar);
        //判断运行次数，不是初次启动隐藏激活引擎按钮
        if (count[0] != 0) {
            activeEngine.setVisibility(View.GONE);
        }

        activeEngine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //判断程序与第几次运行，如果是第一次运行则激活
                if (count[0] == 0) {
                    activeEngine(new View(getApplicationContext()));
                    activeEngine.setVisibility(View.GONE);
                }
                SharedPreferences.Editor editor = Countpreferences.edit();
                //存入数据
                editor.putInt("count", ++count[0]);
                //提交修改
                editor.commit();
            }
        });


        //设置视频模式下的人脸优先检测方向
//        RadioGroup radioGroupFtOrient = findViewById(R.id.radio_group_ft_orient);
//        RadioButton rbOrient0 = findViewById(R.id.rb_orient_0);
//        RadioButton rbOrient90 = findViewById(R.id.rb_orient_90);
//        RadioButton rbOrient180 = findViewById(R.id.rb_orient_180);
//        RadioButton rbOrient270 = findViewById(R.id.rb_orient_270);
//        RadioButton rbOrientAll = findViewById(R.id.rb_orient_all);
//
//        switch (ConfigUtil.getFtOrient(this)) {
//            case FaceEngine.ASF_OP_0_ONLY:
//                rbOrient0.setChecked(true);
//                break;
//            case FaceEngine.ASF_OP_90_ONLY:
//                rbOrient90.setChecked(true);
//                break;
//            case FaceEngine.ASF_OP_180_ONLY:
//                rbOrient180.setChecked(true);
//                break;
//            case FaceEngine.ASF_OP_270_ONLY:
//                rbOrient270.setChecked(true);
//                break;
//            case FaceEngine.ASF_OP_0_HIGHER_EXT:
//                rbOrientAll.setChecked(true);
//                break;
//            default:
//                rbOrient0.setChecked(true);
//                break;
//        }
//        radioGroupFtOrient.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                switch (checkedId) {
//                    case R.id.rb_orient_0:
//                        ConfigUtil.setFtOrient(ChooseFunctionActivity.this, FaceEngine.ASF_OP_0_ONLY);
//                        break;
//                    case R.id.rb_orient_90:
//                        ConfigUtil.setFtOrient(ChooseFunctionActivity.this, FaceEngine.ASF_OP_90_ONLY);
//                        break;
//                    case R.id.rb_orient_180:
//                        ConfigUtil.setFtOrient(ChooseFunctionActivity.this, FaceEngine.ASF_OP_180_ONLY);
//                        break;
//                    case R.id.rb_orient_270:
//                        ConfigUtil.setFtOrient(ChooseFunctionActivity.this, FaceEngine.ASF_OP_270_ONLY);
//                        break;
//                    case R.id.rb_orient_all:
//                        ConfigUtil.setFtOrient(ChooseFunctionActivity.this, FaceEngine.ASF_OP_0_HIGHER_EXT);
//                        break;
//                    default:
//                        ConfigUtil.setFtOrient(ChooseFunctionActivity.this, FaceEngine.ASF_OP_0_ONLY);
//                        break;
//                }
//            }
//        });
    }

    /**
     * 打开相机，显示年龄性别
     *
     * @param view
     */
    public void jumpToPreviewActivity(View view) {
        startActivity(new Intent(this, PreviewActivity.class));
    }

    /**
     * 处理单张图片，显示图片中所有人脸的信息，并且一一比对相似度
     *
     * @param view
     */
    public void jumpToSingleImageActivity(View view) {
        startActivity(new Intent(this, SingleImageActivity.class));
    }

    /**
     * 选择一张主照，显示主照中人脸的详细信息，然后选择图片和主照进行比对
     *
     * @param view
     */
    public void jumpToMultiImageActivity(View view) {
        startActivity(new Intent(this, MultiImageActivity.class));
    }

    /**
     * 打开相机，RGB活体检测，人脸注册，人脸识别
     *
     * @param view
     */
    public void jumpToFaceRecognizeActivity(View view) {
        startActivity(new Intent(this, RegisterAndRecognizeActivity.class));
    }

    /**
     * 当前考勤情况统计
     * @param view
     */
    public void jumpToAttendanceSituationActivity(View view) {

    }

    /**
     * 打开相机，IR活体检测，人脸注册，人脸识别
     *
     * @param view
     */
    public void jumpToIrFaceRecognizeActivity(View view) {
        startActivity(new Intent(this, IrRegisterAndRecognizeActivity.class));
    }

    /**
     * 准备跳转到人脸库
     *
     * @param view 跳转按钮
     */
    public void jumpToBatchRegisterActivity(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.dialog_pwd,null);
        final EditText pwd = v.findViewById(R.id.password);
        builder.setView(v)
                .setCancelable(true)
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //进行密码比对，成功后跳转到人脸库界面
                        String test = pwd.getText().toString();
                        if (pwd.getText().toString().equals("qbz95")) {
                            Intent intent = new Intent();
                            intent.setClass(getApplicationContext(), FaceManageActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ChooseFunctionActivity.this, "密码错误", Toast.LENGTH_SHORT).show();

                            return;
                        }

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Log.e("test","onClick cancel");

                    }
                }).create();
        builder.show();
    }

    /**
     * 激活引擎
     *
     * @param view
     */
    public void activeEngine(final View view) {
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
            return;
        }
        if (view != null) {
            view.setClickable(false);
        }
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                int activeCode = faceEngine.activeOnline(ChooseFunctionActivity.this, Constants.APP_ID, Constants.SDK_KEY);
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {
                            showToast(getString(R.string.active_success));
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            showToast(getString(R.string.already_activated));
                        } else {
                            showToast(getString(R.string.active_failed, activeCode));
                        }

                        if (view != null) {
                            view.setClickable(true);
                        }
                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                        int res = faceEngine.getActiveFileInfo(ChooseFunctionActivity.this,activeFileInfo);
                        if (res == ErrorInfo.MOK) {
                            Log.i(TAG, activeFileInfo.toString());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    /**
     * 提交
     *
     * @param view
     */
    public void commit(View view) {
        //判断是否正在请求
        if (isRequest) {
            Toast.makeText(this, "正在请求中，请稍后", Toast.LENGTH_SHORT).show();
            return;
        }

        attendanceInfoArrayList = RegisterAndRecognizeActivity.getInstance().getAttendanceInfoArrayList(getApplicationContext());

        if (attendanceInfoArrayList.isEmpty()) {
            Toast.makeText(this, "没有数据需要上传", Toast.LENGTH_SHORT).show();
            return;
        }
        isRequest = true;
        //help_center_loading_prgbar.clearAnimation();
        help_center_loading_prgbar.setVisibility(View.VISIBLE);
        // TODO: 19-11-6 volley  新开辟线程提交数据给 webapi,并清空shareperence

        Gson gson = new Gson();
        String parseJson = gson.toJson(attendanceInfoArrayList);
        JSONObject params = new JSONObject();
        try {
            params.put("msg", parseJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, urls, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(ChooseFunctionActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
                Log.e(TAG, response.toString());
                //拿到正确的返回结果才清空shareperference
                RegisterAndRecognizeActivity.getInstance().clearShareperference(getApplicationContext());
                isRequest = false;
                help_center_loading_prgbar.setVisibility(View.INVISIBLE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = error.getMessage();
                Toast.makeText(ChooseFunctionActivity.this, "提交失败,请检查连接网络情况" + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e(TAG, errorMessage);
                isRequest = false;
                help_center_loading_prgbar.setVisibility(View.INVISIBLE);

            }
        }
        );


        //TODO: 测试get方式请求baidu,后续移除
        StringRequest stringRequest = new StringRequest("https://www.baidu.com",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("TAG", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        });


        requestQueue.add(jsonObjectRequest);



    }

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                activeEngine(null);
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }

    private void showToast(String s) {
        if (toast == null) {
            toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            toast.setText(s);
            toast.show();
        }
    }

}
