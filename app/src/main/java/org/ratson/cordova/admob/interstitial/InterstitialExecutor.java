package org.ratson.cordova.admob.interstitial;

import static org.ratson.cordova.admob.interstitial.Globals.TIMER_FINISHED;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;

import org.ratson.cordova.admob.AbstractExecutor;
import org.ratson.cordova.admob.AdMob;
import org.ratson.cordova.admob.AdMobConfig;

import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class InterstitialExecutor extends AbstractExecutor {
    /**
     * The interstitial ad to display to the user.
     */
    private InterstitialAd mInterstitialAd;
    final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    public InterstitialExecutor(AdMob plugin) {
        super(plugin);
    }

    @Override
    public String getAdType() {
        return "interstitial";
    }

    public PluginResult prepareAd(JSONObject options, CallbackContext callbackContext) {
        AdMobConfig config = plugin.config;
        CordovaInterface cordova = plugin.cordova;
        config.setInterstitialOptions(options);

        final CallbackContext delayCallback = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AdMobConfig config = plugin.config;
                CordovaInterface cordova = plugin.cordova;

                destroy();
                        Log.d("INTERSTITUIAL","prepareAd");
                        InterstitialAd.load(cordova.getActivity(), "", plugin.buildAdRequest(), new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                mInterstitialAd = interstitialAd;
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                mInterstitialAd = null;
                            }
                        });

                delayCallback.success();
            }
        });
        return null;
    }

    /**
     * Parses the createAd interstitial view input parameters and runs the createAd interstitial
     * view action on the UI thread.  If this request is successful, the developer
     * should make the requestAd call to request an ad for the banner.
     *
     * @param options The JSONArray representing input parameters.  This function
     *                expects the first object in the array to be a JSONObject with the
     *                input parameters.
     * @return A PluginResult representing whether or not the banner was created
     * successfully.
     */
    public PluginResult createAd(JSONObject options, CallbackContext callbackContext) {
        AdMobConfig config = plugin.config;
        CordovaInterface cordova = plugin.cordova;

        config.setInterstitialOptions(options);

        final CallbackContext delayCallback = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AdMobConfig config = plugin.config;
                CordovaInterface cordova = plugin.cordova;

                destroy();


                mInterstitialAd.setFullScreenContentCallback(new InterstitialListener(InterstitialExecutor.this));
                Log.w("interstitial", config.getInterstitialAdUnitId());
                InterstitialAd.load(cordova.getActivity(), "", plugin.buildAdRequest(), new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });


        delayCallback.success();
            }
        });
        return null;
    }

    @Override
    public void destroy() {
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(null);
            mInterstitialAd = null;
        }
    }

    public PluginResult requestAd(JSONObject options, CallbackContext callbackContext) {
        CordovaInterface cordova = plugin.cordova;

        plugin.config.setInterstitialOptions(options);

        if (mInterstitialAd == null) {
            callbackContext.error("interstitialAd is null, call createInterstitialView first");
            return null;
        }

        final CallbackContext delayCallback = callbackContext;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                InterstitialAd.load(cordova.getActivity(), "", plugin.buildAdRequest(), new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });



        delayCallback.success();
            }
        });

        return null;
    }

    public PluginResult showAd(final boolean show, final CallbackContext callbackContext) {
        if (mInterstitialAd == null) {
            return new PluginResult(PluginResult.Status.ERROR, "interstitialAd is null, call createInterstitialView first.");
        }
        CordovaInterface cordova = plugin.cordova;

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mInterstitialAd == null) {
                    return;
                }
                AdMobConfig config = plugin.config;

                if (mInterstitialAd !=null && TIMER_FINISHED) {
                    mInterstitialAd.show(cordova.getActivity());
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            Log.d("INTERSTITUIAL","onAdDismissedFullScreenContent");
                            Timers.timer().start();
                            TIMER_FINISHED = false;
                            InterstitialAd.load(cordova.getActivity(), "", plugin.buildAdRequest(), new InterstitialAdLoadCallback() {
                                @Override
                                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                    mInterstitialAd = interstitialAd;
                                    Log.d("INTERSTITUIAL","onAdDismissedFullScreenContent");

                                }

                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                    mInterstitialAd = null;
                                }
                            });
                        }
                    });
                    if (callbackContext != null) {
                        callbackContext.success();
                    }
                } else if (!config.autoShowInterstitial) {
                    if (callbackContext != null) {
                        callbackContext.error("Interstital not ready yet");
                    }
                }
            }
        });

        return null;
    }

    public PluginResult isReady(final CallbackContext callbackContext) {
        CordovaInterface cordova = plugin.cordova;

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mInterstitialAd != null) {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, false));
                }
            }
        });

        return null;
    }

    boolean shouldAutoShow() {
        return plugin.config.autoShowInterstitial;
    }

}