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
    private String id;
    private final static String DIR_SD = "BIGDIG/test/B/";
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 10001;
    // объявляем разрешение, которое нам нужно получить
    private static final String WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private String textLink;
    private String AUTHORITY;
    private String PATH;
    private int status;
    private String timeFormatted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app2);

        textLink = getIntent().getStringExtra("linkText");//получаем ссылку из EditText
        AUTHORITY = getIntent().getStringExtra("AUTHORITY");//получаем ссылку из EditText
        PATH = getIntent().getStringExtra("PATH");//получаем ссылку из EditText
//        textLink = "https://wallpaperscraft.ru/image/bmw_motocikl_sportivnyy_doroga_74150_602x339.jpg";

        if (textLink == null) {
            Toast.makeText(this, "Закрываем", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        imageView = findViewById(R.id.imageView);
        boolean processing = getIntent().getBooleanExtra("processing", false);

        //если нажали на ссылку в истории
        if (processing) {
            setPicture();//работа с историей
        } else {
            //при нажатии на "ок"
            createLink();//сохраняем ссылку в базу приложения "А"
        }
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
                        saveLinkInDatabase(textLink, timeFormatted, statusArr[0]);//сохраняем ссылку в базу приложения "А"
                    }

                    @Override
                    public void onError() {
                        statusArr[0] = 2;
                        saveLinkInDatabase(textLink, timeFormatted, statusArr[0]);//сохраняем ссылку в базу приложения "А"
                    }
                });
    }


    private void saveLinkInDatabase(String fileName, String timeFormatted, int status) {
        //сохраняем ссылку в базе
        Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + PATH);
        ContentValues values = new ContentValues();
        values.put("link", fileName);
        values.put("time", timeFormatted);
        values.put("status_link", status);
        Uri mUri = getContentResolver().insert(CONTENT_URI, values);
        if (mUri != null) {
            Toast.makeText(getApplicationContext(), "Successfully added to Content Provider", Toast.LENGTH_LONG).show();
        }
    }//saveLinkInDatabase

//    private void createList() {
//        DownloadManager.Query query = new DownloadManager.Query();
//        query.setFilterByStatus(DownloadManager.STATUS_PAUSED |
//                DownloadManager.STATUS_PENDING |
//                DownloadManager.STATUS_RUNNING |
//                DownloadManager.STATUS_SUCCESSFUL);
//        Cursor cur = downloadManager.query(query);
//        cur.moveToFirst();
//        for (int i = 0; i < cur.getCount(); i++) {
//            String fileName = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_URI));
//            listLinksName.add(fileName);
//
//            long time = cur.getLong(cur.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP));
//            Date date = new Date(time);
//            DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
//            String timeFormatted = formatter.format(date);
//            listLinksTime.add(timeFormatted);
//
//            int status;
//            int downloadStatus = cur.getInt(cur.getColumnIndex(DownloadManager.COLUMN_STATUS));
//            switch (downloadStatus) {
//                case DownloadManager.STATUS_SUCCESSFUL:
//                    status = 1;
//                    break;
//                case DownloadManager.STATUS_FAILED:
//                    status = 2;
//                    break;
//                default:
//                    status = 3;
//                    break;
//            }
//            listLinksStatus.add(status);
//            cur.moveToNext();
//        }
//        cur.close();
//    }

    // приёмник уведомления об успешной загрузке
//    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
//            if (id == reference) {                // Ваши действия с загруженным файлом
//                imageView.setImageURI(downloadManager.getUriForDownloadedFile(reference));//показываем картинку
//            }
//        }
//    };

    private void setPicture() {
        Picasso.with(getApplicationContext())
                .load(textLink)
                .error(R.drawable.error)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        processingHistory();
                    }

                    @Override
                    public void onError() {
                    }
                });

