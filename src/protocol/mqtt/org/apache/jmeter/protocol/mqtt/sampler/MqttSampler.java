package org.apache.jmeter.protocol.mqtt.sampler;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.eclipse.kura.core.cloud.CloudPayloadProtoBufEncoderImpl;
import org.eclipse.kura.message.KuraPayload;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.eclipse.kura.core.cloud.CloudPayloadJsonDecoder.buildFromByteArray;

/**
 * Sampler that runs request toward Mqtt broker.
 */
public class MqttSampler extends AbstractSampler implements ThreadListener, Interruptible {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(MqttSampler.class);

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
            Arrays.asList(
                    "org.apache.jmeter.config.gui.LoginConfigGui",
                    "org.apache.jmeter.protocol.mqtt.config.gui.MqttConfigGui",
                    "org.apache.jmeter.config.gui.SimpleConfigGui"
            ));

    /**
     * Decision of sampler being run first time.
     */
    private transient boolean firstSample;

    public static final String SERVER = "MqttSampler.server"; // $NON-NLS-1$

    public static final String PORT = "MqttSampler.port"; // $NON-NLS-1$

    public static final String PORT_DEFAULT = "1883"; // $NON-NLS-1$

    public static final String QOS = "MqttSampler.qos"; // $NON-NLS-1$

    public static final int QOS_DEFAULT = 0;

    public static final String RETAINED = "MqttSampler.cleanSession"; // $NON-NLS-1$

    public static final boolean RETAINED_DEFAULT = false;

    public static final String CLIENT_ID = "MqttSampler.clientId"; // $NON-NLS-1$

    public static final String TOPIC = "MqttSampler.topic"; // $NON-NLS-1$

    public static final String MESSAGE_BODY = "MqttSampler.messageBody"; // $NON-NLS-1$

    public static final String RE_USE_CONNECTION = "MqttSampler.reUseConnection"; //$NON-NLS-1$

    public static final boolean RE_USE_CONNECTION_DEFAULT = true;

    public static final String CLOSE_CONNECTION = "MqttSampler.closeConnection"; //$NON-NLS-1$

    public static final boolean CLOSE_CONNECTION_DEFAULT = false;

    private static final String MQTTKEY = "MQTT"; //$NON-NLS-1$ key for HashMap

    private static final String ERRKEY = "ERR"; //$NON-NLS-1$ key for HashMap

    /**
     * The cache of MQTT Connections
     */
    // KEY = MQTTKEY or ERRKEY, Entry= Socket or String
    private static final ThreadLocal<Map<String, Object>> tp =
            ThreadLocal.withInitial(HashMap::new);

    private transient volatile MqttClient currentClient; // used for interrupting the sampler

    public MqttSampler() {
        log.debug("Created " + this);
    }

    private String getError() {
        Map<String, Object> cp = tp.get();
        return (String) cp.get(ERRKEY);
    }

    private MqttClient getMqttClient(String mqttKey) {
        Map<String, Object> cp = tp.get();
        MqttClient client = null;
        if (isReUseConnection()) {
            client = (MqttClient) cp.get(mqttKey);
            if (client != null) {
                log.debug(this + " Reusing mqtt client " + client); //$NON-NLS-1$
            }
        }
        if (client == null) {
            // Not in cache, so create new one and cache it
            try {
                closeMqttClient(mqttKey);
                MemoryPersistence persistence = new MemoryPersistence();
                client = new MqttClient(getServer() + ":" + getPort(),
                        getClientId(), persistence);
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                connOpts.setUserName(getUsername());
                connOpts.setPassword(getPassword().toCharArray());
                client.connect(connOpts);
                if(log.isDebugEnabled()) {
                    log.debug("Created new mqtt client connection " + client); //$NON-NLS-1$
                }
                cp.put(mqttKey, client);
            } catch (MqttException mqtte) {
                log.warn("Could not create mqtt client for " + getLabel(), mqtte); //$NON-NLS-1$
                cp.put(ERRKEY, mqtte.toString());

                return null;
            }
        }

        return client;
    }

    /**
     * @return String mqtt key in cache Map
     */
    private String getMqttKey() {
        return MQTTKEY + "#" + getServer() + "#" + getPort() + "#" + getUsername() + "#" + getPassword() + "#" + getClientId();
    }

    public String getUsername() {
        return getPropertyAsString(ConfigTestElement.USERNAME);
    }

    public String getPassword() {
        return getPropertyAsString(ConfigTestElement.PASSWORD);
    }

    public void setServer(String newServer) {
        this.setProperty(SERVER, newServer);
    }

    public String getServer() {
        return getPropertyAsString(SERVER);
    }

    public void setPort(String newPort) {
        this.setProperty(PORT, newPort, ""); // $NON-NLS-1$
    }

    public String getPort() {
        return getPropertyAsString(PORT, PORT_DEFAULT); // $NON-NLS-1$
    }

    public int getPortAsInt() {
        return getPropertyAsInt(PORT, Integer.valueOf(PORT_DEFAULT));
    }

    public void setQOS(String newQos) {
        this.setProperty(QOS, newQos);
    }

    public String getQOS() {
        return getPropertyAsString(QOS, "0");
    }

    public int getQOSAsInt() {
        return getPropertyAsInt(QOS, 0);
    }

    public void setRetained(String newRetained) {
        this.setProperty(RETAINED, newRetained, "");
    }

    public boolean isRetained() {
        return getPropertyAsBoolean(RETAINED, RETAINED_DEFAULT);
    }

    public void setClientId(String newClientId) {
        this.setProperty(CLIENT_ID, newClientId, "");
    }

    public String getClientId() {
        return getPropertyAsString(CLIENT_ID);
    }

    public void setTopic(String newTopic) {
        this.setProperty(TOPIC, newTopic, "");
    }

    public String getTopic() {
        return getPropertyAsString(TOPIC, "");
    }

    public void setMessageBody(String newMessageBody) {
        this.setProperty(MESSAGE_BODY, newMessageBody, "");
    }

    public String getMessageBody() {
        return this.getPropertyAsString(MESSAGE_BODY, "");
    }

    public void setReUseConnection(String newReUseConnection) {
        this.setProperty(RE_USE_CONNECTION, newReUseConnection, "");
    }

    public boolean isReUseConnection() {
        return this.getPropertyAsBoolean(RE_USE_CONNECTION, RE_USE_CONNECTION_DEFAULT);
    }

    public void setCloseConnection(String newCloseConnection) {
        this.setProperty(CLOSE_CONNECTION, newCloseConnection, "");
    }

    public boolean isCloseConnection() {
        return this.getPropertyAsBoolean(CLOSE_CONNECTION, CLOSE_CONNECTION_DEFAULT);
    }

    /**
     * Returns a formatted string label describing this sampler Example output:
     * tcp://iot.eclipse.org:1883
     *
     * @return a formatted string label describing this sampler
     */
    public String getLabel() {
        StringBuilder sb = new StringBuilder();
        sb.append(getServer());
        sb.append(getPort());

        return sb.toString();
    }

    @Override
    public SampleResult sample(Entry e) {

        if (firstSample) { // Do stuff we cannot do as part of threadStarted()
            initSampling();
            firstSample = false;
        }
        final boolean reUseConnection = isReUseConnection();
        final boolean closeConnection = isCloseConnection();
        String mqttKey = getMqttKey();
        SampleResult res = new SampleResult();
        boolean isSuccessful = false;
        res.setSuccessful(false); // failure is best option
        res.setSampleLabel(getName());
        StringBuilder sb = new StringBuilder();
        sb.append("Host: ").append(getServer()); // $NON-NLS-1$
        sb.append(" Port: ").append(getPort()); // $NON-NLS-1$
        sb.append("\n"); // $NON-NLS-1$
        sb.append("Reuse: ").append(reUseConnection); // $NON-NLS-1$
        sb.append(" Close: ").append(closeConnection); // $NON-NLS-1$
        sb.append("]"); // $NON-NLS-1$
        res.setSamplerData(sb.toString());
        res.sampleStart();
        try {
            MqttClient mqttClient;
            try {
                mqttClient = getMqttClient(mqttKey);
            } finally {
                res.connectEnd();
            }
            if (mqttClient == null) {
                res.setResponseCode("500");
                res.setResponseMessage(getError());
            } else {
                currentClient = mqttClient;
            }
            String content = getMessageBody();
            byte[] jsonContent = content.getBytes();
            KuraPayload kuraPayload = buildFromByteArray(jsonContent);
            CloudPayloadProtoBufEncoderImpl kuraPayloadProtobufEncoder = new CloudPayloadProtoBufEncoderImpl(kuraPayload);
            MqttMessage message = new MqttMessage(kuraPayloadProtobufEncoder.getBytes());
            message.setQos(getQOSAsInt());
            message.setRetained(isRetained());
            currentClient.publish(getTopic(), message);
            res.setResponseMessage("Mqtt message sent:\n" + message.toString());
            isSuccessful = true;
        } catch (MqttException e1) {
            e1.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        } finally {
            currentClient = null;
            res.sampleEnd();
            res.setSuccessful(isSuccessful);

            if (!reUseConnection || closeConnection) {
                closeMqttClient(mqttKey);
            }
        }

        return res;
    }

    @Override
    public void threadStarted() {
        log.debug("Thread Started"); //$NON-NLS-1$
        firstSample = true;
    }

    // Cannot do this as part of threadStarted() because the Config elements have not been processed.
    private void initSampling() {
        // Mogoče se odpre connection mqttClient tukaj
    }

    /**
     * Close mqttClient of current sampler
     */
    private void closeMqttClient(String mqttKey) {
        Map<String, Object> cp = tp.get();
        MqttClient client = (MqttClient) cp.remove(mqttKey);
        if (client != null) {
            log.debug(this + " Closing mqtt client connection " + client); //$NON-NLS-1$
            try {
                client.disconnect();
                client.close();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void threadFinished() {
        log.debug("Thread Finished"); //$NON-NLS-1$
        tearDown();
    }

    /**
     * Closes all connections, clears Map and remove thread local Map
     */
    private void tearDown() {
        Map<String, Object> cp = tp.get();
        cp.forEach((k, v) -> {
            if (k.startsWith(MQTTKEY)) {
                try {
                    ((MqttClient) v).close();
                    ((MqttClient) v).disconnect();
                } catch (MqttException mqtte) {
                    // NOOP
                }
            }
        });
        cp.clear();
        tp.remove();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean interrupt() {
        Optional<MqttClient> mqttClient = Optional.ofNullable(currentClient);
        if (mqttClient.isPresent()) {
            try {
                mqttClient.get().close();
                mqttClient.get().disconnect();
            } catch (MqttException mqtte) {
                mqtte.printStackTrace();
                // NOOP
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }
}
