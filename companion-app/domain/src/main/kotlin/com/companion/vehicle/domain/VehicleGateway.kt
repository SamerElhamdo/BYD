package com.companion.vehicle.domain

sealed interface ExecutionResult {
    data object Success : ExecutionResult
    data class RejectedByPolicy(val reason: String) : ExecutionResult
    data class BackendFailure(val message: String) : ExecutionResult
}

/**
 * Single seam between the UI/domain layer and the vehicle. Every write goes through
 * [SafetyPolicy] here — never bypassed by a specific backend — so BydAutoBackend,
 * AutoServiceBackend and FakeBackend all inherit the same interlocks for free.
 */
interface VehicleGateway {
    fun currentState(): VehicleState
    fun execute(command: Command): ExecutionResult
}

abstract class PolicyEnforcingGateway : VehicleGateway {
    final override fun execute(command: Command): ExecutionResult {
        val state = currentState()
        return when (val verdict = SafetyPolicy.check(command, state)) {
            is PolicyResult.Rejected -> ExecutionResult.RejectedByPolicy(verdict.reason)
            PolicyResult.Allowed -> performExecute(command)
        }
    }

    /** Backend-specific send. Called only after [SafetyPolicy] has already allowed [command]. */
    protected abstract fun performExecute(command: Command): ExecutionResult
}
