// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.RelativeLayout;

import com.mintegral.msdk.out.BannerAdListener;
import com.mintegral.msdk.out.BannerSize;
import com.mintegral.msdk.out.MTGBannerView;
import com.mintegral.msdk.out.SDKInitStatusListener;
import com.openmediation.sdk.mediation.AdapterErrorBuilder;
import com.openmediation.sdk.mediation.CustomBannerEvent;
import com.openmediation.sdk.mediation.MediationInfo;

import java.util.Map;

public class MintegralBanner extends CustomBannerEvent implements BannerAdListener {

    private static final String PAY_LOAD = "pay_load";
    private MTGBannerView mBannerView;

    @Override
    public void loadAd(final Activity activity, final Map<String, String> config) throws Throwable {
        super.loadAd(activity, config);
        if (!check(activity, config)) {
            return;
        }
        if (MintegralSingleTon.getInstance().isInit()) {
            loadBanner(activity, config);
        } else {
            MintegralSingleTon.getInstance().initSDK(activity, config.get("AppKey"), new SDKInitStatusListener() {
                @Override
                public void onInitSuccess() {
                    loadBanner(activity, config);
                }

                @Override
                public void onInitFail() {
                    if (!isDestroyed) {
                        onInsError(AdapterErrorBuilder.buildLoadError(
                                AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, "Mintegral Ad Load Failed"));
                    }
                }
            });
        }
    }

    private void loadBanner(Activity activity, Map<String, String> config) {
        String payload = "";
        if (config.containsKey(PAY_LOAD)) {
            payload = config.get(PAY_LOAD);
        }
        if (mBannerView != null) {
            if (TextUtils.isEmpty(payload)) {
                mBannerView.load();
            } else {
                mBannerView.loadFromBid(payload);
            }
            return;
        }
        mBannerView = new MTGBannerView(activity.getApplicationContext());
        BannerSize adSize = getAdSize(activity, config);
        mBannerView.init(adSize, "", mInstancesKey);
        mBannerView.setLayoutParams(new RelativeLayout.LayoutParams(dip2px(activity, adSize.getWidth()), dip2px(activity, adSize.getHeight())));
        mBannerView.setAllowShowCloseBtn(false);
        mBannerView.setRefreshTime(0);
        mBannerView.setBannerAdListener(this);
        if (TextUtils.isEmpty(payload)) {
            mBannerView.load();
        } else {
            mBannerView.loadFromBid(payload);
        }
    }

    private static int dip2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    @Override
    public int getMediation() {
        return MediationInfo.MEDIATION_ID_14;
    }

    @Override
    public void destroy(Activity activity) {
        if (mBannerView != null) {
            mBannerView.release();
            mBannerView = null;
        }
        isDestroyed = true;
    }

    private BannerSize getAdSize(Activity activity, Map<String, String> config) {
        String bannerDesc = getBannerDesc(config);
        switch (bannerDesc) {
            case DESC_LEADERBOARD:
                return new BannerSize(BannerSize.DEV_SET_TYPE, 728, 90);
            case DESC_RECTANGLE:
                return new BannerSize(BannerSize.MEDIUM_TYPE, 300, 250);
            case DESC_SMART:
                if (isLargeScreen(activity)) {
                    return new BannerSize(BannerSize.DEV_SET_TYPE, 728, 90);
                }
            default:
                return new BannerSize(BannerSize.STANDARD_TYPE, 320, 50);
        }
    }

    @Override
    public void onLoadFailed(String s) {
        if (!isDestroyed) {
            onInsError(AdapterErrorBuilder.buildLoadError(
                    AdapterErrorBuilder.AD_UNIT_BANNER, mAdapterName, s));
        }
    }

    @Override
    public void onLoadSuccessed() {
        if (!isDestroyed) {
            onInsReady(mBannerView);
        }
    }

    @Override
    public void onLogImpression() {

    }

    @Override
    public void onClick() {
        if (!isDestroyed) {
            onInsClicked();
        }
    }

    @Override
    public void onLeaveApp() {

    }

    @Override
    public void showFullScreen() {

    }

    @Override
    public void closeFullScreen() {

    }

    @Override
    public void onCloseBanner() {

    }
}