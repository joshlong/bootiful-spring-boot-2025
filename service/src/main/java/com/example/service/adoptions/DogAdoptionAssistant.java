package com.example.service.adoptions;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration 
@RegisterReflectionForBinding (DogAdoptionSuggestion.class)
class DogAdoptionAssistant {

    @Bean
    ApplicationRunner applicationRunner(ChatClient chatClient) {
        return args -> {

            var content = chatClient
                    .prompt("do you have any neurotic dogs?")
                    .call()
                    .entity(DogAdoptionSuggestion.class);
            System.out.println("content [" + content + "]");

        };
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder,
                          DogRepository dogRepository,
                          VectorStore vectorStore) {

        if (false)
            dogRepository.findAll().forEach(dog -> {
                var dogument = new Document("id: %s, name: %s, description: %s".formatted(
                        dog.id(), dog.name(), dog.description()
                ));
                vectorStore.add(List.of(dogument));
            });

        var system = """
                You are an AI powered assistant to help people adopt a dog from the adoption\s
                agency named Pooch Palace with locations in Antwerp, Seoul, Tokyo, Singapore, Paris,\s
                Mumbai, New Delhi, Barcelona, San Francisco, and London. Information about the dogs available\s
                will be presented below. If there is no information, then return a polite response suggesting we\s
                don't have any dogs available.
                """;
        return builder
                .defaultSystem(system)
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
    }
}


record DogAdoptionSuggestion(int id, String name, String description) {
}