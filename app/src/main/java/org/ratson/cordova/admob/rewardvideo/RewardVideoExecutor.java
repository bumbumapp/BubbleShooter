package org.ratson.cordova.admob.rewardvideo;

import android.os.Bundle;
import android.util.Log;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;
import org.ratson.cordova.admob.AbstractExecutor;
import org.ratson.cordova.admob.AdMob;

public class RewardVideoExecutor extends AbstractExecutor {
    /**
     * RewardVideo
     */
//    private RewardedVideoAd rewardedVideoAd;
    boolean isRewardedVideoLoading = false;
    final Object rewardedVideoLock = new Object();

    public RewardVideoExecutor(AdMob plugin) {
        super(plugin);
    }

    @Override
    public String getAdType() {
        return "rewardvideo";
    }

    public PluginResult prepareAd(JSONObject options, CallbackContext callbackContext) {
        CordovaInterface cordova = plugin.cordova;
        plugin.config.setRewardVideoOptions(options);

        final CallbackContext delayCallback = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CordovaInterface cordova = plugin.cordova;
                clearAd();

//                rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(cordova.getActivity());
//                rewardedVideoAd.setRewardedVideoAdListener(new RewardVideoListener(RewardVideoExecutor.this));
//                Log.w("rewardedvideo", plugin.config.getRewardedVideoAdUnitId());
//
//                synchronized (rewardedVideoLock) {
//                    if (!isRewardedVideoLoading) {
//                        isRewardedVideoLoading = true;
//                        Bundle extras = new Bundle();
//                        extras.putBoolean("_noRefresh", true);
//                        AdRequest adRequest = new AdRequest.Builder()
//                                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
//                                .build();
//                        rewardedVideoAd.loadAd(plugin.config.getRewardedVideoAdUnitId(), adRequest);
//                        delayCallback.success();
//                    }
//                }
            }
        });
        return null;
    }

    public void clearAd() {

    }

    public PluginResult showAd(final boolean show, final CallbackContext callbackContext) {

        CordovaInterface cordova = plugin.cordova;

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

//                if (rewardedVideoAd instanceof RewardedVideoAd) {
//                    RewardedVideoAd rvad = rewardedVideoAd;
//                    if (rvad.isLoaded()) {
//                        rvad.show();
//                    }
//                }

                if (callbackContext != null) {
                    callbackContext.success();
                }
            }
        });

        return null;
    }

    @Override
    public void destroy() {
        this.clearAd();
    }

    public PluginResult isReady(final CallbackContext callbackContext) {
        CordovaInterface cordova = plugin.cordova;

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if (rewardedVideoAd != null ) {
//                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
//                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, false));
//                }
            }
        });

        return null;
    }

    boolean shouldAutoShow() {
        return plugin.config.autoShowRewardVideo;
    }
}
