package org.apache.jmeter.protocol.mqtt.config.gui;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.TristateCheckBox;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.mqtt.sampler.MqttSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;

public class MqttConfigGui extends AbstractConfigGui {

    private static final long serialVersionUID = 1L;

    private JTextField server;

    private JTextField port;

    private JTextField clientId;

    private JTextField topic;

    private JComboBox<String> qos = new JComboBox<>(new String[] {"At most once", "At least once ", "Exactly once"}); // $NON-NLS-1$

    private JCheckBox retain;

    private JCheckBox reUseConnection;

    private TristateCheckBox closeConnection;

    private JSyntaxTextArea requestData;

    private boolean displayName = true;

    public MqttConfigGui() {
        this(true);
    }

    public MqttConfigGui(boolean displayName) {
        this.displayName = displayName;
        init();
    }

    @Override
    public String getLabelResource() {
        return "mqtt_sample_title"; // $NON-NLS-1$
    }


    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        server.setText(""); // $NON-NLS-1$
        port.setText(MqttSampler.PORT_DEFAULT); // $NON-NLS-1$
        clientId.setText(""); // $NON-NLS-1$
        topic.setText(""); // $NON-NLS-1$
        qos.setSelectedIndex(0);
        retain.setSelected(false);
        reUseConnection.setSelected(MqttSampler.RE_USE_CONNECTION_DEFAULT);
        closeConnection.setSelected(MqttSampler.CLOSE_CONNECTION_DEFAULT);
        requestData.setText(""); // $NON-NLS-1$
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        // Note: the element is a ConfigTestElement when used standalone, so we cannot use MqttSampler access methods
        server.setText(element.getPropertyAsString(MqttSampler.SERVER));
        port.setText(element.getPropertyAsString(MqttSampler.PORT, MqttSampler.PORT_DEFAULT));
        clientId.setText(element.getPropertyAsString(MqttSampler.CLIENT_ID));
        topic.setText(element.getPropertyAsString(MqttSampler.TOPIC));
        qos.setSelectedIndex(element.getPropertyAsInt(MqttSampler.QOS, MqttSampler.QOS_DEFAULT));
        retain.setSelected(element.getPropertyAsBoolean(MqttSampler.RETAINED));
        reUseConnection.setSelected(element.getPropertyAsBoolean(MqttSampler.RE_USE_CONNECTION));
        closeConnection.setSelected(element.getPropertyAsBoolean(MqttSampler.CLOSE_CONNECTION));
        requestData.setInitialText(element.getPropertyAsString(MqttSampler.MESSAGE_BODY));
    }

    @Override
    public TestElement createTestElement() {
        ConfigTestElement element = new ConfigTestElement();
        modifyTestElement(element);

        return element;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        // Note: the element is a ConfigTestElement, so cannot use MqttSampler access methods
        element.setProperty(MqttSampler.SERVER,server.getText());
        element.setProperty(MqttSampler.PORT,port.getText());
        element.setProperty(MqttSampler.CLIENT_ID,clientId.getText());
        element.setProperty(MqttSampler.TOPIC, topic.getText());
        element.setProperty(MqttSampler.QOS, qos.getSelectedIndex());
        element.setProperty(MqttSampler.RETAINED, retain.isSelected());
        element.setProperty(MqttSampler.RE_USE_CONNECTION, reUseConnection.isSelected());
        closeConnection.setPropertyFromTristate(element, MqttSampler.CLOSE_CONNECTION); // Don't use default for saving tristates
        element.setProperty(MqttSampler.MESSAGE_BODY, requestData.getText());
    }

    private JPanel createServerPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("mqtt_server")); //$NON-NLS-1$

        server = new JTextField(10);
        label.setLabelFor(server);

        JPanel serverPanel = new JPanel(new BorderLayout(5, 0));
        serverPanel.add(label, BorderLayout.WEST);
        serverPanel.add(server, BorderLayout.CENTER);

        return serverPanel;
    }

    private JPanel createPortPanel() {
        port = new JTextField(4);

        JLabel label = new JLabel(JMeterUtils.getResString("mqtt_server_port")); // $NON-NLS-1$
        label.setLabelFor(port);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(port, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createClientIdPanel() {
        clientId = new JTextField(10);

        JLabel label = new JLabel(JMeterUtils.getResString("mqtt_client_id")); // $NON-NLS-1$
        label.setLabelFor(clientId);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(clientId, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTopicPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("mqtt_topic")); //$NON-NLS-1$

        topic = new JTextField(30);
        topic.setMaximumSize(new Dimension(topic.getPreferredSize()));
        label.setLabelFor(topic);

        JPanel topicPanel = new JPanel(new FlowLayout());
        topicPanel.add(label);
        topicPanel.add(topic);

        return topicPanel;
    }

    private JPanel createQoSPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("mqtt_qos")); //$NON-NLS-1$

        qos.setSelectedIndex(0);
        label.setLabelFor(qos);

        JPanel qosPanel = new JPanel(new FlowLayout());
        qosPanel.add(label);
        qosPanel.add(qos);

        return qosPanel;
    }

    private JPanel createRetainPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("mqtt_retain")); //$NON-NLS-1$

        retain = new JCheckBox("", false);
        label.setLabelFor(retain);

        JPanel retainPanel = new JPanel(new FlowLayout());
        retainPanel.add(label);
        retainPanel.add(retain);

        return retainPanel;
    }

    private JPanel createReuseConnectionPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("mqtt_reuse_connection")); //$NON-NLS-1$

        reUseConnection = new JCheckBox("", true);
        reUseConnection.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                closeConnection.setEnabled(true);
            } else {
                closeConnection.setEnabled(false);
            }
        });
        label.setLabelFor(reUseConnection);

        JPanel closePortPanel = new JPanel(new FlowLayout());
        closePortPanel.add(label);
        closePortPanel.add(reUseConnection);

        return closePortPanel;
    }

    private JPanel createCloseConnectionPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("mqtt_close_connection")); // $NON-NLS-1$

        closeConnection = new TristateCheckBox("", MqttSampler.CLOSE_CONNECTION_DEFAULT);
        label.setLabelFor(closeConnection);

        JPanel closeConnectionPanel = new JPanel(new FlowLayout());
        closeConnectionPanel.add(label);
        closeConnectionPanel.add(closeConnection);

        return closeConnectionPanel;
    }

    private JPanel createRequestPanel() {
        JLabel reqLabel = new JLabel(JMeterUtils.getResString("mqtt_request_data")); // $NON-NLS-1$
        requestData = JSyntaxTextArea.getInstance(15, 80);
        requestData.setLanguage("text"); //$NON-NLS-1$
        reqLabel.setLabelFor(requestData);

        JPanel reqDataPanel = new JPanel(new BorderLayout(5, 0));
        reqDataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));

        reqDataPanel.add(reqLabel, BorderLayout.WEST);
        reqDataPanel.add(JTextScrollPane.getInstance(requestData), BorderLayout.CENTER);

        return reqDataPanel;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));

        if (displayName) {
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
        }

        // MAIN PANEL
        VerticalPanel mainPanel = new VerticalPanel();
        // Server panel
        JPanel serverPanel = new HorizontalPanel();
        serverPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));
        serverPanel.add(createServerPanel());
        serverPanel.add(createPortPanel());
        serverPanel.add(createClientIdPanel());
        mainPanel.add(serverPanel);
        // Mqtt options panel
        JPanel mqttPanel = new HorizontalPanel();
        mqttPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));
        mqttPanel.add(createTopicPanel());
        mqttPanel.add(createQoSPanel());
        mqttPanel.add(createRetainPanel());
        mqttPanel.add(createReuseConnectionPanel());
        mqttPanel.add(createCloseConnectionPanel());
        mainPanel.add(mqttPanel);
        // Request data panel
        mainPanel.add(createRequestPanel());

        add(mainPanel, BorderLayout.CENTER);
    }
}
