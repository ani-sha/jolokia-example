package org.example;

import org.jolokia.client.exception.J4pException;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws MalformedObjectNameException, J4pException {
        KafkaConnect.startContainers();
        String metricsName = "HeapMemoryUsage";
        String metricsType = "Memory";
        JSONObject jsonObject = JolokiaClient.getMetrics(metricsName, metricsType);
        LOGGER.info("Response data is " + jsonObject);
        KafkaConnect.stopContainers();
    }
}