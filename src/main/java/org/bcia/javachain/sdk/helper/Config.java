/*
 *  Copyright 2016, 2017 IBM, DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.bcia.javachain.sdk.helper;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.mvel2.MVEL;
import org.yaml.snakeyaml.Yaml;

/**
 * Config allows for a global config of the toolkit. Central location for all
 * toolkit configuration defaults. Has a local config file that can override any
 * property defaults. Config file can be relocated via a system property
 * "org.bcia.javachain.sdk.configuration". Any property can be overridden
 * with environment variable and then overridden
 * with a java system property. Property hierarchy goes System property
 * overrides environment variable which overrides config file for default values specified here.
 * 
 * modified for Node,SmartContract,Consenter,
 * Group,TransactionPackage,TransactionResponsePackage,
 * EventsPackage,ProposalPackage,ProposalResponsePackage
 * by wangzhe in ftsafe 2018-07-02
 */

public class Config {
    private static final Log logger = LogFactory.getLog(Config.class);

    private static final String DEFAULT_CONFIG = "config_gm.properties";
    public static final String ORG_HYPERLEDGER_FABRIC_SDK_CONFIGURATION = "org.bcia.javachain.sdk.configuration";
    /**
     * Timeout settings
     **/
    public static final String PROPOSAL_WAIT_TIME = "org.bcia.javachain.sdk.proposal.wait.time";
    public static final String CHANNEL_CONFIG_WAIT_TIME = "org.bcia.javachain.sdk.channelconfig.wait_time";
    public static final String TRANSACTION_CLEANUP_UP_TIMEOUT_WAIT_TIME = "org.bcia.javachain.sdk.client.transaction_cleanup_up_timeout_wait_time";
    public static final String ORDERER_RETRY_WAIT_TIME = "org.bcia.javachain.sdk.orderer_retry.wait_time";
    public static final String ORDERER_WAIT_TIME = "org.bcia.javachain.sdk.orderer.ordererWaitTimeMilliSecs";
    public static final String PEER_EVENT_REGISTRATION_WAIT_TIME = "org.bcia.javachain.sdk.peer.eventRegistration.wait_time";
    public static final String PEER_EVENT_RETRY_WAIT_TIME = "org.bcia.javachain.sdk.peer.retry_wait_time";
    public static final String EVENTHUB_CONNECTION_WAIT_TIME = "org.bcia.javachain.sdk.eventhub_connection.wait_time";
    public static final String GENESISBLOCK_WAIT_TIME = "org.bcia.javachain.sdk.channel.genesisblock_wait_time";
    /**
     * Crypto configuration settings
     **/
    public static final String CRYPTO_ID = "org.bcia.javachain.sdk.crypto.id";
    public static final String DEFAULT_CRYPTO_SUITE_FACTORY = "org.bcia.javachain.sdk.crypto.default_crypto_suite_factory";
    public static final String SECURITY_LEVEL = "org.bcia.javachain.sdk.security_level";
    public static final String SECURITY_PROVIDER_CLASS_NAME = "org.bcia.javachain.sdk.security_provider_class_name";
    public static final String SECURITY_CURVE_MAPPING = "org.bcia.javachain.sdk.security_curve_mapping";
    public static final String HASH_ALGORITHM = "org.bcia.javachain.sdk.hash_algorithm";
    public static final String ASYMMETRIC_KEY_TYPE = "org.bcia.javachain.sdk.crypto.asymmetric_key_type";
    public static final String CERTIFICATE_FORMAT = "org.bcia.javachain.sdk.crypto.certificate_format";
    public static final String SIGNATURE_ALGORITHM = "org.bcia.javachain.sdk.crypto.default_signature_algorithm";
    /**
     * Logging settings
     **/
    public static final String MAX_LOG_STRING_LENGTH = "org.bcia.javachain.sdk.log.stringlengthmax";
    public static final String EXTRALOGLEVEL = "org.bcia.javachain.sdk.log.extraloglevel";  // ORG_HYPERLEDGER_FABRIC_SDK_LOG_EXTRALOGLEVEL
    public static final String LOGGERLEVEL = "org.bcia.javachain.sdk.loglevel";  // ORG_HYPERLEDGER_FABRIC_SDK_LOGLEVEL=TRACE,DEBUG
    public static final String DIAGNOTISTIC_FILE_DIRECTORY = "org.bcia.javachain.sdk.diagnosticFileDir"; //ORG_HYPERLEDGER_FABRIC_SDK_DIAGNOSTICFILEDIR

