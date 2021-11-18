package com.jetec.cameraexample;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PatternMatcher;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.Rotate;
import com.bumptech.glide.request.RequestOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

//!!!!!!------Import Socket libs------!!!!!!
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //內部subclass，專門在其他執行續處理socket的傳輸，這樣就不會盯著螢幕矇逼幾小時ㄌ
    private class SocketThread extends AsyncTask<Byte[], Integer, Integer> {

        protected Integer doInBackground(Byte[]... bytearray) {
            try {
                //設定目標IP位置
                InetAddress serverAddr = InetAddress.getByName("192.168.43.232");//建婷.0.129
                SocketAddress sc_add= new InetSocketAddress(serverAddr,1111);

                Log.e("Socket", "Client: Connecting...");
                try {
                    Log.e("Socket", "Initialize a new socket instance.");

                    //初始化socket，然後連線~~
                    socket = new Socket();
                    Log.e("Socket", "Connecting...");
                    socket.connect(sc_add,2000);
                    Log.e("Socket", "Get output stream...");
                    OutputStream outputstream = socket.getOutputStream();

                    Log.e("Socket", "Unpack Byte[] back to byte[].");
                    //把Byte[]轉回byte[]，因為socket傳的是byte[]Orz
                    int j=0;
                    byte[] bytes = new byte[bytearray[0].length];
                    for(Byte b: bytearray[0])
                        bytes[j++] = b.byteValue();

                    Log.e("Socket", "Sending a byte array with length:"+bytearray[0].length);
                    //送出訊息然後來去睏
                    outputstream.write(bytes, 0, bytearray[0].length);
                    outputstream.flush();
                } catch (Exception e) {
                    Log.e("Socket", "Client: Error", e);
                } finally {
                    socket.close();
                }
            } catch (Exception e) {
            }
            return 0;
        }
        protected void onProgressUpdate(Integer... progress) { }
        protected void onPostExecute(Integer result) {
            Log.e("Socket", "End...");
        }
    }

    public static final String TAG = MainActivity.class.getSimpleName()+"My";

    private String mPath = "";//設置高畫質的照片位址
    public static final int CAMERA_PERMISSION = 100;//檢測相機權限用
    public static final int REQUEST_HIGH_IMAGE = 101;//檢測高畫質相機回傳
    public static final int REQUEST_LOW_IMAGE = 102;//檢測低畫質相機回傳

    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btHigh = findViewById(R.id.buttonHigh);
        Button btLow = findViewById(R.id.buttonLow);
        /**取得相機權限*/
        if (checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION);
        /**按下低畫質照相之拍攝按鈕*/
        btLow.setOnClickListener(v -> {
            Intent lowIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //檢查是否已取得權限
            if (lowIntent.resolveActivity(getPackageManager()) == null) return;
            startActivityForResult(lowIntent,REQUEST_LOW_IMAGE);
        });
        /**按下高畫質照相之拍攝按鈕*/
        btHigh.setOnClickListener(v->{
            Intent highIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //檢查是否已取得權限
            if (highIntent.resolveActivity(getPackageManager()) == null) return;
            //取得相片檔案的URI位址及設定檔案名稱
            File imageFile = getImageFile();
            if (imageFile == null) return;
            //取得相片檔案的URI位址
            Uri imageUri = FileProvider.getUriForFile(
                    this,
                    "com.jetec.cameraexample.CameraEx",//記得要跟AndroidManifest.xml中的authorities 一致
                    imageFile
            );
            highIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
            startActivityForResult(highIntent,REQUEST_HIGH_IMAGE);//開啟相機
        });
    }
    /**取得相片檔案的URI位址及設定檔案名稱*/
    private File getImageFile()  {
        String time = new SimpleDateFormat("yyMMdd").format(new Date());
        String fileName = time+"_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imageFile = File.createTempFile(fileName,".jpg",dir);
            mPath = imageFile.getAbsolutePath();
            return imageFile;
        } catch (IOException e) {
            return null;
        }
    }
    /**取得照片回傳*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**可在此檢視回傳為哪個相片，requestCode為上述自定義，resultCode為-1就是有拍照，0則是使用者沒拍照*/
        Log.d(TAG, "onActivityResult: requestCode: "+requestCode+", resultCode "+resultCode);
        /**如果是高畫質的相片回傳*/
        if (requestCode == REQUEST_HIGH_IMAGE && resultCode == -1){
            ImageView imageHigh = findViewById(R.id.imageViewHigh);
            new Thread(()->{
                AtomicReference<Bitmap> getHighImage = new AtomicReference<>(BitmapFactory.decodeFile(mPath));
                Matrix matrix = new Matrix();
                matrix.setRotate(90f);//轉90度
                getHighImage.set(Bitmap.createBitmap(getHighImage.get()
                        ,0,0
                        ,getHighImage.get().getWidth()
                        ,getHighImage.get().getHeight()
                        ,matrix,true));
                runOnUiThread(()->{
                    Glide.with(this)
                            .load(getHighImage.get())
                            .centerCrop()
                            .into(imageHigh);
                });
            }).start();
        }/***/
        else if (requestCode == REQUEST_LOW_IMAGE && resultCode == -1){
            ImageView imageLow = findViewById(R.id.imageViewLow);
            Bundle getImage = data.getExtras();
            Bitmap getLowImage = (Bitmap) getImage.get("data");
            Glide.with(this)
                    .load(getLowImage)
                    .centerCrop()
                    .into(imageLow);

            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            //把圖片壓縮成JPG，然後輸出到OutputStream~~
            ByteArrayOutputStream str = new ByteArrayOutputStream();
            getLowImage.compress(Bitmap.CompressFormat.JPEG, 80, str);

            byte[] mybytearray = str.toByteArray();
            Byte[] byteObjects = new Byte[mybytearray.length];

            Log.e("Socket", "Get byte array with length:"+mybytearray.length);
            Log.e("Socket", "Pack array into Byte[]");

            //把byte[]包裝成Byte[]，因為SocketThread繼承的AsyncTask不給用byte[]QQ
            int i=0;
            for(byte b: mybytearray)
                byteObjects[i++] = b;  // Autoboxing.

            Log.e("Socket", "Get Byte array with length:"+byteObjects.length);

            //啟動新的執行續，丟入圖片位元陣列~
            new SocketThread().execute(byteObjects);

        }/***/
        else{
            Toast.makeText(this, "未作任何拍攝", Toast.LENGTH_SHORT).show();
        }
    }
}