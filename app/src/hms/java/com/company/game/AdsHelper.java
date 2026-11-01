package com.company.game;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.InterstitialAd;
import com.huawei.hms.ads.NonPersonalizedAd;
import com.huawei.hms.ads.RequestOptions;
import com.huawei.hms.ads.banner.BannerView;

/**
 * Huawei AppGallery build: same API and interstitial cap as the Play build's AdsHelper, served by
 * Huawei Ads (Petal) instead of AdMob. Only non-personalized ads are requested, so there is no
 * consent form; {@link #gatherConsent} just initialises the SDK.
 */
public final class AdsHelper {
    private static final String TAG = "AdsHelper";

    /** Minimum time between two interstitials (and between app start and the first one). */
    public static final long INTERSTITIAL_MIN_INTERVAL_MS = 60_000L;
    /** If show() has not opened the ad by then, the game continues (Huawei only reports load failures). */
    private static final long SHOW_FALLBACK_MS = 3_000L;

    private static boolean sdkReady;
    private static InterstitialAd interstitialAd;
    private static boolean interstitialLoading;
    private static long lastInterstitialAt = -1L;

    public interface OnAdsReady {
        void onAdsReady();
    }

    private AdsHelper() {
    }

    /** Initialises Huawei Ads (non-personalized only) and calls {@code onReady} once if ads may be requested. */
    public static void gatherConsent(@NonNull Activity activity, @NonNull OnAdsReady onReady) {
        if (lastInterstitialAt < 0) {
            lastInterstitialAt = SystemClock.elapsedRealtime();
        }
        if (!sdkReady) {
            try {
                HwAds.init(activity.getApplicationContext());
                RequestOptions options = HwAds.getRequestOptions().toBuilder()
                        .setNonPersonalizedAd(NonPersonalizedAd.ALLOW_NON_PERSONALIZED)
                        .build();
                HwAds.setRequestOptions(options);
                sdkReady = true;
            } catch (RuntimeException e) {
                Log.w(TAG, "Huawei Ads init failed", e);
            }
        }
        if (sdkReady && !activity.isFinishing()) {
            onReady.onAdsReady();
        }
    }

    public static boolean canRequestAds(@NonNull Context context) {
        return sdkReady;
    }

    public static boolean isPrivacyOptionsRequired(@NonNull Context context) {
        return false;
    }

    public static void showPrivacyOptions(@NonNull Activity activity) {
    }

    /** Adds a full-width banner to {@code container} (hidden until filled); returns null if ads are off. */
    @Nullable
    public static BannerView loadBanner(@NonNull Activity activity, @NonNull ViewGroup container, @NonNull String adUnitId) {
        if (!canRequestAds(activity)) {
            return null;
        }
        BannerView banner = new BannerView(activity);
        banner.setAdId(adUnitId);
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        int widthDp = (int) (metrics.widthPixels / metrics.density);
        banner.setBannerAdSize(BannerAdSize.getCurrentDirectionBannerSize(activity, widthDp));
        container.setVisibility(View.GONE);
        banner.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                container.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailed(int code) {
                Log.d(TAG, "Banner failed: " + code);
            }
        });
        container.removeAllViews();
        container.addView(banner, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        banner.loadAd(new AdParam.Builder().build());
        return banner;
    }

    /** Loads one interstitial in the background if none is cached or loading. */
    public static void preloadInterstitial(@NonNull Context context, @NonNull String adUnitId) {
        if (interstitialAd != null || interstitialLoading || !canRequestAds(context)) {
            return;
        }
        interstitialLoading = true;
        final InterstitialAd ad = new InterstitialAd(context.getApplicationContext());
        ad.setAdId(adUnitId);
        ad.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                interstitialAd = ad;
                interstitialLoading = false;
            }

            @Override
            public void onAdFailed(int code) {
                interstitialLoading = false;
                Log.d(TAG, "Interstitial failed: " + code);
            }
        });
        ad.loadAd(new AdParam.Builder().build());
    }

    /**
     * Shows the cached interstitial at a natural break point if the frequency cap allows it.
     * Returns true if an ad is being shown.
     */
    public static boolean showInterstitialIfReady(@NonNull Activity activity, @NonNull String adUnitId) {
        return showInterstitialIfReady(activity, adUnitId, null);
    }

    /**
     * Like {@link #showInterstitialIfReady(Activity, String)}; {@code afterAd} runs once when the ad is
     * closed or fails, after {@link #SHOW_FALLBACK_MS} if it never opens, or immediately if no ad is shown.
     */
    public static boolean showInterstitialIfReady(@NonNull Activity activity, @NonNull String adUnitId,
                                                  @Nullable Runnable afterAd) {
        boolean shown = showInterstitialInternal(activity, adUnitId, afterAd);
        if (!shown && afterAd != null) {
            afterAd.run();
        }
        return shown;
    }

    private static boolean showInterstitialInternal(Activity activity, String adUnitId, @Nullable Runnable afterAd) {
        long now = SystemClock.elapsedRealtime();
        InterstitialAd ad = interstitialAd;
        if (ad == null || !ad.isLoaded()) {
            if (ad != null) {
                interstitialAd = null;
            }
            preloadInterstitial(activity, adUnitId);
            return false;
        }
        if (!isIntervalElapsed(lastInterstitialAt, now, INTERSTITIAL_MIN_INTERVAL_MS) || activity.isFinishing()) {
            return false;
        }
        final Context app = activity.getApplicationContext();
        interstitialAd = null;
        final boolean[] opened = {false};
        final boolean[] done = {false};
        final Runnable finish = () -> {
            if (done[0]) {
                return;
            }
            done[0] = true;
            preloadInterstitial(app, adUnitId);
            if (afterAd != null) {
                afterAd.run();
            }
        };
        ad.setAdListener(new AdListener() {
            @Override
            public void onAdOpened() {
                opened[0] = true;
            }

            @Override
            public void onAdClosed() {
                finish.run();
            }

            @Override
            public void onAdFailed(int code) {
                finish.run();
            }
        });
        lastInterstitialAt = now;
        ad.show(activity);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!opened[0]) {
                finish.run();
            }
        }, SHOW_FALLBACK_MS);
        return true;
    }

    /** Frequency cap: true when at least {@code minIntervalMs} passed since {@code lastAt} (negative = never). */
    static boolean isIntervalElapsed(long lastAt, long now, long minIntervalMs) {
        return lastAt < 0 || now - lastAt >= minIntervalMs;
    }
}
