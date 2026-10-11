package com.companion.vehicle.domain

sealed interface PolicyResult {
    data object Allowed : PolicyResult
    data class Rejected(val reason: String) : PolicyResult
}

/**
 * Speed/gear interlocks from byd-seal05-research-plan.md section 6.3. Every limit here mirrors
 * a documented community precedent (BYDMate), not a guess. Closing/locking is always allowed —
 * only opening/unlocking is gated, so a reflex safety action can never be refused.
 */
object SafetyPolicy {
    private const val WINDOW_OPEN_MAX_SPEED_KMH = 120
    private const val SUNROOF_OPEN_MAX_SPEED_KMH = 80
    private const val UNLOCK_MAX_SPEED_KMH = 30

    fun check(command: Command, state: VehicleState): PolicyResult = when (command) {
        is Command.SetWindowPercent -> checkWindow(command, state)
        Command.SunroofOpen, Command.SunroofVent -> checkSunroof(state)
        Command.UnlockDoors -> checkUnlock(state)
        Command.OpenFrontTrunk -> checkParkedOnly(state, "فتح الصندوق الأمامي")
        Command.FoldMirrors, Command.UnfoldMirrors -> checkStoppedOnly(state, "طي/فرد المرايا")
        else -> PolicyResult.Allowed
    }

    private fun checkWindow(command: Command.SetWindowPercent, state: VehicleState): PolicyResult {
        require(command.percent in 0..100) { "window percent must be 0..100" }
        // Closing (percent 0) must never be blocked — it's the safety-recovery direction.
        if (command.percent == 0) return PolicyResult.Allowed
        return if (state.speedKmh > WINDOW_OPEN_MAX_SPEED_KMH) {
            PolicyResult.Rejected("فتح النافذة ممنوع فوق $WINDOW_OPEN_MAX_SPEED_KMH كم/س")
        } else {
            PolicyResult.Allowed
        }
    }

    private fun checkSunroof(state: VehicleState): PolicyResult =
        if (state.speedKmh > SUNROOF_OPEN_MAX_SPEED_KMH) {
            PolicyResult.Rejected("فتح فتحة السقف ممنوع فوق $SUNROOF_OPEN_MAX_SPEED_KMH كم/س")
        } else {
            PolicyResult.Allowed
        }

    private fun checkUnlock(state: VehicleState): PolicyResult =
        if (state.speedKmh > UNLOCK_MAX_SPEED_KMH) {
            PolicyResult.Rejected("فتح القفل ممنوع فوق $UNLOCK_MAX_SPEED_KMH كم/س")
        } else {
            PolicyResult.Allowed
        }

    private fun checkParkedOnly(state: VehicleState, actionLabel: String): PolicyResult =
        if (state.gear != Gear.PARK) {
            PolicyResult.Rejected("$actionLabel مسموح فقط والسيارة بوضع P")
        } else {
            PolicyResult.Allowed
        }

    private fun checkStoppedOnly(state: VehicleState, actionLabel: String): PolicyResult =
        if (state.speedKmh != 0) {
            PolicyResult.Rejected("$actionLabel مسموح فقط والسيارة متوقفة")
        } else {
            PolicyResult.Allowed
        }
}
