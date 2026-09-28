import sys
import time
import random
import paho.mqtt.client as mqtt

# Ensure UTF-8 output on Windows consoles
if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")

BROKER = "broker.hivemq.com"
PORT = 1883
TOPIC_TEMP = "iotsimulator_myapp/temperature"
TOPIC_LED = "iotsimulator_myapp/led"

# Global state to track LED status
led_is_on = False
base_temp = 24.0

def get_live_temperature():
    global base_temp
    delta = random.uniform(-0.4, 0.4)
    base_temp = max(21.0, min(29.0, base_temp + delta))
    return f"{base_temp:.1f}"

def on_connect(client, userdata, flags, rc, properties=None):
    print("==================================================")
    print(" [+] Connected successfully to HiveMQ Broker!")
    print(f" [+] Broker: {BROKER}:{PORT}")
    print("==================================================")
    client.subscribe(TOPIC_LED)
    print(f" [*] Subscribed to topic: '{TOPIC_LED}'")
    print(" [*] Waiting for LED commands from your Android app...")
    print(" [*] When LED is ON  -> Streams live temperature data")
    print(" [*] When LED is OFF -> Sensor in Standby (no data sent)")
    print("--------------------------------------------------\n")

def on_message(client, userdata, msg):
    global led_is_on
    payload = msg.payload.decode("utf-8").strip().lower()
    print(f"\n[RECEIVED FROM APP] LED Command: '{payload.upper()}'")

    if payload == "on":
        led_is_on = True
        print("  -> ESP32 LED IS TURNED ON  (GPIO 26 -> HIGH)")
        print("  -> LIVE temperature streaming ACTIVE [Updates every 5s]")
        # Publish immediate reading
        simulated_temp = get_live_temperature()
        client.publish(TOPIC_TEMP, simulated_temp)
        print(f"  -> [PUBLISHED] Live Temperature: {simulated_temp} °C")
    elif payload == "off":
        led_is_on = False
        print("  -> ESP32 LED IS TURNED OFF (GPIO 26 -> LOW)")
        print("  -> Sensor in STANDBY (Temperature updates paused)")
    else:
        print(f"  -> Unknown command: {payload}")
    print()

def main():
    try:
        client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, "Python_ESP32_Simulator")
    except AttributeError:
        client = mqtt.Client("Python_ESP32_Simulator")

    client.on_connect = on_connect
    client.on_message = on_message

    print("Connecting to HiveMQ broker...")
    client.connect(BROKER, PORT, 60)
    client.loop_start()

    try:
        while True:
            if led_is_on:
                simulated_temp = get_live_temperature()
                client.publish(TOPIC_TEMP, simulated_temp)
                print(f"[LIVE] Temperature: {simulated_temp} °C  --> Topic: '{TOPIC_TEMP}'")
            else:
                print("[STANDBY] LED is OFF -> Sensor idle. Turn LED ON from app to stream live data.")

            time.sleep(5)
    except KeyboardInterrupt:
        print("\n\nStopping ESP32 simulator...")
        client.loop_stop()
        client.disconnect()
        print("Disconnected.")

if __name__ == "__main__":
    main()
