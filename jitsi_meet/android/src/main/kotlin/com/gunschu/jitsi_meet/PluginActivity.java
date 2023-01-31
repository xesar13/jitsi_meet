package com.gunschu.jitsi_meet;

import static com.gunschu.jitsi_meet.JitsiMeetPlugin.JITSI_MEETING_CLOSE;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.util.HashMap;

public class PluginActivity extends JitsiMeetActivity {
    private final String TAG = PluginActivity.class.getSimpleName();
    boolean onStopCalled = false;
    private static PluginActivity instance;

    public static PluginActivity getInstance(){
        return instance;
    }


 public  void launchActivity(Activity context, JitsiMeetConferenceOptions options){
      Intent intent = new Intent(context, PluginActivity.class);
      intent.setAction("org.jitsi.meet.CONFERENCE");
      intent.putExtra("JitsiMeetConferenceOptions", options);
      context.startActivity(intent);
   }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode){
           JitsiMeetEventStreamHandler.Companion.getInstance().onPictureInPictureWillEnter();
        }else {
            JitsiMeetEventStreamHandler.Companion.getInstance().onPictureInPictureTerminated();
        }
        if (!isInPictureInPictureMode && onStopCalled){
            getJitsiView().dispose();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == JITSI_MEETING_CLOSE){
                finish();
            }
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        onStopCalled = true;
        unregisterReceiver(receiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        onStopCalled = false;
        registerReceiver(receiver, new IntentFilter(JITSI_MEETING_CLOSE));
    }

    @Override
    protected void onConferenceWillJoin(HashMap<String, Object> extraData) {
        JitsiMeetEventStreamHandler.Companion.getInstance().onConferenceWillJoin(extraData);
        super.onConferenceWillJoin(extraData);
    }

    @Override
    protected void onConferenceJoined(HashMap<String, Object> extraData) {
       JitsiMeetEventStreamHandler.Companion.getInstance().onConferenceJoined(extraData);
       super.onConferenceJoined(extraData);
    }

    @Override
    protected void onConferenceTerminated(HashMap<String, Object> extraData) {
        JitsiMeetEventStreamHandler.Companion.getInstance().onConferenceTerminated(extraData);
        super.onConferenceTerminated(extraData);
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        turnScreenOnAndKeyguardOff();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        turnScreenOffAndKeyguardOn();
    }

    private void turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
        }else {
           getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED );
           getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD );
           getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN );
           getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
           getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON );
           getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON );
        }
    }

    private void turnScreenOffAndKeyguardOn(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
            setShowWhenLocked(false);
            setTurnScreenOn(false);

        }else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED );
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD );
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN );
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON );
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON );

        }
    }
}
