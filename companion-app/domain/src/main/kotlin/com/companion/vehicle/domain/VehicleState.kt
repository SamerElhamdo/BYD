package com.companion.vehicle.domain

enum class Gear { PARK, REVERSE, NEUTRAL, DRIVE }

data class VehicleState(
    val speedKmh: Int,
    val gear: Gear,
    val socPercent: Int,
    val driverDoorOpen: Boolean = false,
    val driverWindowPercent: Int = 0,
)
