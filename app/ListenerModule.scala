import connectors.FileHashRetriever
import connectors.aws.S3FileHashRetriever
import play.api.{Configuration, Environment}
import play.api.inject.{Binding, Module}
import utils.{CallbackConsumer, PlayCallbackConsumer}

class ListenerModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[FileHashRetriever].to[S3FileHashRetriever],
    bind[CallbackConsumer].to[PlayCallbackConsumer]
  )
}
