package com.vilian.vshare;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import static com.vilian.vshare.Utils.log;

public class FileInfo implements Parcelable {
    public String fileName;
    public String id;
    public String fileHash;
    public Uri uri;
    public String fileDownloadUrl;
    public long fileSize;
    public InputStream is = null;
    public FileInfo(String file_name, String file_hash, String file_downloadurl, long file_size){
        fileName = file_name;
        fileHash = file_hash;
        fileDownloadUrl = file_downloadurl;
        fileSize = file_size;
    }
    public FileInfo(Parcel parcel){
        fileName = parcel.readString();
        fileHash = parcel.readString();
        fileDownloadUrl = parcel.readString();
        fileSize = parcel.readLong();

    }

    public void handleUri(Uri uri){
        String path = uri.getPath();
        String scheme = uri.getScheme();
        String ss = uri.toString();
        String host = uri.getHost();
        String auth = uri.getAuthority();
        String realPath = "";
        log("ss: "+ss);
        log(", path is:"+path+", "+uri.getPathSegments() + ", host: "+host+", auth:"+auth);
        if (ss.startsWith("content://com.huawei.internal.app.fileprovider/share/")){
            realPath = URLDecoder.decode(ss.substring(53));
        }else if(ss.startsWith("content:/storage/")){
            realPath = ss.substring(8);
        }else if(ss.startsWith("")){

        }
        log("real path is: "+realPath);
    }

    public FileInfo(Context context, Uri uri){
        if (uri.getHost().equals(""))
        handleUri(uri);

        String[] ss = Utils.getFilePath(context, uri);
        id = Utils.md5(ss[0]);
        this.uri = uri;
        File fd = new File(ss[0]);
        if (!fd.exists()){
            log("file not found:"+ss[0]);
            is.equals(1);
            return;
        }
        this.fileSize = fd.length();
        this.fileHash = Utils.calculateMD5(fd);
        this.fileName = ss[1];
        try {
            this.is = new FileInputStream(fd);
        }catch (Exception e){
            e.printStackTrace();
        }
        fileDownloadUrl = VWebServer.HTTP_PREFIX+MainActivity.CURRENT_IP+":"+VWebServer.PORT+"/"+VWebServer.URL_GETFILE+"?id="+id;
    }
    public FileInfo(Uri tUri){
        try {
            id = Utils.md5(tUri.getPath());
            uri = tUri;
            String path = uri.getPath();
            is = MainActivity.ctx.getContentResolver().openInputStream(uri);
            fileSize = is.available();
            fileHash = Utils.calculateMD5(is);
            fileName = "2020_"+path.substring(path.length()-9).replace("/","_");
            log("file uri in queue + " + fileName + ", md5 is:"+fileHash);
            is = MainActivity.ctx.getContentResolver().openInputStream(uri);
            fileDownloadUrl = VWebServer.HTTP_PREFIX+MainActivity.CURRENT_IP+":"+VWebServer.PORT+"/"+VWebServer.URL_GETFILE+"?id="+id;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void close(){
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            log("Exception on closing MD5 input stream");
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }



    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(fileName);
        parcel.writeString(fileHash);
        parcel.writeString(fileDownloadUrl);
        parcel.writeLong(fileSize);
    }
    public static final Creator<FileInfo> CREATOR = new Creator<FileInfo>() {

        /**
         * 供外部类反序列化本类数组使用
         */
        @Override
        public FileInfo[] newArray(int size) {
            return new FileInfo[size];
        }

        /**
         * 从Parcel中读取数据
         */
        @Override
        public FileInfo createFromParcel(Parcel source) {
            return new FileInfo(source);
        }
    };
}
