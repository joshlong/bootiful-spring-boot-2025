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

@Externalized(IntegrationConfiguration.DOGS_CHANNEL)
public record DogAdoptionEvent(int dogId) {
}

@Configuration
class IntegrationConfiguration {

    static final String DOGS_CHANNEL = "dogsChannel";

    @Bean(name = DOGS_CHANNEL)
    DirectChannelSpec dogsChannel() {
        return MessageChannels.direct();
    }

    @Bean
    IntegrationFlow integrationFlow(@Qualifier(DOGS_CHANNEL) MessageChannel channel) {
        return IntegrationFlow
                .from(channel)
                .handle((GenericHandler<DogAdoptionEvent>) (payload, headers) -> {
                    System.out.println("payload " + payload);
                    headers.forEach((k, v) -> System.out.println(k + " " + v));
                    return null;
                })
                .get();
    }
}