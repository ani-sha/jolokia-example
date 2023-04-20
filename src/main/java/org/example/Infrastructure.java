package org.example;

import io.debezium.testing.testcontainers.ConnectorConfiguration;
import io.debezium.testing.testcontainers.DebeziumContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.stream.Stream;

public class Infrastructure {

    private static final Logger LOGGER = LoggerFactory.getLogger(Infrastructure.class);
    private static final String KAFKA_HOSTNAME = "kafka-dbz";
    private static final Network NETWORK = Network.newNetwork();

    private static final KafkaContainer KAFKA_CONTAINER =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.3"))
                    .withNetworkAliases(KAFKA_HOSTNAME)
                    .withNetwork(NETWORK);
    private static final DebeziumContainer DEBEZIUM_CONTAINER = new DebeziumContainer("quay.io/debezium/connect:2.2")
            .withNetwork(NETWORK)
            .withKafka(KAFKA_CONTAINER.getNetwork(), KAFKA_HOSTNAME + ":9092")
            .withLogConsumer(new Slf4jLogConsumer(LOGGER))
            .dependsOn(KAFKA_CONTAINER)
            .withStartupTimeout(Duration.ofSeconds(120))
            .withEnv("ENABLE_JOLOKIA", "true")
            .withExposedPorts(8083, 8778);

    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>(DockerImageName.parse("quay.io/debezium/example-postgres:2.2")
                    .asCompatibleSubstituteFor("postgres"))
                    .withNetwork(NETWORK)
                    .withNetworkAliases("postgres");

    public static Integer getJolokiaPort() {
        return DEBEZIUM_CONTAINER.getMappedPort(8778);
    }

    public static DebeziumContainer getDebeziumContainer() {
        return DEBEZIUM_CONTAINER;
    }

    public static void startContainers() {
        Startables.deepStart(Stream.of(KAFKA_CONTAINER, DEBEZIUM_CONTAINER, POSTGRES_CONTAINER)).join();
    }

    public static void stopContainers() {
        Stream.of(DEBEZIUM_CONTAINER, KAFKA_CONTAINER, POSTGRES_CONTAINER).parallel().forEach(GenericContainer::stop);
        NETWORK.close();
    }

    public static ConnectorConfiguration getPostgresConnectorConfiguration(int id, String... options) {
        final ConnectorConfiguration config = ConnectorConfiguration.forJdbcContainer(POSTGRES_CONTAINER)
                .with("snapshot.mode", "never")
                .with("topic.prefix", "dbserver" + id)
                .with("slot.name", "debezium_" + id);

        if (options != null && options.length > 0) {
            for (int i = 0; i < options.length; i += 2) {
                config.with(options[i], options[i + 1]);
            }
        }
        return config;
    }
}
