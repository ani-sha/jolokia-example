package org.example;

import io.debezium.testing.testcontainers.DebeziumContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.stream.Stream;

public class KafkaConnect {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConnect.class);
    private static final String KAFKA_HOSTNAME = "kafka-dbz";
    private static final Network NETWORK = Network.newNetwork();

    private static final KafkaContainer KAFKA_CONTAINER =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.3"))
                    .withNetworkAliases(KAFKA_HOSTNAME)
                    .withNetwork(NETWORK);

    private static final Path dockerFile = Paths.get("src", "main", "docker").toFile().getAbsoluteFile().toPath();

    private static final DebeziumContainer DEBEZIUM_CONTAINER = new DebeziumContainer(new ImageFromDockerfile().withFileFromPath(".", dockerFile))
                .withNetwork(NETWORK)
                .withKafka(KAFKA_CONTAINER.getNetwork(), KAFKA_HOSTNAME + ":9092")
                .withLogConsumer(new Slf4jLogConsumer(LOGGER))
                .dependsOn(KAFKA_CONTAINER)
                .withStartupTimeout(Duration.ofSeconds(120))
                .withEnv("KAFKA_OPTS", "-javaagent:/tmp/jolokia/jolokia-jvm-1.7.2.jar=port=8778,host=*")
                .withExposedPorts(8083, 8778);

    public static Integer getDebeziumPort() {
        return DEBEZIUM_CONTAINER.getFirstMappedPort();
    }

    public static String getDebeziumHost() {
        return DEBEZIUM_CONTAINER.getContainerInfo().getConfig().getHostName();
    }

    public static Integer getJolokiaPort() {
        return DEBEZIUM_CONTAINER.getMappedPort(8778);
    }

    public static void startContainers() {
        Startables.deepStart(Stream.of(KAFKA_CONTAINER, DEBEZIUM_CONTAINER)).join();
    }

    public static void stopContainers() {
        Stream.of(DEBEZIUM_CONTAINER, KAFKA_CONTAINER).parallel().forEach(GenericContainer::stop);
        NETWORK.close();
    }
}
