package info.sayederfanarefin.hello.hello.impl;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.TextMessage;
import akka.http.javadsl.model.ws.WebSocketRequest;
import akka.http.javadsl.model.ws.WebSocketUpgradeResponse;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.Sink;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import info.sayederfanarefin.hello.hello.api.ExternalService;
import info.sayederfanarefin.hello.hello.api.GreetingMessage;

import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

import java.util.concurrent.CompletionStage;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

@Singleton
public class KafkaConsumer {


    private final ExternalService kafkaService;
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Inject
    public KafkaConsumer(ExternalService kafkaService) {
        this.kafkaService = kafkaService;
//        kafkaService.getPosts().invoke();
        System.out.println(" ********* it works ***********");
        ccc();
        log.println(" ********* it works ***********" );
//        kafkaService.greetingsTopic().subscribe()
//                .atLeastOnce(Flow.fromFunction(this::displayMessage));
    }


    private void ccc(){
        ActorSystem system = ActorSystem.create();
        Materializer materializer = ActorMaterializer.create(system);
        Http http = Http.get(system);

// print each incoming text message
// would throw exception on non strict or binary message
        Sink<Message, CompletionStage<Done>> printSink =
                Sink.foreach((message) ->
                        System.out.println("Got message: " + message.asTextMessage().getStrictText())
                );

// send this as a message over the WebSocket
        Source<Message, NotUsed> helloSource =
                Source.single(TextMessage.create("hello world"));


        Flow<Message, Message, CompletionStage<WebSocketUpgradeResponse>> webSocketFlow =
                http.webSocketClientFlow(WebSocketRequest.create("ws://echo.websocket.org"));


        Pair<CompletionStage<WebSocketUpgradeResponse>, CompletionStage<Done>> pair =
                helloSource.viaMat(webSocketFlow, Keep.right())
                        .toMat(printSink, Keep.both())
                        .run(materializer);


// The first value in the pair is a CompletionStage<WebSocketUpgradeResponse> that
// completes when the WebSocket request has connected successfully (or failed)
        CompletionStage<WebSocketUpgradeResponse> upgradeCompletion = pair.first();

// the second value is the completion of the sink from above
// in other words, it completes when the WebSocket disconnects
        CompletionStage<Done> closed = pair.second();

        CompletionStage<Done> connected = upgradeCompletion.thenApply(upgrade->
        {
            // just like a regular http request we can access response status which is available via upgrade.response.status
            // status code 101 (Switching Protocols) indicates that server support WebSockets
            if (upgrade.response().status().equals(StatusCodes.SWITCHING_PROTOCOLS)) {
                return Done.getInstance();
            } else {
                throw new RuntimeException(("Connection failed: " + upgrade.response().status()));
            }
        });

// in a real application you would not side effect here
// and handle errors more carefully
        connected.thenAccept(done -> System.out.println("Connected"));
        closed.thenAccept(done -> System.out.println("Connection closed"));
    }

//    private Done displayMessage(String message) {
//        System.out.println("Message :::::::::::  " + message);
//        try {
//            GreetingMessage greetingMessage = jsonMapper.readValue(message, GreetingMessage.class);
//            if (StringUtils.isNotEmpty(greetingMessage.message)) {
//                System.out.println("Action performed :::::::::::  " + message);
//
//                // Do your action here
//            }
//        } catch (Exception ex) {
//            System.out.println("Error in consuming kafka message");
//        }
//        return Done.getInstance();
//    }
}