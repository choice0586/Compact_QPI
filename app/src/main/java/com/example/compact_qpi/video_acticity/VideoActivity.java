package com.example.compact_qpi.video_acticity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.compact_qpi.R;
import com.example.compact_qpi.gallery.Gallery;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener{
    // public constants
    public final static String CAMERA = "camera";

    // instance variables
    private FrameLayout frameLayout;
    private Video_1_VideoFragment videoFragment; // 여기서 streaming_socket 쓰자

    // GalleryFragment galleryFragment;
    private final static int REQUEST_WRITE_EXTERNAL_STORAGE = 73;
    private static final int MY_READ_PERMISSION_CODE = 101;

    // 원래 QPI랑 합치자
    // 버튼 송신
    private Socket button_socket = null;
    public static String SERVERIP = "192.168.4.1";
    public static final int SERVERPORT = 4321; // button_socket

    // 버튼용
    private Thread clientThread;  // 버튼 누르면 바로 안하고 thread 쓸꺼다

    private boolean connected = false;

    private boolean img_receive1 = false;
    private boolean img_receive2 = false;
    private boolean img_receive3 = false;
    private boolean img_receive4 = false;
    private boolean img_receive5 = false;

    private ImageView camImageView;
    private Button QPI, Restart, Wide_field,Gallery, Exit;
    private ProgressBar loading;

    private Python python_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2_video);

        getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN); // 전체화면 만드는 법!

        // 처음에 쓰는거 퍼미션 구하는 거! - 이거 안하면 찍은거 저장 안됨
        int check = ContextCompat.checkSelfPermission(VideoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (check != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        int check2 = ContextCompat.checkSelfPermission(VideoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (check2 != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_READ_PERMISSION_CODE);
        }

        // 원래 QPI
        // 버튼부분 시작
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //버튼연결
        QPI = findViewById(R.id.QPI);
        loading = findViewById(R.id.loading);
        Restart = findViewById(R.id.Restart);
        Wide_field = findViewById(R.id.Wide_field);
        Gallery = findViewById(R.id.Gallery);
        Exit = findViewById(R.id.Exit);

        // 마지막에 이미지 송신
        camImageView = findViewById(R.id.Image);

        // 산자르 꺼는 이 밑에! ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
        // get the frame layout, handle system visibility changes
        frameLayout = findViewById(R.id.video);

    }

    @Override
    protected void onStart() { //이거슨 비디오 스트리밍 부분
        super.onStart();

//        // Fragment 표현방법 잘 알아두자 ★★★★★★★★★★★★★★★★
//
//        videoFragment = VideoFragment.newInstance();
//        FragmentTransaction fragTran = getSupportFragmentManager().beginTransaction();
//        fragTran.add(R.id.video, videoFragment); // 비디오에 Fragment를 넣는다!
//        fragTran.commit();
//
//        // 산자르 꺼는 이 위에! ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★


        // 소켓통신 정의하기
        // 와 이걸 밖에 놨어야 됐네!! - Thread에 놓으면 안댐!
        try { // 여기서 이어준다 우선! - 소켓은 try, catch 있어야함
            button_socket = new Socket(SERVERIP, SERVERPORT);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // 버튼들 onClickListener - 길어서 합쳐서 밑으로 뺐음
        QPI.setOnClickListener(this);
        Restart.setOnClickListener(this);
        Wide_field.setOnClickListener(this);
        Gallery.setOnClickListener(this);
        Exit.setOnClickListener(this);

        // 파이썬 코드 불러오기
        if(!Python.isStarted())
            Python.start(new AndroidPlatform(this));

        python_start = Python.getInstance();

        // Fragment 표현방법 잘 알아두자 ★★★★★★★★★★★★★★★★
        // 여기서 streaming_socket 쓰네
        videoFragment = Video_1_VideoFragment.newInstance();
        FragmentTransaction fragTran = getSupportFragmentManager().beginTransaction();
        fragTran.add(R.id.video, videoFragment); // 비디오에 Fragment를 넣는다!
        fragTran.commit();

        System.out.println("ㅇㅇ");

        // 산자르 꺼는 이 위에! ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★

    }




    // QPI 부분 이미지 송신도 할 수 있게 만들어보자 ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
    // 버튼 부분 - 쓰레드에서 데이터 보내게!
    private void QPI() { // 이 이름으로 불러온다!
        if (clientThread != null) { // 예의상 한거 같음
            clientThread.interrupt();
        }
        clientThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (!img_receive1){ // 한 번만 보내게!
                    try {

                        runOnUiThread(new Runnable() { // UI를 바꾸르면 이거 써야한대 - 토스트 메세지 띄우려고
                            @Override
                            public void run() {
                                Toast.makeText(VideoActivity.this, "Preparing for QPI...", Toast.LENGTH_SHORT).show();
                            }
                        });

                        // "1"이라는 신호 보내기!
                        PrintWriter printWriter = new PrintWriter(button_socket.getOutputStream());
                        printWriter.write("1");
                        printWriter.flush();
                        System.out.println("1 보냄");

                        img_receive1 = true;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                while(img_receive1){
                    try {
                        InputStream inputStream = button_socket.getInputStream();
                        inputStream.skip(inputStream.available());

                        int bytes;
                        byte[] buffer = new byte[1024];

                        final String recived_data;
                        bytes = inputStream.read(buffer, 0, 1024);

                        recived_data = new String(buffer, 0, bytes);

//                        System.out.println(recived_data);

                        

                        if(recived_data.equals("1")){  // 주소값이 다른 것은 무조건 .equals() 써야 작동함

                            System.out.println("1 받음");
                            Thread.sleep(1500); // 조금 쉬고

                            videoFragment.takeSnapshot(1);

                            // "2"라는 신호 보내기!
                            PrintWriter printWriter = new PrintWriter(button_socket.getOutputStream());
                            printWriter.write("2");
                            printWriter.flush();

                            img_receive1 = false;
                            img_receive2 = true;

                        }

                        Thread.sleep(10);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                while(img_receive2){
                    try {
                        InputStream inputStream = button_socket.getInputStream();
                        inputStream.skip(inputStream.available());

                        int bytes;
                        byte[] buffer = new byte[1024];

                        final String recived_data;
                        bytes = inputStream.read(buffer, 0, 1024);

                        recived_data = new String(buffer, 0, bytes);
                        inputStream = null;

                        if(recived_data.equals("2")){

                            System.out.println("2 받음");
                            Thread.sleep(1500); // 조금 쉬고

                            videoFragment.takeSnapshot(2);

                            // "3"이라는 신호 보내기!
                            PrintWriter printWriter = new PrintWriter(button_socket.getOutputStream());
                            printWriter.write("3");
                            printWriter.flush();

                            img_receive2 = false;
                            img_receive3 = true;

                        }

                        Thread.sleep(10);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                while (img_receive3){
                    try {
                        InputStream inputStream = button_socket.getInputStream();
                        inputStream.skip(inputStream.available());

                        int bytes;
                        byte[] buffer = new byte[1024];

                        final String recived_data;
                        bytes = inputStream.read(buffer, 0, 1024);

                        recived_data = new String(buffer, 0, bytes);
                        inputStream = null;

                        if(recived_data.equals("3")){

                            System.out.println("3 받음");
                            Thread.sleep(1500); // 조금 쉬고
                            videoFragment.takeSnapshot(3);

                            // "4"라는 신호 보내기!
                            PrintWriter printWriter = new PrintWriter(button_socket.getOutputStream());
                            printWriter.write("4");
                            printWriter.flush();

                            img_receive3 = false;
                            img_receive4 = true;
                            img_receive5 = true;

                        }

                        Thread.sleep(10);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                }
                while (img_receive3){
                    try {
                        InputStream inputStream = button_socket.getInputStream();
                        inputStream.skip(inputStream.available());

                        int bytes;
                        byte[] buffer = new byte[1024];

                        final String recived_data;
                        bytes = inputStream.read(buffer, 0, 1024);

                        recived_data = new String(buffer, 0, bytes);
                        inputStream = null;

                        if(recived_data.equals("3")){

                            System.out.println("3 받음");
                            Thread.sleep(1500); // 조금 쉬고
                            videoFragment.takeSnapshot(3);

                            // "4"라는 신호 보내기!
                            PrintWriter printWriter = new PrintWriter(button_socket.getOutputStream());
                            printWriter.write("4");
                            printWriter.flush();

                            img_receive3 = false;
                            img_receive4 = true;

                        }

                        Thread.sleep(10);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                while (img_receive4){
                    try {
                        InputStream inputStream = button_socket.getInputStream();
                        inputStream.skip(inputStream.available());

                        int bytes;
                        byte[] buffer = new byte[1024];

                        final String recived_data;
                        bytes = inputStream.read(buffer, 0, 1024);

                        recived_data = new String(buffer, 0, bytes);
                        inputStream = null;

                        if(recived_data.equals("4")){

                            System.out.println("4 받음");
                            Thread.sleep(1500); // 조금 쉬고
                            videoFragment.takeSnapshot(4);

                            // "5"라는 신호 보내기!
                            PrintWriter printWriter = new PrintWriter(button_socket.getOutputStream());
                            printWriter.write("5");
                            printWriter.flush();

                            img_receive4 = false;
                            img_receive5 = true;

                        }

                        Thread.sleep(10);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                while (img_receive5){
                    try {
                        InputStream inputStream = button_socket.getInputStream();
                        inputStream.skip(inputStream.available());

                        int bytes;
                        byte[] buffer = new byte[1024];

                        final String recived_data;
                        bytes = inputStream.read(buffer, 0, 1024);

                        recived_data = new String(buffer, 0, bytes);
                        inputStream = null;

                        if(recived_data.equals("5")){

                            System.out.println("5 받음");
                            Thread.sleep(7000); // 조금 쉬고
                            videoFragment.takeSnapshot(5);


                            runOnUiThread(new Runnable() { // UI를 바꾸르면 이거 써야한대 - 토스트 메세지 띄우려고
                                @Override
                                public void run() {
                                    Toast.makeText(VideoActivity.this, "Calculating QPI...", Toast.LENGTH_SHORT).show();
                                    System.out.println("Calculating QPI...");
                                }
                            });

                            img_receive5 = false;

                        }

                        Thread.sleep(10);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }

                }



                while (connected){ // 버튼 눌렀을때 connected로 바꿨었음
                    try {
                        // 이제 02_Calculate_qpi.py 돌리기
                        PyObject python_code = python_start.getModule("02_Calculate_qpi");
                        PyObject return_value = python_code.callAttr("main");

                        // 그냥 잘 되었는지 확인용
                        String return_value_string = return_value.toString();
                        System.out.println(return_value_string);

                        // 파이썬 작업 후 다시 돌아옴

                        // convert it to byte array
                        byte data[] = android.util.Base64.decode(return_value_string, Base64.DEFAULT);
                        // convert to bitmap
                        Bitmap bmp = BitmapFactory.decodeByteArray(data,0,data.length);
                        
                        // 들어왔는데 180도 회전되서 들어옴 - 돌리자
                        Matrix rotate_Matrix = new Matrix();
                        rotate_Matrix.postRotate(180); //-360~360

                        Bitmap rotated_bmp = Bitmap.createBitmap(bmp, 0, 0,
                                bmp.getWidth(), bmp.getHeight(), rotate_Matrix, false);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                
                                camImageView.setImageBitmap(rotated_bmp);

                                SimpleDateFormat date = new SimpleDateFormat("yyMMdd_hh:mm:ss");
                                String name = "QPI_" + date.format(new Date()) + ".png";
                                Video_4_SaveImage.saveImage(getContentResolver(), rotated_bmp, name, null);

                                runOnUiThread(new Runnable() { // UI를 바꾸르면 이거 써야한대 - 토스트 메세지 띄우려고
                                    @Override
                                    public void run() {
                                        Toast.makeText(VideoActivity.this, "QPI success!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                Restart.setVisibility(View.VISIBLE);
                                loading.setVisibility(View.INVISIBLE);
                                QPI.setVisibility(View.INVISIBLE);
                            }

                        });

                        Thread.sleep(10);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    connected = false; // 이거 해줘야 루프 나옴

                }
                
            }
        });
        clientThread.start();
    }

    // 나중에 지워도 될듯!
    // 버튼 부분 - 쓰레드에서 데이터 보내게!
    private void Restart() { // 이 이름으로 불러온다!
        if (clientThread != null) { // 예의상 한거 같음
            clientThread.interrupt();
        }
        clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (connected) { // 연결되면! 계속 돌아가겠네

                    PrintWriter printWriter = null;
                    try {
                        printWriter = new PrintWriter(button_socket.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    printWriter.write("Restart");
                    printWriter.flush();


                    runOnUiThread(new Runnable() { // UI를 바꾸려면 무조건 이거 있어야함! 그냥 해서 에러 났었음
                        @Override
                        public void run() {
                            camImageView.setImageBitmap(null);
                            //camImageView.setImageResource(android.R.color.transparent);
                            System.out.println("이미지 Reset");
                            connected = false;

                        }
                    });

                }

            }
        });
        clientThread.start();
    }

    // 버튼 부분 - 쓰레드에서 데이터 보내게!
    private void Gallery() { // 이 이름으로 불러온다!
        if (clientThread != null) { // 예의상 한거 같음
            clientThread.interrupt();
        }
        clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (connected) { // 연결되면! 계속 돌아가겠네

                    PrintWriter printWriter = null;
                    try {
                        printWriter = new PrintWriter(button_socket.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    printWriter.write("Gallery");
                    printWriter.flush();


                    connected = false;
                }
            }
        });
        clientThread.start();
    }

    // 버튼 부분 - 쓰레드에서 데이터 보내게!
    private void Wide_field() { // 이 이름으로 불러온다!
        if (clientThread != null) { // 예의상 한거 같음
            clientThread.interrupt();
        }
        clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (connected) { // 연결되면! 계속 돌아가겠네

                    PrintWriter printWriter = null;
                    try {
                        printWriter = new PrintWriter(button_socket.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    printWriter.write("Wide_field");
                    printWriter.flush();

                    connected = false;
                }
            }
        });
        clientThread.start();
    }



    // 버튼 부분 - 쓰레드에서 데이터 보내게!
    private void Exit() { // 이 이름으로 불러온다!
        if (clientThread != null) { // 예의상 한거 같음
            clientThread.interrupt();
        }
        clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (connected) { // 연결되면! 계속 돌아가겠네

                    PrintWriter printWriter = null;
                    try {
                        printWriter = new PrintWriter(button_socket.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    printWriter.write("Exit");
                    printWriter.flush();

                    connected = false;
                }
            }
        });
        clientThread.start();
    }


    @Override
    public void onClick(View v) {
        // 버튼들이 엄청 많을 때는 이 방식 쓰자 - implements View.OnClickListener 해주고 이거 만들어주면 댐
        switch (v.getId()){

            case R.id.QPI:
                if (!connected) {
                    connected = true;
                    QPI.setVisibility(View.INVISIBLE);  // 이렇게 하는게 로딩창 만드는 거 핵심! - 레이아웃에서는 Framelayout으로 곂치게
                    loading.setVisibility(View.VISIBLE);
                    Restart.setVisibility(View.INVISIBLE);
                    QPI();

                }
                break;
            case R.id.Restart:
                if (!connected) {
                    Restart.setVisibility(View.INVISIBLE);
                    loading.setVisibility(View.INVISIBLE);
                    QPI.setVisibility(View.VISIBLE);
                    connected = true;
                    Restart();
                }
                break;

            case R.id.Wide_field:

                if (!connected) {
                    connected = true;
                    Wide_field();

                }

                break;

            case R.id.Gallery:

                if (!connected) {
                    connected = true;
                    Gallery();

                    try {

                        videoFragment.stop();
                        connected = false;

                        if(button_socket != null){
                            button_socket.close();
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent,200); // GET_GALLERY_IMAGE = 200 - 200개 가져온다는 건가



//                    finish();
//                    System.out.println("finish 작동");

//                    galleryFragment = GalleryFragment.newInstance();
//                    FragmentTransaction fragTran = getSupportFragmentManager().beginTransaction();
//                    fragTran.add(R.id.gallery, galleryFragment); // 비디오에 Fragment를 넣는다!
//                    fragTran.commit();


                    // Gallery로 넘어가자
//                    Intent intent = new Intent(getApplicationContext(), com.example.compact_qpi.gallery.Gallery.class);
//                    startActivity(intent);
                }

                break;

            case R.id.Exit:
                if (!connected) {
                    connected = true;
                    Exit();

                    // 어플 완전히 끄게 하는 법
                    moveTaskToBack(true);						// 태스크를 백그라운드로 이동
                    finishAndRemoveTask();                              // 액티비티 종료 and task 리스트에서 지우기
                    android.os.Process.killProcess(android.os.Process.myPid());  // 앱 프로세스 종료
                }
                break;

        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        try {

            videoFragment.stop();
            connected = false;

            if(button_socket != null){
                button_socket.close();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {

            videoFragment.stop();
            connected = false;

            if(button_socket != null){
                button_socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //******************************************************************************
    // onBackPressed
    //******************************************************************************
    @Override
    public void onBackPressed()
    {
        try {

            videoFragment.stop();
            connected = false;

            if(button_socket != null){
                button_socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onBackPressed();
    }
}


