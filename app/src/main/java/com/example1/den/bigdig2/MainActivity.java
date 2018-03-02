package com.example1.den.bigdig2;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private TextView tvInfo;
    private TextView tvInfoTime;
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        res = getResources();//доступ к ресерсам
        tvInfo = findViewById(R.id.textView);
        tvInfoTime = findViewById(R.id.textViewTime);
        tvInfo.setText(res.getString(R.string.attention));

        MyTask mt = new MyTask();
        mt.execute();
    }//onCreate

    class MyTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (int i = 11; i > 0; i--) {
                    TimeUnit.SECONDS.sleep(1);
                    publishProgress(i - 1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }//doInBackground

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            tvInfoTime.setText(values[0] + " " + res.getString(R.string.seconds));
        }//onProgressUpdate

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            finish();
        }//onPostExecute
    }//class MyTask
}//class MainActivity
