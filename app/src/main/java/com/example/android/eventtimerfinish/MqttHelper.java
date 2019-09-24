package com.example.android.eventtimerfinish;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttHelper {

    private MqttAndroidClient mqttAndroidClient;

    final private String serverUri = "tcp://soldier.cloudmqtt.com:16424";

    final private String clientId = "StartApp";
    final private String subscriptionTopic = "eventTimer";

    final private String username = "yrzlekwy";
    final private String key = "pBVkVlJy413x";

    public MqttHelper(Context context) {
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended(){
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable){

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Mqtt", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken){

            }
        });
    }

    public void setCallBack(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    void connect(final String msg){

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(key.toCharArray());

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    boolean connect  = mqttAndroidClient.isConnected();
                    try {
                        publishMessage(msg);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                    Log.w("Mqtt", "Successfully connected to: " + serverUri + "Connected? " + connect);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " + serverUri + exception.toString());
                }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    private void subscribeToTopic() {
        try{
            mqttAndroidClient.subscribe(subscriptionTopic, 2, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt", "Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt","Subscribed fail!");
                }
            });
        } catch (MqttException ex) {
            System.err.println("Exception while subscribing");
            ex.printStackTrace();
        }
    }

    private void publishMessage(String msg) throws MqttException {
        if (mqttAndroidClient.isConnected()) {
            MqttMessage message = new MqttMessage(msg.getBytes());
            message.setQos(1);
            mqttAndroidClient.publish(subscriptionTopic, message);
            mqttAndroidClient.disconnect();
        } else
            Log.w("Mqtt","Publish Failed!");
    }
}
