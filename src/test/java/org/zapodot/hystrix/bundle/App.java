package org.zapodot.hystrix.bundle;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.servlets.PingServlet;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlet.ServletHolder;

public class App extends Application<AppConfiguration> {

    @Override
    public void initialize(final Bootstrap<AppConfiguration> bootstrap) {
        bootstrap.addBundle(HystrixBundle.withDefaultSettings());
    }

    @Override
    public void run(final AppConfiguration configuration, final Environment environment) throws Exception {
        environment.healthChecks().register("dummy", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });
        environment.getApplicationContext().addServlet(new ServletHolder(new PingServlet()), "/ping");

    }
}
