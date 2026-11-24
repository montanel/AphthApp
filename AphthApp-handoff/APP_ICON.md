# AphthApp — app icon ("Orbit", blue-grey ring, red dot)

The icon is a blue-grey ring with one red dot sitting on it, on a dark grey background. The dot is the app's
aphtha red: it stands for an aphtha and for a logged day. You can see it in
`design/AppIcon.dc.html`.

**Background tint:** `#1A1F29`, a dark blue-grey (Luca's pick, Q-008, 2026-11-24). Near-black
`#0A0A0B` disappeared on Luca's black wallpaper.

It's an adaptive icon. Everything sits inside the 66 dp safe circle, so no launcher mask (circle,
squircle or rounded square) cuts it off. At notification size it may read a little like a "refresh"
arrow; Luca keeps it for now and will judge it on his phone (`DECISIONS.md`, D-017).

**res/drawable/ic_launcher_background.xml**
```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#1A1F29"/>
</shape>
```

**res/drawable/ic_launcher_foreground.xml**
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <!-- ring: centre 54,54, radius 22, stroke 7 -->
    <path android:fillColor="#00000000" android:strokeColor="#8FA3C0" android:strokeWidth="7"
        android:pathData="M54,32 a22,22 0 1,1 0,44 a22,22 0 1,1 0,-44"/>
    <!-- gap around the dot, in the background colour -->
    <path android:fillColor="#1A1F29"
        android:pathData="M59,38.5 a10.5,10.5 0 1,0 21,0 a10.5,10.5 0 1,0 -21,0"/>
    <!-- dot: centre 69.5,38.5, radius 8, aphtha red -->
    <path android:fillColor="#FF453A"
        android:pathData="M61.5,38.5 a8,8 0 1,0 16,0 a8,8 0 1,0 -16,0"/>
</vector>
```

**res/mipmap-anydpi-v26/ic_launcher.xml** (also use it for `ic_launcher_round`)
```xml
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
    <monochrome android:drawable="@drawable/ic_launcher_monochrome"/>
</adaptive-icon>
```

**res/drawable/ic_launcher_monochrome.xml** (themed icons): the ring and the dot from the
foreground in `#FFFFFF`, without the gap path. The system recolours it anyway.

The notification's small icon (`ic_stat_*`) is the same ring and dot in white on transparent
(Android draws small icons in one colour; it may tint them).

**Launch screen (Android 12+, D-029):** black, with the ring and dot centred on a round background
in the same tint.
```xml
<style name="Theme.AphthApp.Starting" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">#000000</item>
    <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
    <item name="windowSplashScreenIconBackgroundColor">#1A1F29</item>
    <item name="postSplashScreenTheme">@style/Theme.AphthApp</item>
</style>
```
