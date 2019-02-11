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


import javax.inject.Inject;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
        log.println(" ********* it works ***********");
//        kafkaService.greetingsTopic().subscribe()
//                .atLeastOnce(Flow.fromFunction(this::displayMessage));
    }


    private void aaa(){

    }
    private void ccc() {
        String url= "wss://api-pub.bitfinex.com/ws/2" ;
        String src =
                "{ \"event\": \"subscribe\", \"channel\": \"book\",  \"symbol\": \"tBTCUSD\", \"prec\": \"P0\", \"freq\": \"F0\" }";

        TextMessage textMessage = TextMessage.create(src);

        ActorSystem system = ActorSystem.create();
        Materializer materializer = ActorMaterializer.create(system);
        Http http = Http.get(system);

// print each incoming text message
// would throw exception on non strict or binary message
        Sink<Message, CompletionStage<Done>> printSink =
                Sink.foreach((message) ->
                        System.out.println("****************************Got message: " + message.asTextMessage().getStrictText())
                );

// send this as a message over the WebSocket






        // emit "one" and then "two" and then keep the source from completing
        final Source<Message, CompletableFuture<Optional<Message>>> source =
                Source.from(Arrays.<Message>asList(textMessage))
                        .concatMat(Source.maybe(), Keep.right());


        final Flow<Message, Message, CompletableFuture<Optional<Message>>> flow =
                Flow.fromSinkAndSourceMat(
                        printSink,
                        source,
                        Keep.right());

        final Pair<CompletionStage<WebSocketUpgradeResponse>, CompletableFuture<Optional<Message>>> pair =
                http.singleWebSocketRequest(
                        WebSocketRequest.create(url),
                        flow,
                        materializer);

 //     at some later time we want to disconnect
        pair.second().complete(Optional.empty());

    }

}