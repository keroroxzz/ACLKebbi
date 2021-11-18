package com.nuwarobotics.example.motor;

import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.nuwarobotics.example.R;
import com.nuwarobotics.service.IClientId;
import com.nuwarobotics.service.agent.NuwaRobotAPI;
import com.nuwarobotics.service.agent.RobotEventCallback;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

//socket
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;



//camera
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class MovementTest extends AppCompatActivity{

    private InetAddress serverAddr;
    private SocketAddress sc_add;
    private Socket socket;

    private PreviewView preview;
    private ListenableFuture<ProcessCameraProvider> cameraFuture;

    private InetAddress serverAddr_cam;
    private SocketAddress sc_add_cam;
    private DatagramSocket socket_cam;

    // socket thread class for image transmisstion
    private class SocketThreadCam extends AsyncTask<Byte[], Integer, Integer> {
        protected Integer doInBackground(Byte[]... bytearray) {
            try {
                //把Byte[]轉回byte[]，因為socket傳的是byte[]Orz
                int j=0;
                byte[] bytes = new byte[bytearray[0].length];
                for(Byte b: bytearray[0])
                    bytes[j++] = b.byteValue();

                DatagramPacket packet = new DatagramPacket(bytes, bytearray[0].length, serverAddr_cam, 2222);
                socket_cam.send(packet);                             // 傳送
                //socket.close();                                 // 關閉 UDP socket.
            } catch (Exception e) {
                Log.e("Socket", "Client: Error", e);
            } finally {
                //socket.close();
            }
            return 0;
        }
        protected void onProgressUpdate(Integer... progress) { }
        protected void onPostExecute(Integer result) {
            Log.e("Socket", "End...");
        }
    }

    protected void onCreateCam(@Nullable Bundle instanceState)
    {
        preview = findViewById(R.id.preview);

        cameraFuture = ProcessCameraProvider.getInstance(this);
        cameraFuture.addListener(
                new Runnable(){
                    @Override
                    public void run() {
                        try{
                            ProcessCameraProvider cameraProvider = cameraFuture.get();
                            bindImageAnalysis(cameraProvider);
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, ContextCompat.getMainExecutor(this));
        try{
            serverAddr_cam = InetAddress.getByName("192.168.50.197");//建婷.50.79
            sc_add_cam = new InetSocketAddress(serverAddr_cam,2222);
            socket_cam = new DatagramSocket();
        }
        catch (Exception e){Log.e("YO,", "You just fucked up, bro.");}
    }

    //
    private void bindImageAnalysis(@NonNull ProcessCameraProvider provider){
        ImageAnalysis ana=
                new ImageAnalysis.Builder().setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        ana.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                ImageProxy.PlaneProxy planes[] = image.getPlanes();

                ByteBuffer yBuf = planes[0].getBuffer();
                ByteBuffer vuBuf = planes[2].getBuffer();

                int ySize = yBuf.remaining();
                int vuSize = vuBuf.remaining();

                byte nv21[] = new byte[ySize+vuSize];
                yBuf.get(nv21, 0, ySize);
                vuBuf.get(nv21, ySize, vuSize);
                YuvImage img = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                img.compressToJpeg(new Rect(0, 0, img.getWidth(), img.getHeight()), 50, out);

                byte[] mybytearray = out.toByteArray();
                Byte[] byteObjects = new Byte[mybytearray.length];

                //把byte[]包裝成Byte[]，因為SocketThread繼承的AsyncTask不給用byte[]QQ
                int i=0;
                for(byte b: mybytearray)
                    byteObjects[i++] = b;  // Autoboxing.

                //啟動新的執行續，丟入圖片位元陣列~
                new SocketThreadCam().execute(byteObjects);

                image.close();
            }
        });
        Preview prev = new Preview.Builder().build();
        CameraSelector selector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
        prev.setSurfaceProvider(preview.getSurfaceProvider());

        provider.bindToLifecycle((LifecycleOwner) this, selector, ana, prev);
        Log.e("error", "Client:~~~~");//讀取一行字串資料
    }

    // The socket class for controlling commands
    public class ServerThreadCode extends Thread {
        private ServerSocket m_serverSocket;//伺服器端的Socket，接收Client端的連線
        private Socket m_socket;//Server和Client之間的連線通道

        public ServerThreadCode(int port) {
            try {
                m_serverSocket = new ServerSocket(port);//建立伺服器端的Socket，並且設定Port
            } catch (IOException e) {
                System.out.println(e.getMessage());//出現例外時，捕捉並顯示例外訊息
            }
        }

        @Override
        public void run()//覆寫Thread內的run()方法
        {
            try {
                Log.e("error", "等待連線......");
                m_socket = m_serverSocket.accept();//等待伺服器端的連線，若未連線則程式一直停在這裡
                Log.e("error", "連線成功！");

                BufferedReader reader;//在此我使用BufferedReader將資料進行接收和讀取

                //BufferedReader在建構時接受一個Reader物件，在讀取標準輸入串流時，會使用InputStreamReader，它繼承了Reader類別
                reader = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));//接收傳進來的資料，所以是Input

                while (true) {
                    Log.e("error", "Client: " + reader.read());//讀取一行字串資料
                    int idontknow = reader.read();
                    if(Move(idontknow))
                        return;
                    else
                        SetPose(reader.read());
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());//出現例外時，捕捉並顯示例外訊息(連線成功不會出現例外)
            }
        }
    }

    NuwaRobotAPI mRobotAPI;
    IClientId mClientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        initView();

        //Step 1 : Initial Nuwa API Object
        mClientId = new IClientId(this.getPackageName());
        mRobotAPI = new NuwaRobotAPI(this, mClientId);

        //Step 2 : Register to receive Robot Event
        mRobotAPI.registerRobotEventListener(mRobotEventCallback);//listen callback of robot service event
        new ServerThreadCode(1111).start();//建立物件，傳入Port並執行等待接受連線的動作

        //初始化畫面顯示的部分
        onCreateCam(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRobotAPI.release();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean Move(int receive)
    {
        float value = 0;
        try {
            if(receive == 119) //move front
            {
                value = 0.1f;
                if (0 != value) {
                    mRobotAPI.move(value);
                    int second = 1 * 1000;
                    if (0 < second) {
                        findViewById(R.id.button_movement_forward).postDelayed(() -> mRobotAPI.move(0), second);
                        Log.e("Damn", "Bruh");
                    }
                }
                return true;
            }
            else if(receive == 115) //move backward
            {
                value = -0.1f;
                if (0 != value) {
                    mRobotAPI.move(value);
                    int second = 1 * 1000;
                    if (0 < second) {
                        findViewById(R.id.button_movement_backward).postDelayed(() -> mRobotAPI.move(0), second);
                    }
                }
                return true;
            }
            else if(receive == 100) //move right
            {
                value = -20;
                if (0 != value) {
                    mRobotAPI.turn(value);
                    int second = 1 * 1000;
                    if (0 < second) {
                        findViewById(R.id.button_movement_backward).postDelayed(() -> mRobotAPI.turn(0), second);
                    }
                }
                return true;
            }
            else if(receive == 97)
            {
                value = 20;
                if (0 != value) {
                    mRobotAPI.turn(value);
                    int second = 1 * 1000;
                    if (0 < second) {
                        findViewById(R.id.button_movement_backward).postDelayed(() -> mRobotAPI.turn(0), second);
                    }
                }
                return true;
            }
            else if(receive == 't'){
                mRobotAPI.move(0);
                mRobotAPI.turn(0);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean SetPose(int receive)
    {
        float value = 0;
        try {
            if(receive == 119) //move front
            {
                value = 0.1f;
                if (0 != value) {
                    mRobotAPI.move(value);
                    int second = 1 * 1000;
                    if (0 < second) {
                        findViewById(R.id.button_movement_forward).postDelayed(() -> mRobotAPI.move(0), second);
                        Log.e("Damn", "Bruh");
                    }
                }
                return true;
            }
            else if(receive == 115) //move backward
            {
                value = -0.1f;
                if (0 != value) {
                    mRobotAPI.move(value);
                    int second = 1 * 1000;
                    if (0 < second) {
                        findViewById(R.id.button_movement_backward).postDelayed(() -> mRobotAPI.move(0), second);
                    }
                }
                return true;
            }
            else if(receive == 100) //move right
            {
                value = -20;
                if (0 != value) {
                    mRobotAPI.turn(value);
                    int second = 1 * 1000;
                    if (0 < second) {
                        findViewById(R.id.button_movement_backward).postDelayed(() -> mRobotAPI.turn(0), second);
                    }
                }
                return true;
            }
            else if(receive == 97)
            {
                value = 20;
                if (0 != value) {
                    mRobotAPI.turn(value);
                    int second = 1 * 1000;
                    if (0 < second) {
                        findViewById(R.id.button_movement_backward).postDelayed(() -> mRobotAPI.turn(0), second);
                    }
                }
                return true;
            }
            else if(receive == 't'){
                mRobotAPI.move(0);
                mRobotAPI.turn(0);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initView(){
        setContentView(R.layout.activity_movementtest);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(this.getClass().getCanonicalName());
        }

        //init Button action
        findViewById(R.id.button_movement_forward).setOnClickListener(v -> {
            try {
                float value = 1;
                if (0 != value) {
                    mRobotAPI.move(value);
                    int second = 1 * 1000;
                    if (0 < second) {
                        findViewById(R.id.button_movement_forward).postDelayed(() -> mRobotAPI.move(0), second);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        findViewById(R.id.button_movement_backward).setOnClickListener(v -> {
            try {
                float value = -1;
                if (0 != value) {
                    mRobotAPI.move(value);
                    int second = 1 * 1000;
                    if (0 < second) {
                        findViewById(R.id.button_movement_backward).postDelayed(() -> mRobotAPI.move(0), second);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        findViewById(R.id.button_movement_right).setOnClickListener(v -> {
            try {
                float value = 20;
                if (0 != value) {
                    mRobotAPI.turn(value);
                    int second = 1 * 1000;
                    if (0 < second) {
                        findViewById(R.id.button_movement_backward).postDelayed(() -> mRobotAPI.turn(0), second);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        findViewById(R.id.button_movement_left).setOnClickListener(v -> {
            try {
                float value = -20;
                if (0 != value) {
                    mRobotAPI.turn(value);
                    int second = 1 * 1000;
                    if (0 < second) {
                        findViewById(R.id.button_movement_backward).postDelayed(() -> mRobotAPI.turn(0), second);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        findViewById(R.id.button_movement_stop2).setOnClickListener(v -> {
            mRobotAPI.move(0);
            mRobotAPI.turn(0);
        });
    }

    private final RobotEventCallback mRobotEventCallback = new RobotEventCallback() {
        @Override
        public void onWindowSurfaceReady() {
        }

        @Override
        public void onWikiServiceStop() {
        }

        @Override
        public void onWikiServiceStart() {
            //ready to moving
            //initSwitchState(); //
        }

        @Override
        public void onWikiServiceCrash() {

        }

        @Override
        public void onWikiServiceRecovery() {

        }

        @Override
        public void onDropSensorEvent(int value) {
            Toast.makeText(getApplicationContext(), "onDropSensorEvent(" + value + ") received", Toast.LENGTH_SHORT).show();
        }
    };
}
