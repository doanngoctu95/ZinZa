package vn.com.zinza.zinzamessenger.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import vn.com.zinza.zinzamessenger.R;
import vn.com.zinza.zinzamessenger.adapter.AdapterMessageChat;
import vn.com.zinza.zinzamessenger.firebasestorage.Upload;
import vn.com.zinza.zinzamessenger.model.Message;
import vn.com.zinza.zinzamessenger.model.ResultData;
import vn.com.zinza.zinzamessenger.model.User;
import vn.com.zinza.zinzamessenger.utils.Helper;
import vn.com.zinza.zinzamessenger.utils.RealPathUtils;
import vn.com.zinza.zinzamessenger.utils.Utils;

public class ChattingActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton mBtnBack;
    private ImageView mImgAvatar;
    private TextView mTxtName;
    private Button mBtnOpenCamera;
    private Button mBtnOpenGallery;
    private Button mBtnOpenAttach;
    private Button mBtnText;
    private ImageView mBtnOption;

    private String mIdRecipient;
    private String mIdSender;
    private String keyConversation;

    private AdapterMessageChat mAdapterMessageChat;
    private RecyclerView mListview;
    private List<Message> mMessageList;

    private Button mBtnSendMessage;
    private EmojiconEditText mEdtMessage;
    private EmojIconActions mEmojIcon;
    private ImageView mBtEmoji;
    private View contentRoot;

    private FirebaseDatabase mMsDatabase;
    private DatabaseReference mMsRef;
    private ChildEventListener messageChatListener;

    private ProgressDialog mProgressDialog;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;


    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    public static final String MESSAGE_PROGRESS = "message_progress";
    public static final int RESULT_OPEN_ATTACH = 3;
    public static final int REQUEST_STORAGE = 0x3;

    String urlPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);
        initControl();
        askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_STORAGE);
        Helper.createDirectory();
        setFirebaseInstance();
        setFirebaseStorage();
        implementLisenter();

        getExtra();
        loadData();
        setListview();

    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(ChattingActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(ChattingActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(ChattingActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(ChattingActivity.this, new String[]{permission}, requestCode);
            }
        } else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                //Location
                case REQUEST_STORAGE:
                    askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, requestCode);

                    break;
                //Call

            }

            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void implementLisenter() {
        mBtnBack.setOnClickListener(this);
        mBtnSendMessage.setOnClickListener(this);
        mBtnOpenCamera.setOnClickListener(this);
        mBtnOpenGallery.setOnClickListener(this);
        mBtnOpenAttach.setOnClickListener(this);
        mBtnOption.setOnClickListener(this);
        mBtnText.setOnClickListener(this);
    }

    private void bindButterKnife() {
        ButterKnife.bind(this);
    }

    private void initControl() {
        mMessageList = new ArrayList<>();
        contentRoot = findViewById(R.id.activity_chatting);
        mBtnBack = (ImageButton) findViewById(R.id.btnBackChatting);
        mBtnOption = (ImageView) findViewById(R.id.optionChat);

        mImgAvatar = (ImageView) findViewById(R.id.imgAvatarFriend);
        mTxtName = (TextView) findViewById(R.id.txtnameFriendChatting);

        //Extra action
        mBtnOpenCamera = (Button) findViewById(R.id.btnOpenCamera);
        mBtnOpenGallery = (Button) findViewById(R.id.btnOpenGallery);
        mBtnSendMessage = (Button) findViewById(R.id.btnSendMessage);
        mBtnOpenAttach = (Button) findViewById(R.id.btnOpenAttachment);
        mBtnText = (Button) findViewById(R.id.btnTypeText);

        mListview = (RecyclerView) findViewById(R.id.list_content_message);
        mBtEmoji = (ImageView) findViewById(R.id.btnEmotion);
        mEdtMessage = (EmojiconEditText) findViewById(R.id.edtMessageInput);
        mEmojIcon = new EmojIconActions(this, contentRoot, mEdtMessage, mBtEmoji);

        mEmojIcon.ShowEmojIcon();
        mBtEmoji.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnTypeText:
                mEdtMessage.requestFocus();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mEdtMessage, InputMethodManager.SHOW_IMPLICIT);
                break;
            case R.id.btnEmotion:
                mEmojIcon.ShowEmojIcon();
                break;
            case R.id.btnBackChatting:
                finish();
                break;
            case R.id.btnSendMessage:
                String message = mEdtMessage.getText().toString();
                if (message.equals("")) {

                } else {
                    sendMessage(message);
                    mEdtMessage.setText("");
                }
                break;
            case R.id.btnOpenCamera:
                openCamera();
                break;
            case R.id.btnOpenGallery:
                openGallery();
                break;
            case R.id.btnOpenAttachment:
                openFileAttach();
                break;
            case R.id.optionChat:
                showPopupOption(v);
                break;
            default:
                break;
        }

    }

    private void showPopupOption(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.popup_option_chat,
                popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.changeColor:
//                        Utils.COLOR=true;
                        Toast.makeText(getApplicationContext(), "Dang xay dung", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.option2:
                        Toast.makeText(getApplicationContext(), "dang xay dung", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }


    private void openFileAttach() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, RESULT_OPEN_ATTACH);
    }

    private void setListview() {
        mListview.setLayoutManager(new LinearLayoutManager(this));
        mListview.setHasFixedSize(true);
//        ((SimpleItemAnimator) mListview.getItemAnimator()).setSupportsChangeAnimations(false);
        mAdapterMessageChat = new AdapterMessageChat(this, mMessageList);
        mListview.setAdapter(mAdapterMessageChat);
    }

    private void sendMessage(String message) {
        String mId = mMsRef.push().getKey();
        Message mMessage = new Message(mId, Utils.USER_ID, mIdRecipient, Utils.TEXT, message, Utils.createAt());
        mAdapterMessageChat.addMessage(mMessage);
        mMsRef.child(keyConversation).child(mId).setValue(mMessage);
    }

    private void sendMessageAttach(Uri uriContent, String type) {
        String nameOfFile = Utils.NAME_FILE;
        String mId = mMsRef.push().getKey();
        Message mMessage = new Message(mId, Utils.USER_ID, mIdRecipient, type, uriContent.toString() + "---" + nameOfFile, Utils.createAt());
        mAdapterMessageChat.addMessage(mMessage);
        mMsRef.child(keyConversation).child(mId).setValue(mMessage);
    }

    private void getExtra() {
        Bundle bd = getIntent().getExtras();
        if (bd != null) {
            Intent t = getIntent();
            User user = (User) t.getSerializableExtra(Utils.FR_USER);
            if (!user.getmAvatar().equals("")) {
                Glide.with(this).load(user.getmAvatar()).into(mImgAvatar);
            }
            mTxtName.setText(user.getmUsername());
            mIdRecipient = t.getStringExtra(Utils.RECIPIENT_ID);
            mIdSender = t.getStringExtra(Utils.SENDER_ID);

        }
    }

    private void getMessage() {
        mAdapterMessageChat.cleanUp();
        mMsRef.child(keyConversation).limitToLast(100).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Load message here
                if (dataSnapshot.exists()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    //SENDER
                    if (message.getmIdSender().equals(Utils.USER_ID) && message.getmType().equals(Utils.TEXT)) {
                        message.setRecipientOrSenderStatus(AdapterMessageChat.SENDER_TEXT);
                    } else if (message.getmIdSender().equals(Utils.USER_ID) && message.getmType().equals(Utils.IMAGE)) {
                        message.setRecipientOrSenderStatus(AdapterMessageChat.SENDER_IMAGE);
                    } else if (message.getmIdSender().equals(Utils.USER_ID) && message.getmType().equals(Utils.FILE)) {
                        message.setRecipientOrSenderStatus(AdapterMessageChat.SENDER_FILE);
                    } else if (message.getmIdSender().equals(Utils.USER_ID) && message.getmType().equals(Utils.VIDEO)) {
                        message.setRecipientOrSenderStatus(AdapterMessageChat.SENDER_VIDEO);
                    }
                    // RECIPIENT
                    else if (!message.getmIdSender().equals(Utils.USER_ID) && message.getmType().equals(Utils.TEXT)) {
                        message.setRecipientOrSenderStatus(AdapterMessageChat.RECIPENT_TEXT);
                    } else if (!message.getmIdSender().equals(Utils.USER_ID) && message.getmType().equals(Utils.IMAGE)) {
                        message.setRecipientOrSenderStatus(AdapterMessageChat.RECIPENT_IMAGE);
                    } else if (!message.getmIdSender().equals(Utils.USER_ID) && message.getmType().equals(Utils.FILE)) {
                        message.setRecipientOrSenderStatus(AdapterMessageChat.RECIPENT_FILE);
                    } else if (!message.getmIdSender().equals(Utils.USER_ID) && message.getmType().equals(Utils.VIDEO)) {
                        message.setRecipientOrSenderStatus(AdapterMessageChat.RECIPENT_VIDEO);
                    }

                    mAdapterMessageChat.addMessage(message);
                    mListview.scrollToPosition(mAdapterMessageChat.getItemCount() - 1);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void loadData() {

        final String kcv1 = mIdSender + "-" + mIdRecipient;
        final String kcv2 = mIdRecipient + "-" + mIdSender;

        mMsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(kcv1).exists()) {
                    keyConversation = kcv1;
                } else {
                    keyConversation = kcv2;
                }
                getMessage();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
                    showAlert();
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA);
                }
            }
        } else {
            Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intentCamera, REQUEST_CAMERA);
        }

    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        mProgressDialog = new ProgressDialog(this);
