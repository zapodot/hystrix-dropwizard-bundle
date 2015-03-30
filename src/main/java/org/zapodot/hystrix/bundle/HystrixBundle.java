package org.zapodot.hystrix.bundle;

import com.netflix.hystrix.contrib.codahalemetricspublisher.HystrixCodaHaleMetricsPublisher;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.strategy.HystrixPlugins;
import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DropWizard bundle that enables the Hystrix event stream to be registered, and the Hystrix metrics to be integrated
 * with DropWizard's metrics
 */
public class HystrixBundle implements Bundle {

    /**
     * The default path that will be used for binding the HystrixMetricsStreamServlet to the admin context.
     */
    public static final String DEFAULT_STREAM_PATH = "/hystrix.stream";
    private static final Logger logger = LoggerFactory.getLogger(HystrixBundle.class);
    public static final String SERVLET_NAME = "hystrixMetricsStream";
    private final String adminStreamPath;
    private final String applicationStreamUri;
    private final boolean publishHystrixMetrics;

    /**
     * A default constructor that will add the HystrixMetricsStreamServlet to the @{link #DEFAULT_STREAM_PATH} path on
     * the Admin context as well as enable the Hystrix to DropWizard Metrics publisher.
     */
    public HystrixBundle() {
        this(DEFAULT_STREAM_PATH, null, true);
    }

    HystrixBundle(final String adminStreamPath,
                  final String applicationStreamUri,
                  final boolean publishHystrixMetrics) {
        this.adminStreamPath = adminStreamPath;
        this.applicationStreamUri = applicationStreamUri;
        this.publishHystrixMetrics = publishHystrixMetrics;
    }

    public static HystrixBundle withDefaultSettings() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
        logger.debug("Resetting Hystrix before changing the configuration");
        HystrixPlugins.reset();
    }

    @Override
    public void run(final Environment environment) {
        if (adminStreamPath != null) {
            logger.info("Mapping \"{}\" to the HystrixMetricsStreamServlet in the admin context", adminStreamPath);
            environment.getAdminContext()
                       .addServlet(new ServletHolder(SERVLET_NAME, new HystrixMetricsStreamServlet()), adminStreamPath);
        }
        if (applicationStreamUri != null) {
            logger.info("Mapping \"{}\" to the HystrixMetricsStreamServlet in the application context", applicationStreamUri);
            environment.getApplicationContext()
                       .addServlet(new ServletHolder(SERVLET_NAME, new HystrixMetricsStreamServlet()), applicationStreamUri);
        }
        if (publishHystrixMetrics) {
            logger.info("Enabling the Hystrix to DropWizard metrics publisher");
            final HystrixCodaHaleMetricsPublisher metricsPublisher =
                    new HystrixCodaHaleMetricsPublisher(environment.metrics());
            HystrixPlugins.getInstance().registerMetricsPublisher(metricsPublisher);
        } else {
            logger.info("The Hystrix to DropWizard metrics publisher is disabled");
        }


    }

    /**
     * A builder that is convenient to use if you need to configure this bundle in any way
     */
    public static class Builder {
        private String adminPath = DEFAULT_STREAM_PATH;
        private String applicationPath;
        private boolean publishHystrixMetrics = true;

        public Builder withAdminStreamUri(final String path) {
            adminPath = path;
            return this;
        }

        public Builder disableStreamServletInAdminContext() {
            adminPath = null;
            return this;
        }

        public Builder withApplicationStreamPath(final String path) {
            applicationPath = path;
            return this;
        }

        public Builder disableMetricsPublisher() {
            publishHystrixMetrics = false;
            return this;
        }

        public HystrixBundle build() {
            return new HystrixBundle(adminPath, applicationPath, publishHystrixMetrics);
        }
    }
}
