package com.arcsoft.arcfacedemo.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.widget.ProgressDialog;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.face.util.ImageUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 批量注册页面
 */
public class FaceManageActivity extends AppCompatActivity {
    //注册图所在的目录
    private static final String ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "arcfacedemo";
    public static final String REGISTER_DIR = ROOT_DIR + File.separator + "register";
    public static final String REGISTER_FAILED_DIR = ROOT_DIR + File.separator + "failed";
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private TextView tvNotificationRegisterResult;

    ProgressDialog progressDialog = null;
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_manage);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        tvNotificationRegisterResult = findViewById(R.id.notification_register_result);
        progressDialog = new ProgressDialog(this);
        FaceServer.getInstance().init(this);
        //创建存放人脸图片路径目录，初始化时完成！！！
        new File(REGISTER_DIR).mkdirs();
        new File(REGISTER_FAILED_DIR).mkdirs();
    }

    @Override
    protected void onDestroy() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        FaceServer.getInstance().unInit();
        super.onDestroy();
    }

    public void batchRegister(View view) {
        if (checkPermissions(NEEDED_PERMISSIONS)) {
            doRegister();
        } else {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        }
    }

    private void doRegister() {
        File dir = new File(REGISTER_DIR);
        if (!dir.exists()) {
            Toast.makeText(this, "path \n" + REGISTER_DIR + "\n is not exists", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!dir.isDirectory()) {
            Toast.makeText(this, "path \n" + REGISTER_DIR + "\n is not a directory", Toast.LENGTH_SHORT).show();
            return;
        }
        final File[] jpgFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(FaceServer.IMG_SUFFIX);
            }
        });
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final int totalCount = jpgFiles.length;

                int successCount = 0;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setMaxProgress(totalCount);
                        progressDialog.show();
                        tvNotificationRegisterResult.setText("");
                        tvNotificationRegisterResult.append("process start,please wait\n\n");
                    }
                });
                for (int i = 0; i < totalCount; i++) {
                    Log.e("test", "start handling jpg");
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDialog != null) {
                                progressDialog.refreshProgress(finalI);
                            }
                        }
                    });
                    final File jpgFile = jpgFiles[i];
                    int tangle = readPictureDegree(jpgFile.getPath());
                    Bitmap resizedBitmap;
                    Bitmap bitmap = BitmapFactory.decodeFile(jpgFile.getAbsolutePath());
                    if (tangle == 0) {
                        resizedBitmap = bitmap;
                    } else {
                        resizedBitmap = rotaingImageView(tangle,bitmap);
                    }

                    if (resizedBitmap == null) {
                        File failedFile = new File(REGISTER_FAILED_DIR + File.separator + jpgFile.getName());
                        if (!failedFile.getParentFile().exists()) {
                            failedFile.getParentFile().mkdirs();
                        }
                        jpgFile.renameTo(failedFile);
                        continue;
                    }
                    bitmap = ImageUtils.alignBitmapForBgr24(resizedBitmap);
                    if (bitmap == null) {
                        File failedFile = new File(REGISTER_FAILED_DIR + File.separator + jpgFile.getName());
                        if (!failedFile.getParentFile().exists()) {
                            failedFile.getParentFile().mkdirs();
                        }
                        jpgFile.renameTo(failedFile);
                        continue;
                    }
                    byte[] bgr24 = ImageUtils.bitmapToBgr24(bitmap);
                    boolean success = FaceServer.getInstance().registerBgr24(FaceManageActivity.this, bgr24, bitmap.getWidth(), bitmap.getHeight(),
                            jpgFile.getName().substring(0, jpgFile.getName().lastIndexOf(".")));
                    Log.e("test", "stop handling jpg");
                    if (!success) {
                        File failedFile = new File(REGISTER_FAILED_DIR + File.separator + jpgFile.getName());
                        if (!failedFile.getParentFile().exists()) {
                            failedFile.getParentFile().mkdirs();
                        }
                        jpgFile.renameTo(failedFile);
                    } else {
                        successCount++;
                    }
                }
                final int finalSuccessCount = successCount;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        tvNotificationRegisterResult.append("process finished!\ntotal count = " + totalCount + "\nsuccess count = " + finalSuccessCount + "\nfailed count = " + (totalCount - finalSuccessCount)
                                + "\nfailed images are in directory '" + REGISTER_FAILED_DIR + "'");
                    }
                });
                //Log.i(FaceManageActivity.class.getSimpleName(), "run: " + executorService.isShutdown());
            }
        });
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
                doRegister();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this.getApplicationContext(), neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    public void clearFaces(View view) {
        int faceNum = FaceServer.getInstance().getFaceNumber(this);
        if (faceNum == 0){
            Toast.makeText(this, R.string.no_face_need_to_delete, Toast.LENGTH_SHORT).show();
        }else {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.notification)
                    .setMessage(getString(R.string.confirm_delete,faceNum)  )
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int deleteCount = FaceServer.getInstance().clearAllFaces(FaceManageActivity.this);
                            Toast.makeText(FaceManageActivity.this, deleteCount + " faces cleared!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel,null)
                    .create();
            dialog.show();
        }
    }

    //获取图片的旋转角度
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }


    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

//    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
//    bitmapOptions.inSampleSize = 8;
//    File file = new File(SD_CARD_TEMP_DIR);
//    /**
//     * 获取图片的旋转角度，有些系统把拍照的图片旋转了，有的没有旋转
//     */
//    int degree = ImageDispose.readPictureDegree(file.getAbsolutePath());
//    Bitmap cameraBitmap = BitmapFactory.decodeFile(SD_CARD_TEMP_DIR, bitmapOptions);
//    bitmap = cameraBitmap;
//    /**
//     * 把图片旋转为正的方向
//     */
//    bitmap = ImageDispose.rotaingImageView(degree, bitmap);
//    upload(bitmap);
//    /**
//     * 旋转图片
//     * @param angle
//     * @param bitmap
//     * @return Bitmap
//     */
//    public static Bitmap rotaingImageView(int angle , Bitmap bitmap) {
//        //旋转图片 动作
//        Matrix matrix = new Matrix();
//        ;
//        matrix.postRotate(angle);
//        System.out.println("angle2=" + angle);
//        // 创建新的图片
//        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
//                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//        return resizedBitmap;
//    }

}
