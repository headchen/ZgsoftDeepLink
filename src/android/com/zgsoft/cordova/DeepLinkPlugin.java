package com.zgsoft.cordova;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

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


    // endregion
}
