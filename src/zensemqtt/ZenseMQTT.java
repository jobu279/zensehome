package zensemqtt;
/* ZenseHome MQTT handler for the original ZenseHome Boks
 * Created by Jonas Büttcher, j@onas.dk
 * 
 * Actions in Zense API implemented (and tested):
 * setOn
 * setOff
 * setFade 
 */

import java.net.SocketException;
import java.util.Map;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class ZenseMQTT implements MqttCallback {
	static boolean debug = false; // set true to get extra system.out.println info
	MqttClient client;
	MqttClient subClient;
	static ZenseProperties properties = new ZenseProperties();
    static Map<String, String> configProperties = properties.getValues();
	static String mqttBrokerIP = configProperties.get("mqttIP"); //port is non-SSL, can be changed to SSL port 8883 (slower)
	static String zenseIP = configProperties.get("zenseIP"); //ip for the homebox
	static int zensePort = Integer.parseInt(configProperties.get("zensePort")); //standard port for the homebox
	static int zenseID = Integer.parseInt(configProperties.get("zenseID")); //homebox ID
	//private static final int MQTT_KEEPALIVE = 300;
	
	public ZenseMQTT() {
		
	}
	
	public static void main(String[] args) {
		Log.logging("Starting ZenseHome MQTT listener");
		new ZenseMQTT().listenToMQTT();
		//new ZenseMQTT().sendToMQTT();
	}
	
	public void listenToMQTT() {
	    MqttClient client;
	    
	    try {
	        client = new MqttClient("tcp://" + mqttBrokerIP, "ZenseMQTTReceiver2");
            MqttConnectOptions connOpts = new MqttConnectOptions();
            //connOpts.setKeepAliveInterval( MQTT_KEEPALIVE);
            //connOpts.setCleanSession(true);
            //connOpts.setAutomaticReconnect(true);
            //connOpts.setUserName( getMqttUsername() );
            //connOpts.setPassword( getMqttPassword().toCharArray() );
	        client.connect(connOpts);
	        client.setCallback(new MqttCallback() {
	            public void connectionLost(Throwable cause) {
	        		Log.logging("Error - probably do to forceful disconnect. New Sender Client ID might be needed, or close down original connection to mqtt: " + cause);
	            }
	
	            public void messageArrived(String topic,
	                    MqttMessage message)
	                            throws Exception {
	                System.out.println("-- New message --");
	        		Log.logging("New message with topic: " + topic + ", Message: " + message.toString());
	                String deviceIdString = topic.substring(14,19);
	                int deviceId = 0;
	                /*Pattern pattern = Pattern.compile("\\w+"); 	//setting up regex is a next version - more stable operations
	                Matcher matcher = pattern.matcher(topic);
	                while (matcher.find()) {
	                    System.out.println("group: " + matcher.group(0));
	                }
                   // deviceIdString = matcher.group(3);*/
	                if (deviceIdString.matches("[0-9]+")) { deviceId = Integer.parseInt(deviceIdString); } // Convert deviceIdString to Int if it contains numbers (deviceId in Zense is always Int)
	                
	                if (deviceIdString.equals("pingr")) {
	                	if (debug == true) {System.out.println("pingreq received");}
	                	sendToMQTT("zense/pingresp", "pingresp");
	                } else {
		                try {
			                if (Execute.openSocket(zenseIP, zensePort)) { // open connection to homebox
			                	if (debug == true) {System.out.println("Socket created");}
								if (Execute.commandLogin(zenseID) != 0) { // close socket, if login to homebox is not possible
									Log.logging("err","Error in CommandLogin - closing socket...");
									Execute.closeSocket();
									return;
								}
								
				                switch (message.toString()) {
					                case "ON":
					                	Execute.commandSetOn(deviceId);
					                	break;
					                case "OFF":
					                	Execute.commandSetOff(deviceId);
					                	break;
					                default:
					                	if (Integer.parseInt(message.toString())<10) {
				                			Execute.commandSetOff(deviceId);
				                			return;
					                	} else if (Integer.parseInt(message.toString())>90) {
				                			Execute.commandSetOn(deviceId); 
				                			return;
					                	} else {
						                	Execute.commandSetFade(deviceId, message.toString());
						                	break; 
					                	}
				                }
			                	if (debug == true) {System.out.println("Sending Command Logout");}
				                Execute.commandLogout();
			                	if (debug == true) {System.out.println("Closing socket");}
				                Execute.closeSocket();
				                System.out.println("-- Success --");
			                } else {
			                	throw new SocketException();  //Connection could not be made to socket - dump program (version 1 feature - should be handled better)
			                }
		                } catch (SocketException e) {
		                	Log.logging("err","Error in socket creation: " + e);
		                    e.printStackTrace();
		                }
	                }
	            }
	
	            public void deliveryComplete(IMqttDeliveryToken token) { System.out.println("-- Delivered --"); }
	        });
	        client.subscribe("zense/execute/#");
	
	    } catch (MqttException e) {
	    	Log.logging("err","General exception thrown in receiver: " + e);
	        e.printStackTrace();
	    }
	}
	
	public void sendToMQTT(String topic, String payload) {
	    try {
	      	String publisherId = UUID.randomUUID().toString();
	    	publisherId = "Sending-" + System.currentTimeMillis() + ": " + publisherId;
	        client = new MqttClient("tcp://" + mqttBrokerIP, publisherId);
	        client.connect();
	        client.setCallback(this);
	        //client.subscribe("zense/execute/37475");
	        MqttMessage message = new MqttMessage();
	        message.setPayload(payload
	                .getBytes());
	        message.setQos(0);
	        client.publish(topic, message);
	    } catch (MqttException e) {
	    	Log.logging("err","General exception thrown in sender: " + e);
	        e.printStackTrace();
	    }
	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
	}
}
