package com.op.aod.enhance.hook

import android.content.res.AssetManager
import android.util.Log
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.io.ByteArrayInputStream

/**
 * AOD レイアウト差し替え（SystemUI プロセス）。
 *
 * AOD の見た目は com.oplus.aod の assets/aod/aod_layout_*.xml で決まり、
 * UIEngine (com.oplus.uiengine) が AssetManager.open() で読み込む。
 * ここでは読み込み時に「時刻 + 日付を小さく1行」にした XML へ差し替える。
 *
 * ログ確認: su -c "logcat -d | grep AOD_Enhance"
 */
internal object LayoutOverrideHook {

    private const val TAG = "AOD_Enhance"

    // ---- 調整用の定数（dp）----
    /** 時刻・日付の文字サイズ */
    private const val TEXT_SIZE = 17
    /** 時刻と日付の間隔 */
    private const val GAP = 8
    /** 画面上端からの距離 */
    private const val TOP_MARGIN = 56

    /** 差し替え対象。標準のデジタル時計（TimeView + DateView 構成）。 */
    private val TARGETS = setOf(
        "aod/aod_layout_0010000.xml",
    )

    /** logcat が読めない端末向けに、LSPosed のログ画面にも出す。 */
    private fun log(msg: String, t: Throwable? = null) {
        runCatching { Log.i(TAG, msg) }
        runCatching { XposedBridge.log("$TAG: $msg") }
        if (t != null) runCatching { XposedBridge.log(t) }
    }

    fun YukiBaseHooker.hookLayoutOverride() {
        log("hookLayoutOverride() called")
        try {
            XposedHelpers.findAndHookMethod(
                AssetManager::class.java,
                "open",
                String::class.java,
                Integer.TYPE,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val name = param.args[0] as? String ?: return
                        if (name.startsWith("aod/aod_layout_")) {
                            log("asset open: $name")
                        }
                        if (name !in TARGETS) return
                        try {
                            param.result = ByteArrayInputStream(buildLayout().toByteArray(Charsets.UTF_8))
                            log("layout override applied: $name")
                        } catch (e: Throwable) {
                            log("layout override failed: $name", e)
                        }
                    }
                }
            )
            log("layout override hook registered")
        } catch (e: Throwable) {
            log("layout override hook registration FAILED", e)
        }
    }

    private fun buildLayout(): String = LAYOUT
        .replace("{TEXT_SIZE}", TEXT_SIZE.toString())
        .replace("{GAP}", GAP.toString())
        .replace("{TOP_MARGIN}", TOP_MARGIN.toString())

    private val LAYOUT = """<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<GroupLayout
    gravity="top|center_horizontal"
    groupLayout="vertical"
    isRootGradient="true"
    height="match_parent"
    width="match_parent">

    <GroupLayout
        gravity="center"
        groupLayout="vertical"
        isRootGradient="true"
        height="wrap_content"
        width="wrap_content"
        marginTop="{TOP_MARGIN}">

        <GroupLayout
            gravity="center"
            groupLayout="horizontal"
            isRootGradient="true"
            height="wrap_content"
            width="wrap_content">

            <TimeView
                alignment="center"
                direction="horizontal"
                width="wrap_content"
                height="wrap_content"
                linearGradientStart="lt"
                linearGradientEnd="rb"
                linearGradientColors="#FFFFFF|#FFFFFF"
                textSize="{TEXT_SIZE}"
                textWidget="330"
                defaultTextType="horizontal"
                supportGradientColors="true"
                supportCustomize="true"/>

            <DateView
                alignment="center"
                direction="horizontal"
                width="wrap_content"
                height="wrap_content"
                textSize="{TEXT_SIZE}"
                marginStart="{GAP}"
                typefacePath="normal"
                linearGradientStart="lt"
                linearGradientEnd="rb"
                supportVisiableDate="true"
                supportGradientColors="true"
                linearGradientColors="#FFFFFF|#FFFFFF"
                visibility="visiable"
                zoomX="1.0"
                zoomY="1.0"/>
        </GroupLayout>

        <GroupLayout
            groupLayout="vertical"
            isRootGradient="true"
            height="wrap_content"
            width="wrap_content"
            gravity="center"
            marginStart="0"
            marginTop="3">

            <LunarView
                alignment="center"
                direction="horizontal"
                width="wrap_content"
                height="wrap_content"
                textSize="14"
                marginTop="2"
                typefacePath="normal"
                linearGradientStart="lt"
                linearGradientEnd="rb"
                supportVisiableLunar="true"
                supportGradientColors="true"
                linearGradientColors="#FFFFFF|#FFFFFF"
                visibility="gone"
                zoomX="1.0"
                zoomY="1.0"/>

            <AodUvTextView
                alignment="center"
                direction="horizontal"
                width="wrap_content"
                height="wrap_content"
                textSize="14"
                marginTop="2"
                typefacePath="normal"
                linearGradientStart="lt"
                linearGradientEnd="rb"
                supportVisibleUV="true"
                supportGradientColors="true"
                linearGradientColors="#FFFFFF|#FFFFFF"
                visibility="gone"
                zoomX="1.0"
                zoomY="1.0"/>

            <AodMediaView
                alignment="center"
                direction="horizontal"
                width="wrap_content"
                height="wrap_content"
                maxWidth="200"
                marginTop="2"
                textSize="14"
                linearGradientStart="lt"
                linearGradientEnd="rb"
                supportVisiableMedia="true"
                supportGradientColors="true"
                linearGradientColors="#FFFFFF|#FFFFFF"
                visibility="visiable"
                zoomX="1.0"
                zoomY="1.0"/>

            <BatteryView
                marginTop="2"
                width="wrap_content"
                height="wrap_content"
                textSize="14"
                showPercent="true"
                typefacePath="normal"
                direction="horizontal"
                linearGradientStart="lt"
                linearGradientEnd="rb"
                supportGradientColors="true"
                supportVisiableBattery="true"
                linearGradientColors="#FFFFFF|#FFFFFF"
                visibility="visiable"
                zoomX="1.0"
                zoomY="1.0"/>
        </GroupLayout>

        <GroupLayout
            height="wrap_content"
            width="wrap_content"
            groupLayout="horizontal"
            gravity="center"
            marginStart="0"
            marginTop="20">
            <NotificationView
                width="wrap_content"
                height="wrap_content"
                linearGradientStart="lt"
                linearGradientEnd="rb"
                supportVisiableNotification="true"
                supportGradientColors="true"
                supportDirection="true"
                linearGradientColors="#FFFFFF|#FFFFFF"
                visibility="visiable"
                zoomX="1.0"
                zoomY="1.0"/>
        </GroupLayout>
    </GroupLayout>
</GroupLayout>
"""
}
