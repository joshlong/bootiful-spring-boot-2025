package com.example.adoptions.adoptions;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.DirectChannelSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import org.springframework.modulith.events.Externalized;

@Externalized(IntegrationConfiguration.CHANNEL_NAME)
public record DogAdoptionEvent(int dogId) {
}


@Configuration
class IntegrationConfiguration {

    static final String CHANNEL_NAME = "adoptionsChannel";

    @Bean
    IntegrationFlow adoptionsIntegrationFlow(@Qualifier(CHANNEL_NAME) MessageChannel messageChannel) {
        return IntegrationFlow
                .from(messageChannel)
                .handle((GenericHandler<DogAdoptionEvent>) (payload, headers) -> {
                    System.out.println("adoption event received [" + payload + "]");
                    headers.forEach((k, v) -> System.out.println("header [" + k + "] = [" + v + "]"));
                    return null;
                })
                .get();
    }

    @Bean(name = CHANNEL_NAME)
    DirectChannelSpec outboundChannel() {
        return MessageChannels.direct();
    }
}