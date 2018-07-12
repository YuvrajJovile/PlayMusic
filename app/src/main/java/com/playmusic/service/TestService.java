package com.playmusic.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class TestService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
 /*
    @Nullable
   @Override
    public IBinder onBind(Intent intent) {
        showMessage("IBinder");
        return null;
    }

    @Override
    public void onCreate() {
        showMessage("OnCreate");
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showMessage("onStartCommand");

        if (intent != null)
            showMessage("intent data ==" + intent.getStringExtra("data"));


        ScheduledExecutorService lScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        lScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                senseActivity();
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);
        return START_STICKY;
    }

    private void senseActivity() {
        try {
            ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);


            String activityOnTop;
            if (Build.VERSION.SDK_INT > 20) {
                activityOnTop = mActivityManager.getRunningAppProcesses().get(0).processName;
            } else {
                List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager.getRunningTasks(1);
                ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
                activityOnTop = ar.topActivity.getPackageName();
            }

            Log.e("activity on TOp", "" + activityOnTop);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    *//*private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture

            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }*//*

    public TestService() {
        super();
        showMessage("Constructor");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        showMessage("onStart-Depricated");
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        showMessage("onDestroy");
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        showMessage("ConfigChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        showMessage("LowMemory");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        showMessage("TrimMemory");
        super.onTrimMemory(level);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        showMessage("Unbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        showMessage("Rebind");
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        showMessage("TaskRemoved");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        showMessage("Dump");
        super.dump(fd, writer, args);
    }

    private void showMessage(String pMessage) {
        Log.e("ServiceClass", pMessage);
    }*/
}
