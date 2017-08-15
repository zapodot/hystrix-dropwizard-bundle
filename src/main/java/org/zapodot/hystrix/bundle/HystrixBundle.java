package org.zapodot.hystrix.bundle;

import com.netflix.hystrix.contrib.codahalemetricspublisher.HystrixCodaHaleMetricsPublisher;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.strategy.HystrixPlugins;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A DropWizard bundle that enables the Hystrix event stream to be registered, and the Hystrix metrics to be integrated
 * with DropWizard's metrics
 */
public class HystrixBundle<T extends Configuration> implements ConfiguredBundle<T> {

    /**
     * The default path that will be used for binding the HystrixMetricsStreamServlet to the admin context.
     */
    @SuppressWarnings("squid:S1075")
    static final String DEFAULT_STREAM_PATH = "/hystrix.stream";
    static final boolean DEFAULT_PUBLISH_HYSTRIX_METRICS = true;
    static final String SERVLET_NAME = "hystrixMetricsStream";
    static final MetricsPublishPredicate DEFAULT_PUBLISH_PREDICATE = (c) -> DEFAULT_PUBLISH_HYSTRIX_METRICS;
    private static final Logger logger = LoggerFactory.getLogger(HystrixBundle.class);

    private final String adminStreamPath;
    private final String applicationStreamUri;
    private final MetricsPublishPredicate publishPredicate;

    public HystrixBundle() {
        this(DEFAULT_STREAM_PATH, null, DEFAULT_PUBLISH_PREDICATE);
    }

    /**
     * The one and only constructor. Use either @{link #withDefaultSettings} or @{link builder}
     * to construct a new bundle instance
     *
     * @param adminStreamPath
     * @param applicationStreamUri
     * @param publishPredicate
     */
    private HystrixBundle(final String adminStreamPath,
                  final String applicationStreamUri,
                  final MetricsPublishPredicate publishPredicate) {
        this.adminStreamPath = adminStreamPath;
        this.applicationStreamUri = applicationStreamUri;
        this.publishPredicate = publishPredicate;
    }

    /**
     * Creates a bundle instance with default settings i.e the HystrixMetricsStreamServlet will be added to
     * the admin context mapped by the @{link #DEFAULT_STREAM_PATH} path and the Hystrix to
     * DropWizard metrics publisher is enabled
     *
     * @return an HystrixBundle instance configured using default settings
     */
    public static HystrixBundle withDefaultSettings() {
        return builder().build();
    }

    /**
     * Creates a new @{link Builder} that may be used to configure this bundle's behaviour.
     *
     * @return a new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
        logger.debug("Resetting Hystrix before changing the configuration");
        HystrixPlugins.reset();
    }
    
    /**
     * Setup method for the {@link ConfiguredBundle}
     *
     * @param configuration the configuration as provided by DropWizard
     * @param environment   the environment as provided by DropWizard
     * @see ConfiguredBundle#run(Object, Environment)
     */
    @Override
    public void run(final T configuration, final Environment environment) {
        if (adminStreamPath != null) {
            logger.info("Mapping \"{}\" to the HystrixMetricsStreamServlet in the admin context", adminStreamPath);
            environment.getAdminContext()
                    .addServlet(new ServletHolder(SERVLET_NAME, new HystrixMetricsStreamServlet()), adminStreamPath);
        }
        if (applicationStreamUri != null) {
            logger.info("Mapping \"{}\" to the HystrixMetricsStreamServlet in the application context",
                    applicationStreamUri);
            environment.getApplicationContext()
                    .addServlet(new ServletHolder(SERVLET_NAME, new HystrixMetricsStreamServlet()),
                            applicationStreamUri);
        }
        if (publishPredicate.enabled(configuration)) {
            logger.info("Enabling the Hystrix to DropWizard metrics publisher");
            final HystrixCodaHaleMetricsPublisher metricsPublisher =
                    new HystrixCodaHaleMetricsPublisher(environment.metrics());
            HystrixPlugins.getInstance().registerMetricsPublisher(metricsPublisher);
        } else {
            logger.info("The Hystrix to DropWizard metrics publisher is disabled");
        }
    }

    /**
     * Predicate to be used for deciding whether Hystrix Metrics should be published through DropWizard or not
     */
    @FunctionalInterface
    public interface MetricsPublishPredicate<T> {

        /**
         * Is publishing enabled?
         * @return true to enable, false to disable
         */
        boolean enabled(T configuration);
    }

    /**
     * A builder that is convenient to use if you need to configure this bundle in any way
     */
    public static class Builder {
        private String adminPath = DEFAULT_STREAM_PATH;
        private String applicationPath;
        private MetricsPublishPredicate metricsPublishPredicate = DEFAULT_PUBLISH_PREDICATE;

        /**
         * Configure the path that the HystrixMetricsStreamServlet will be mapped to in the admin context
         *
         * @param path a valid servlet mapping path
         * @return the same builder with the adminPath property set to the new value
         */
        public Builder withAdminStreamUri(final String path) {
            adminPath = path;
            return this;
        }

        /**
         * Disables the HystrixMetricsStreamServlet from being registered in the admin context
         *
         * @return the same builder with the adminPath property set to null
         */
        public Builder disableStreamServletInAdminContext() {
            adminPath = null;
            return this;
        }

        /**
         * Enables the registration of the HystrixMetricsStreamServlet in the application context
         *
         * @param path the path to map the servlet to (must be a valid servlet mapping path)
         * @return the same builder with the applicationPath set to the provided value
         */
        public Builder withApplicationStreamPath(final String path) {
            applicationPath = path;
            return this;
        }

        /**
         * Disables the Hystrix to DropWizard (i.e CodaHale) Metrics publisher
         *
         * @return the same builder the publisher property set to false
         */
        public Builder disableMetricsPublisher() {
            return withMetricsPublisherPredicate((c) -> false);
        }

        /**
         * Allows clients to specify some other way of specifying whether Hystrix metrics should be published or not
         *
         * @param metricsPublisherPredicate a lambda or anonymous implementation of {@link MetricsPublishPredicate}
         * @return the same builder with the metricsPublishPredicate property set to the given value
         */
        public Builder withMetricsPublisherPredicate(final MetricsPublishPredicate metricsPublisherPredicate) {
            Objects.nonNull(metricsPublisherPredicate);
            this.metricsPublishPredicate = metricsPublisherPredicate;
            return this;
        }

        /**
         * Builds a new instance of the Bundle based on previous inputs (or defaults if none has been provided).
         *
         * @return a new HystrixBundle instance
         */
        public HystrixBundle build() {
            return new HystrixBundle(adminPath, applicationPath, metricsPublishPredicate);
        }
    }
}
