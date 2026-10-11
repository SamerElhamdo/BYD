package com.companion.vehicle.domain

enum class WindowTarget { DRIVER, PASSENGER, REAR_LEFT, REAR_RIGHT, ALL }
enum class Seat { DRIVER, PASSENGER }

/**
 * Every command the app can ever issue. Driving, braking, steering, gear, ADAS/DiPilot,
 * suspension and HV battery have no representation here on purpose — the type system itself
 * makes them inexpressible, not just policy-rejected.
 */
sealed interface Command {
    data object AcOn : Command
    data object AcOff : Command
    data class SetTemperature(val celsius: Int) : Command
    data class SetFanSpeed(val level: Int) : Command

    data class SetSeatHeat(val seat: Seat, val level: Int) : Command
    data class SetSeatVent(val seat: Seat, val level: Int) : Command

    data class SetWindowPercent(val target: WindowTarget, val percent: Int) : Command

    data object SunroofOpen : Command
    data object SunroofClose : Command
    data object SunroofVent : Command

    data object LockDoors : Command
    data object UnlockDoors : Command

    data object OpenFrontTrunk : Command

    data object FoldMirrors : Command
    data object UnfoldMirrors : Command
}
