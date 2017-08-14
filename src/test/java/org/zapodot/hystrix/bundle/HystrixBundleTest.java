package org.zapodot.hystrix.bundle;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterators;
import com.netflix.hystrix.contrib.codahalemetricspublisher.HystrixCodaHaleMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.validation.Validation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verifyZeroInteractions;

public class HystrixBundleTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Bootstrap<?> bootstrap;

    private Environment environment;

    private Configuration configuration;

    @Before
    public void setUp() throws Exception {
        environment = new Environment(getClass().getName(),
                                      new ObjectMapper(),
                                      Validation.buildDefaultValidatorFactory().getValidator(),
                                      new MetricRegistry(),
                                      getClass().getClassLoader());

    }

    @Test
    public void testWithDefaultsUsingConstructor() throws Exception {
        final HystrixBundle hystrixBundle = new HystrixBundle();
        hystrixBundle.initialize(bootstrap);
        hystrixBundle.run(configuration, environment);
        assertNotNull(environment.getAdminContext()
                                 .getServletContext()
                                 .getServletRegistration(HystrixBundle.SERVLET_NAME));
        assertNull(environment.getApplicationContext()
                              .getServletContext()
                              .getServletRegistration(HystrixBundle.SERVLET_NAME));
        assertTrue(HystrixPlugins.getInstance().getMetricsPublisher() instanceof HystrixCodaHaleMetricsPublisher);
        verifyZeroInteractions(bootstrap);
    }

    @Test
    public void testWithDefaultsUsingBuilder() throws Exception {
        final HystrixBundle hystrixBundle = HystrixBundle.withDefaultSettings();
        hystrixBundle.initialize(bootstrap);
        hystrixBundle.run(configuration, environment);
        assertNotNull(environment.getAdminContext()
                .getServletContext()
                .getServletRegistration(HystrixBundle.SERVLET_NAME));
        assertNull(environment.getApplicationContext()
                .getServletContext()
                .getServletRegistration(HystrixBundle.SERVLET_NAME));
        assertTrue(HystrixPlugins.getInstance().getMetricsPublisher() instanceof HystrixCodaHaleMetricsPublisher);
        verifyZeroInteractions(bootstrap);
    }

    @Test
    public void testAddToApplicationContext() throws Exception {
        final HystrixBundle hystrixBundle = HystrixBundle.builder().withApplicationStreamPath(HystrixBundle.DEFAULT_STREAM_PATH).disableStreamServletInAdminContext().build();
        hystrixBundle.initialize(bootstrap);
        hystrixBundle.run(configuration, environment);
        assertNotNull(environment.getApplicationContext()
                                 .getServletContext()
                                 .getServletRegistration(HystrixBundle.SERVLET_NAME));
        assertNull(environment.getAdminContext()
                              .getServletContext()
                              .getServletRegistration(HystrixBundle.SERVLET_NAME));
        assertTrue(HystrixPlugins.getInstance().getMetricsPublisher() instanceof HystrixCodaHaleMetricsPublisher);
        verifyZeroInteractions(bootstrap);
    }

    @Test
    public void testCustomAdminPath() throws Exception {
        final String adminServletPath = "/my-path";
        final HystrixBundle hystrixBundle = HystrixBundle.builder().withAdminStreamUri(adminServletPath).build();
        hystrixBundle.initialize(bootstrap);
        hystrixBundle.run(configuration, environment);
        assertEquals(adminServletPath,
                     Iterators.getOnlyElement(environment.getAdminContext()
                                                         .getServletContext()
                                                         .getServletRegistration(HystrixBundle.SERVLET_NAME)
                                                         .getMappings()
                                                         .iterator()));

    }

    @Test
    public void testDisableMetricsPublisher() throws Exception {
        final HystrixBundle hystrixBundle = HystrixBundle.builder().disableMetricsPublisher().build();
        hystrixBundle.initialize(bootstrap);
        hystrixBundle.run(configuration, environment);
        assertFalse(HystrixPlugins.getInstance().getMetricsPublisher() instanceof HystrixCodaHaleMetricsPublisher);
    }

    @Test
    public void testConfigurableMetricsPublisher() {
        boolean publishHystrixMetricsByDefault = false;
        final HystrixBundle hystrixBundle = new HystrixBundle<Configuration>("", "", publishHystrixMetricsByDefault) {
            @Override
            protected boolean canPublishHystrixMetrics(Configuration configuration) {
                return true; //override using configuration value
            }
        };
        hystrixBundle.initialize(bootstrap);
        hystrixBundle.run(configuration, environment);
        assertTrue(HystrixPlugins.getInstance().getMetricsPublisher() instanceof HystrixCodaHaleMetricsPublisher);
    }
}
