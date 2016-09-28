package com.zgsoft.cordova;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by headchen on 2016-09-13.
 */

public class DeepLinkPlugin extends CordovaPlugin
{
    /**
     * PluginName in config.xml
     */
    private static final String TAG = "ZgsoftDeepLinkPlugin";
    /**
     * Created by Nikolay Demyankov on 09.09.15.
     * <p/>
     * Class holds list of method names that is called from JS side.
     */
    public final class JSAction {

        /**
         * Subscribe to event.
         */
        public static final String SUBSCRIBE = "jsSubscribe";

        /**
         * Unsubscribe from event.
         */
        public static final String UNSUBSCRIBE = "jsUnsubscribe";

        public static  final  String CANOPENAPP="canOpenApp";
    }

    // list of subscribers
    private CallbackContext subscriber;

    // stored url, that is captured on application launch
    private JSONObject storedEvent;

    // region Public API

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.d(TAG, "ZgsoftDeepLinkPlugin: firing up...");
        handleIntent(cordova.getActivity().getIntent());
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        boolean isHandled = true;
        if (JSAction.SUBSCRIBE.equals(action)) {
            subscribe( callbackContext);
        } else if (JSAction.UNSUBSCRIBE.equals(action)) {
            unsubscribe();
        } else if (JSAction.CANOPENAPP.equals(action)){
            canOpenApp(args.getString(0),callbackContext);
        }
        else {
            isHandled = false;
        }

        return isHandled;
    }

    @Override
    public void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    // endregion

    // region JavaScript methods

    /**
     * Add subscriber for the event.
     *
     * @param callbackContext callback to use when event is captured
     */
    private void subscribe(final CallbackContext callbackContext)
    {
        this.subscriber = callbackContext;
        checkFirstRunEvent(this.cordova.getActivity().getApplicationContext());
        tryToConsumeEvent();
    }

    /**
     * Remove subscriber from the event.
     *
     */
    private void unsubscribe() {
        subscriber=null;
    }


    /**
     * Check if we can open an app with a given URI scheme.
     *
     * Thanks to https://github.com/ohh2ahh/AppAvailability/blob/master/src/android/AppAvailability.java
     */
    private void canOpenApp(String uri, final CallbackContext callbackContext) {
        Context ctx = this.cordova.getActivity().getApplicationContext();
        final PackageManager pm = ctx.getPackageManager();

        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            callbackContext.success();
        } catch(PackageManager.NameNotFoundException e) {}

        callbackContext.error("");
    }
    /**
     * Try to send event to the subscribers.
     */
    private void tryToConsumeEvent() {
        if (subscriber == null || storedEvent == null) {
            return;
        }
        sendMessageToJs(storedEvent, subscriber);

    }

    /**
     * Send message to JS side.
     *
     * @param event  message to send
     * @param callback to what callback we are sending the message
     */
    private void sendMessageToJs(JSONObject event, CallbackContext callback) {
        final PluginResult result = new PluginResult(PluginResult.Status.OK, event);
        result.setKeepCallback(true);
        callback.sendPluginResult(result);
    }

    // endregion

    // region Intent handling

    /**
     * Handle launch intent.
     * If it is an UL intent - then event will be dispatched to the JS side.
     *
     * @param intent launch intent
     */
    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        // read intent
        String action = intent.getAction();
        Uri launchUri = intent.getData();

        // if app was not launched by the url - ignore
        if (!Intent.ACTION_VIEW.equals(action) || launchUri == null) {
            Log.d(TAG, "launchUri is null or action != VIEW");
            return;
        }


        // store message and try to consume it
        storedEvent = createEventFromUrl(launchUri);
        tryToConsumeEvent();
    }

    private JSONObject createEventFromUrl(Uri url)
    {
        JSONObject lastEvent;
        try {
            lastEvent = new JSONObject();
            lastEvent.put("url", url.toString());
            lastEvent.put("path", url.getPath());
            lastEvent.put("queryString", url.getQuery());
            lastEvent.put("scheme", url.getScheme());
            lastEvent.put("host", url.getHost());
            lastEvent.put("fragment", url.getFragment());
            return  lastEvent;
        } catch(JSONException ex) {
            Log.e(TAG, "Unable to process URL scheme deeplink", ex);
            return null;
        }
    }

    //检查第一次运行时的信息
    private  void checkFirstRunEvent(Context context)
    {
       if(storedEvent!=null || !checkFirstRun(context))
           return;

        String url = getPkgComment(context);
        if(url == null)
            return;
         storedEvent = createEventFromUrl(Uri.parse(url));
    }


    // endregion

    // region first laughch get install info


    private boolean checkFirstRun(Context context) {

        final String PREFS_NAME = "com.zgsoft.prefs";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;
       // String packageName=null;


        // Get current version code
        int currentVersionCode = 0;
        try {
            PackageInfo packageInfo =context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            currentVersionCode =packageInfo.versionCode;
            //packageName = packageInfo.packageName;

        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            // handle exception
            e.printStackTrace();
            return false;
        }


        // Get saved version code
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {

            // This is just a normal run
            return false;

        } else {
            // Update the shared preferences with the current version code
            prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).commit();
            return true;
        }

    }

    @Nullable
    private static String getPkgComment(final Context context)
    {
        File file=new File(context.getApplicationInfo().sourceDir);
        try {
            String result= readZipComment(file);
            Log.i(TAG,"pkgComment : " + result);
            return result;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }


  //读取short
    private static short readShort(DataInput input) throws IOException {
        byte[] buf = new byte[SHORT_LENGTH];
        input.readFully(buf);
        ByteBuffer bb = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
        return bb.getShort(0);
    }

    static  final  int SHORT_LENGTH = 2;
    static final String UTF_8 = "UTF-8";

    @Nullable
    public static String readZipComment(File file) throws IOException
    {
        RandomAccessFile raf = null;
        try
        {
            raf = new RandomAccessFile(file, "r");
            long index = raf.length();
                index -= SHORT_LENGTH;
                raf.seek(index);
                // read content length field
                int length = readShort(raf);
                if (length > 2)
                {
                    length -=SHORT_LENGTH; //因为后面的长度增加 了最后的short
                    index -= length ;
                    raf.seek(index);
                    // read content bytes
                    byte[] bytesComment = new byte[length];
                    raf.readFully(bytesComment);
                    return new String(bytesComment, UTF_8);
                }
                else
                {
                    return null;
                }

        }
        finally
        {
            if (raf != null)
            {
                raf.close();
            }
        }
    }

    // endregion
}
