package com.test.application;

import io.dropwizard.Configuration;
import org.junit.Test;
import org.zapodot.hystrix.bundle.HystrixBundle;


public class ExternalAppTest {

    @Test
    public void shouldBeAbleToCreateBundleWithoutUsingBuilder(){
        new HystrixBundle<Configuration>("random","random", false) {
            protected boolean canPublishHystrixMetrics(Configuration configuration) {
                return true;
            }
        };
    }
}
