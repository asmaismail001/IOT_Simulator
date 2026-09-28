package com.example.iotsimulator

import android.os.Handler
import android.os.Looper
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import java.nio.charset.StandardCharsets

private const val TOPIC_TEMPERATURE = "iotsimulator_myapp/temperature"
private const val TOPIC_LED = "iotsimulator_myapp/led"

object MqttService {

    private val mainHandler = Handler(Looper.getMainLooper())

    private val client: Mqtt3AsyncClient = Mqtt3Client.builder()
        .identifier("android-client-" + System.currentTimeMillis())
        .serverHost("broker.hivemq.com")
        .serverPort(1883)
        .buildAsync()

    private var isConnected = false

    fun connect(onConnected: () -> Unit, onError: (String) -> Unit) {
        client.connect()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    mainHandler.post { onError("Connection failed: ${throwable.message}") }
                } else {
                    isConnected = true
                    mainHandler.post { onConnected() }
                }
            }
    }

    fun subscribeToTemperature(onMessage: (String) -> Unit) {
        client.subscribeWith()
            .topicFilter(TOPIC_TEMPERATURE)
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback { publish ->
                val payload = publish.payloadAsBytes
                val message = String(payload, StandardCharsets.UTF_8)
                mainHandler.post { onMessage(message) }
            }
            .send()
    }

    fun publishLedCommand(state: String) {
        client.publishWith()
            .topic(TOPIC_LED)
            .qos(MqttQos.AT_LEAST_ONCE)
            .payload(state.toByteArray(StandardCharsets.UTF_8))
            .send()
    }

    fun disconnect() {
        if (isConnected) {
            client.disconnect()
            isConnected = false
        }
    }
}