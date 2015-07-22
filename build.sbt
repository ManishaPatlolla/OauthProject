name := """OauthProject"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  javaJpa,
  "org.hibernate" % "hibernate-entitymanager" % "4.3.8.Final",
  "mysql" % "mysql-connector-java" % "5.1.6",
  "javax.persistence" % "persistence-api" % "1.0.2"
  //"com.typesafe.play" % "play-jdbc_2.11" % "2.4.2"
  //"com.typesafe.play" % "play_2.10" % "2.4.0"

)
// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
