package com.companion.vehicle.domain

import kotlin.test.Test
import kotlin.test.assertIs

private fun state(speedKmh: Int = 0, gear: Gear = Gear.PARK) =
    VehicleState(speedKmh = speedKmh, gear = gear, socPercent = 80)

class SafetyPolicyTest {

    @Test
    fun `rejects opening the window above 120 kmh`() {
        val result = SafetyPolicy.check(
            Command.SetWindowPercent(WindowTarget.DRIVER, 50),
            state(speedKmh = 130),
        )
        assertIs<PolicyResult.Rejected>(result)
    }

    @Test
    fun `allows opening the window at or below 120 kmh`() {
        val result = SafetyPolicy.check(
            Command.SetWindowPercent(WindowTarget.DRIVER, 50),
            state(speedKmh = 120),
        )
        assertIs<PolicyResult.Allowed>(result)
    }

    @Test
    fun `closing the window is always allowed even at high speed`() {
        val result = SafetyPolicy.check(
            Command.SetWindowPercent(WindowTarget.DRIVER, 0),
            state(speedKmh = 180),
        )
        assertIs<PolicyResult.Allowed>(result)
    }

    @Test
    fun `rejects opening the sunroof above 80 kmh`() {
        val result = SafetyPolicy.check(Command.SunroofOpen, state(speedKmh = 81))
        assertIs<PolicyResult.Rejected>(result)
    }

    @Test
    fun `allows opening the sunroof at or below 80 kmh`() {
        val result = SafetyPolicy.check(Command.SunroofOpen, state(speedKmh = 80))
        assertIs<PolicyResult.Allowed>(result)
    }

    @Test
    fun `rejects unlocking above 30 kmh`() {
        val result = SafetyPolicy.check(Command.UnlockDoors, state(speedKmh = 31))
        assertIs<PolicyResult.Rejected>(result)
    }

    @Test
    fun `allows unlocking at or below 30 kmh`() {
        val result = SafetyPolicy.check(Command.UnlockDoors, state(speedKmh = 30))
        assertIs<PolicyResult.Allowed>(result)
    }

    @Test
    fun `locking is always allowed regardless of speed`() {
        val result = SafetyPolicy.check(Command.LockDoors, state(speedKmh = 180))
        assertIs<PolicyResult.Allowed>(result)
    }

    @Test
    fun `rejects opening the front trunk outside park`() {
        val result = SafetyPolicy.check(Command.OpenFrontTrunk, state(gear = Gear.DRIVE))
        assertIs<PolicyResult.Rejected>(result)
    }

    @Test
    fun `allows opening the front trunk in park`() {
        val result = SafetyPolicy.check(Command.OpenFrontTrunk, state(gear = Gear.PARK))
        assertIs<PolicyResult.Allowed>(result)
    }

    @Test
    fun `rejects folding mirrors while moving`() {
        val result = SafetyPolicy.check(Command.FoldMirrors, state(speedKmh = 5))
        assertIs<PolicyResult.Rejected>(result)
    }

    @Test
    fun `climate control has no speed or gear restriction`() {
        val result = SafetyPolicy.check(Command.AcOn, state(speedKmh = 180, gear = Gear.DRIVE))
        assertIs<PolicyResult.Allowed>(result)
    }
}