    /**
     * Miscellaneous settings
     **/
    public static final String PROPOSAL_CONSISTENCY_VALIDATION = "org.bcia.javachain.sdk.proposal.consistency_validation";

    public static final String BLOCK_PATH = "org.bcia.javachain.sdk.block_path";

    private static Config config;
    private static final Properties sdkProperties = new Properties();

    
    private Map<String, String> yamlMap;
    
    public String getValue(String key) {
    	String value = (String) MVEL.eval(key, yamlMap);
    	logger.info("config >> key: "+ key +", value: "+ value);
    	return value;
    }
    
    private Config() {
//        File loadFile;
        InputStream configProps;

        try {
//          loadFile = new File(System.getProperty(ORG_BCIA_JAVACHAIN_SDK_CONFIGURATION, DEFAULT_CONFIG))
//          .getAbsoluteFile();
//  logger.debug(format("Loading configuration from %s and it is present: %b", loadFile.toString(),
//          loadFile.exists()));
//  configProps = new FileInputStream(loadFile);
        	configProps = Config.class.getResourceAsStream("/config_gm.properties");
            sdkProperties.load(configProps);
            //yamlMap = (LinkedHashMap<String, String>) new Yaml().load(this.getClass().getResourceAsStream("/config_gm.yaml"));
        } catch (IOException e) {
        	e.printStackTrace();
            logger.warn(format("Failed to load any configuration from: %s. Using toolkit defaults",
                    DEFAULT_CONFIG));
        } finally {

            // Default values
            /**
             * Timeout settings
             **/
            defaultProperty(PROPOSAL_WAIT_TIME, "200000");
            defaultProperty(CHANNEL_CONFIG_WAIT_TIME, "1500000");
            defaultProperty(ORDERER_RETRY_WAIT_TIME, "200");
            // defaultProperty(ORDERER_WAIT_TIME, "10000");
            defaultProperty(ORDERER_WAIT_TIME, "300000");
            defaultProperty(PEER_EVENT_REGISTRATION_WAIT_TIME, "5000");
            defaultProperty(PEER_EVENT_RETRY_WAIT_TIME, "500");
            defaultProperty(EVENTHUB_CONNECTION_WAIT_TIME, "1000");
            defaultProperty(GENESISBLOCK_WAIT_TIME, "5000");
            /**
             * This will NOT complete any transaction futures time out and must be kept WELL above any expected future timeout
             * for transactions sent to the Orderer. For internal cleanup only.
             */

            defaultProperty(TRANSACTION_CLEANUP_UP_TIMEOUT_WAIT_TIME, "600000"); //10 min.

            /**
             * Crypto configuration settings
             **/
            /*
            defaultProperty(DEFAULT_CRYPTO_SUITE_FACTORY, "org.bcia.javachain.sdk.security.HLSDKJCryptoSuiteFactory");
            defaultProperty(SECURITY_LEVEL, "256");
            defaultProperty(SECURITY_PROVIDER_CLASS_NAME, BouncyCastleProvider.class.getName());
            defaultProperty(SECURITY_CURVE_MAPPING, "256=secp256r1:384=secp384r1");
            defaultProperty(HASH_ALGORITHM, "SHA2");
            defaultProperty(ASYMMETRIC_KEY_TYPE, "EC");
            defaultProperty(CERTIFICATE_FORMAT, "X.509");
            defaultProperty(SIGNATURE_ALGORITHM, "SHA256withECDSA");
            */
            
            defaultProperty(DEFAULT_CRYPTO_SUITE_FACTORY, "org.bcia.javachain.sdk.security.gm.GmHLSDKJCryptoSuiteFactory");//wangzhe
            defaultProperty(SECURITY_LEVEL, "256");
            defaultProperty(SECURITY_PROVIDER_CLASS_NAME, BouncyCastleProvider.class.getName());
            defaultProperty(SECURITY_CURVE_MAPPING, "256=sm2p256v1");
            defaultProperty(HASH_ALGORITHM, "SM3");//SHA2
            defaultProperty(ASYMMETRIC_KEY_TYPE, "EC");
            defaultProperty(CERTIFICATE_FORMAT, "X.509");
            defaultProperty(SIGNATURE_ALGORITHM, "SM3withSM2");

            /**
             * Logging settings
             **/
            defaultProperty(MAX_LOG_STRING_LENGTH, "64");
            defaultProperty(EXTRALOGLEVEL, "0");
            defaultProperty(LOGGERLEVEL, null);
            defaultProperty(DIAGNOTISTIC_FILE_DIRECTORY, null);
            /**
             * Miscellaneous settings
             */
            defaultProperty(PROPOSAL_CONSISTENCY_VALIDATION, "false");//TODO 这个校验暂时不加算返回成功，等julongchain返回码统一

            defaultProperty(BLOCK_PATH, "/home/bcia/julongchain");


            final String inLogLevel = sdkProperties.getProperty(LOGGERLEVEL);

            if (null != inLogLevel) {

                Level setTo;

                switch (inLogLevel.toUpperCase()) {

                    case "TRACE":
                        setTo = Level.TRACE;
                        break;

                    case "DEBUG":
                        setTo = Level.DEBUG;
                        break;

                    case "INFO":
                        setTo = Level.INFO;
                        break;

                    case "WARN":
                        setTo = Level.WARN;
                        break;

                    case "ERROR":
                        setTo = Level.ERROR;
                        break;

                    default:
                        setTo = Level.INFO;
                        break;

                }

                if (null != setTo) {
                    org.apache.log4j.Logger.getLogger("org.bcia.javachain").setLevel(setTo);
                }

            }

        }

    }

