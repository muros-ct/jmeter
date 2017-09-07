/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and/or its affiliates
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kura.message;

/**
 * Models a topic for messages posted to the Kura platform.
 * Topics are expected to be in the form of "account/asset/&lt;application_specific&gt;";
 * The system control topic prefix is defined in the {@link CloudService} and defaults
 * to $EDC.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class KuraTopic {

    private String m_fullTopic;
    private String[] m_topicParts;
    private String m_prefix;
    private String m_accountName;
    private String m_deviceId;
    private String m_applicationId;
    private String m_applicationTopic;

    public KuraTopic(String fullTopic) {
        this(fullTopic, "$");
    }
    
    public KuraTopic(String fullTopic, String controlPrefix) {
        this.m_fullTopic = fullTopic;
        if (fullTopic.compareTo("#") == 0) {
            return;
        }

        this.m_topicParts = fullTopic.split("/");
        if (this.m_topicParts.length == 0) {
            return;
        }

        // prefix
        int index = 0;
        int offset = 0; // skip a slash
        if (this.m_topicParts[0].startsWith(controlPrefix)) {
            this.m_prefix = this.m_topicParts[index];
            offset += this.m_prefix.length() + 1;
            index++;
        }

        // account name
        if (index < this.m_topicParts.length) {
            this.m_accountName = this.m_topicParts[index];
            offset += this.m_accountName.length() + 1;
            index++;
        }

        // deviceId
        if (index < this.m_topicParts.length) {
            this.m_deviceId = this.m_topicParts[index];
            offset += this.m_deviceId.length() + 1;
            index++;
        }

        // applicationId
        if (index < this.m_topicParts.length) {
            this.m_applicationId = this.m_topicParts[index];
            offset += this.m_applicationId.length() + 1;
            index++;
        }

        // applicationTopic
        if (offset < this.m_fullTopic.length()) {
            this.m_applicationTopic = this.m_fullTopic.substring(offset);
        }
    }

    public String getFullTopic() {
        return this.m_fullTopic;
    }

    public String[] getTopicParts() {
        return this.m_topicParts;
    }

    public String getPrefix() {
        return this.m_prefix;
    }

    public String getAccountName() {
        return this.m_accountName;
    }

    public String getDeviceId() {
        return this.m_deviceId;
    }

    public String getApplicationId() {
        return this.m_applicationId;
    }

    public String getApplicationTopic() {
        return this.m_applicationTopic;
    }
}
