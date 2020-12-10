import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-27" % "3.2.0"
  )

  def test(scope: String = "test") = Seq(
    "org.pegdown"            % "pegdown"                      % "1.6.0"             % scope,
    "com.typesafe.play"      %% "play-test"                   % PlayVersion.current % scope,
    "org.mockito"            % "mockito-core"                 % "2.6.2"             % scope,
    "org.scalamock"          %% "scalamock-scalatest-support" % "3.5.0"             % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"          % "4.0.0"             % scope,
    "com.typesafe.play"      %% "play-ws"                     % PlayVersion.current % scope,
    "commons-io"             % "commons-io"                   % "2.6"               % scope,
    "org.scalamock"          %% "scalamock-scalatest-support" % "3.5.0"             % scope
  )



}
