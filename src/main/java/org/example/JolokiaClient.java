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
    public static J4pClient createJolokiaClient() {
        Integer kafkaConnectPort = Infrastructure.getJolokiaPort();
        String jolokiaUrl = String.format("http://localhost:%d/jolokia/", kafkaConnectPort);
        return J4pClient.url(jolokiaUrl)
                .authenticator(new BasicAuthenticator().preemptive())
                .connectionTimeout(3000)
                .useProxyFromEnvironment()
                .build();
    }

    public static J4pReadResponse getMetrics(String metricsName, String metricsType) throws MalformedObjectNameException, J4pException {
        J4pClient j4p = createJolokiaClient();
        J4pReadRequest request = new J4pReadRequest(metricsName, metricsType);
        return j4p.execute(request);
    }
}
