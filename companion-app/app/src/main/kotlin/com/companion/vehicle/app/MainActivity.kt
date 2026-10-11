package com.companion.vehicle.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.companion.vehicle.domain.Command
import com.companion.vehicle.domain.ExecutionResult
import com.companion.vehicle.domain.FakeBackend
import com.companion.vehicle.domain.Gear
import com.companion.vehicle.domain.VehicleState
import com.companion.vehicle.domain.WindowTarget

/**
 * Stage 1 preview shell: forces Arabic/RTL regardless of system locale (plan section 4.1 —
 * never depend on the head unit's own locale) and exercises SafetyPolicy live against the
 * FakeBackend, with no vehicle involved.
 */
class MainActivity : ComponentActivity() {
    private val gateway = FakeBackend(
        VehicleState(speedKmh = 0, gear = Gear.PARK, socPercent = 80),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        CompanionScreen(gateway)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompanionScreen(gateway: FakeBackend) {
    var speed by remember { mutableIntStateOf(0) }
    var windowPercent by remember { mutableIntStateOf(gateway.currentState().driverWindowPercent) }
    var lastResult by remember { mutableStateOf("—") }

    fun runCommand(command: Command) {
        gateway.setSpeedForTesting(speed)
        when (val result = gateway.execute(command)) {
            ExecutionResult.Success -> {
                lastResult = "تم التنفيذ"
                windowPercent = gateway.currentState().driverWindowPercent
            }
            is ExecutionResult.RejectedByPolicy -> lastResult = "رُفض: ${result.reason}"
            is ExecutionResult.BackendFailure -> lastResult = "فشل: ${result.message}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("رفيقي — معاينة المرحلة 1", style = MaterialTheme.typography.headlineSmall)

        Text("السرعة المحاكاة: $speed كم/س")
        Slider(
            value = speed.toFloat(),
            onValueChange = { speed = it.toInt() },
            valueRange = 0f..180f,
            modifier = Modifier.fillMaxWidth(),
        )

        Text("نسبة فتح نافذة السائق: $windowPercent%")

        Button(onClick = { runCommand(Command.SetWindowPercent(WindowTarget.DRIVER, 50)) }) {
            Text("افتح النافذة للنصف (50%)")
        }
        Button(onClick = { runCommand(Command.SetWindowPercent(WindowTarget.DRIVER, 0)) }) {
            Text("أغلق النافذة")
        }

        Text("النتيجة: $lastResult", style = MaterialTheme.typography.bodyLarge)
    }
}
