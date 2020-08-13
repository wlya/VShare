package com.vilian.vshare;

import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.text.format.Time;

import com.vilian.vshare.okhttputils.CallBackUtil;
import com.vilian.vshare.okhttputils.OkhttpUtil;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import sunbufu.okhttputil.OkHttpUtil;
import sunbufu.okhttputil.callback.FileCallback;
import sunbufu.okhttputil.callback.StringCallback;

import static com.vilian.vshare.MainActivity.sendMsgToMainActivity;
import static com.vilian.vshare.Utils.log;
import static com.vilian.vshare.Utils.logrecv;
import static com.vilian.vshare.Utils.logsend;
import static com.vilian.vshare.VWebServer.HTTP_PREFIX;

public class VWebClient {

    public String genFileInfo(){
        return "";
    }

    public static Map<String, String> remoteIPs = new HashMap<>();
    public static void scanNetwork(String ipRange){
        log("ip range is:"+ ipRange);
        for (int i = 1; i< 254; i++){
            String targetUrl = HTTP_PREFIX + ipRange +i+":"+VWebServer.PORT+"/"+VWebServer.URL_GETIP;
            log(targetUrl);
            OkHttpUtil.get(targetUrl)
                    .param("key", "value")
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(String result) {
                            log("try parse ip: "+result);
                            remoteIPs.put(result,result);
                            Message message = Message.obtain();
                            Bundle bundle = new Bundle();
                            bundle.putString("ip", result);
                            message.arg1 = MainService.FOUND_IP;
                            message.setData(bundle);
                            MainActivity.handler.sendMessage(message);
                        }

                    });

        }
    }
    public static void sendFileInfoToRemote(FileInfo fileInfo, String remoteIP){

        String remoteUrl = HTTP_PREFIX+remoteIP+":"+VWebServer.PORT+"/"+VWebServer.URL_POSTINFO;
        logsend("sendFileInfoToRemote: "+remoteUrl);
        OkHttpUtil.post(remoteUrl)
                .param("file_name",fileInfo.fileName)
                .param("file_size", ""+fileInfo.fileSize)
                .param("file_hash", fileInfo.fileHash)
                .param("file_url", fileInfo.fileDownloadUrl)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String result) {
                        logsend("send file to remote done, response is: "+result);
                    }

                });

    }

    public static void downloadFileFromRemote(FileInfo fileInfo){
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        logrecv("go download fle from remote:" + fileInfo.fileDownloadUrl + ", save to:"+Environment.getExternalStorageDirectory().getAbsolutePath()+"/vshare/"+ sdf.format(d.getTime())+fileInfo.fileName);
        OkhttpUtil.okHttpDownloadFile(fileInfo.fileDownloadUrl, new CallBackUtil.CallBackFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/vshare/",sdf.format(d.getTime())+fileInfo.fileName) {
            @Override
            public void onFailure(Call call, Exception e) {
                logrecv("download file error");
                e.printStackTrace();
            }

            @Override
            public void onProgress(float progress, long total) {
                logrecv("current progress :"+progress + ", total :"+total);
            }

            @Override
            public void onResponse(File response) {
                if (response == null){
                    sendMsgToMainActivity(MainActivity.MSG_DOWN_ERROR, MainActivity.MSG_DOWN_KEY, "下载失败！！！！");
                    log("下载失败");
                }else{
                    logrecv("abs path is" + response.getAbsolutePath());
                    sendMsgToMainActivity(MainActivity.MSG_DOWN_ERROR, MainActivity.MSG_DOWN_KEY, "下载成功成功成功成功成功成功成功");
                }
            }
        });
    }
}