//        DownloadManager.Query query = new DownloadManager.Query();
//        query.setFilterByStatus(DownloadManager.STATUS_PAUSED |
//                DownloadManager.STATUS_PENDING |
//                DownloadManager.STATUS_RUNNING |
//                DownloadManager.STATUS_SUCCESSFUL);
//        Cursor cur = downloadManager.query(query);
//        cur.moveToFirst();
//        for (int i = 0; i < cur.getCount(); i++) {
//            String url = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_URI));//получаем url
//
//            if (textLink.equals(url)) {
//                Long id = cur.getLong(cur.getColumnIndex(DownloadManager.COLUMN_ID));//получаем id
//                imageView.setImageURI(downloadManager.getUriForDownloadedFile(id));//показываем картинку


//            }
//            if (textLink.equals(url)&& downloadStatus == 1) {
//                Long id = cur.getLong(cur.getColumnIndex(DownloadManager.COLUMN_ID));
//                imageView.setImageURI(downloadManager.getUriForDownloadedFile(id));//показываем картинку
//                downloadManager.remove(id);
//
//                break;
//            }
//            cur.moveToNext();
//        }
//        cur.close();
    }

    private void processingHistory() {
        final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + PATH);
        status = getIntent().getIntExtra("status", 3);//получаем ссылку из EditText
        id = getIntent().getStringExtra("id");//получаем ссылку из EditText
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
        }
    }

    private void updateLink() {
        final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + PATH);
        id = getIntent().getStringExtra("id");//получаем ссылку из EditText
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
                        ContentValues values = new ContentValues();
                        values.put("link", textLink);
                        values.put("time", timeFormatted);
                        values.put("status_link", statusArr[0]);
                        getContentResolver().update(CONTENT_URI, values, "_id=?", selectionArgs);
                    }

                    @Override
                    public void onError() {
                        statusArr[0] = 2;
                        ContentValues values = new ContentValues();
                        values.put("link", textLink);
                        values.put("time", timeFormatted);
                        values.put("status_link", statusArr[0]);
                        getContentResolver().update(CONTENT_URI, values, "_id=?", selectionArgs);
                    }
                });
    }

    private void checkPermisAndSavePicture() {
        //сохраняем картинку
        if (textLink != null && !textLink.equals("")) {
            if (isPermissionGranted(WRITE_EXTERNAL_STORAGE_PERMISSION)) {
                Toast.makeText(this, "Разрешения есть, можно работать", Toast.LENGTH_SHORT).show();
                savePicture();
            } else {
                // иначе запрашиваем разрешение у пользователя
                requestPermission(WRITE_EXTERNAL_STORAGE_PERMISSION, REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void savePicture() {
        Uri downloadUri = Uri.parse(textLink);
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "SD-карта не доступна: " + Environment.getExternalStorageState(), Toast.LENGTH_LONG).show();
            return;
        }
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
        }
    }

    private void startMainApp() {
        Intent intent = new Intent();
        intent.setClassName("com.example.den.bigdig", "com.example.den.bigdig.MainActivity");
        startActivity(intent);
        finish();
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        IntentFilter intentFilter = new IntentFilter(
//                DownloadManager.ACTION_DOWNLOAD_COMPLETE);
//        registerReceiver(downloadReceiver, intentFilter);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        unregisterReceiver(downloadReceiver);
//    }


    //---------------------------------------------------------------------------------------------
    private boolean isPermissionGranted(String permission) {
        // проверяем разрешение - есть ли оно у нашего приложения
        int permissionCheck = ActivityCompat.checkSelfPermission(this, permission);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

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
        }
    }

    private void requestPermission(String permission, int requestCode) {
        // запрашиваем разрешение
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

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
    }

    private void openAppSettings() {
        Intent intent1 = new Intent();
        intent1.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent1.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent1, REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            requestApplicationConfig();
        }
    }

    private void requestApplicationConfig() {
        if (isPermissionGranted(WRITE_EXTERNAL_STORAGE_PERMISSION)) {
            Toast.makeText(App2.this, "Теперь уже разрешения получены", Toast.LENGTH_LONG).show();
            savePicture();
        } else {
            Toast.makeText(App2.this, "Пользователь снова не дал нам разрешение", Toast.LENGTH_LONG).show();
            requestPermission(WRITE_EXTERNAL_STORAGE_PERMISSION, REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onBackPressed() {
        startMainApp();
        super.onBackPressed();
    }
}

