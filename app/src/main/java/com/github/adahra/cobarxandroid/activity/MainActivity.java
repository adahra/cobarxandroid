package com.github.adahra.cobarxandroid.activity;

import android.content.Intent;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.adahra.cobarxandroid.R;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String STRING_TAG = MainActivity.class.getSimpleName();
    private Looper backgroundLooper;
    private Button buttonRunScheduler;
    private Button buttonRxAndroid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BackgroundThread backgroundThread = new BackgroundThread();
        backgroundThread.start();
        backgroundLooper = backgroundThread.getLooper();

        buttonRunScheduler = (Button) findViewById(R.id.button_run_scheduler);
        if (buttonRunScheduler != null) {
            buttonRunScheduler.setOnClickListener(this);
        }
        buttonRxAndroid = (Button) findViewById(R.id.button_rx_android);
        if (buttonRxAndroid != null) {
            buttonRxAndroid.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == buttonRunScheduler.getId()) {
            onRunSchedulerExampleButtonClicked();
        }

        if (id == buttonRxAndroid.getId()) {
            Intent intentRxAndroid = new Intent(MainActivity.this, Main2Activity.class);
            startActivity(intentRxAndroid);
        }
    }

    protected static class BackgroundThread extends HandlerThread {
        protected BackgroundThread() {
            super("SchedulerSample-BackgroundThread", THREAD_PRIORITY_BACKGROUND);
        }
    }

    protected static Observable<String> sampleObservable() {
        return Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                try {
                    // ...
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }

                return Observable.just("one", "two", "three", "four", "five");
            }
        });
    }

    protected void onRunSchedulerExampleButtonClicked() {
        sampleObservable()
                // Run on a background thread
                .subscribeOn(AndroidSchedulers.from(backgroundLooper))
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d(STRING_TAG, "onCompleted()");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(STRING_TAG, "onError()", e);
                    }

                    @Override
                    public void onNext(String s) {
                        Log.d(STRING_TAG, "onNext(" + s + ")");
                    }
                });
    }
}
