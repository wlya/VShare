package com.vilian.vshare;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;

import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;

import static com.vilian.vshare.Utils.logrecv;

@RestController
public class VAWebHandler {

    public static final String URL_GETVERSION = "getversion";
    public static final String URL_GETIP = "getip";
    public static final String URL_POSTINFO = "postinfo";
    public static final String URL_GETFILE = "getfile";

    @GetMapping("/"+URL_GETVERSION)
    String getversion() {
        logrecv("new request, version: ");
        return ">>>>version ok";
    }

    @GetMapping("/"+URL_GETIP)
    String getip() {
        logrecv("new request, getip: ");
        return MainActivity.CURRENT_IP;
    }

    @PostMapping("/"+URL_POSTINFO)
    void postinfo(@RequestParam("file_name") String file_name, @RequestParam("file_url") String file_url,
                    @RequestParam("file_size") int file_size, @RequestParam("file_hash") String file_hash){
        //receiver exec
        //String file_name, String file_hash, String file_downloadurl, long file_size
        logrecv("postinfo: "+file_name + ", "+file_hash+", "+ file_url+", "+ file_size);
        FileInfo fio = new FileInfo(file_name, file_hash, file_url, file_size);
        Message message = Message.obtain();
        message.arg1 = MainService.DOWNLOAD_FILE_FROM_REMOTE;
        Bundle bundle = new Bundle();
        bundle.putParcelable(VWebServer.FILEINFO_KEY, fio);
        message.setData(bundle);
        MainService.handler.sendMessage(message);

    }
}
