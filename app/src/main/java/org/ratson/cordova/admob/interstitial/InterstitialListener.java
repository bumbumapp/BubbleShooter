package org.ratson.cordova.admob.interstitial;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;

import org.json.JSONException;
import org.json.JSONObject;

import org.ratson.cordova.admob.AbstractExecutor;

class InterstitialListener extends FullScreenContentCallback {
    private final InterstitialExecutor executor;

    InterstitialListener(InterstitialExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void onAdClicked() {
        executor.fireAdEvent("admob.interstitial.events.OPEN");
        executor.fireAdEvent("onPresentInterstitialAd");
    }

    @Override
    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
        JSONObject data = new JSONObject();
        try {
            data.put("error", adError.getCode());
            data.put("reason", AbstractExecutor.getErrorReason(adError.getCode()));
            data.put("adType", executor.getAdType());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        executor.fireAdEvent("admob.interstitial.events.LOAD_FAIL", data);
        executor.fireAdEvent("onFailedToReceiveAd", data);
    }



//    @Override
//    public void onAdLeftApplication() {
//        JSONObject data = new JSONObject();
//        try {
//            data.put("adType", executor.getAdType());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        executor.fireAdEvent("admob.interstitial.events.EXIT_APP", data);
//        executor.fireAdEvent("onLeaveToAd", data);
//    }


    @Override
    public void onAdShowedFullScreenContent() {
        Log.w("AdMob", "InterstitialAdLoaded");
        executor.fireAdEvent("admob.interstitial.events.LOAD");
        executor.fireAdEvent("onReceiveInterstitialAd");

        if (executor.shouldAutoShow()) {
            executor.showAd(true, null);
        }
    }

    @Override
    public void onAdDismissedFullScreenContent() {
        executor.fireAdEvent("admob.interstitial.events.CLOSE");
        executor.fireAdEvent("onDismissInterstitialAd");
        executor.destroy();
    }


}