package com.example.compact_qpi.video_acticity;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaActionSound;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.compact_qpi.R;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Video_1_VideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Video_1_VideoFragment extends Fragment implements TextureView.SurfaceTextureListener{
    // 여기가 딜레이 없이 스트리밍 하는 법!

    // public constants
    public final static String CAMERA = "camera";
    public final static String FULL_SCREEN = "full_screen";

    // local constants
    private final static float MIN_ZOOM = 1;
    private final static float MAX_ZOOM = 10;

    // instance variables
    private Video_3_Camera updCamera;
    private DecoderThread decoderThread;
    private Video_2_ZoomPanTextureView textureView;
    private TextView nameView, messageView;
    private Runnable finishRunner, startVideoRunner;
    private Handler finishHandler, startVideoHandler;
    private String networkname = "QPImobile";
    private String devicename = "QPI";
    private String ipaddress = "192.168.4.1";
    private int port = 1234;

    String bitmap_string_image = "";
    private Python python_start;


    //******************************************************************************
    // newInstance
    //******************************************************************************

    public static Video_1_VideoFragment newInstance()
    {
        Video_1_VideoFragment fragment = new Video_1_VideoFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // finish handler and runnable 만들기
        finishHandler = new Handler();
        finishRunner = new Runnable()
        {
            @Override
            public void run()
            {
                getActivity().finish();
            }
        };

        // start video handler and runnable 만들기
        startVideoHandler = new Handler();
        startVideoRunner = new Runnable()
        {
            @Override
            public void run()
            {
                MediaFormat format = decoderThread.getMediaFormat();
                int videoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
                int videoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
                textureView.setVideoSize(videoWidth, videoHeight);
            }
        };

        // 파이썬 코드 불러오기
        if(!Python.isStarted())
            Python.start(new AndroidPlatform(getContext()));

        python_start = Python.getInstance();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        // 이어주기
        View view = inflater.inflate(R.layout.fragment_1_video, container, false);


        // IP 주소랑 port 가 여기 들어가네
        // 여기서 Camera Class 씀! ★★★★★★★★★★★★★★★★★★★★★★★★★★★
        updCamera = new Video_3_Camera(networkname, devicename, ipaddress, port);

        // configure the name


        // initialize the message
        // 처음에 나오는 메세지 정하는 부분
        messageView = view.findViewById(R.id.video_message);
        messageView.setTextColor(getResources().getColor(R.color.white));
        messageView.setText(R.string.initializing_video);

        // set the texture listener
        // Streaming 되는 동영상 나오는 부분
        textureView = view.findViewById(R.id.video_surface);
        textureView.setSurfaceTextureListener(this);
        textureView.setZoomRange(MIN_ZOOM, MAX_ZOOM);

        return view;
    }

    //******************************************************************************
    // onStart
    //******************************************************************************
    @Override
    public void onStart()
    {
        super.onStart();

        // create the decoder thread
        // 디코더가 복잡하고 어려움 보자
        decoderThread = new DecoderThread();
        decoderThread.start();

    }

    //******************************************************************************
    // onStop
    //******************************************************************************
    @Override
    public void onStop()
    {
        super.onStop();

        if (decoderThread != null)
        {
            decoderThread.interrupt();
            decoderThread = null;
        }


    }

    //******************************************************************************
    // onDestroy
    //******************************************************************************
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        finishHandler.removeCallbacks(finishRunner);
    }

    //******************************************************************************
    // onSurfaceTextureAvailable
    //******************************************************************************
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height)
    {
        if (decoderThread != null)
        {
            decoderThread.setSurface(new Surface(surfaceTexture), startVideoHandler, startVideoRunner);
        }
    }

    //******************************************************************************
    // onSurfaceTextureSizeChanged
    //******************************************************************************
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height)
    {
    }

    //******************************************************************************
    // onSurfaceTextureDestroyed
    //******************************************************************************
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture)
    {
        if (decoderThread != null)
        {
            decoderThread.setSurface(null, null, null);
        }
        return true;
    }

    //******************************************************************************
    // onSurfaceTextureUpdated
    //******************************************************************************
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {}

    //******************************************************************************
    // takeSnapshot ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
    //******************************************************************************
    public void takeSnapshot(int i)
    {
        // get the snapshot image
        Bitmap image = textureView.getBitmap(1080,720); // 여기를 raspi 카메라 reolution이랑 맞춰주면 좋음

        // 하나는 계산용 하나는 갤러리용
        String name = String.format("%04d.png",i);
        Video_4_SaveImage.DPC_SAVE(getActivity().getContentResolver(), image, name, null); // 여기서 저장하는 java 씀

        SimpleDateFormat date = new SimpleDateFormat("yyMMdd_hh:mm:ss");
        String name2 = "DPC_" + date.format(new Date()) + ".png";
        Video_4_SaveImage.DPC_SAVE(getActivity().getContentResolver(), image, name2, null); // 여기서 저장하는 java 씀

        // 파이썬 돌리는거에 응용해보자
        bitmap_string_image = getStringImage(image); // 비트맵 이미지를 보내기 위해 string으로

        PyObject python_code = python_start.getModule("01_Save_four_image");
        PyObject return_value = python_code.callAttr("main", bitmap_string_image, i); // 이걸 파라미터로 던진다

        // 그냥 잘 되었는지 확인용
        String return_value_string = return_value.toString();
        System.out.println(return_value_string);


    }

    private String getStringImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        // store in byte array
        byte[] imageBytes = baos.toByteArray();
        // finally encode to string
        String encodedImage = android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    //******************************************************************************
    // stop
    //******************************************************************************
    public void stop()
    {
        if (decoderThread != null)
        {
            messageView.setText(R.string.closing_video);
            //messageView.setTextColor(App.getClr(R.color.good_text)); /////////////////////////////////////////////
            messageView.setVisibility(View.VISIBLE);
            decoderThread.interrupt();
            try
            {
                decoderThread.join(Video_5_TcpIpReader.IO_TIMEOUT * 2);
            }
            catch (Exception ex) {}
            decoderThread = null;
        }
    }

    ////////////////////
    // DecoderThread - 여기가 중요! ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
    ////////////////////
    private class DecoderThread extends Thread
    {
        // local constants
        private final static int FINISH_TIMEOUT = 5000;
        private final static int BUFFER_SIZE = 16384;
        private final static int NAL_SIZE_INC = 4096;
        private final static int MAX_READ_ERRORS = 300;

        // instance variables
        private MediaCodec mediadecoder = null;
        private MediaFormat format;
        private boolean decoding = false;
        private Surface surface;
        private byte[] buffer = null;
        private ByteBuffer[] inputBuffers = null;
        private long presentationTime;
        private long presentationTimeInc = 66666;
        private Video_5_TcpIpReader reader = null;
        private Handler startVideoHandler;
        private Runnable startVideoRunner;

        //******************************************************************************
        // setSurface
        //******************************************************************************
        void setSurface(Surface surface, Handler handler, Runnable runner)
        {
            this.surface = surface;
            this.startVideoHandler = handler;
            this.startVideoRunner = runner;
            if (mediadecoder != null)
            {
                if (surface != null)
                {
                    boolean newDecoding = decoding;
                    if (decoding)
                    {
                        setDecodingState(false);
                    }
                    if (format != null)
                    {
                        try
                        {
                            mediadecoder.configure(format, surface, null, 0);
                        }
                        catch (Exception ex) {}
                        if (!newDecoding)
                        {
                            newDecoding = true;
                        }
                    }
                    if (newDecoding)
                    {
                        setDecodingState(newDecoding);
                    }
                }
                else if (decoding)
                {
                    setDecodingState(false);
                }
            }
        }

        //******************************************************************************
        // getMediaFormat
        //******************************************************************************
        MediaFormat getMediaFormat()
        {
            return format;
        }

        //******************************************************************************
        // setDecodingState
        //******************************************************************************
        private synchronized void setDecodingState(boolean newDecoding)
        {
            try
            {
                if (newDecoding != decoding && mediadecoder != null)
                {
                    if (newDecoding)
                    {
                        mediadecoder.start();
                    }
                    else
                    {
                        mediadecoder.stop();
                    }
                    decoding = newDecoding;
                }
            } catch (Exception ex) {}
        }

        //******************************************************************************
        // run
        //******************************************************************************
        @Override
        public void run()
        {
            byte[] nal = new byte[NAL_SIZE_INC];
            int nalLen = 0;
            int numZeroes = 0;
            int numReadErrors = 0;

            try
            {
                // create the decoder
                mediadecoder = MediaCodec.createDecoderByType("video/avc");

                // create the reader
                buffer = new byte[BUFFER_SIZE];
                // 여기서 TcpIpReader Class 쓴다!
                reader = new Video_5_TcpIpReader(updCamera);
                if (!reader.isConnected())
                {
                    throw new Exception();
                }

                // read until we're interrupted
                while (!isInterrupted())
                {
                    // read from the stream
                    int len = reader.read(buffer);
                    if (isInterrupted()) break;

                    // process the input buffer
                    if (len > 0)
                    {
                        // 들어온 정보가 0이 아닐때!
                        numReadErrors = 0;
                        for (int i = 0; i < len && !isInterrupted(); i++)
                        {
                            // add the byte to the NAL
                            if (nalLen == nal.length)
                            {
                                nal = Arrays.copyOf(nal, nal.length + NAL_SIZE_INC);
                            }
                            nal[nalLen++] = buffer[i];

                            // look for a header
                            if (buffer[i] == 0)
                            {
                                numZeroes++;
                            }
                            else
                            {
                                if (buffer[i] == 1 && numZeroes == 3)
                                {
                                    if (nalLen > 4)
                                    {
                                        int nalType = processNal(nal, nalLen - 4);
                                        if (isInterrupted()) break;
                                        if (nalType == -1)
                                        {
                                            nal[0] = nal[1] = nal[2] = 0;
                                            nal[3] = 1;
                                        }
                                    }
                                    nalLen = 4;
                                }
                                numZeroes = 0;
                            }
                        }

                    }
                    else
                    {
                        numReadErrors++;

                        if (numReadErrors >= MAX_READ_ERRORS)
                        {
                            System.out.println("lost_connection");
                            setMessage(R.string.error_lost_connection);
                            break;
                        }
                    }

                    // send an output buffer to the surface
                    if (format != null && decoding)
                    {
                        if (isInterrupted()) break;
                        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                        int index;
                        do
                        {
                            index = mediadecoder.dequeueOutputBuffer(info, 0);
                            if (isInterrupted()) break;
                            if (index >= 0)
                            {
                                mediadecoder.releaseOutputBuffer(index, true);
                            }
                            //Log.info(String.format("dequeueOutputBuffer index = %d", index));
                        } while (index >= 0);
                    }
                }
            }
            catch (Exception ex)
            {
//                Log.error(ex.toString());
                if (reader == null || !reader.isConnected())
                {
                    setMessage(R.string.error_couldnt_connect);
                    // 우선 안꺼지게 하자
//                    finishHandler.postDelayed(finishRunner, FINISH_TIMEOUT);
                }
                else
                {
                    System.out.println("lost_connection");
                    setMessage(R.string.error_lost_connection);
                }
                ex.printStackTrace();
            }

            // close the reader
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (Exception ex) {}
                reader = null;
            }

            // stop the decoder
            if (mediadecoder != null)
            {
                try
                {
                    setDecodingState(false);
                    mediadecoder.release();
                }
                catch (Exception ex) {}
                mediadecoder = null;
            }
        }

        //******************************************************************************
        // processNal
        //******************************************************************************
        private int processNal(byte[] nal, int nalLen)
        {
            // get the NAL type
            int nalType = (nalLen > 4 && nal[0] == 0 && nal[1] == 0 && nal[2] == 0 && nal[3] == 1) ? (nal[4] & 0x1F) : -1;
            //Log.info(String.format("NAL: type = %d, len = %d", nalType, nalLen));

            // process the first SPS record we encounter
            if (nalType == 7 && !decoding)
            {
                // SpsParser Class 여기서 씀! ★★★★★★★★★★★★★★★★★★★★
                // SpsParser 안에 SpsReader Class가 있음
                Video_6_SpsParser parser = new Video_6_SpsParser(nal, nalLen);
                format = MediaFormat.createVideoFormat("video/avc", parser.width, parser.height);
                presentationTimeInc = 66666;
                presentationTime = System.nanoTime() / 1000;
//                Log.info(String.format("SPS: %02X, %d x %d, %d", nal[4], parser.width, parser.height, presentationTimeInc));
                mediadecoder.configure(format, surface, null, 0);
                setDecodingState(true);
                inputBuffers = mediadecoder.getInputBuffers();
                hideMessage();

                startVideoHandler.post(startVideoRunner);
            }

            // queue the frame
            if (nalType > 0 && decoding)
            {
                int index = mediadecoder.dequeueInputBuffer(0);
                if (index >= 0)
                {
                    ByteBuffer inputBuffer = inputBuffers[index];
                    //ByteBuffer inputBuffer = decoder.getInputBuffer(index);
                    inputBuffer.put(nal, 0, nalLen);
                    mediadecoder.queueInputBuffer(index, 0, nalLen, presentationTime, 0);
                    presentationTime += presentationTimeInc;
                }
                //Log.info(String.format("dequeueInputBuffer index = %d", index));
            }


            return nalType;
        }

        //******************************************************************************
        // hideMessage
        //******************************************************************************
        private void hideMessage()
        {
            // Streaming 시작되면 나와있던 메세지 없애기
            getActivity().runOnUiThread(new Runnable()
            {
                public void run()
                {
                    messageView.setVisibility(View.GONE);
                }
            });
        }

        //******************************************************************************
        // setMessage
        //******************************************************************************
        private void setMessage(final int id) {
            getActivity().runOnUiThread(new Runnable()
            {
                public void run()
                {
                    messageView.setText(id);
                    messageView.setTextColor(getResources().getColor(R.color.white));
                    messageView.setVisibility(View.VISIBLE);
                }
            });
        }

    }

}
