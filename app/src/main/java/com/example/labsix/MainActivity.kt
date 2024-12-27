package com.example.labsix

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlin.math.sqrt
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Task()
        }
    }
}

data class ElectroDevice(
    val name: String,
    val efficiency: Double, // "ККД"
    val powerFactor: Double, // "Коефіцієнт потужності"
    val voltage: Double, // "Напруга"
    val devicesCount: Int, // "Кількість пристроїв"
    val power: Int, // "Потужність"
    val utilizationFactor: Double, // "Коефіцієнт використання"
    val reactivePowerFactor: Double, // "Коефіцієнт реактивної потужності"

    var multiplication: Int = 0,
    var calculatedCurrent: Double = 0.0
)

@Composable
fun Task() {
    val scrollState = rememberScrollState()
    var showResults by remember { mutableStateOf(false) }

    var electroDevicesList by remember { mutableStateOf(listOf<ElectroDevice>()) }
    var bigElectroDevicesList by remember { mutableStateOf(listOf<ElectroDevice>()) }
    var name by remember { mutableStateOf("") }
    var efficiency by remember { mutableStateOf("") }
    var powerFactor by remember { mutableStateOf("") }
    var voltage by remember { mutableStateOf("") }
    var devicesCount by remember { mutableStateOf("") }
    var power by remember { mutableStateOf("") }
    var utilizationFactor by remember { mutableStateOf("") }
    var reactivePowerFactor by remember { mutableStateOf("") }

    var groupUtilizationRate by remember { mutableStateOf(0.0) }
    var effectiveAmountED by remember { mutableStateOf(0.0) }
    var fullPower by remember { mutableStateOf(0.0) }
    var estimatedGroupCurrent by remember { mutableStateOf(0.0) }
    var kP by remember { mutableStateOf(0.0) }
    var kP2 by remember { mutableStateOf(0.0) }
    var calculatedActiveLoad by remember { mutableStateOf(0.0) }
    var calculatedReactiveLoad by remember { mutableStateOf(0.0) }
    var workshopUtilizationRates by remember { mutableStateOf(0.0) }
    var effectiveNumberEDWorkshop by remember { mutableStateOf(0.0) }
    var fullPowerBuses by remember { mutableStateOf(0.0) }
    var calculatedActiveLoadBuses by remember { mutableStateOf(0.0) }
    var calculatedReactiveLoadBuses by remember { mutableStateOf(0.0) }
    var estimatedGroupCurrentBuses by remember { mutableStateOf(0.0) }

    fun clearFields() {
        name = ""
        efficiency = ""
        powerFactor = ""
        voltage = ""
        devicesCount = ""
        power = ""
        utilizationFactor = ""
        reactivePowerFactor = ""
    }

    fun addToSHR() {
        val electroDevice = ElectroDevice(
            name = name,
            efficiency = efficiency.toDoubleOrNull() ?: 0.0, // ККД
            powerFactor = powerFactor.toDoubleOrNull() ?: 0.0, // Коефіцієнт потужності
            voltage = voltage.toDoubleOrNull() ?: 0.0, // Напруга
            devicesCount = devicesCount.toIntOrNull() ?: 0, // Кількість пристроїв
            power = power.toIntOrNull() ?: 0, // Потужність
            utilizationFactor = utilizationFactor.toDoubleOrNull() ?: 0.0, // Коефіцієнт використання
            reactivePowerFactor = reactivePowerFactor.toDoubleOrNull() ?: 0.0 // Коефіцієнт реактивної потужності
        )
        electroDevicesList = electroDevicesList.toMutableList().apply {
            add(electroDevice)
        }
        clearFields()
    }

    fun calculate() {
        // добуток і розрахунковий струм для кожного ЕП
        for (device in electroDevicesList) {
            device.multiplication = device.power * device.devicesCount
            device.calculatedCurrent = device.multiplication.toDouble() / (sqrt(3.0) * device.voltage * device.powerFactor * device.efficiency)
        }
        var nPnKv = 0.0
        var nPn = 0.0
        var nPn2 = 0.0

        var kPkBkN = 0.0
        for (device in electroDevicesList) {
            nPnKv += device.power * device.devicesCount * device.utilizationFactor
            nPn += device.power * device.devicesCount
            nPn2 += device.devicesCount * device.power.toDouble().pow(2.0)
            kPkBkN += device.devicesCount * device.power * device.utilizationFactor * device.reactivePowerFactor
        }
        // груповий коефіцієнт використання:
        groupUtilizationRate = nPnKv / nPn
        // ефективна кількість ЕП:
        effectiveAmountED = nPn.toDouble().pow(2.0) / nPn2
        kP = 1.25 // табличне значення
        // розрахункове активне навантаження:
        calculatedActiveLoad = kP * nPnKv
        // розрахункове реактивне навантаження:
        calculatedReactiveLoad =  1.0 * kPkBkN
        // повна потужність:
        fullPower = sqrt((calculatedActiveLoad.pow(2.0) + calculatedReactiveLoad.pow(2.0)))
        // розрахунковий груповий струм ШР1:
        estimatedGroupCurrent = calculatedActiveLoad / electroDevicesList[0].voltage
        // Розрахункові навантаження цеху:
        workshopUtilizationRates = 752.0 / 2330.0
        effectiveNumberEDWorkshop = (2330.0).pow(2.0) / 96388
        kP2 = 0.7 // табличне значення
        calculatedActiveLoadBuses = kP2 * 752
        calculatedReactiveLoadBuses = kP2 * 657
        fullPowerBuses = sqrt((calculatedActiveLoadBuses.pow(2.0) + calculatedReactiveLoadBuses.pow(2.0)))
        estimatedGroupCurrentBuses = calculatedActiveLoadBuses / electroDevicesList[0].voltage
    }

    Column (modifier = Modifier.verticalScroll(scrollState)) {
        Spacer(modifier = Modifier.size(25.dp))
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Назва ЕП") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        TextField(
            value = efficiency,
            onValueChange = { efficiency = it },
            label = { Text("Номінальне значення коефіцієнта корисної дії ЕП") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        TextField(
            value = powerFactor,
            onValueChange = { powerFactor = it },
            label = { Text("Коефіцієнт потужності навантаження") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        TextField(
            value = voltage,
            onValueChange = { voltage = it },
            label = { Text("Напруга навантаження") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        TextField(
            value = devicesCount,
            onValueChange = { devicesCount = it },
            label = { Text("Кількість ЕП") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        TextField(
            value = power,
            onValueChange = { power = it },
            label = { Text("Номінальна потужність ЕП") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        TextField(
            value = utilizationFactor,
            onValueChange = { utilizationFactor = it },
            label = { Text("Коефіцієнт використання") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        TextField(
            value = reactivePowerFactor,
            onValueChange = { reactivePowerFactor = it },
            label = { Text("Коефіцієнт реактивної потужності") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { addToSHR() }) {
                Text("Додати ЕП на ШР")
            }
            Button(onClick = {
                calculate()
                showResults = true
            }) {
                Text("Розрахувати")
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

        if (showResults) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "Для заданого складу ЕП та їх характеристик цехової мережі силове навантаження\n" +
                                "становитиме:" +
                                "Груповий коефіцієнт використання для ШР1=ШР2=ШР3: $groupUtilizationRate \n" +
                                "Ефективна кількість ЕП для ШР1=ШР2=ШР3: $effectiveAmountED \n" +
                                "Розрахунковий коефіцієнт активної потужності для ШР1=ШР2=ШР3: $kP \n" +
                                "Розрахункове активне навантаження для ШР1=ШР2=ШР3: $calculatedActiveLoad кВт.\n" +
                                "Розрахункове реактивне навантаження для ШР1=ШР2=ШР3: $calculatedReactiveLoad квар.\n" +
                                "Повна потужність для ШР1=ШР2=ШР3: $fullPower кВ*А.\n" +
                                "Розрахунковий груповий струм для ШР1=ШР2=ШР3: $estimatedGroupCurrent A. \n" +
                                "Коефіцієнти використання цеху в цілому: $workshopUtilizationRates \n" +
                                "Ефективна кількість ЕП цеху в цілому: $effectiveNumberEDWorkshop \n" +
                                "Розрахунковий коефіцієнт активної потужності цеху в цілому: $kP2 \n" +
                                "Розрахункове активне навантаження на шинах 0,38 кВ ТП: $calculatedActiveLoadBuses кВт. \n" +
                                "Розрахункове реактивне навантаження на шинах 0,38 кВ ТП: $calculatedReactiveLoadBuses квар. \n" +
                                "Повна потужність на шинах 0,38 кВ ТП: $fullPowerBuses кВ*А.\n" +
                                "Розрахунковий груповий струм на шинах 0,38 кВ ТП: $estimatedGroupCurrentBuses А.\n"
                    )
            }
        }
    }
}
