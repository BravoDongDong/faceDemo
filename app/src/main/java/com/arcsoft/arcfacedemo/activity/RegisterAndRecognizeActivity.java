package com.arcsoft.arcfacedemo.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
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
import com.arcsoft.arcfacedemo.common.getIp;
import com.arcsoft.arcfacedemo.faceserver.CompareResult;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.model.DrawInfo;
import com.arcsoft.arcfacedemo.model.FacePreviewInfo;
import com.arcsoft.arcfacedemo.model.PersonAttendanceStatus;
import com.arcsoft.arcfacedemo.model.attendanceInfo;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.DrawHelper;
import com.arcsoft.arcfacedemo.util.camera.CameraHelper;
import com.arcsoft.arcfacedemo.util.camera.CameraListener;
import com.arcsoft.arcfacedemo.util.face.FaceHelper;
import com.arcsoft.arcfacedemo.util.face.FaceListener;
import com.arcsoft.arcfacedemo.util.face.RequestFeatureStatus;
import com.arcsoft.arcfacedemo.widget.FaceRectView;
import com.arcsoft.arcfacedemo.widget.ShowFaceInfoAdapter;
import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.VersionInfo;
import com.don.pieviewlibrary.AnimationPercentPieView;
import com.don.pieviewlibrary.LinePieView;
import com.don.pieviewlibrary.PercentPieView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RegisterAndRecognizeActivity extends AppCompatActivity implements ViewTreeObserver.OnGlobalLayoutListener {

    private static volatile RegisterAndRecognizeActivity registerAndRecognizeActivity;
    private static final String TAG = "RegisterAndRecognize";
    private static final int MAX_DETECT_NUM = 10;
    /**
     * 当FR成功，活体未成功时，FR等待活体的时间
     */
    private static final int WAIT_LIVENESS_INTERVAL = 50;
    private CameraHelper cameraHelper;
    private DrawHelper drawHelper;
    private Camera.Size previewSize;
    /**
     * 优先打开的摄像头，本界面主要用于单目RGB摄像头设备，因此默认打开前置
     */
    private Integer rgbCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private FaceEngine faceEngine;
    private FaceHelper faceHelper;
    private List<CompareResult> compareResultList;
    private ShowFaceInfoAdapter adapter;
    /**
     * 活体检测的开关
     */
    private boolean livenessDetect = true;

    /**
     * 注册人脸状态码，准备注册
     */
    private static final int REGISTER_STATUS_READY = 0;
    /**
     * 注册人脸状态码，注册中
     */
    private static final int REGISTER_STATUS_PROCESSING = 1;
    /**
     * 注册人脸状态码，注册结束（无论成功失败）
     */
    private static final int REGISTER_STATUS_DONE = 2;

    private int registerStatus = REGISTER_STATUS_DONE;

    private int afCode = -1;
    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();
    private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();
    /**
     * 相机预览显示的控件，可为SurfaceView或TextureView
     */
    private View previewView;
    /**
     * 绘制人脸框的控件
     */
    private FaceRectView faceRectView;
    /**
     * 饼图
     */
    private AnimationPercentPieView pieView;
    private PercentPieView pieView1;

    private Switch switchLivenessDetect;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static final float SIMILAR_THRESHOLD = 0.7F;

    /**
     * 保存考勤信息以便上传
     */

    private SharedPreferences attendancePreferences;
    private SharedPreferences firstOrSecondPreferences;
    /**
     * 请求队列
     */
    private RequestQueue requestQueue ;


    private List<attendanceInfo> attendanceInfoArrayList;

    /**
     * 考勤信息
     */
    private List<String> userId = new ArrayList<String>();
    private List<String> status = new ArrayList<String>();

    /**
     * 所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE

    };


    public static RegisterAndRecognizeActivity getInstance() {
        if (registerAndRecognizeActivity == null) {
            registerAndRecognizeActivity = new RegisterAndRecognizeActivity();
        }
        return registerAndRecognizeActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_and_recognize);
        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }

        // Activity启动后就锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        //本地人脸库初始化
        FaceServer.getInstance().init(this);

        initView();

        requestQueue = Volley.newRequestQueue(RegisterAndRecognizeActivity.this);
    }

    private void initView() {



//        pieView = (AnimationPercentPieView) findViewById(R.id.pieView3);
//        //设置指定颜色
//        pieView.setData(data, name, color);

        pieView1 = (PercentPieView) findViewById(R.id.pieView2);
        //设置指定颜色


        previewView = findViewById(R.id.texture_preview);
        //在布局结束后才做初始化操作
        previewView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        faceRectView = findViewById(R.id.face_rect_view);
        switchLivenessDetect = findViewById(R.id.switch_liveness_detect);
        switchLivenessDetect.setChecked(livenessDetect);
        switchLivenessDetect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                livenessDetect = isChecked;
            }
        });
        RecyclerView recyclerShowFaceInfo = findViewById(R.id.recycler_view_person);
        compareResultList = new ArrayList<>();
        adapter = new ShowFaceInfoAdapter(compareResultList, this);
        recyclerShowFaceInfo.setAdapter(adapter);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int spanCount = (int) (dm.widthPixels / (getResources().getDisplayMetrics().density * 100 + 0.5f));
        recyclerShowFaceInfo.setLayoutManager(new GridLayoutManager(this, spanCount));
        recyclerShowFaceInfo.setItemAnimator(new DefaultItemAnimator());

        //为ChooseFunctionActivity页面做文本更改，显得更流畅
        firstOrSecondPreferences = getSharedPreferences("firstOrSecond", MODE_PRIVATE);
        Boolean firstOrSecond = firstOrSecondPreferences.getBoolean("firstOrSecond", false);

        if (firstOrSecond)
            ChooseFunctionActivity.firstOrSecondButton.setText("课后考勤");
        else
            ChooseFunctionActivity.firstOrSecondButton.setText("课前考勤");
    }

    /**
     * 初始化引擎
     */
    private void initEngine() {
        faceEngine = new FaceEngine();
        afCode = faceEngine.init(this, FaceEngine.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(this),
                16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_LIVENESS);
        VersionInfo versionInfo = new VersionInfo();
        faceEngine.getVersion(versionInfo);
        Log.i(TAG, "initEngine:  init: " + afCode + "  version:" + versionInfo);

        if (afCode != ErrorInfo.MOK) {
            Toast.makeText(this, getString(R.string.init_failed, afCode), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 销毁引擎
     */
    private void unInitEngine() {

        if (afCode == ErrorInfo.MOK) {
            afCode = faceEngine.unInit();
            Log.i(TAG, "unInitEngine: " + afCode);
        }
    }


    @Override
    protected void onDestroy() {


        //没啥用，原因见ChooseFunctionActivity onResume解释
        firstOrSecondPreferences = getSharedPreferences("firstOrSecond", MODE_PRIVATE);
        Boolean firstOrSecond = firstOrSecondPreferences.getBoolean("firstOrSecond", false);

        SharedPreferences.Editor editor = firstOrSecondPreferences.edit();
        //存入数据
        editor.putBoolean("firstOrSecond", !firstOrSecond);
        //提交修改
        editor.commit();
        Log.e(TAG, String.valueOf(!firstOrSecond));
        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }

        //faceHelper中可能会有FR耗时操作仍在执行，加锁防止crash
        if (faceHelper != null) {
            synchronized (faceHelper) {
                unInitEngine();
            }
            ConfigUtil.setTrackId(this, faceHelper.getCurrentTrackId());
            faceHelper.release();
        } else {
            unInitEngine();
        }
        if (getFeatureDelayedDisposables != null) {
            getFeatureDelayedDisposables.dispose();
            getFeatureDelayedDisposables.clear();
        }
        FaceServer.getInstance().unInit();
        super.onDestroy();

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

    private void initCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final FaceListener faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "onFail: " + e.getMessage());
            }

            //请求FR的回调
            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId) {
                //FR成功
                if (faceFeature != null) {
//                    Log.i(TAG, "onPreview: fr end = " + System.currentTimeMillis() + " trackId = " + requestId);

                    //不做活体检测的情况，直接搜索
                    if (!livenessDetect) {
                        searchFace(faceFeature, requestId);

                    }
                    //活体检测通过，搜索特征
                    else if (livenessMap.get(requestId) != null && livenessMap.get(requestId) == LivenessInfo.ALIVE) {
                        searchFace(faceFeature, requestId);
                    }
                    //活体检测未出结果，延迟100ms再执行该函数
                    else if (livenessMap.get(requestId) != null && livenessMap.get(requestId) == LivenessInfo.UNKNOWN) {
                        getFeatureDelayedDisposables.add(Observable.timer(WAIT_LIVENESS_INTERVAL, TimeUnit.MILLISECONDS)
                                .subscribe(new Consumer<Long>() {
                                    @Override
                                    public void accept(Long aLong) {
                                        onFaceFeatureInfoGet(faceFeature, requestId);
                                    }
                                }));
                    }
                    //活体检测失败
                    else {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.NOT_ALIVE);
                    }

                }
                //FR 失败
                else {
                    requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                }
            }

        };


        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                previewSize = camera.getParameters().getPreviewSize();
                drawHelper = new DrawHelper(previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientation
                        , cameraId, isMirror, false, false);
                Log.i(TAG, "onCameraOpened: " + drawHelper.toString());
                faceHelper = new FaceHelper.Builder()
                        .faceEngine(faceEngine)
                        .frThreadNum(MAX_DETECT_NUM)
                        .previewSize(previewSize)
                        .faceListener(faceListener)
                        .currentTrackId(ConfigUtil.getTrackId(RegisterAndRecognizeActivity.this.getApplicationContext()))
                        .build();
            }


            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                if (faceRectView != null) {
                    faceRectView.clearFaceInfo();
                }
                List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21);
                if (facePreviewInfoList != null && faceRectView != null && drawHelper != null) {
                    drawPreviewInfo(facePreviewInfoList);
                }
                registerFace(nv21, facePreviewInfoList);
                clearLeftFace(facePreviewInfoList);

                if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && previewSize != null) {
                    for (int i = 0; i < facePreviewInfoList.size(); i++) {
                        if (livenessDetect) {
                            livenessMap.put(facePreviewInfoList.get(i).getTrackId(), facePreviewInfoList.get(i).getLivenessInfo().getLiveness());
                        }
                        /**
                         * 对于每个人脸，若状态为空或者为失败，则请求FR（可根据需要添加其他判断以限制FR次数），
                         * FR回传的人脸特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer)}中回传
                         */
                        if (requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == null
                                || requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == RequestFeatureStatus.FAILED) {
                            requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
                            faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
//                            Log.i(TAG, "onPreview: fr start = " + System.currentTimeMillis() + " trackId = " + facePreviewInfoList.get(i).getTrackId());
                        }
                    }
                }
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                if (drawHelper != null) {
                    drawHelper.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(previewView.getMeasuredWidth(), previewView.getMeasuredHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(rgbCameraID != null ? rgbCameraID : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .isMirror(false)
                .previewOn(previewView)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
        cameraHelper.start();
    }

    private void registerFace(final byte[] nv21, final List<FacePreviewInfo> facePreviewInfoList) {
        if (registerStatus == REGISTER_STATUS_READY && facePreviewInfoList != null && facePreviewInfoList.size() > 0) {
            registerStatus = REGISTER_STATUS_PROCESSING;
            Observable.create(new ObservableOnSubscribe<Boolean>() {
                @Override
                public void subscribe(ObservableEmitter<Boolean> emitter) {
                    boolean success = FaceServer.getInstance().registerNv21(RegisterAndRecognizeActivity.this, nv21.clone(), previewSize.width, previewSize.height,
                            facePreviewInfoList.get(0).getFaceInfo(), "registered " + 1);
                    emitter.onNext(success);
                }
            })
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Boolean success) {
                            String result = success ? "register success!" : "register failed!";
                            Toast.makeText(RegisterAndRecognizeActivity.this, result, Toast.LENGTH_SHORT).show();
                            registerStatus = REGISTER_STATUS_DONE;
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(RegisterAndRecognizeActivity.this, "register failed!", Toast.LENGTH_SHORT).show();
                            registerStatus = REGISTER_STATUS_DONE;
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

    private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList) {
        String name = null;
        List<DrawInfo> drawInfoList = new ArrayList<>();
        for (int i = 0; i < facePreviewInfoList.size(); i++) {
             name = faceHelper.getName(facePreviewInfoList.get(i).getTrackId());
            Integer liveness = livenessMap.get(facePreviewInfoList.get(i).getTrackId());
            drawInfoList.add(new DrawInfo(drawHelper.adjustRect(facePreviewInfoList.get(i).getFaceInfo().getRect()), GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE,
                    liveness == null ? LivenessInfo.UNKNOWN : liveness,
                    name == null ? String.valueOf(facePreviewInfoList.get(i).getTrackId()) : name));
        }
        drawHelper.draw(faceRectView, drawInfoList);
        //Log.e("test","drawPreviewInfo" +  name);
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
//                activeEngine();
                initEngine();
                initCamera();
                if (cameraHelper != null) {
                    cameraHelper.start();
                }
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 删除已经离开的人脸
     *
     * @param facePreviewInfoList 人脸和trackId列表
     */
    private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
        Set<Integer> keySet = requestFeatureStatusMap.keySet();
        if (compareResultList != null) {
            for (int i = compareResultList.size() - 1; i >= 0; i--) {
                if (!keySet.contains(compareResultList.get(i).getTrackId())) {
                    compareResultList.remove(i);
                    adapter.notifyItemRemoved(i);
                }
            }
        }
        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
            requestFeatureStatusMap.clear();
            livenessMap.clear();
            return;
        }

        for (Integer integer : keySet) {
            boolean contained = false;
            for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
                if (facePreviewInfo.getTrackId() == integer) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                requestFeatureStatusMap.remove(integer);
                livenessMap.remove(integer);
            }
        }

    }

    private void searchFace(final FaceFeature frFace, final Integer requestId) {
        //Log.e("test","searchFace" + requestId);
        Observable
                .create(new ObservableOnSubscribe<CompareResult>() {
                    @Override
                    public void subscribe(ObservableEmitter<CompareResult> emitter) {
//                        Log.i(TAG, "subscribe: fr search start = " + System.currentTimeMillis() + " trackId = " + requestId);
                        CompareResult compareResult = FaceServer.getInstance().getTopOfFaceLib(frFace);
//                        Log.i(TAG, "subscribe: fr search end = " + System.currentTimeMillis() + " trackId = " + requestId);
                        if (compareResult == null) {
                            emitter.onError(null);
                        } else {
                            emitter.onNext(compareResult);
                        }
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CompareResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CompareResult compareResult) {
                        if (compareResult == null || compareResult.getUserName() == null) {
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                            faceHelper.addName(requestId, "VISITOR " + requestId);
                            return;
                        }

//                        Log.i(TAG, "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = " + requestId + "  similar = " + compareResult.getSimilar());
                        if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
                            //比对成功，进行数据库操作
                            Log.e("test", "searchfacesuccess");
                            boolean isAdded = false;
                            if (compareResultList == null) {
                                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                                faceHelper.addName(requestId, "VISITOR " + requestId);
                                return;
                            }
                            for (CompareResult compareResult1 : compareResultList) {
                                if (compareResult1.getTrackId() == requestId) {
                                    isAdded = true;
                                    break;
                                }
                            }
                            if (!isAdded) {
                                //对于多人脸搜索，假如最大显示数量为 MAX_DETECT_NUM 且有新的人脸进入，则以队列的形式移除
                                if (compareResultList.size() >= MAX_DETECT_NUM) {
                                    compareResultList.remove(0);
                                    adapter.notifyItemRemoved(0);
                                }
                                //添加显示人员时，保存其trackId
                                compareResult.setTrackId(requestId);
                                compareResultList.add(compareResult);
                                adapter.notifyItemInserted(compareResultList.size() - 1);
                            }
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
                            faceHelper.addName(requestId, compareResult.getUserName());

                            //在这里保存考勤人以及时间等信息

//                            Message msg = Message.obtain();
//                            Bundle bundle = new Bundle();
//                            bundle.putString("id", compareResult.getUserName());
//                            msg.setData(bundle);
//                            msg.obj = "1";
//                            handler.sendMessage(msg);
//                            getSharedPreferences(getApplicationContext());
//                            setSharedPreference(compareResult.getUserName(), new Date());
                            firstOrSecondPreferences = getSharedPreferences("firstOrSecond", MODE_PRIVATE);
                            Boolean firstOrSecond = firstOrSecondPreferences.getBoolean("firstOrSecond", false);
                            String id = compareResult.getUserName();
                            String url;
                            //判断运行单复数 true:first  false:second
                            if (firstOrSecond) {
                                url = getIp.ip + "/Values/FirstAttendance?id=" + id;

                            } else {
                                url = getIp.ip + "/Values/SecondAttendance?id=" + id;
                            }


                            StringRequest request = new StringRequest(url, new Response.Listener<String>() {

                                @Override
                                public void onResponse(String response) {
                                    Log.e(TAG, response);
                                    Toast.makeText(RegisterAndRecognizeActivity.this, "考勤成功", Toast.LENGTH_SHORT).show();

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(TAG, error.toString());
                                    Toast.makeText(RegisterAndRecognizeActivity.this, "考勤请求失败,请检查连接网络情况", Toast.LENGTH_SHORT).show();
                                }

                            });
                            url = getIp.ip + "/Values/PersonalStatus";
                            Gson gson = new Gson();
                            String parseJson = gson.toJson(attendanceInfoArrayList);
                            JSONObject params = new JSONObject();
                            try {
                                params.put("id", id);
                                params.put("range","");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.e(TAG, response.toString());

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    String errorMessage = error.getMessage();
                                    Toast.makeText(RegisterAndRecognizeActivity.this, "个人统计请求失败,请检查连接网络情况" + errorMessage, Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, errorMessage);
                                }
                            }
                            );

                            requestQueue.add(request);
                            getPersonalStatus(id,30);

                        } else {
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                            faceHelper.addName(requestId, "VISITOR " + requestId);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private void getPersonalStatus(String id, int range) {


        String url = getIp.ip + "/Values/PersonalStatus";
        JSONObject params = new JSONObject();
        try {
            params.put("id", id);
            params.put("range",range);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.e(TAG + "123", response.toString());
                int[] data = new int[]{0, 0, 0, 0};//用数值计算百分比，总和在饼图中间
                String[] name = new String[]{"旷课", "迟到", "早退", "正常"};//饼图分类项
                int[] color = new int[]{//饼图分类项颜色
                        getResources().getColor(R.color.red),
                        getResources().getColor(R.color.colorPrimaryDark),
                        getResources().getColor(R.color.blue),
                        getResources().getColor(R.color.green)};
                try {
                    JSONArray jsonArray = response.getJSONArray("Table");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String finallyStatus = jsonObject.getString("finallyStatus");
                        String num = jsonObject.getString("num");
                        switch (finallyStatus) {
                            case "0":
                                data[0] = Integer.parseInt(num);
                                break;
                            case "1":
                                data[1] = Integer.parseInt(num);
                                break;
                            case "2":
                                data[2] = Integer.parseInt(num);
                                break;
                            case "3":
                                data[3] = Integer.parseInt(num);
                                break;
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                {"Table":[{"finallyStatus":2,"数量":1},{"finallyStatus":3,"数量":1}]}
                pieView1.setVisibility(View.VISIBLE);
                pieView1.setData(data, name, color);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = error.getMessage();

                Toast.makeText(RegisterAndRecognizeActivity.this, "个人统计请求失败,请检查连接网络情况" + errorMessage, Toast.LENGTH_SHORT).show();

            }
        }
        );



        requestQueue.add(jsonObjectRequest);
    }

    /**
     * 将准备注册的状态置为{@link #REGISTER_STATUS_READY}
     *
     * @param view 注册按钮
     */
    public void register(View view) {
        if (registerStatus == REGISTER_STATUS_DONE) {
            registerStatus = REGISTER_STATUS_READY;
        }
    }



    /**
     * 在{@link #previewView}第一次布局完成后，去除该监听，并且进行引擎和相机的初始化
     */
    @Override
    public void onGlobalLayout() {
        previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            initEngine();
            initCamera();
        }
    }
    // TODO: 19-11-5 每次保存前获取刷新考勤信息getSharedPreferences 判定是否已存在，状态信息，
    // TODO: 19-11-5  后续单独给出ｇｅｔ方法获取信息发送请求

    /**
     * 获取考勤缓存信息
     */

    public void getSharedPreferences(Context context) {

        attendancePreferences = context.getSharedPreferences("attendance", MODE_PRIVATE);
        String json = attendancePreferences.getString("handleJson", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<attendanceInfo>>(){}.getType();
            attendanceInfoArrayList = new ArrayList<attendanceInfo>();
            attendanceInfoArrayList = gson.fromJson(json, type);
            for(int i = 0; i < attendanceInfoArrayList.size(); i++)
            {
                Log.e(TAG, "getSharepreference" + attendanceInfoArrayList.get(i).getId()+":" + String.valueOf(attendanceInfoArrayList.get(i).getFirstTime()));
            }
        } else {
            attendanceInfoArrayList = new ArrayList<>();
        }
    }


    public void setSharedPreference(String id, Date Time) {

        boolean isHave = false;

        for(int i = 0; i < attendanceInfoArrayList.size(); i++) {
            //判断是否已存在该id
            if (attendanceInfoArrayList.get(i).getId().equals(id) ) {
                attendanceInfoArrayList.get(i).setEndTime(Time);
                Log.e(TAG, "已存在该id");
                isHave = true;
            }

        }
        if (!isHave) {
            attendanceInfo attendanceInfo = new attendanceInfo(id, Time);
            attendanceInfoArrayList.add(attendanceInfo);
            Log.e(TAG, "添加新信息");
        }
        for (int i = 0; i < attendanceInfoArrayList.size(); i ++ ) {
            Log.e(TAG, "new attendanceInfoArrayList" + attendanceInfoArrayList.get(i).getId()+":" + String.valueOf(attendanceInfoArrayList.get(i).getFirstTime()));

        }
        SharedPreferences.Editor editor = getSharedPreferences("attendance", MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(attendanceInfoArrayList);
        Log.e(TAG, "list is "+ attendanceInfoArrayList);

        Log.e(TAG, "saved json is "+ json);
        editor.putString("handleJson", json);
        editor.commit();

    }

    public void clearShareperference(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("attendance", MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = null;

        Log.e(TAG, "saved json is "+ json);
        editor.putString("handleJson", json);
        editor.commit();
    }

    public List getAttendanceInfoArrayList(Context context) {
        getSharedPreferences(context);
        return attendanceInfoArrayList;
    }


}
