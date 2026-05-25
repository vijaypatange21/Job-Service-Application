package com.companyms.security;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalFeignAuthConfig {

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Service-Secret";

    @Bean
    public RequestInterceptor internalServiceRequestInterceptor(
            @Value("${gateway.internal.secret:change-this-internal-service-secret}") String internalServiceSecret
    ) {
        return template -> template.header(INTERNAL_SECRET_HEADER, internalServiceSecret);
    }
}
