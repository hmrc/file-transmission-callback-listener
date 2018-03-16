import javax.inject.Provider

import connectors.FileHashRetriever
import connectors.aws.S3FileHashRetriever
import org.joda.time.DateTime
import play.api.{Configuration, Environment}
import play.api.inject.{Binding, Module}
import utils.{CallbackConsumer, InMemoryResponseConsumer, PlayCallbackConsumer, ResponseConsumer}

class ListenerModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[FileHashRetriever].to[S3FileHashRetriever],
    bind[CallbackConsumer].to[PlayCallbackConsumer],
    bind[ResponseConsumer].toProvider[InMemoryResponseConsumerProvider].eagerly()
  )
}

class InMemoryResponseConsumerProvider extends Provider[InMemoryResponseConsumer] {
  override def get(): InMemoryResponseConsumer = new InMemoryResponseConsumer(Nil, DateTime.now())
}