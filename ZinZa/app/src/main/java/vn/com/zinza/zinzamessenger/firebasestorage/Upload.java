package vn.com.zinza.zinzamessenger.firebasestorage;

import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

/**
 * Created by ASUS on 3/27/2017.
 */

public class Upload implements Runnable {
    StorageReference storageReference;
    private String nameFile;
    private String nameConver;
    private String nameFolderStorage;
    private String namePart;
    public boolean done = false;
    private String ROOT = Environment.getExternalStorageDirectory() + "/ZinZaMessenger";

    public Upload(StorageReference storageReference, String nameFile, String nameConver, String nameFolderStorage, String namePart) {
        this.storageReference = storageReference;
        this.nameFile = nameFile;
        this.nameConver = nameConver;
        this.nameFolderStorage = nameFolderStorage;
        this.namePart = namePart;
    }

    @Override
    public void run() {
        uploadFile();

    }

    private void uploadFile(){
        File file = new File(ROOT+"/"+namePart);
        Uri uri = Uri.fromFile(file);
        String folder = nameFile.substring(0,nameFile.indexOf("."));
        storageReference.child(nameConver).child(nameFolderStorage).child(folder).child(namePart).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(com.google.firebase.storage.UploadTask.TaskSnapshot taskSnapshot) {
                Log.e("Upload","Success"+namePart);
                done = true;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Upload","Failure"+e.toString());
            }
        });
        while (!done){

        }
        Log.e("Check","Done");
    }
}
