package org.ratson.cordova.admob.banner;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;

import org.json.JSONException;
import org.json.JSONObject;
import org.ratson.cordova.admob.AbstractExecutor;

class BannerListener extends AdListener {
    private final BannerExecutor executor;
    BannerListener(BannerExecutor executor) {
        super();
        this.executor = executor;
    }

    @Override
    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
        JSONObject data = new JSONObject();
        try {
            data.put("error", loadAdError.getCode());
            data.put("reason", AbstractExecutor.getErrorReason(loadAdError.getCode()));
            data.put("adType", executor.getAdType());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        executor.fireAdEvent("admob.banner.events.LOAD_FAIL", data);
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
//        executor.fireAdEvent("admob.banner.events.EXIT_APP", data);
//        executor.fireAdEvent("onLeaveToAd", data);
//    }

    @Override
    public void onAdLoaded() {
        Log.w("AdMob", "BannerAdLoaded");
        if (executor.shouldAutoShow() && !executor.bannerVisible) {
            executor.showAd(true, null);
        }
        executor.fireAdEvent("admob.banner.events.LOAD");
        executor.fireAdEvent("onReceiveAd");
    }

    @Override
    public void onAdOpened() {
        executor.fireAdEvent("admob.banner.events.OPEN");
        executor.fireAdEvent("onPresentAd");
    }

    @Override
    public void onAdClosed() {
        executor.fireAdEvent("admob.banner.events.CLOSE");
        executor.fireAdEvent("onDismissAd");
    }
}
