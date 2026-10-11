package com.companion.vehicle.bridge

import com.companion.vehicle.domain.Command
import com.companion.vehicle.domain.ExecutionResult
import com.companion.vehicle.domain.Gear
import com.companion.vehicle.domain.PolicyEnforcingGateway
import com.companion.vehicle.domain.VehicleState

/**
 * Real backend talking to [HelperDaemon] on the car. UNTESTED end to end (section 8.6) — the
 * wiring below is split deliberately into "confirmed enough to wire" and "needs a live read
 * before wiring" so nothing here is a guessed feature ID pretending to be validated.
 *
 * Door lock state (LOCK=2 / UNLOCK=1) comes directly from BYDAutoDoorLockDevice's own public
 * constants (not inferred), so it's wired. Window/mirror percent commands need the getter's
 * per-window area-code parameter confirmed live first (the one percent we've actually observed,
 * section 8.3, came from a postEvent broadcast field, which is not provably the same value space
 * as getWindowOpenPercent(int)'s argument) — those stay BackendFailure until then.
 */
class AutoServiceBackend(private val client: HelperDaemonClient) : PolicyEnforcingGateway() {

    override fun currentState(): VehicleState =
        // TODO(live validation): wire speed/gear/soc/window reads once the daemon is confirmed
        // alive and enforceCallingOrSelfPermission passes for the daemon's uid (section 8.6).
        VehicleState(speedKmh = 0, gear = Gear.PARK, socPercent = 0)

    override fun performExecute(command: Command): ExecutionResult = when (command) {
        Command.LockDoors -> execSetting(BydAutoCatalog.Setting.SET_DOOR_LOCK, DOOR_LOCK_STATE_LOCK)
        Command.UnlockDoors -> execSetting(BydAutoCatalog.Setting.SET_DOOR_LOCK, DOOR_LOCK_STATE_UNLOCK)
        else -> ExecutionResult.BackendFailure(
            "${command::class.simpleName} not wired yet — needs a live parameter check (section 8.6)",
        )
    }

    private fun execSetting(methodName: String, vararg args: Int): ExecutionResult {
        val reply = client.exec(ExecRequest(BydAutoCatalog.SETTING_DEVICE, methodName, args)) ?: return ExecutionResult.BackendFailure("daemon unreachable")
        return when (reply.status) {
            HelperProtocol.STATUS_OK -> ExecutionResult.Success
            HelperProtocol.STATUS_DENIED -> ExecutionResult.BackendFailure("permission denied by BYDAuto SDK")
            HelperProtocol.STATUS_UNSUPPORTED -> ExecutionResult.BackendFailure("method not found on this firmware")
            else -> ExecutionResult.BackendFailure("autoservice call failed")
        }
    }

    private companion object {
        // android.hardware.bydauto.doorlock.BYDAutoDoorLockDevice.DOOR_LOCK_STATE_LOCK/UNLOCK —
        // observed class constants, section 8.6. Assumed (not yet proven) to be the same enum
        // BYDAutoSettingDevice.setDoorLock(int) expects.
        const val DOOR_LOCK_STATE_UNLOCK = 1
        const val DOOR_LOCK_STATE_LOCK = 2
    }
}
