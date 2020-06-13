package de.tgbyte.issue1152reproducer;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@SpringBootTest
public class ReproducerTest {

  @Autowired
  private WebClient.Builder webClientBuilder;

  private MockWebServer server;

  @BeforeEach
  public void setup() {
    this.server = new MockWebServer();
    this.server.setDispatcher(new Dispatcher() {

      @Override
      public MockResponse dispatch(RecordedRequest recordedRequest) {
        var response = new MockResponse();
        response.setHeader("content-type", "application/json; charset=UTF-8");
        response.setBody("{\"status\":\"ok\"");
        return response;
      }
    });
  }

  @Test
  public void test() {
    var webClient = webClientBuilder
        .baseUrl(server.url("/").toString())
        .build();

    var flux = Flux
        .just(1)
        .flatMap(integer -> webClient
            .get()
            .retrieve()
            .bodyToMono(String.class)
        )
        .repeat(Integer.MAX_VALUE);

    flux.blockLast();
  }
}
