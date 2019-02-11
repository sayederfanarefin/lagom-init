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

@Singleton
public class KafkaConsumer {

    String url= "wss://api-pub.bitfinex.com/ws/2" ;
    String src =
            "{ \"event\": \"subscribe\", \"channel\": \"book\",  \"symbol\": \"tBTCUSD\", \"prec\": \"P0\", \"freq\": \"F0\" }";

    TextMessage textMessage = TextMessage.create(src);

    public KafkaConsumer() {
        System.out.println(" ********* it works ***********");
        getBitfinex();
    }


    private void getBitfinex(){

        Sink<Message, CompletionStage<Done>> printSink =
                Sink.foreach((message) ->
                        System.out.println("****************************Got message: " + message.asTextMessage().getStrictText())
                );


        ActorSystem system = ActorSystem.create();
        Materializer materializer = ActorMaterializer.create(system);
        Http http = Http.get(system);

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

        CompletionStage<WebSocketUpgradeResponse> upgradeCompletion = pair.first();

        CompletableFuture<Optional<Message>> closed = pair.second();

        CompletionStage<Done> connected = upgradeCompletion.thenApply(upgrade->
        {

            System.out.println("************* response *************** " + upgrade.response().status());

            // just like a regular http request we can access response status which is available via upgrade.response.status
            // status code 101 (Switching Protocols) indicates that server support WebSockets
            if (upgrade.response().status().equals(StatusCodes.SWITCHING_PROTOCOLS)) {
                return Done.getInstance();
            } else {
                throw new RuntimeException(("Connection failed: " + upgrade.response().status()));
            }
        });

        connected.thenAccept(done -> connected());
        closed.thenAccept(done -> System.out.println("************* Connection closed"));

// at some later time we want to disconnect
   //     pair.second().complete(Optional.empty());
    }

    private void connected(){
        System.out.println("************* Connected");
    }

}