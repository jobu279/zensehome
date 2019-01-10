# zensehome
MQTT program for the ZenseHome system

Originally build for integration between OpenHAB and ZenseHome with the embedded MQTT server from OpenHAB (any MQTT server can be used).
Added functionality for listening on MQTT for topic: zense/execute/#
Publish with device ID ie. zense/execute/67673
Reacts to payloads containing:
- ON
- OFF
- 10-90 (value for fading)

ie. 
- zense/execute/67673 > ON
- zense/execute/67673 > OFF
- zense/execute/67673 > 50

To come functionality:
- Homie 3.0 support (topic will change)
- Update of status for devices
- Automatically import all devices in OpenHAB
