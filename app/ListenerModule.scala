import javax.inject.Singleton
import java.time.LocalDate

import com.google.inject.{AbstractModule, Provides}
import utils.{InMemoryResponseConsumer, ResponseConsumer}

class ListenerModule extends AbstractModule {
  override def configure(): Unit = {}

  @Provides @Singleton
  def responseConsumer(): ResponseConsumer = new InMemoryResponseConsumer(LocalDate.now, Nil)
}
