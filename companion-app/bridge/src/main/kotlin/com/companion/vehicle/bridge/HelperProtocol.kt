package com.companion.vehicle.bridge

/**
 * Our own Binder wire protocol between the app process and [HelperDaemon] — unrelated to
 * autoservice's own transaction numbers (which we never reverse-engineered, section 8.3) and
 * unrelated to BYDMate's protocol (studied for architecture only, never copied, section 8.4).
 */
object HelperProtocol {
    const val SERVICE_NAME = "companion_vehicle_helper"
    const val DESCRIPTOR = "com.companion.vehicle.bridge.IHelper"

    const val TX_PING = android.os.IBinder.FIRST_CALL_TRANSACTION
    const val TX_EXEC = android.os.IBinder.FIRST_CALL_TRANSACTION + 1

    const val STATUS_OK = 0
    const val STATUS_UNSUPPORTED = 1
    const val STATUS_DENIED = 2
    const val STATUS_FAILED = 3
}

data class ExecRequest(val deviceClassName: String, val methodName: String, val args: IntArray)

data class ExecReply(val status: Int, val value: Int = 0)
