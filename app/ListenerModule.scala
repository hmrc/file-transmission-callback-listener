import javax.inject.Provider

import java.time.LocalDate
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import utils.{InMemoryResponseConsumer, ResponseConsumer}

class ListenerModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[ResponseConsumer].toProvider[InMemoryResponseConsumerProvider]
  )
}

class InMemoryResponseConsumerProvider extends Provider[InMemoryResponseConsumer] {
  override def get(): InMemoryResponseConsumer = new InMemoryResponseConsumer(LocalDate.now(), Nil)
}