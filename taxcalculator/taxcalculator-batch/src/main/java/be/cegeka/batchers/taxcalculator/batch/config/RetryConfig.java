package be.cegeka.batchers.taxcalculator.batch.config;

import be.cegeka.batchers.taxcalculator.application.service.exceptions.TaxWebServiceNonFatalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RetryConfig {

    @Value("${employeeJob.taxProcessor.retry.initialInterval:100}")
    private long initialInterval = 100;

    @Value("${employeeJob.taxProcessor.retry.maxAttemptsPerEmployee:3}")
    private int maxAttempts = 3;

    public RetryTemplate createRetryTemplate() {
        Map<Class<? extends Throwable>, Boolean> exceptions = new HashMap<>();
        exceptions.put(TaxWebServiceNonFatalException.class, true);

        RetryTemplate template = new RetryTemplate();
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts, exceptions);
        template.setRetryPolicy(retryPolicy);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }
}
