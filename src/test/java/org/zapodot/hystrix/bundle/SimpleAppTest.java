package org.zapodot.hystrix.bundle;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import org.apache.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleAppTest {

    @ClassRule
    public static final DropwizardAppRule<AppConfiguration> APP_RULE = new DropwizardAppRule<>(App.class, new AppConfiguration());

    @Test
    public void testStuff() throws Exception {
        final JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setTimeToLive(Duration.seconds(5L));
        final Client client = new JerseyClientBuilder(APP_RULE.getEnvironment()).using(configuration).build(
                "test");
        final Response eventResponse = client.target(String.format("http://localhost:%d%s",
                                                                   APP_RULE.getAdminPort(),
                                                                   HystrixBundle.DEFAULT_STREAM_PATH)).request().options();
        assertThat(eventResponse.getStatus()).isEqualTo(HttpStatus.SC_OK);

    }
}
