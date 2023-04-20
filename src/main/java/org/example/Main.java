package org.example;

import io.debezium.testing.testcontainers.Connector;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.J4pReadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws MalformedObjectNameException, J4pException {
        Infrastructure.startContainers();
        Infrastructure.getDebeziumContainer().registerConnector("my-postgres-connector", Infrastructure.getPostgresConnectorConfiguration(1));
        Infrastructure.getDebeziumContainer().ensureConnectorState("my-postgres-connector", Connector.State.RUNNING);
        J4pReadResponse response1 = JolokiaClient.getMetrics("debezium.postgres:type=connector-metrics,context=streaming,server=dbserver1", "MilliSecondsSinceLastEvent");
        J4pReadResponse response2 = JolokiaClient.getMetrics("debezium.postgres:type=connector-metrics,context=streaming,server=dbserver1", "Connected");
        J4pReadResponse response3 = JolokiaClient.getMetrics("debezium.postgres:type=connector-metrics,context=streaming,server=dbserver1", "TotalNumberOfEventsSeen");
        LOGGER.info("Response for MilliSecondsSinceLastEvent : " + response1.asJSONObject().toJSONString());
        LOGGER.info("Response for Connected : " + response2.asJSONObject().toJSONString());
        LOGGER.info("Response for TotalNumberOfEventsSeen : " + response3.asJSONObject().toJSONString());
        Infrastructure.stopContainers();
    }
}