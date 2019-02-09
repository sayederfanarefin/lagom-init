package info.sayederfanarefin.hello.hello.impl;

import akka.Done;
import akka.stream.javadsl.Flow;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import info.sayederfanarefin.hello.hello.api.ExternalService;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

@Singleton
public class KafkaConsumer {


    private final ExternalService kafkaService;
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Inject
    public KafkaConsumer(ExternalService kafkaService) {
        this.kafkaService = kafkaService;
        kafkaService.getPosts().invoke();
        System.out.println(" ********* it works ***********");
        log.println(" ********* it works ***********" );
//        kafkaService.greetingsTopic().subscribe()
//                .atLeastOnce(Flow.fromFunction(this::displayMessage));
    }

    private Done displayMessage(String message) {
        System.out.println("Message :::::::::::  " + message);
        try {
            GreetingMessage greetingMessage = jsonMapper.readValue(message, GreetingMessage.class);
            if (StringUtils.isNotEmpty(greetingMessage.message)) {
                System.out.println("Action performed :::::::::::  " + message);

                // Do your action here
            }
        } catch (Exception ex) {
            System.out.println("Error in consuming kafka message");
        }
        return Done.getInstance();
    }
}