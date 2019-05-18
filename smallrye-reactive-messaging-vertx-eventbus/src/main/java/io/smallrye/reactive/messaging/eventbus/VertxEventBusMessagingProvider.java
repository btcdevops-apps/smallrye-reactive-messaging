package io.smallrye.reactive.messaging.eventbus;

import io.smallrye.reactive.messaging.spi.IncomingConnectorFactory;
import io.smallrye.reactive.messaging.spi.OutgoingConnectorFactory;
import io.vertx.reactivex.core.Vertx;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.MessagingProvider;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class VertxEventBusMessagingProvider implements OutgoingConnectorFactory, IncomingConnectorFactory {

  @Inject
  private Instance<Vertx> instanceOfVertx;

  private boolean internalVertxInstance = false;
  private Vertx vertx;

  public void terminate(@Observes @BeforeDestroyed(ApplicationScoped.class) Object event) {
    if (internalVertxInstance) {
      vertx.close();
    }
  }

  @PostConstruct
  void init() {
    if (instanceOfVertx.isUnsatisfied()) {
      internalVertxInstance = true;
      this.vertx = Vertx.vertx();
    } else {
      this.vertx = instanceOfVertx.get();
    }
  }


  @Override
  public PublisherBuilder<? extends Message> getPublisherBuilder(Config config) {
    return new EventBusSource(vertx, config).source();
  }

  @Override
  public SubscriberBuilder<? extends Message, Void> getSubscriberBuilder(Config config) {
    return new EventBusSink(vertx, config).sink();
  }

  @Override
  public Class<? extends MessagingProvider> type() {
    return VertxEventBus.class;
  }

}
