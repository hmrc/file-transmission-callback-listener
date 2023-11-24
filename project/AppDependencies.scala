import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val bootstrapVersion = "8.0.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapVersion
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30"     % bootstrapVersion   % Test,
    "org.mockito" %% "mockito-scala-scalatest"    % "1.17.29"          % Test,
  )
}
