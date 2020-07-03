package com.drs24.recorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    // 錄音按鈕
    private Button recordBt;
    // 撥放按鈕
    private Button playBt;
    //base64轉換按鈕
    private Button base64;
    //顯示視窗
    private TextView textView;
    // 音檔站存資料夾
    File RF;
    // 錄音器
    MediaRecorder MR = null;
    // 撥放器
    MediaPlayer MP = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        request();

        // 抓取錄音按鈕
        recordBt = (Button) findViewById(R.id.rButton);
        // 抓取撥放按鈕
        playBt = (Button) findViewById(R.id.Button2);

        base64 = (Button) findViewById(R.id.button64);

        textView = (TextView) findViewById(R.id.textView);

        // 設定一開始不能按的按鈕
        playBt.setEnabled(false);

        // 設定監聽
        recordBt.setOnTouchListener(this);
        playBt.setOnClickListener(clickLT);
        base64.setOnClickListener(clickLT);
    }

    private void request() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission_group.STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.WAKE_LOCK},
                100);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startRecord();
                recordBt.setBackgroundColor(Color.RED);
                break;
            case MotionEvent.ACTION_UP:
                stopRecord();
                recordBt.setBackgroundColor(Color.BLACK);
                break;
        }

        return false;
    }

    private View.OnClickListener clickLT = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
//                case R.id.toggleButton:
//                    startRecord();
//                    break;
                case R.id.Button2:
                    play();
                    break;
                case R.id.button64:
                    try {
//                        toFile(encodeBase64File(RF.getAbsolutePath()), Environment.getExternalStorageDirectory() + "/download");
                        textView.setText(encodeBase64File(RF.getAbsolutePath()));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    private void startRecord() {
        //設定錄音檔名
//            String fileName = "dicom_audio.mp3";
        // 錄音流程
        try {
            // 設定檔案位置，可以在手機上的檔案管理找到剛剛錄下的聲音
            File SDCardpath = Environment.getExternalStorageDirectory();
//                File myDataPath = new File(SDCardpath.getAbsolutePath() + "/download");
//                if (!myDataPath.exists()) myDataPath.mkdirs();
//                RF = new File(SDCardpath.getAbsolutePath() + "/download/" + fileName);

            //建立新資料夾
            File myDataPath = new File(SDCardpath.getAbsolutePath(), "DicomRadio");//在路徑下產生DicomRadio資料夾
            if (!myDataPath.exists()) {
                myDataPath.mkdirs();
            }

//            String fileName = String.valueOf(sb.append(radioPath).append("/").append(imageScanRecord.getTitle()).append("_").append(counter++).append(".mp3"));
//            RF = new File(fileName);
            RF = File.createTempFile("tmp", ".mp3", myDataPath);

            MR = new MediaRecorder();
            MR.setAudioSource(MediaRecorder.AudioSource.MIC);//設定音訊資源的來源包括：麥克風，通話上行，通話下行等；程式中設定音訊來源為麥克風
            MR.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//設定輸出檔案的格式如3gp、mpeg4等
            MR.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);//設定音訊編碼器，程式中設定音訊編碼為AMR窄帶編碼

            MR.setOutputFile(RF.getAbsolutePath());//設定檔案輸出路徑，程式中的PATH_NAME要用實際路徑替換掉
            MR.prepare();//準備開始，這就在start前，必須呼叫
            MR.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecord() {
        // 停止錄音
        if (MR != null) {
            MR.stop();//錄完音要停止
            MR.release();//釋放資源
            MR = null;
            // 開啟不能按的按鈕
            playBt.setEnabled(true);
        }
    }

    private void play() {
        // 播放流程
//        Uri uri = Uri.fromFile(RF.getAbsoluteFile());
        MP = new MediaPlayer();
        try {
            MP.setDataSource(RF.getAbsolutePath());
            MP.prepare();
            MP.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MP.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer MP) {
                MP.release();
            }
        });
    }

    @Override
    protected void onPause() {
        Toast.makeText(this, "Pause", Toast.LENGTH_LONG).show();
        super.onPause();
    }

    /**
     * 將文件轉成base64字符串
     *
     * @param path 文件路徑
     * @return
     * @throws Exception
     */
    public static String encodeBase64File(String path) throws Exception {
        File file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        inputFile.read(buffer);
        inputFile.close();
        return Base64.encodeToString(buffer, Base64.DEFAULT);
    }

    /**
     * 将base64字符解码保存文件
     *
     * @param base64Code
     * @param targetPath
     * @throws Exception
     */
    public static void decoderBase64File(String base64Code, String targetPath) throws Exception {
        byte[] buffer = Base64.decode(base64Code, Base64.DEFAULT);
        FileOutputStream out = new FileOutputStream(targetPath);
        out.write(buffer);
        out.close();
    }

    /**
     * 将base64字符保存文本文件
     *
     * @param base64Code
     * @param targetPath
     * @throws Exception
     */
    public static void toFile(String base64Code, String targetPath) throws Exception {
        byte[] buffer = base64Code.getBytes();
        FileOutputStream out = new FileOutputStream(targetPath);
        out.write(buffer);
        out.close();
    }

}
