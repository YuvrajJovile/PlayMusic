package com.playmusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import com.playmusic.service.BackgroundPlayService;
import com.playmusic.util.CodeSnipet;

import static com.playmusic.util.IContstants.serviceIntentData.COMMAND_DATA;
public class MyPlayNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        CodeSnipet.showLog("BroadCastExternal","action=="+ intent.getAction());
        Intent lIntent = new Intent(context, BackgroundPlayService.class);
        lIntent.putExtra(COMMAND_DATA, intent.getAction());
        context.startService(lIntent);
    }
}
