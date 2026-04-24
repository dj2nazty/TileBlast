package com.dj2nazty.tileblast.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.concurrent.atomic.AtomicBoolean

object AdManager {
    private const val TAG = "AdManager"
    private val initialized = AtomicBoolean(false)

    private var interstitial: InterstitialAd? = null
    private var rewarded: RewardedAd? = null

    fun init(context: Context) {
        if (!initialized.compareAndSet(false, true)) return
        MobileAds.initialize(context) { Log.i(TAG, "MobileAds init complete") }
        preloadInterstitial(context)
        preloadRewarded(context)
    }

    // ── Interstitial ─────────────────────────────────
    fun preloadInterstitial(context: Context) {
        if (interstitial != null) return
        InterstitialAd.load(
            context,
            AdIds.INTERSTITIAL,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial loaded")
                    interstitial = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Interstitial failed: ${error.message}")
                    interstitial = null
                }
            },
        )
    }

    fun showInterstitial(activity: Activity, onFinished: () -> Unit) {
        val ad = interstitial
        if (ad == null) {
            onFinished()
            preloadInterstitial(activity)
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                preloadInterstitial(activity)
                onFinished()
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                interstitial = null
                preloadInterstitial(activity)
                onFinished()
            }
        }
        ad.show(activity)
    }

    // ── Rewarded ─────────────────────────────────────
    fun preloadRewarded(context: Context) {
        if (rewarded != null) return
        RewardedAd.load(
            context,
            AdIds.REWARDED,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded loaded")
                    rewarded = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Rewarded failed: ${error.message}")
                    rewarded = null
                }
            },
        )
    }

    /**
     * Shows a rewarded ad. Calls [onReward] iff the user earned the reward
     * (watched the full ad). Calls [onFinished] once, after close/fail.
     *
     * If no ad is cached yet we grant the reward anyway so gameplay isn't blocked
     * — safe because this is a free, nice-to-have reward, not a paywall.
     */
    fun showRewarded(
        activity: Activity,
        onReward: () -> Unit,
        onFinished: () -> Unit = {},
    ) {
        val ad = rewarded
        if (ad == null) {
            onReward()
            onFinished()
            preloadRewarded(activity)
            return
        }
        var rewardGranted = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewarded = null
                preloadRewarded(activity)
                onFinished()
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                rewarded = null
                preloadRewarded(activity)
                if (!rewardGranted) onReward()
                onFinished()
            }
        }
        ad.show(activity) {
            rewardGranted = true
            onReward()
        }
    }
}
