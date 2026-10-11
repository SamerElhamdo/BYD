package com.companion.vehicle.bridge

/**
 * Class/method names observed by static analysis of AutoVoice.apk (byd-seal05-research-plan.md
 * section 8.6) — names and signatures only, no BYD/AISpeech code. Every entry here is a reflection
 * target for [BydSdkReflection], never an import.
 */
object BydAutoCatalog {
    const val BODYWORK_DEVICE = "android.hardware.bydauto.bodywork.BYDAutoBodyworkDevice"
    const val SETTING_DEVICE = "android.hardware.bydauto.setting.BYDAutoSettingDevice"
    const val AC_DEVICE = "android.hardware.bydauto.ac.BYDAutoAcDevice"

    object Bodywork {
        const val SET_ALL_WINDOW_STATE = "setAllWindowState" // (frontLeft, frontRight, rearLeft, rearRight)
        const val GET_WINDOW_OPEN_PERCENT = "getWindowOpenPercent" // (window)
        const val GET_WINDOW_STATE = "getWindowState" // (window)
        const val GET_POWER_LEVEL = "getPowerLevel" // () — matches the live logcat read, section 8.3
        const val SET_MOON_ROOF_STATE = "setMoonRoofState" // (state)
        const val SET_SUNSHADE_STATE = "setSunshadeState" // (state)
    }

    object Setting {
        const val GET_DOOR_LOCK = "getDoorLock"
        const val SET_DOOR_LOCK = "setDoorLock" // (state)
        const val GET_REAR_VIEW_MIRROR_FLIP = "getRearViewMirrorFlip"
        const val SET_REAR_VIEW_MIRROR_FLIP = "setRearViewMirrorFlip" // (state)
        const val GET_SEAT_HEATING_STATE = "getSeatHeatingState" // (seat)
        const val SET_SEAT_HEATING_STATE = "setSeatHeatingState" // (seat, level)
        const val GET_SEAT_VENTILATING_STATE = "getSeatVentilatingState" // (seat)
        const val SET_SEAT_VENTILATING_STATE = "setSeatVentilatingState" // (seat, level)
    }

    object Ac {
        const val START = "start" // (zone)
        const val STOP = "stop" // (zone)
        const val SET_AC_WIND_LEVEL = "setAcWindLevel" // (zone, level)
        const val SET_AC_WIND_MODE = "setAcWindMode" // (zone, mode)
    }
}
