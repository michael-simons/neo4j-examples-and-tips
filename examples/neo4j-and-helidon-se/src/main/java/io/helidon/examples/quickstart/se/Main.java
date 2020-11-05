
package io.helidon.examples.quickstart.se;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.LogManager;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import io.helidon.config.Config;
import io.helidon.examples.quickstart.se.api.MovieService;
import io.helidon.examples.quickstart.se.domain.MovieRepository;
import io.helidon.health.HealthSupport;
import io.helidon.health.checks.HealthChecks;
import io.helidon.media.jsonb.JsonbSupport;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.metrics.MetricsSupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;

/**
 * The application main class.
 */
public final class Main {

    /**
     * Cannot be instantiated.
     */
    private Main() {
    }

    /**
     * Application main entry point.
     * @param args command line arguments.
     * @throws IOException if there are problems reading logging properties
     */
    public static void main(final String[] args) throws IOException {
        startServer();
    }

    /**
     * Start the server.
     * @return the created {@link WebServer} instance
     * @throws IOException if there are problems reading logging properties
     */
    static WebServer startServer() throws IOException {
        // load logging configuration
        setupLogging();

        // By default this will pick up application.yaml from the classpath
        Config config = Config.create();

        // Build server with JSONP support
        WebServer server = WebServer.builder(createRouting(config))
                .config(config.get("server"))
                .addMediaSupport(JsonpSupport.create())
                .addMediaSupport(JsonbSupport.create())
                .build();

        // Try to start the server. If successful, print some info and arrange to
        // print a message at shutdown. If unsuccessful, print the exception.
        server.start()
                .thenAccept(ws -> {
                    System.out.println(
                            "WEB server is up! http://localhost:" + ws.port() + "/greet");
                    ws.whenShutdown().thenRun(()
                            -> System.out.println("WEB server is DOWN. Good bye!"));
                })
                .exceptionally(t -> {
                    System.err.println("Startup failed: " + t.getMessage());
                    t.printStackTrace(System.err);
                    return null;
                });

        // Server threads are not daemon. No need to block. Just react.

        return server;
    }

    private static Driver createNeo4jDriver(Config config) {
        var username = config.get("neo4j.driver.authentication.username").as(String.class).get();
        var password = config.get("neo4j.driver.authentication.password").as(String.class).get();
        var uri = config.get("neo4j.driver.uri").as(URI.class).get();
        
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    /**
     * Creates new {@link Routing}.
     *
     * @return routing configured with JSON support, a health check, and a service
     * @param config configuration of this server
     */
    private static Routing createRouting(Config config) {
        MetricsSupport metrics = MetricsSupport.create();
        GreetService greetService = new GreetService(config);

        var driver = createNeo4jDriver(config);
        var movieService = new MovieService(new MovieRepository(driver));


        HealthSupport health = HealthSupport.builder()
                .addLiveness(HealthChecks.healthChecks())   // Adds a convenient set of checks
                .build();

        return Routing.builder()
                .register(health)                   // Health at "/health"
                .register(metrics)                  // Metrics at "/metrics"
                .register("/greet", greetService)
                .register(movieService)
                .build();
    }

    /**
     * Configure logging from logging.properties file.
     */
    private static void setupLogging() throws IOException {
        try (InputStream is = Main.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        }
    }
}
