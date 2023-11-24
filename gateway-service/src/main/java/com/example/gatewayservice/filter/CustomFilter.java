package com.example.gatewayservice.filter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {
   public CustomFilter() {
       super(Config.class);
   }
    @Override
    public GatewayFilter apply(Config config) {
        return null;
    }

    @Getter
    public static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;

    }
}
