package com.vilian.vshare;

import android.os.Bundle;
import android.os.Message;

import com.yanzhenjie.andserver.annotation.Controller;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.framework.body.StreamBody;
import com.yanzhenjie.andserver.http.HttpResponse;

import static com.vilian.vshare.Utils.log;
import static com.vilian.vshare.Utils.logsend;

@Controller
public class VCWebHandler {

    @GetMapping("/"+VWebServer.URL_GETFILE)
    public void getfile(@RequestParam("id")String id, HttpResponse response) {
        logsend("getfile: id="+id);

        if (VWebServer.isSessionExist(id)){
            logsend("remote want id: "+ id);
            try {

                FileInfo fio1 = VWebServer.getSession(id);
                StreamBody body = new StreamBody(fio1.is);
                response.setBody(body);
                logsend("get file info from sessions: "+fio1.fileDownloadUrl);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            logsend("id not found!!"+ id);
        }
    }


}
