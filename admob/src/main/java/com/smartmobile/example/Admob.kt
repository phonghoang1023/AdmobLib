package com.smartmobile.example

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.smartmobile.admob.databinding.AdUnifiedBinding
import com.smartmobile.admob.databinding.ShimmerBannerBinding
import com.smartmobile.admob.databinding.ShimmerNativeBinding


object Admob {
    const val TAG = "Admob"
    private var mIsTesting = true
    private var mIsEnableAd = true
    private var mIsShowing = false
    private var mTimeStamp = 0L
    private var mLimitTime = 0L
    private var mLastTimeShowedInter = 0L
    private var mInterstitialAd: InterstitialAd? = null

    fun init(
        context: Context,
        isTesting: Boolean = true,
        isEnableAd: Boolean = true,
        limitSecond: Int = 30
    ) {
        mIsTesting = isTesting
        mIsEnableAd = isEnableAd
        mLimitTime = limitSecond * 1000L
        MobileAds.initialize(context.applicationContext) { }
    }

    fun showAdBanner(activity: Activity, adId: String, viewGroup: FrameLayout) {
        if (!mIsEnableAd) return

        val adSize = getAdSize(activity, viewGroup)
        viewGroup.minimumHeight = adSize.height.toPx.toInt()

        val shimmer = ShimmerFrameLayout(activity)
        shimmer.addView(ShimmerBannerBinding.inflate(activity.layoutInflater).root)
        shimmer.startShimmer()

        val bannerId = if (mIsTesting) TestAdId.BANNER_ID else adId
        val adView = AdView(activity)
        adView.setAdSize(adSize)
        adView.adUnitId = bannerId

        viewGroup.removeAllViews()
        viewGroup.addView(shimmer)
        viewGroup.addView(adView)

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                shimmer.stopShimmer()
                shimmer.gone()
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                shimmer.stopShimmer()
                shimmer.gone()
            }
        }

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    fun showAdNative(activity: Activity, adId: String, viewGroup: FrameLayout) {
        val adsView = AdUnifiedBinding.inflate(activity.layoutInflater)
        val nativeId = if (mIsTesting) TestAdId.NATIVE_ID else adId

        val adLoader = AdLoader.Builder(activity, nativeId).forNativeAd {
            populateNativeAdView(it, adsView)
            viewGroup.removeAllViews()
            viewGroup.addView(adsView.root)
        }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                viewGroup.removeAllViews()
            }
        }).withNativeAdOptions(NativeAdOptions.Builder().build()).build()

        adLoader.loadAd(AdRequest.Builder().build())

        val shimmer = ShimmerFrameLayout(activity)
        shimmer.addView(ShimmerNativeBinding.inflate(activity.layoutInflater).root)
        shimmer.startShimmer()

        viewGroup.removeAllViews()
        viewGroup.addView(shimmer)
    }

    fun loadInter(activity: Activity, adId: String) {
        val adRequest = AdRequest.Builder().build()
        val interId = if (mIsTesting) TestAdId.INTER_ID else adId
        InterstitialAd.load(activity, interId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                loge(adError.toString())
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                log("Admob onAdLoaded")
                mInterstitialAd = interstitialAd
            }
        })
    }

    fun showAdmobInter(activity: Activity, onFinished: () -> Unit) {
        if (mIsShowing) return

        if (System.currentTimeMillis() - mTimeStamp < 1000) {
            mTimeStamp = System.currentTimeMillis()
            return
        }

        if (System.currentTimeMillis() - mLastTimeShowedInter < mLimitTime) {
            onFinished()
            return
        }

        if (mIsEnableAd && mInterstitialAd != null) {
            initCallback(onFinished)
            mInterstitialAd!!.show(activity)
            mIsShowing = true
            mTimeStamp = System.currentTimeMillis()
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
            mIsShowing = false
            onFinished()
        }
    }

    private fun initCallback(onFinished: () -> Unit) {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
//                override fun onAdClicked() {
//                    Log.d(TAG, "Ad was clicked.")
//                }

//                override fun onAdImpression() {
//                    Log.d(TAG, "Ad recorded an impression.")
//                }

//                override fun onAdShowedFullScreenContent() {
//                    Log.d(TAG, "Ad showed fullscreen content.")
//                }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed fullscreen content.")
                mInterstitialAd = null
                mIsShowing = false
                onFinished()
                mLastTimeShowedInter = System.currentTimeMillis()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad failed to show fullscreen content.")
                mInterstitialAd = null
                mIsShowing = false
                onFinished()
            }

        }
    }

    private fun getAdSize(activity: Activity, viewGroup: ViewGroup): AdSize {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = viewGroup.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    /**
     * Populates a [NativeAdView] object with data from a given [NativeAd].
     *
     * @param nativeAd the object containing the ad's assets
     * @param unifiedAdBinding the binding object of the layout that has NativeAdView as the root view
     */
    fun populateNativeAdView(nativeAd: NativeAd, unifiedAdBinding: AdUnifiedBinding) {
        val nativeAdView = unifiedAdBinding.root

        // Set the media view.
        nativeAdView.mediaView = unifiedAdBinding.adMedia

        // Set other ad assets.
        nativeAdView.headlineView = unifiedAdBinding.adHeadline
        nativeAdView.bodyView = unifiedAdBinding.adBody
        nativeAdView.callToActionView = unifiedAdBinding.adCallToAction
        nativeAdView.iconView = unifiedAdBinding.adAppIcon
        nativeAdView.priceView = unifiedAdBinding.adPrice
        nativeAdView.starRatingView = unifiedAdBinding.adStars
        nativeAdView.storeView = unifiedAdBinding.adStore
        nativeAdView.advertiserView = unifiedAdBinding.adAdvertiser

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        unifiedAdBinding.adHeadline.text = nativeAd.headline
        nativeAd.mediaContent?.let { unifiedAdBinding.adMedia.setMediaContent(it) }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            unifiedAdBinding.adBody.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adBody.visibility = View.VISIBLE
            unifiedAdBinding.adBody.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            unifiedAdBinding.adCallToAction.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adCallToAction.visibility = View.VISIBLE
            unifiedAdBinding.adCallToAction.text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            unifiedAdBinding.adAppIcon.visibility = View.GONE
        } else {
            unifiedAdBinding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
            unifiedAdBinding.adAppIcon.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            unifiedAdBinding.adPrice.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adPrice.visibility = View.VISIBLE
            unifiedAdBinding.adPrice.text = nativeAd.price
        }

        if (nativeAd.store == null) {
            unifiedAdBinding.adStore.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adStore.visibility = View.VISIBLE
            unifiedAdBinding.adStore.text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            unifiedAdBinding.adStars.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adStars.rating = nativeAd.starRating!!.toFloat()
            unifiedAdBinding.adStars.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            unifiedAdBinding.adAdvertiser.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adAdvertiser.text = nativeAd.advertiser
            unifiedAdBinding.adAdvertiser.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        nativeAdView.setNativeAd(nativeAd)

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        val vc = nativeAd.mediaContent?.videoController

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc != null && vc.hasVideoContent()) {
            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
                override fun onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
//                        mainActivityBinding.refreshButton.isEnabled = true
//                        mainActivityBinding.videostatusText.text = "Video status: Video playback has ended."
                    super.onVideoEnd()
                }
            }
        } else {
//            mainActivityBinding.videostatusText.text = "Video status: Ad does not contain a video asset."
//            mainActivityBinding.refreshButton.isEnabled = true
        }
    }

}