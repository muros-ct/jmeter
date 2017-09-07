/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kura.core.cloud;

import org.eclipse.kura.core.message.KuraBirthPayload;
import org.eclipse.kura.core.message.KuraBirthPayload.KuraBirthPayloadBuilder;
import org.eclipse.kura.core.message.KuraDeviceProfile;
import org.eclipse.kura.core.message.KuraDisconnectPayload;
import org.eclipse.kura.message.KuraPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jodd.util.SystemUtil.osName;
import static jodd.util.SystemUtil.osVersion;

/**
 * Utility class to build lifecycle payload messages.
 */
public class LifeCyclePayloadBuilder {

    private static final String ERROR = "ERROR";

    private static final Logger s_logger = LoggerFactory.getLogger(LifeCyclePayloadBuilder.class);

    private static final String UNKNOWN = "UNKNOWN";

    LifeCyclePayloadBuilder() {
    }

    public KuraBirthPayload buildBirthPayload(String deviceName, String payloadEncoding, String imei, String iccid, String imsi, String rssi, String uptime, String modelName, String modelId,
            String partNumber, String serialNumber, String firmwareVersion, String biosVersion, String javaVmName, String javaVmVersion, String javaVmInfo, String javaVendor,
            String javaVersion, String kuraVersion, String connectionInterface, String connectionIp, Double latitude, Double longitude, Double altitude, char[] numberOfProcessors, char[] totalMemory,
            String osArch, String osgiFwName, String osgiFwVersion) {
        // build device profile
        KuraDeviceProfile deviceProfile = buildDeviceProfile(uptime, deviceName, modelName, modelId, partNumber, serialNumber, firmwareVersion, biosVersion, javaVmName, javaVmVersion, javaVmInfo,
                javaVendor, javaVersion, kuraVersion, connectionInterface, connectionIp, latitude, longitude, altitude, numberOfProcessors, totalMemory, osArch, osgiFwName, osgiFwVersion);

        // build application IDs
        String appIds = buildApplicationIDs();

        // build accept encoding
        String acceptEncoding = buildAcceptEncoding();

        // build birth certificate
        KuraBirthPayloadBuilder birthPayloadBuilder = new KuraBirthPayloadBuilder();
        birthPayloadBuilder.withUptime(deviceProfile.getUptime()).withDisplayName(deviceName)
                .withModelName(deviceProfile.getModelName()).withModelId(deviceProfile.getModelId())
                .withPartNumber(deviceProfile.getPartNumber()).withSerialNumber(deviceProfile.getSerialNumber())
                .withFirmwareVersion(deviceProfile.getFirmwareVersion()).withBiosVersion(deviceProfile.getBiosVersion())
                .withOs(deviceProfile.getOs()).withOsVersion(deviceProfile.getOsVersion())
                .withJvmName(deviceProfile.getJvmName()).withJvmVersion(deviceProfile.getJvmVersion())
                .withJvmProfile(deviceProfile.getJvmProfile()).withKuraVersion(deviceProfile.getKuraVersion())
                .withConnectionInterface(deviceProfile.getConnectionInterface())
                .withConnectionIp(deviceProfile.getConnectionIp()).withAcceptEncoding(acceptEncoding)
                .withApplicationIdentifiers(appIds).withAvailableProcessors(deviceProfile.getAvailableProcessors())
                .withTotalMemory(deviceProfile.getTotalMemory()).withOsArch(deviceProfile.getOsArch())
                .withOsgiFramework(deviceProfile.getOsgiFramework())
                .withOsgiFrameworkVersion(deviceProfile.getOsgiFrameworkVersion()).withPayloadEncoding(payloadEncoding);

        birthPayloadBuilder.withModemImei(imei);
        birthPayloadBuilder.withModemIccid(iccid);
        birthPayloadBuilder.withModemImsi(imsi);

        birthPayloadBuilder.withModemRssi(rssi);

        KuraPosition kuraPosition = new KuraPosition();
        kuraPosition.setLatitude(deviceProfile.getLatitude());
        kuraPosition.setLongitude(deviceProfile.getLongitude());
        kuraPosition.setAltitude(deviceProfile.getAltitude());
        birthPayloadBuilder.withPosition(kuraPosition);

        return birthPayloadBuilder.build();
    }

    public KuraDisconnectPayload buildDisconnectPayload(String uptime, String deviceName) {

        return new KuraDisconnectPayload(uptime, deviceName);
    }

    public KuraDeviceProfile buildDeviceProfile(String uptime, String deviceName, String modelName, String modelId, String partNumber, String serialNumber, String firmwareVersion,
            String biosVersion, String javaVmName, String javaVmVersion, String javaVmInfo, String javaVendor, String javaVersion, String kuraVersion, String connectionInterface,
            String connectionIp, Double latitude, Double longitude, Double altitude, char[] numberOfProcessors, char[] totalMemory, String osArch, String osgiFwName, String osgiFwVersion) {
        //
        // build the profile
        return new KuraDeviceProfile(uptime, deviceName,
                modelName, modelId, partNumber,
                serialNumber, firmwareVersion, biosVersion,
                osName(), osVersion(), javaVmName,
                javaVmVersion + " " + javaVmInfo,
                javaVendor + " " + javaVersion, kuraVersion,
                connectionInterface, connectionIp, latitude, longitude, altitude,
                String.valueOf(numberOfProcessors), String.valueOf(totalMemory),
                osArch, osgiFwName, osgiFwVersion);
    }

    private String buildConnectionIp() {
        String connectionIp = UNKNOWN;

        return connectionIp;
    }

    private String buildConnectionInterface() {
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }

    private String buildApplicationIDs() {
        String[] appIdArray = {};
        StringBuilder sbAppIDs = new StringBuilder();
        for (int i = 0; i < appIdArray.length; i++) {
            if (i != 0) {
                sbAppIDs.append(",");
            }
            sbAppIDs.append(appIdArray[i]);
        }
        return sbAppIDs.toString();
    }

    private String buildAcceptEncoding() {
        String acceptEncoding = "";
        acceptEncoding = "gzip";

        return acceptEncoding;
    }
}
