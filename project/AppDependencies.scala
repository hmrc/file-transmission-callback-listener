import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-25" % "4.9.0"
  )

  def test(scope: String = "test") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "3.2.0" % scope,
    "uk.gov.hmrc"            %% "http-verbs-test"             % "1.3.0"             % scope,
    "org.scalatest"          %% "scalatest"                   % "3.0.5"             % scope,
    "org.pegdown"            % "pegdown"                      % "1.6.0"             % scope,
    "com.typesafe.play"      %% "play-test"                   % PlayVersion.current % scope,
    "org.mockito"            % "mockito-core"                 % "2.6.2"             % scope,
    "org.scalamock"          %% "scalamock-scalatest-support" % "3.5.0"             % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"          % "2.0.1"             % scope,
    "com.typesafe.play"      %% "play-ws"                     % PlayVersion.current % scope,
    "commons-io"             % "commons-io"                   % "2.6"               % scope,
    "org.scalatest" %% "scalatest" % "3.0.5" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.mockito"            % "mockito-core"                 % "2.6.2"             % scope,
    "org.scalamock"          %% "scalamock-scalatest-support" % "3.5.0"             % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
  )



}
