package com.vilian.vshare;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.vilian.vshare.MainService.FOUND_IP;
import static com.vilian.vshare.Utils.getIPAddress;
import static com.vilian.vshare.Utils.log;
import static com.vilian.vshare.Utils.logsend;
import static com.vilian.vshare.VWebServer.FILEINFO_KEY;

public class MainActivity extends AppCompatActivity {

    public static Context ctx;
    private List<String> mIPs;
    private List<String> mNames;
    private GridView mGridView;
    private GridViewAdapter adapter;
    public static FileInfo curFileSend = null;
    public static FileInfo curFileRecv = null;
    public static final int DOWNLOAD = 900;

    public static final int MSG_DOWN_START = 4001;
    public static final int MSG_DOWN_ING = 4002;
    public static final int MSG_DOWN_SUCCESS = 4003;
    public static final int MSG_DOWN_ERROR = 4004;
    public static final String MSG_DOWN_KEY = "down";

    public static Handler handler;
    public static String CURRENT_IP = "";
    public static String CURRENT_IP_RANGE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAndApplyPermission();

        ctx = getApplicationContext();
//        scanNetwork();
        startMainService(null);
        Button btn = (Button)findViewById(R.id.btnScan);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanNetwork();
            }
        });
        Button btntestdown = (Button)findViewById(R.id.btnTestDown);
        btntestdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileInfo fio = new FileInfo("aaa.zip", "bbb.hash", "https://down.qq.com/qqweb/PCQQ/PCQQ_EXE/PCQQ2020.exe", 1234568);
                VWebClient.downloadFileFromRemote(fio);
            }
        });

        initMsgHandler();
        initDatas();
        initGridView();
        ctx = getApplicationContext();


        Intent intent = getIntent();
        if (intent != null){
            String action = intent.getAction();
            String type = intent.getType();
            if (Intent.ACTION_SEND.equals(action) && type!=null ){

                startMainService(null);
                Uri tUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if(tUri!=null ){
                    Utils.getFilePath(ctx, tUri);
                    VWebServer.addFileToShareQueue(tUri);
                }
                log("file stream received: " + tUri.toString());

            }
        }
    }


    private void initMsgHandler(){
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.arg1){
                    case MSG_DOWN_START:
                    case MSG_DOWN_ING:
                    case MSG_DOWN_ERROR:
                    case MSG_DOWN_SUCCESS:
                        String str = msg.getData().getString(MSG_DOWN_KEY);
                        Toast.makeText(ctx, str, Toast.LENGTH_SHORT).show();
                        break;
                    case DOWNLOAD:
                        log("msg id:" + msg.arg1);
                        FileInfo fio = msg.getData().getParcelable(FILEINFO_KEY);
                        Toast.makeText(ctx, "start download from remote: "+ fio.fileDownloadUrl, Toast.LENGTH_SHORT).show();
                        log("message fileDownloadUrl is:" + fio.fileDownloadUrl);
                        VWebClient.downloadFileFromRemote(fio);
                        break;
                    case FOUND_IP:
                        String ip = msg.getData().getString("ip");
                        logsend("sender found ip:" + ip);
                        addRemoteItem(ip);
                        break;
                    default:
                        log("msg id:" + msg.arg1);
                }

            }
        };
    }
    public static void sendMsgToMainActivity(int msgId, String key ,String value){
        Message msg = Message.obtain();
        msg.arg1 = msgId;
        Bundle bundle = new Bundle();
        bundle.putString(key, value);
        msg.setData(bundle);
        MainActivity.handler.sendMessage(msg);
    }
    private void initGridView(){
        mGridView=(GridView) findViewById(R.id.grid_view);
        //初始化数据
        adapter=new GridViewAdapter(MainActivity.this, mNames,mIPs);
        mGridView.setNumColumns(2);
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                VWebClient.sendFileInfoToRemote(curFileSend, mIPs.get(position));
                Toast.makeText(MainActivity.this, "您点击了:"+position+curFileSend.fileDownloadUrl, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void scanNetwork(){
        CURRENT_IP = getIPAddress(ctx);
        CURRENT_IP_RANGE = CURRENT_IP.substring(0, CURRENT_IP.lastIndexOf(".")+1);
        VWebClient.scanNetwork(CURRENT_IP_RANGE);
    }
    private void initDatas() {
        mIPs = new ArrayList<>();
        mNames = mIPs;
    }
    public void addRemoteItem(String remoteIP){
        mIPs.add(remoteIP);
        mNames = mIPs;
        initGridView();
    }
    public void checkAndApplyPermission(){
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    public void startMainService(View v){
        log("startServicing");
        Intent intent = new Intent(this, MainService.class);
        startService(intent);
    }

    public void stopMainService(View v){
        Intent intent = new Intent(this, MainService.class);
        stopService(intent);
    }
}