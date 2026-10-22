package org.ratson.cordova.admob;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.company.game.AdsHelper;
import com.company.game.R;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;

/**
 * Minimal native side of cordova-plugin-admob-free, rewritten for Google Mobile Ads SDK 25 + UMP.
 * The JS API (admob.banner / admob.interstitial) is unchanged; ad unit IDs come from the build
 * type resources (Google test IDs in debug builds), not from JavaScript.
 */
public class AdMob extends CordovaPlugin {
    private FrameLayout bannerContainer;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        final Activity activity = cordova.getActivity();
        switch (action) {
            case "setOptions":
                callbackContext.success();
                return true;
            case "createBannerView":
                activity.runOnUiThread(() -> AdsHelper.gatherConsent(activity, () -> {
                    ensureBannerContainer(activity);
                    AdsHelper.loadBanner(activity, bannerContainer, activity.getString(R.string.banner_id));
                    fire("admob.banner.events.LOAD");
                }));
                callbackContext.success();
                return true;
            case "showAd": {
                final boolean show = args.optBoolean(0, true);
                activity.runOnUiThread(() -> {
                    if (bannerContainer != null) {
                        bannerContainer.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
                callbackContext.success();
                return true;
            }
            case "destroyBannerView":
                activity.runOnUiThread(() -> {
                    if (bannerContainer != null) {
                        bannerContainer.removeAllViews();
                    }
                });
                callbackContext.success();
                return true;
            case "createInterstitialView":
            case "requestInterstitialAd":
            case "prepareInterstitial":
                activity.runOnUiThread(() -> AdsHelper.gatherConsent(activity,
                        () -> AdsHelper.preloadInterstitial(activity, activity.getString(R.string.interstitial_id))));
                callbackContext.success();
                return true;
            case "showInterstitialAd":
                activity.runOnUiThread(() -> {
                    boolean shown = AdsHelper.showInterstitialIfReady(activity,
                            activity.getString(R.string.interstitial_id), () -> fire("admob.interstitial.events.CLOSE"));
                    if (shown) {
                        fire("admob.interstitial.events.OPEN");
                    }
                });
                callbackContext.success();
                return true;
            case "isInterstitialReady":
                callbackContext.success(0);
                return true;
            case "createRewardVideo":
            case "showRewardVideo":
            case "isRewardVideoReady":
                callbackContext.error("Reward video is not supported");
                return true;
            default:
                return false;
        }
    }

    /** Puts the WebView and a bottom banner slot into a vertical layout, so the banner never covers the game. */
    private void ensureBannerContainer(Activity activity) {
        if (bannerContainer != null) {
            return;
        }
        View web = webView.getView();
        ViewGroup parent = (ViewGroup) web.getParent();
        bannerContainer = new FrameLayout(activity);
        if (parent == null) {
            return;
        }
        int index = parent.indexOfChild(web);
        ViewGroup.LayoutParams lp = web.getLayoutParams();
        parent.removeView(web);
        LinearLayout column = new LinearLayout(activity);
        column.setOrientation(LinearLayout.VERTICAL);
        column.addView(web, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        column.addView(bannerContainer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        parent.addView(column, index, lp != null ? lp : new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        web.requestFocus();
    }

    private void fire(String event) {
        cordova.getActivity().runOnUiThread(() ->
                webView.loadUrl("javascript:cordova.fireDocumentEvent('" + event + "');"));
    }
}
