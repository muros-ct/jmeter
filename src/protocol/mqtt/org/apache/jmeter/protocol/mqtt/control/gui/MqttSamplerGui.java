package org.apache.jmeter.protocol.mqtt.control.gui;

import org.apache.jmeter.config.gui.LoginConfigGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.mqtt.config.gui.MqttConfigGui;
import org.apache.jmeter.protocol.mqtt.sampler.MqttSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

import javax.swing.BorderFactory;
import java.awt.BorderLayout;

/**
 * Mqtt GUI for user setting sample request to Mqtt broker.
 */
public class MqttSamplerGui extends AbstractSamplerGui {

    private static final long serialVersionUID = 1L;

    /** Panel with credentials */
    private LoginConfigGui loginPanel;

    /** Mqtt request settings*/
    private MqttConfigGui mqttDefaultPanel;

    public MqttSamplerGui() {
        init();
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        loginPanel.configure(element);
        mqttDefaultPanel.configure(element);
    }

    @Override
    public TestElement createTestElement() {
        MqttSampler sampler = new MqttSampler();
        modifyTestElement(sampler);

        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement sampler) {
        sampler.clear();
        sampler.addTestElement(mqttDefaultPanel.createTestElement());
        sampler.addTestElement(loginPanel.createTestElement());
        super.configureTestElement(sampler);
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        mqttDefaultPanel.clearGui();
        loginPanel.clearGui();
    }

    @Override
    public String getLabelResource() {
        return "mqtt_testing_title"; // $NON-NLS-1$
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        VerticalPanel mainPanel = new VerticalPanel();

        mqttDefaultPanel = new MqttConfigGui(false);
        mainPanel.add(mqttDefaultPanel);

        loginPanel = new LoginConfigGui(false);
        loginPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("login_config"))); // $NON-NLS-1$
        mainPanel.add(loginPanel);

        add(mainPanel, BorderLayout.CENTER);
    }
}