    /**
     * getConfig return back singleton for SDK configuration.
     *
     * @return Global configuration
     */
    public static Config getConfig() {
        if (null == config) {
            config = new Config();
        }
        return config;

    }

    /**
     * getProperty return back property for the given value.
     *
     * @param property
     * @return String value for the property
     */
    private String getProperty(String property) {

        String ret = sdkProperties.getProperty(property);

        if (null == ret) {
            logger.warn(format("No configuration value found for '%s'", property));
        }
        return ret;
    }

    private static void defaultProperty(String key, String value) {

        String ret = System.getProperty(key);
        if (ret != null) {
            sdkProperties.put(key, ret);
        } else {
            String envKey = key.toUpperCase().replaceAll("\\.", "_");
            ret = System.getenv(envKey);
            if (null != ret) {
                sdkProperties.put(key, ret);
            } else {
                if (null == sdkProperties.getProperty(key) && value != null) {
                    sdkProperties.put(key, value);
                }

            }

        }
    }

    /**
     * Get the configured security level. The value determines the elliptic curve used to generate keys.
     *
     * @return the security level.
     */
    public int getSecurityLevel() {

        return Integer.parseInt(getProperty(SECURITY_LEVEL));

    }

    /**
     * Get the configured security provider.
     * This is the security provider used for the default SDK crypto suite factory.
     *
     * @return the security provider.
     */
    public String getSecurityProviderClassName() {
        return getProperty(SECURITY_PROVIDER_CLASS_NAME);
    }

    /**
     * Get the name of the configured hash algorithm, used for digital signatures.
     *
     * @return the hash algorithm name.
     */
    public String getHashAlgorithm() {
        return getProperty(HASH_ALGORITHM);

    }

    private Map<Integer, String> curveMapping = null;

    /**
     * Get a mapping from strength to curve desired.
     *
     * @return mapping from strength to curve name to use.
     */
    public Map<Integer, String> getSecurityCurveMapping() {

        if (curveMapping == null) {

            curveMapping = parseSecurityCurveMappings(getProperty(SECURITY_CURVE_MAPPING));
        }

        return Collections.unmodifiableMap(curveMapping);
    }

    public static Map<Integer, String> parseSecurityCurveMappings(final String property) {
        Map<Integer, String> lcurveMapping = new HashMap<>(8);

        if (property != null && !property.isEmpty()) { //empty will be caught later.

            String[] cmaps = property.split("[ \t]*:[ \t]*");
            for (String mape : cmaps) {

                String[] ep = mape.split("[ \t]*=[ \t]*");
                if (ep.length != 2) {
                    logger.warn(format("Bad curve mapping for %s in property %s", mape, SECURITY_CURVE_MAPPING));
                    continue;
                }

                try {
                    int parseInt = Integer.parseInt(ep[0]);
                    lcurveMapping.put(parseInt, ep[1]);
                } catch (NumberFormatException e) {
                    logger.warn(format("Bad curve mapping. Integer needed for strength %s for %s in property %s",
                            ep[0], mape, SECURITY_CURVE_MAPPING));
                }

            }

        }
        return lcurveMapping;
    }