//        mProgressDialog.setTitle("Send");
//        mProgressDialog.show();
//        mProgressDialog.setCancelable(false);
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            uploadData("Send images", data, "images", Utils.IMAGE);
        } else if ((requestCode == REQUEST_GALLERY && resultCode == RESULT_OK)) {
            uploadData("Send images", data, "images", Utils.IMAGE);
        } else if (requestCode == RESULT_OPEN_ATTACH && resultCode == RESULT_OK) {
            if (checkDataVideo(getNameData(data.getData()))) {
//                    uploadFileMutlti("Sending....", data, "files", Utils.VIDEO, this.keyConversation);
                ResultData mData = new ResultData(data,keyConversation,Utils.VIDEO);
                new ProcessTask().execute(mData);
            } else {
                ResultData mData = new ResultData(data,keyConversation,Utils.FILE);
                new ProcessTask().execute(mData);
            }
        }
    }

    private boolean checkDataVideo(String nameData) {
        if (nameData.contains("mp4") || nameData.contains("MP4") || nameData.contains("3gp") || nameData.contains("3GP") ||
                nameData.contains(".avi") || nameData.contains(".wmv") || nameData.contains(".flv")) {
            return true;
        }
        return false;
    }

    private void uploadData(final String title, Intent data, String folder, final String type) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(title);
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        final Uri uri = data.getData();
        Utils.NAME_FILE = getNameData(uri);
        StorageReference filePath = mStorageReference.child(keyConversation).child(folder).child(Utils.NAME_FILE);
        Log.e("File path:", filePath + "--- " + uri.getLastPathSegment() + "");
        filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mProgressDialog.dismiss();
                Uri url = taskSnapshot.getDownloadUrl();
                sendMessageAttach(url, type);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mProgressDialog.dismiss();
                Utils.showToast(e.toString(), getApplicationContext());
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                //displaying percentage in progress dialog
                mProgressDialog.setMessage(title + " " + ((int) progress) + "%...");
            }
        });
    }

    private void uploadFileMutlti(String title, Intent data, String folder, final String type, String keyConversation) {

        final Uri uri = data.getData();
        Utils.NAME_FILE = getNameData(uri);
        String realPath = RealPathUtils.getPathFromURI(this, uri);
        Log.e("Real Path", realPath);
        String typeOfFile = Helper.getTypeFromUri(ChattingActivity.this, uri);
        try {

            if (Helper.splitFile(realPath, Utils.ROOT_FOLDER + "/", 5, typeOfFile)) {
                startUploadThread(typeOfFile, keyConversation, folder, Utils.NAME_FILE, type);
                Log.e("Cut File", "Success");
                mProgressDialog.dismiss();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void startUploadThread(final String typeOfFile, String keyConversation, String folderStorage, String fileName, String type) {

        File f1 = new File(Utils.ROOT_FOLDER + "/1" + typeOfFile);
        File f2 = new File(Utils.ROOT_FOLDER + "/2." + typeOfFile);
        File f3 = new File(Utils.ROOT_FOLDER + "/3." + typeOfFile);
        File f4 = new File(Utils.ROOT_FOLDER + "/4." + typeOfFile);
        File f5 = new File(Utils.ROOT_FOLDER + "/5." + typeOfFile);
        Upload task1 = new Upload(mStorageReference, fileName, keyConversation, folderStorage, "1." + typeOfFile);
        Upload task2 = new Upload(mStorageReference, fileName, keyConversation, folderStorage, "2." + typeOfFile);
        Upload task3 = new Upload(mStorageReference, fileName, keyConversation, folderStorage, "3." + typeOfFile);
        Upload task4 = new Upload(mStorageReference, fileName, keyConversation, folderStorage, "4." + typeOfFile);
        Upload task5 = new Upload(mStorageReference, fileName, keyConversation, folderStorage, "5." + typeOfFile);
        Thread t1 = new Thread(task1);
        Thread t2 = new Thread(task2);
        Thread t3 = new Thread(task3);
        Thread t4 = new Thread(task4);
        Thread t5 = new Thread(task5);
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        try {
            t1.join();
            t2.join();
            t3.join();
            t4.join();
            t5.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (task1.done && task2.done && task3.done && task4.done && task5.done) {
            if(f1.exists()){
                f1.delete();
            }
            if(f2.exists()){
                f2.delete();
            }
            if(f3.exists()){
                f3.delete();
            }
            if(f4.exists()){
                f4.delete();
            }
            if(f5.exists()){
                f5.delete();
            }
            String link = keyConversation + "/" + folderStorage + "/" + fileName;
            sendMessageAttach(Uri.parse(link), type);
            Log.e("FULL-PART",Utils.URL_PART);
        }

    }
    private class ProcessTask extends AsyncTask<ResultData,Integer,ResultData>{
        @Override
        protected ResultData doInBackground(ResultData... params) {
            uploadFileMutlti("Sending..",params[0].getmData(),"files",params[0].getmType(),params[0].getmKey());
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress("Sendding","Please wait");
        }
    }
    // get name of file upload
    private String getNameData(Uri uri) {
        String nameFile = "";
        if (uri.toString().startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = this.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    nameFile = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        return nameFile;
    }

    private void setFirebaseInstance() {
        mMsRef = mMsDatabase.getInstance().getReference().child("tblChat");
    }

    private void setFirebaseStorage() {
        mStorageReference = FirebaseStorage.getInstance().getReference();
    }

    private void showProgress(String title, String message) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(ChattingActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(ChattingActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                REQUEST_CAMERA);
                    }
                });
        alertDialog.show();
    }


}
