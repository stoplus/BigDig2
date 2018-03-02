package com.example1.den.bigdig2;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class App2 extends AppCompatActivity {

    private ImageView imageView;
    private final static String DIR_SD = "BIGDIG/test/B/";
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 10001;
    // объявляем разрешение, которое нам нужно получить
    private static final String WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private String textLink;
    private String AUTHORITY;
    private String PATH;
    private String timeFormatted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app2);

        textLink = getIntent().getStringExtra("linkText");//получаем ссылку из EditText
        AUTHORITY = getIntent().getStringExtra("AUTHORITY");
        PATH = getIntent().getStringExtra("PATH");

        imageView = findViewById(R.id.imageView);
        boolean processing = getIntent().getBooleanExtra("processing", false);
        //если нажали на ссылку в истории
        if (processing) {
            setPicture();//работа с историей
        } else {
            //при нажатии на "ок"
            createLink();//сохраняем ссылку в базу приложения "А"
        }//if
    }//onCreate

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
            Toast.makeText(getApplicationContext(), "Ссылка добавлена в базу данных", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getApplicationContext(), "Ошибка открытия файла!", Toast.LENGTH_LONG).show();
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
                        checkPermisAndSavePicture();//проверка прав и сохранение картинки
                        Toast.makeText(getApplicationContext(), "Ссылка удалена", Toast.LENGTH_LONG).show();
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

    private void checkPermisAndSavePicture() {
        //сохраняем картинку
        if (textLink != null && !textLink.equals("")) {
            if (isPermissionGranted(WRITE_EXTERNAL_STORAGE_PERMISSION)) {
//                Toast.makeText(this, "Разрешения есть, можно работать", Toast.LENGTH_SHORT).show();
                savePicture();
            } else {
                // иначе запрашиваем разрешение у пользователя
                requestPermission(WRITE_EXTERNAL_STORAGE_PERMISSION, REQUEST_WRITE_EXTERNAL_STORAGE);
            }//if
        }//if
    }//checkPermisAndSavePicture

    private void savePicture() {
        Uri downloadUri = Uri.parse(textLink);
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "SD-карта не доступна: " + Environment.getExternalStorageState(), Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, "Can only download HTTP/HTTPS URIs", Toast.LENGTH_LONG).show();
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
    private boolean isPermissionGranted(String permission) {
        // проверяем разрешение - есть ли оно у нашего приложения
        int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }//isPermissionGranted

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(App2.this, "Разрешения получены", Toast.LENGTH_LONG).show();
                savePicture();
            } else {
                Toast.makeText(App2.this, "Разрешения не получены", Toast.LENGTH_LONG).show();
                showPermissionDialog(App2.this);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }//if
    }//onRequestPermissionsResult

    private void requestPermission(String permission, int requestCode) {
        // запрашиваем разрешение
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }//requestPermission

    private void showPermissionDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String title = getResources().getString(R.string.app_name);
        builder.setTitle(title);
        builder.setMessage(title + " требует разрешение для использования SD карты");
        String positiveText = "Настройки";
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openAppSettings();
            }
        });
        String negativeText = "Выход";
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }//showPermissionDialog

    private void openAppSettings() {
        Intent intent1 = new Intent();
        intent1.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent1.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent1, REQUEST_WRITE_EXTERNAL_STORAGE);
    }//openAppSettings

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            requestApplicationConfig();
        }
    }//onActivityResult

    private void requestApplicationConfig() {
        if (isPermissionGranted(WRITE_EXTERNAL_STORAGE_PERMISSION)) {
            Toast.makeText(App2.this, "Теперь уже разрешения получены", Toast.LENGTH_LONG).show();
            savePicture();
        } else {
            Toast.makeText(App2.this, "Пользователь снова не дал нам разрешение", Toast.LENGTH_LONG).show();
            requestPermission(WRITE_EXTERNAL_STORAGE_PERMISSION, REQUEST_WRITE_EXTERNAL_STORAGE);
        }//if
    }//requestApplicationConfig

    @Override
    public void onBackPressed() {
        startMainApp();
        super.onBackPressed();
    }//onBackPressed
}//class App2