    /**
     * Get the timeout for a single proposal request to endorser.
     *
     * @return the timeout in milliseconds.
     */
    public long getProposalWaitTime() {
        return Long.parseLong(getProperty(PROPOSAL_WAIT_TIME));
    }

    /**
     * Get the configured time to wait for genesis block.
     *
     * @return time in milliseconds.
     */
    public long getGenesisBlockWaitTime() {
        return Long.parseLong(getProperty(GENESISBLOCK_WAIT_TIME));
    }

    /**
     * Time to wait for channel to be configured.
     *
     * @return
     */
    public long getGroupConfigWaitTime() {
        return Long.parseLong(getProperty(CHANNEL_CONFIG_WAIT_TIME));
    }

    /**
     * Time to wait before retrying an operation.
     *
     * @return
     */
    public long getConsenterRetryWaitTime() {
        return Long.parseLong(getProperty(ORDERER_RETRY_WAIT_TIME));
    }

    public long getConsenterWaitTime() {
        return Long.parseLong(getProperty(ORDERER_WAIT_TIME));
    }

    /**
     * getNodeEventRegistrationWaitTime
     *
     * @return time in milliseconds to wait for peer eventing service to wait for event registration
     */
    public long getNodeEventRegistrationWaitTime() {
        return Long.parseLong(getProperty(PEER_EVENT_REGISTRATION_WAIT_TIME));
    }

    /**
     * getNodeEventRegistrationWaitTime
     *
     * @return time in milliseconds to wait for peer eventing service to wait for event registration
     */
    public  long getNodeRetryWaitTime() {
        return Long.parseLong(getProperty(PEER_EVENT_RETRY_WAIT_TIME));
    }

    public long getEventHubConnectionWaitTime() {
        return Long.parseLong(getProperty(EVENTHUB_CONNECTION_WAIT_TIME));
    }

    public String getAsymmetricKeyType() {
        return getProperty(ASYMMETRIC_KEY_TYPE);
    }

    public String getCertificateFormat() {
        return getProperty(CERTIFICATE_FORMAT);
    }

    public String getSignatureAlgorithm() {
        return getProperty(SIGNATURE_ALGORITHM);
    }

    public String getCryptoId() {
        return getProperty(CRYPTO_ID);
    }

    public String getDefaultCryptoSuiteFactory() {
        return getProperty(DEFAULT_CRYPTO_SUITE_FACTORY);
    }

    //区块文件路径
    public String getBlockPath() {
        return getProperty(BLOCK_PATH);
    }

    public int maxLogStringLength() {
        return Integer.parseInt(getProperty(MAX_LOG_STRING_LENGTH));
    }

    /**
     * getProposalConsistencyValidation determine if validation of the proposals should
     * be done before sending to the orderer.
     *
     * @return if true proposals will be checked they are consistent with each other before sending to the Orderer
     */

    public boolean getProposalConsistencyValidation() {
        return Boolean.parseBoolean(getProperty(PROPOSAL_CONSISTENCY_VALIDATION));

    }

    private int extraLogLevel = -1;

    public boolean extraLogLevel(int val) {
        if (extraLogLevel == -1) {
            extraLogLevel = Integer.parseInt(getProperty(EXTRALOGLEVEL));
        }

        return val <= extraLogLevel;

    }

    DiagnosticFileDumper diagnosticFileDumper = null;

    /**
     * The directory where diagnostic dumps are to be place, null if none should be done.
     *
     * @return The directory where diagnostic dumps are to be place, null if none should be done.
     */

    public DiagnosticFileDumper getDiagnosticFileDumper() {

        if (diagnosticFileDumper != null) {
            return diagnosticFileDumper;
        }

        String dd = sdkProperties.getProperty(DIAGNOTISTIC_FILE_DIRECTORY);

        if (dd != null) {

            diagnosticFileDumper = DiagnosticFileDumper.configInstance(new File(dd));

        }

        return diagnosticFileDumper;
    }

    /**
     * This does NOT trigger futures time out and must be kept WELL above any expected future timeout
     * for transactions sent to the Orderer
     *
     * @return
     */
    public long getTransactionListenerCleanUpTimeout() {
        return Long.parseLong(getProperty(TRANSACTION_CLEANUP_UP_TIMEOUT_WAIT_TIME));
    }
}