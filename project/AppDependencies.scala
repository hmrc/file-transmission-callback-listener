import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val bootstrapVersion = "5.24.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapVersion
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28"     % bootstrapVersion   % "test, it",
    "org.mockito" %% "mockito-scala-scalatest"    % "1.17.5"           % Test,
  )
  //   "org.pegdown"            %  "pegdown"                     % "1.6.0"             % Test,
  //   "com.typesafe.play"      %% "play-test"                   % PlayVersion.current % Test,
  //   "org.mockito"            %  "mockito-core"                % "2.6.2"             % Test,
  //   "org.scalamock"          %% "scalamock-scalatest-support" % "3.5.0"             % Test,
  //   "org.scalatestplus.play" %% "scalatestplus-play"          % "4.0.0"             % Test,
  //   "com.typesafe.play"      %% "play-ws"                     % PlayVersion.current % Test,
  //   "commons-io"             %  "commons-io"                  % "2.6"               % Test,
  //   "org.scalamock"          %% "scalamock-scalatest-support" % "3.5.0"             % Test
  // )
}
