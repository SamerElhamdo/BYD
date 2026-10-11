package com.companion.vehicle.bridge

import android.content.Context
import android.os.Binder
import android.os.Looper
import android.os.Parcel
import android.util.Log

/**
 * Binder stub run by [HelperDaemonMain], standing in for whatever real Context a BYD device
 * singleton needs. UNTESTED: `app_process` gives us a bare JVM, not a running Activity —
 * `android.app.ActivityThread.systemMain()`/`currentApplication()` tricks (the usual way shell
 * daemons get a Context, see BYDMate section 8.4) still need to be validated on this car.
 */
class HelperDaemon(private val context: Context) : Binder() {

    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        return when (code) {
            HelperProtocol.TX_PING -> {
                reply?.writeNoException()
                true
            }
            HelperProtocol.TX_EXEC -> {
                data.enforceInterface(HelperProtocol.DESCRIPTOR)
                val request = readExecRequest(data)
                val result = execute(request)
                reply?.writeNoException()
                reply?.let { writeExecReply(it, result) }
                true
            }
            else -> super.onTransact(code, data, reply, flags)
        }
    }

    private fun execute(request: ExecRequest): ExecReply {
        val instance = BydSdkReflection.getDeviceInstance(request.deviceClassName, context)
            .getOrElse { return ExecReply(HelperProtocol.STATUS_FAILED) }
        return when (val result = BydSdkReflection.callIntMethod(instance, request.methodName, *request.args)) {
            is BydSdkCallResult.IntValue -> ExecReply(HelperProtocol.STATUS_OK, result.value)
            BydSdkCallResult.Unsupported -> ExecReply(HelperProtocol.STATUS_UNSUPPORTED)
            is BydSdkCallResult.Denied -> {
                Log.w(TAG, "denied: ${request.deviceClassName}.${request.methodName}: ${result.cause}")
                ExecReply(HelperProtocol.STATUS_DENIED)
            }
            is BydSdkCallResult.Failed -> {
                Log.w(TAG, "failed: ${request.deviceClassName}.${request.methodName}: ${result.cause}")
                ExecReply(HelperProtocol.STATUS_FAILED)
            }
        }
    }

    companion object {
        private const val TAG = "HelperDaemon"
    }
}

fun readExecRequest(data: Parcel): ExecRequest {
    val deviceClassName = data.readString().orEmpty()
    val methodName = data.readString().orEmpty()
    val args = IntArray(data.readInt()) { data.readInt() }
    return ExecRequest(deviceClassName, methodName, args)
}

fun writeExecRequest(data: Parcel, request: ExecRequest) {
    data.writeInterfaceToken(HelperProtocol.DESCRIPTOR)
    data.writeString(request.deviceClassName)
    data.writeString(request.methodName)
    data.writeInt(request.args.size)
    request.args.forEach { data.writeInt(it) }
}

fun readExecReply(reply: Parcel): ExecReply {
    reply.readException()
    val status = reply.readInt()
    val value = if (status == HelperProtocol.STATUS_OK) reply.readInt() else 0
    return ExecReply(status, value)
}

fun writeExecReply(reply: Parcel, execReply: ExecReply) {
    reply.writeInt(execReply.status)
    if (execReply.status == HelperProtocol.STATUS_OK) reply.writeInt(execReply.value)
}

/**
 * `app_process` entry point — NOT an Android component, a plain `main()`. UNTESTED deployment
 * path (section 8.4): push this class's dex + launch via
 * `app_process -cp <dex> /system/bin --nice-name=companion_vehicle_helper
 *   com.companion.vehicle.bridge.HelperDaemonMain`, run from `adb shell` (shell uid), with the
 * classpath also including the installed BYD app (e.g. AutoVoice.apk) so
 * `android.hardware.bydauto.*` resolves — see [BydSdkReflection].
 */
object HelperDaemonMain {
    @JvmStatic
    fun main(args: Array<String>) {
        Looper.prepareMainLooper()
        val context = currentApplicationContextOrThrow()
        val daemon = HelperDaemon(context)
        addServiceByReflection(HelperProtocol.SERVICE_NAME, daemon)
        Looper.loop()
    }

    /**
     * `ActivityThread.systemMain()` / `currentActivityThread().getSystemContext()` is the usual
     * shell-daemon trick for a Context with no running app (BYDMate's approach, studied not
     * copied). UNVALIDATED here — first thing to prove on a reconnect.
     */
    private fun currentApplicationContextOrThrow(): Context {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val systemMain = activityThreadClass.getMethod("systemMain")
        val activityThread = systemMain.invoke(null)
        val getSystemContext = activityThreadClass.getMethod("getSystemContext")
        return getSystemContext.invoke(activityThread) as Context
    }

    private fun addServiceByReflection(name: String, service: Binder) {
        val serviceManagerClass = Class.forName("android.os.ServiceManager")
        val addService = serviceManagerClass.getMethod(
            "addService", String::class.java, android.os.IBinder::class.java,
        )
        addService.invoke(null, name, service)
    }
}
