package com.companion.vehicle.domain

/** Pure in-memory backend for emulator/unit-test use — no adb, no car, ever. */
class FakeBackend(initialState: VehicleState) : PolicyEnforcingGateway() {
    private var state = initialState

    override fun currentState(): VehicleState = state

    /** UI-preview / test-only knob — not part of [VehicleGateway]. Real backends read speed live. */
    fun setSpeedForTesting(speedKmh: Int) {
        state = state.copy(speedKmh = speedKmh)
    }

    override fun performExecute(command: Command): ExecutionResult {
        state = applyCommand(state, command)
        return ExecutionResult.Success
    }

    private fun applyCommand(state: VehicleState, command: Command): VehicleState = when (command) {
        is Command.SetWindowPercent -> state.copy(driverWindowPercent = command.percent)
        Command.UnlockDoors -> state.copy(driverDoorOpen = state.driverDoorOpen)
        else -> state
    }
}
