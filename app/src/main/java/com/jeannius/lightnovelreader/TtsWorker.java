package com.jeannius.lightnovelreader;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class TtsWorker extends Worker{
    Context context;
    String text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. ";

    public TtsWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters){
        super(context, workerParameters);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        JeanniusLogger.log("myworker", "Work starting");
        Looper.prepare();
       new Handler(Looper.getMainLooper()).post(new Runnable() {
           @Override
           public void run() {
               Intent callIntent = new Intent();
               callIntent.setAction(Intent.ACTION_SEND);
               callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               callIntent.putExtra(Intent.EXTRA_TEXT, text);
               callIntent.setType("text/plain");
               JeanniusLogger.log("sending intent");
              context.startActivity(callIntent);
           }
       });

        return Result.success();
    }

}
