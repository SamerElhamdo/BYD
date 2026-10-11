package com.companion.vehicle.bridge

import android.content.Context
import java.lang.reflect.InvocationTargetException

sealed interface BydSdkCallResult {
    data class IntValue(val value: Int) : BydSdkCallResult
    data object Unsupported : BydSdkCallResult
    data class Denied(val cause: Throwable) : BydSdkCallResult
    data class Failed(val cause: Throwable) : BydSdkCallResult
}

/**
 * Calls a method on an already-installed android.hardware.bydauto.* device singleton purely by
 * reflection — this process never imports those classes, so nothing from BYD/AISpeech is copied
 * into this repo (byd-seal05-research-plan.md section 6.2). It only works when this JVM's
 * classpath was launched with an existing BYD system app on it (e.g. `app_process -cp
 * /system/priv-app/AutoVoice/AutoVoice.apk`) so the classes are resolvable at runtime — see
 * section 8.6. UNTESTED against a real car as of 2026-10-11; `getDeviceInstance` is the first
 * thing to validate once reconnected (does `enforceCallingOrSelfPermission` pass for our UID?).
 */
object BydSdkReflection {

    fun getDeviceInstance(deviceClassName: String, context: Context): Result<Any> = runCatching {
        val clazz = Class.forName(deviceClassName)
        val getInstance = clazz.getMethod("getInstance", Context::class.java)
        getInstance.invoke(null, context) ?: error("getInstance returned null")
    }

    /** Calls `methodName(int, int, ...)` on [instance], [args].size deciding the overload picked. */
    fun callIntMethod(instance: Any, methodName: String, vararg args: Int): BydSdkCallResult {
        val paramTypes = Array<Class<*>>(args.size) { Int::class.javaPrimitiveType!! }
        return try {
            val method = instance.javaClass.getMethod(methodName, *paramTypes)
            val boxed = args.map { it as Any }.toTypedArray()
            when (val result = method.invoke(instance, *boxed)) {
                is Int -> BydSdkCallResult.IntValue(result)
                else -> BydSdkCallResult.Failed(IllegalStateException("non-int return: $result"))
            }
        } catch (e: NoSuchMethodException) {
            BydSdkCallResult.Unsupported
        } catch (e: InvocationTargetException) {
            val cause = e.targetException
            if (cause is SecurityException) BydSdkCallResult.Denied(cause) else BydSdkCallResult.Failed(cause)
        } catch (e: ReflectiveOperationException) {
            BydSdkCallResult.Failed(e)
        }
    }
}
