package dev.ambitionsoftware.tymeboxed.service

import java.util.Locale

/**
 * Maps installable app packages to canonical hostnames they usually represent when
 * Android opens them via App Links instead of a browser. [AppBlockerAccessibilityService]
 * uses this so domain rules still apply when the user never reaches Chrome et al.
 *
 * Also infers `brand.com` from common `com.<brand>.android` / `com.<brand>.app` patterns so
 * first-party apps are covered without listing every package ID.
 */
object DomainLinkedNativeApps {

    /**
     * Normalized hostnames to test with [ActiveBlockingState.shouldBlockDomain] for this package.
     */
    fun hostsForNativeDomainCheck(packageName: String): List<String> {
        val out = LinkedHashSet<String>()
        canonicalHostsForPackage(packageName).forEach { out.add(it) }
        inferComBrandHosts(packageName).forEach { out.add(it) }
        return out.toList()
    }

    /**
     * Known mappings only (no `com.brand.android` inference). Used for cheap checks.
     */
    fun canonicalHostsForPackage(packageName: String): List<String> =
        domainsByPackage[packageName].orEmpty()

    private fun inferComBrandHosts(pkg: String): List<String> {
        val parts = pkg.lowercase(Locale.ROOT).split('.')
        val out = ArrayList<String>(2)

        if (parts.size == 2 && parts[0] == "com") {
            val brand = parts[1]
            if (brand.length >= 4 && brand !in INFER_BRAND_BLOCKLIST) {
                DomainBlocking.normalize("$brand.com")?.let { out.add(it) }
            }
            return out
        }

        if (parts.size >= 3 && parts[0] == "com") {
            val brand = parts[1]
            val third = parts[2]
            if (brand.length >= 4 && brand !in INFER_BRAND_BLOCKLIST &&
                third in setOf("android", "app", "apps")
            ) {
                DomainBlocking.normalize("$brand.com")?.let { out.add(it) }
            }
        }

        return out
    }

    private val INFER_BRAND_BLOCKLIST = setOf(
        "android",
        "google",
        "samsung",
        "huawei",
        "xiaomi",
        "oppo",
        "vivo",
        "realme",
        "oneplus",
        "nokia",
        "motorola",
        "microsoft",
        "mozilla",
        "example",
        "test",
        "demo",
        "sample",
        "lge",
        "sony",
        "htc",
        "asus",
        "lenovo",
    )

    private val domainsByPackage: Map<String, List<String>> = run {
        val raw = listOf(
            "com.instagram.android" to listOf("instagram.com", "cdninstagram.com", "instagr.am"),
            "com.facebook.katana" to listOf("facebook.com", "m.facebook.com", "fb.com", "fb.watch"),
            "com.facebook.lite" to listOf("facebook.com", "m.facebook.com", "fb.com"),
            "com.twitter.android" to listOf("twitter.com", "x.com", "mobile.twitter.com", "t.co"),
            "com.zhiliaoapp.musically" to listOf("tiktok.com", "www.tiktok.com", "vm.tiktok.com"),
            "com.ss.android.ugc.trill" to listOf("tiktok.com", "www.tiktok.com"),
            "com.ss.android.tt.creator" to listOf("tiktok.com"),
            "com.reddit.frontpage" to listOf("reddit.com", "www.reddit.com", "old.reddit.com"),
            "com.google.android.youtube" to listOf("youtube.com", "youtu.be", "m.youtube.com", "www.youtube.com"),
            "com.snapchat.android" to listOf("snapchat.com", "www.snapchat.com"),
            "com.pinterest" to listOf("pinterest.com", "www.pinterest.com"),
            "com.linkedin.android" to listOf("linkedin.com", "www.linkedin.com"),
            "com.spotify.music" to listOf("spotify.com", "open.spotify.com", "www.spotify.com"),
            "com.netflix.mediaclient" to listOf("netflix.com", "www.netflix.com"),
            "tv.twitch.android.app" to listOf("twitch.tv", "www.twitch.tv"),
            "com.discord" to listOf("discord.com", "discordapp.com"),
            "org.telegram.messenger" to listOf("telegram.org", "t.me", "web.telegram.org"),
            "org.telegram.plus" to listOf("telegram.org", "t.me"),
            "com.whatsapp" to listOf("whatsapp.com", "web.whatsapp.com", "api.whatsapp.com"),
            "com.zhiliaoapp.musically.go" to listOf("tiktok.com"),
            "com.amazon.mShop.android.shopping" to listOf("amazon.com", "www.amazon.com", "smile.amazon.com"),
            "in.amazon.mShop.android.shopping" to listOf("amazon.in", "www.amazon.in"),
            "com.ebay.mobile" to listOf("ebay.com", "www.ebay.com"),
            "com.alibaba.aliexpresshd" to listOf("aliexpress.com", "www.aliexpress.com"),
            "com.medium.reader" to listOf("medium.com", "www.medium.com"),
            "com.quora.android" to listOf("quora.com", "www.quora.com"),
            "com.tumblr" to listOf("tumblr.com", "www.tumblr.com"),
            "com.github.android" to listOf("github.com", "www.github.com"),
            "com.bereal.ft" to listOf("bere.al", "bereal.com"),
            "com.instagram.barcelona" to listOf("threads.net", "instagram.com"),
            "com.instagram.threadsapp" to listOf("threads.net", "instagram.com"),
        )
        raw
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, lists) ->
                lists.flatten()
                    .mapNotNull { DomainBlocking.normalize(it) }
                    .distinct()
            }
    }
}
