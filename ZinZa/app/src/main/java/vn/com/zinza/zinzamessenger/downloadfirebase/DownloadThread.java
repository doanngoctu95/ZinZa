package vn.com.zinza.zinzamessenger.downloadfirebase;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.okhttp.ResponseBody;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import retrofit.Call;
import retrofit.Retrofit;
import vn.com.zinza.zinzamessenger.service.FirebaseService;
import vn.com.zinza.zinzamessenger.utils.Helper;
import vn.com.zinza.zinzamessenger.utils.Utils;

/**
 * Created by dell on 28/03/2017.
 */

public class DownloadThread implements Runnable {
    public static int count = 0;
    public static long start;
    URL url;
    String path;

    public int done = 0;
    String urlTest;
    private boolean isRunning = true;
    private StorageReference mStorageReference;
    private String uriDown;

    public DownloadThread(URL url, String path, String s, String e) {
        this.url = url;
        this.path = path;

    }

    public DownloadThread(String url, String path) {
        this.urlTest = url;
        this.path = path;
        mStorageReference = FirebaseStorage.getInstance().getReference();



    }




        @Override
        public void run () {
//        System.out.println("Starting : " + path);
//        try {
//            File file = new File(path);
//
//            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setRequestProperty("Range", "Bytes=" + s + "-" + e);
//            urlConnection.connect();
//
//            System.out.println("Respnse Code: " + urlConnection.getResponseCode());
//            System.out.println("Content-Length: " + urlConnection.getContentLength());
//
//            InputStream inputStream = urlConnection.getInputStream();
//            OutputStream out;
//            out = new FileOutputStream(file);
//            int o;
//            while ((o = inputStream.read()) != -1) {
//                out.write(o);
//            }
//            inputStream.close();
//            out.close();
//        } catch (MalformedURLException mue) {
//            mue.printStackTrace();
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        }
//        count++;
//        done++;
//        System.out.println("Part download is done");

            mStorageReference.child(this.urlTest).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.e("Uri", uri.toString());
//                    Utils.URL_PART += uri.toString();
                }
            });
        }

    private void downloadFile(ResponseBody body) throws IOException {

        int count;
        byte data[] = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
//        String typeFile = body.contentType().subtype();
//        File source = new File(Utils.ROOT_FOLDER);

        File outputFile = new File(path);
        OutputStream output = new FileOutputStream(outputFile);
        long total = 0;
        long startTime = System.currentTimeMillis();
        int timeCount = 1;
        while ((count = bis.read(data)) != -1) {

            total += count;
            double current = Math.round(total / (Math.pow(1024, 2)));

            int progress = (int) ((total * 100) / fileSize);

            long currentTime = System.currentTimeMillis() - startTime;


            if (currentTime > 1000 * timeCount) {

                timeCount++;
            }

            output.write(data, 0, count);
        }
        isRunning = false;
        done++;
        output.flush();
        output.close();
        bis.close();
    }
}