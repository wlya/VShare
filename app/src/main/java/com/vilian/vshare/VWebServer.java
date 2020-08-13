package com.vilian.vshare;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;

import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.vilian.vshare.Utils.log;
import static com.vilian.vshare.Utils.logsend;

public class VWebServer{

    public final static int PORT = 62222;
    public final static String HTTP_PREFIX = "http://";
    private Context mCtx = null;
    private static Server mServer = null;


    public static final String URL_GETVERSION = "getversion";
    public static final String URL_GETIP = "getip";
    public static final String URL_POSTINFO = "postinfo";
    public static final String URL_GETFILE = "getfile";

    public static final String FILEINFO_KEY = "fileinfo";


    private static Map<String, FileInfo> fileSessions = new HashMap<>();

    public static boolean isSessionExist(String id){
        return fileSessions.containsKey(id);
    }
    public static FileInfo getSession(String id){
        return fileSessions.get(id);
    }
    /*
    主构造函数，也用来启动http服务
    */
    public VWebServer(Context context) {
        mCtx = context;
        //andserver
        mServer = AndServer.webServer(mCtx)
                .port(PORT)
                .timeout(10, TimeUnit.SECONDS)
                .build();
        // startup the server.
        mServer.startup();
        log("Running! Point your browsers");
    }

    public void stop(){
        mServer.shutdown();
    }

    public static void dumpSessions(){
        for (Map.Entry<String, FileInfo> entry : fileSessions.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().fileHash + ", size:");
        }
    }
    /*
    解析的主入口函数，所有请求从这里进，也从这里出
    */
    public static void addFileToShareQueue(Uri uri){
        FileInfo fileinfo = new FileInfo(uri);
        MainActivity.curFileSend = fileinfo;
        fileSessions.put(fileinfo.id, fileinfo);
        logsend("file in queue: " + fileinfo.id);
        dumpSessions();
    }

    public static void remoteFileToShareQueue(Uri uri){
        fileSessions.remove(Utils.md5(uri.getPath()));
    }
}