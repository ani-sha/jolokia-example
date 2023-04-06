package org.example;

import org.jolokia.client.BasicAuthenticator;
import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.*;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;

public class JolokiaClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static J4pClient createJolokiaClient() {
        Integer kafkaConnectPort = KafkaConnect.getDebeziumPort();
        String jolokiaUrl = String.format("http://localhost:%d/jolokia/", kafkaConnectPort);
        LOGGER.info("Jolokia client available at " + jolokiaUrl);
        return J4pClient.url(jolokiaUrl)
                .authenticator(new BasicAuthenticator().preemptive())
                .connectionTimeout(3000)
                .useProxyFromEnvironment()
                .build();
    }

    public static JSONObject getMetrics(String metricsName, String metricsType) throws MalformedObjectNameException, J4pException {
        J4pClient j4p = createJolokiaClient();
        LOGGER.info("Reading metrics " + metricsName + " from " + metricsType);
        J4pReadRequest request = new J4pReadRequest("java.lang:type=" + metricsType, metricsName);
        J4pReadResponse response = j4p.execute(request);
        return response.asJSONObject();
    }
}
