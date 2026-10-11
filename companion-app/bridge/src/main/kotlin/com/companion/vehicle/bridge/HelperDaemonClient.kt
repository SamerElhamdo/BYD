package com.companion.vehicle.bridge

import android.os.IBinder
import android.os.Parcel

/** App-process side: finds [HelperDaemon] via `ServiceManager.getService` (hidden API, reflection
 *  only — same reasoning as [HelperDaemonMain.addServiceByReflection]) and calls it. */
class HelperDaemonClient {

    private fun binderOrNull(): IBinder? = try {
        val serviceManagerClass = Class.forName("android.os.ServiceManager")
        val getService = serviceManagerClass.getMethod("getService", String::class.java)
        getService.invoke(null, HelperProtocol.SERVICE_NAME) as? IBinder
    } catch (e: ReflectiveOperationException) {
        null
    }

    fun isAlive(): Boolean {
        val binder = binderOrNull() ?: return false
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            binder.transact(HelperProtocol.TX_PING, data, reply, 0)
        } catch (e: Exception) {
            false
        } finally {
            data.recycle(); reply.recycle()
        }
    }

    fun exec(request: ExecRequest): ExecReply? {
        val binder = binderOrNull() ?: return null
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            writeExecRequest(data, request)
            if (!binder.transact(HelperProtocol.TX_EXEC, data, reply, 0)) return null
            readExecReply(reply)
        } catch (e: Exception) {
            null
        } finally {
            data.recycle(); reply.recycle()
        }
    }
}
