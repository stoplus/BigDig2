package com.example1.den.bigdig2;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class App2 extends AppCompatActivity {

    private ImageView imageView;
    private final static String DIR_SD = "BIGDIG/test/B/";
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 10001;
    // объявляем разрешение, которое нам нужно получить
    private static final String INTERNET_PERMISSION = Manifest.permission.INTERNET;
    private static final String WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private String textLink;
    private String AUTHORITY;
    private String PATH;
    private String timeFormatted;
    private Resources res;
    private static final int REQUEST_PERMITIONS = 1100;
    private TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app2);
        res = getResources();//доступ к ресерсам
        imageView = findViewById(R.id.imageView);
        textView2 = findViewById(R.id.textView2);
        textView2.setText(res.getString(R.string.needPermis));
        App2PermissionsDispatcher.startWorkingWithPermissionCheck(this);
//        startWorking();
    }//onCreate

    @NeedsPermission({Manifest.permission.INTERNET,  Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void startWorking(){
        textView2.setText("");
        textLink = getIntent().getStringExtra("linkText");//получаем ссылку из EditText
        AUTHORITY = getIntent().getStringExtra("AUTHORITY");
        PATH = getIntent().getStringExtra("PATH");
        boolean processing = getIntent().getBooleanExtra("processing", false);
        //если нажали на ссылку в истории
        if (processing) {
            setPicture();//работа с историей
        } else {
            //при нажатии на "ок"
            createLink();//сохраняем ссылку в базу приложения "А"
        }//if
    }

    //создаем данные по ссылке
    private void createLink() {
        long time = Calendar.getInstance().getTime().getTime();
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        timeFormatted = formatter.format(date);

        final int[] statusArr = {3};
        Picasso.with(getApplicationContext())
                .load(textLink)
                .error(R.drawable.error)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        statusArr[0] = 1;
                        insertLinkInDatabase(textLink, timeFormatted, statusArr[0]);//сохраняем ссылку в базу приложения "А"
                    }//onSuccess

                    @Override
                    public void onError() {
                        statusArr[0] = 2;
                        insertLinkInDatabase(textLink, timeFormatted, statusArr[0]);//сохраняем ссылку в базу приложения "А"
                    }//onError
                });
    }//createLink

    private void insertLinkInDatabase(String fileName, String timeFormatted, int status) {
        //сохраняем ссылку в базе
        Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + PATH);
        ContentValues values = createContentValues(status);
        Uri mUri = getContentResolver().insert(CONTENT_URI, values);
        if (mUri != null) {
            Toast.makeText(getApplicationContext(), res.getString(R.string.addLink), Toast.LENGTH_LONG).show();
        }
    }//insertLinkInDatabase

    private void setPicture() {
        Picasso.with(getApplicationContext())
                .load(textLink)
                .error(R.drawable.error)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        processingHistory();
                    }//onSuccess

                    @Override
                    public void onError() {
                        Toast.makeText(getApplicationContext(), res.getString(R.string.errorOpenFile), Toast.LENGTH_LONG).show();
                    }//onError
                });
    }//setPicture

    private void processingHistory() {
        final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + PATH);
        int status = getIntent().getIntExtra("status", 3);
        String id = getIntent().getStringExtra("id");//получаем ссылку из EditText
        final String[] selectionArgs = {id};

        switch (status) {
            case 1:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getContentResolver().delete(CONTENT_URI, "_id=?", selectionArgs);//удаление из базы
                        savePicture();
//                        checkPermisAndSavePicture();//проверка прав и сохранение картинки
                        Toast.makeText(getApplicationContext(), res.getString(R.string.linkDeleted), Toast.LENGTH_LONG).show();
                    }
                }, 15000);
                break;
            default:
                updateLink();
                break;
        }//switch
    }//processingHistory

    private void updateLink() {
        final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + PATH);
        String id = getIntent().getStringExtra("id");//получаем ссылку из EditText
        final String[] selectionArgs = {id};

        long time = Calendar.getInstance().getTime().getTime();
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        timeFormatted = formatter.format(date);

        final int[] statusArr = {3};
        Picasso.with(getApplicationContext())
                .load(textLink)
                .error(R.drawable.error)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        statusArr[0] = 1;
                        ContentValues values = createContentValues(statusArr[0]);
                        getContentResolver().update(CONTENT_URI, values, "_id=?", selectionArgs);
                    }//onSuccess

                    @Override
                    public void onError() {
                        statusArr[0] = 2;
                        ContentValues values = createContentValues(statusArr[0]);
                        getContentResolver().update(CONTENT_URI, values, "_id=?", selectionArgs);
                    }//onError
                });
    }//updateLink

    private ContentValues createContentValues(int status) {
        ContentValues values = new ContentValues();
        values.put("link", textLink);
        values.put("time", timeFormatted);
        values.put("status_link", status);
        return values;
    }//createContentValues

    private void savePicture() {
        Uri downloadUri = Uri.parse(textLink);
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, res.getString(R.string.sdCardnotAvailable) + Environment.getExternalStorageState(), Toast.LENGTH_LONG).show();
            return;
        }//if
        File sdPath = Environment.getExternalStorageDirectory();// получаем путь к SD
        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD); // добавляем свой каталог к пути
        sdPath.mkdirs();// создаем каталог

        try {
            //Запускаем зугузку файла по указанному пути
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setDestinationUri(Uri.fromFile(sdPath));//указываем путь сохранения
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, res.getString(R.string.onlyHTTP_HTTPS_URI), Toast.LENGTH_LONG).show();
            startMainApp();
        }//try-catch
    }//savePicture

    private void startMainApp() {
        Intent intent = new Intent();
        intent.setClassName("com.example.den.bigdig", "com.example.den.bigdig.MainActivity");
        startActivity(intent);
        finish();
    }//startMainApp

    //---------------------------------------------------------------------------------------------
    @OnPermissionDenied({Manifest.permission.INTERNET,  Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void permissionsDenied() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_PERMITIONS);
    }//permissionsDenied

    @OnNeverAskAgain({Manifest.permission.INTERNET,  Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void onNeverAskAgain() {
        new android.app.AlertDialog.Builder(this)
                .setTitle(res.getString(R.string.needPermission))
                .setMessage( res.getString(R.string.PermissionsRationale))
                .setPositiveButton( res.getString(R.string.ok), (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton( res.getString(R.string.no), (dialog, which) -> dialog.dismiss()).create()
                .show();
    }

    @OnShowRationale({Manifest.permission.INTERNET,  Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showRationale(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setTitle(res.getString(R.string.needPermission))
                .setMessage(res.getString(R.string.PermissionsRationale))
                .setPositiveButton(res.getString(R.string.ok), (dialog, button) -> request.proceed())
                .setNegativeButton(res.getString(R.string.no), (dialog, button) -> request.cancel())
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMITIONS) {
            App2PermissionsDispatcher.startWorkingWithPermissionCheck(this);
        }
    }//onActivityResult

    @Override
    public void onBackPressed() {
        startMainApp();
    }//onBackPressed
}//class App2
