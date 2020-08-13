package com.vilian.vshare;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.Nullable;

import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


import static com.vilian.vshare.Utils.log;
import static com.vilian.vshare.VWebServer.FILEINFO_KEY;

public class MainService extends Service {
    private VWebServer mHttpServer = null;
    public static Context ctx;
    public static Handler handler;
    public static final int SCAN = 10;
    public static final int FOUND_IP = 20;
    public static final int POSTINFO = 30;
    public static final int DOWNLOAD_FILE_FROM_REMOTE = 900;

    @Override
    public void onCreate() {
        ctx = getApplicationContext();
        mHttpServer = new VWebServer(ctx);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                log("msg id:" + msg.arg1);
                switch (msg.arg1){
                    case SCAN:
                        log("mannual scan network");

                        break;
                    case POSTINFO:
                        log("sendFileInfoToRemote done ");
                        log("message is:"+ msg.getData().getString("ip"));
                        break;
                    case DOWNLOAD_FILE_FROM_REMOTE:
                        log("ready to download file from remote");
                        FileInfo fio = msg.getData().getParcelable(FILEINFO_KEY);
                        log("message is:"+ fio.fileDownloadUrl);
                        VWebClient.downloadFileFromRemote(fio);
                        break;
                    default:
                        log("msg id:" + msg.arg1);
                }

            }
        };

    }


    @Override
    public boolean onUnbind(Intent intent) {
        this.onDestroy();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        //在这里关闭HTTP Server
        if (mHttpServer != null){
            mHttpServer.stop();
            mHttpServer = null;
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}