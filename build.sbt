
scalikejdbcSettings

val Organization = "yohei224"
val Name = "turqey"
val ScalatraVersion = "2.4.0"
val ScalikejdbcVersion = "2.3.5"
val JettyVersion = "9.2.15.v20160210"

lazy val root = (project in file(".")).enablePlugins(SbtTwirl, JettyPlugin)


containerLibs in Jetty := Seq("org.eclipse.jetty" % "jetty-runner" % "9.2.15.v20160210" intransitive())
containerMain in Jetty := "org.eclipse.jetty.runner.Runner"
containerPort := 8081

import ScalateKeys._
seq(scalateSettings:_*)
scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
  Seq(
    TemplateConfig(
      base / "webapp" / "WEB-INF" / "templates",
      Seq(),
      Seq(),
      None
    )
  )
}


sourcesInBase := false
organization := Organization
name := Name
version := "1.0"
scalaVersion := "2.11.7"

checksums in update := Nil


// dependency settings
resolvers ++= Seq(
  Classpaths.typesafeReleases,
  "amateras-repo" at "http://amateras.sourceforge.jp/mvn/",
  "amateras-snapshot-repo" at "http://amateras.sourceforge.jp/mvn-snapshot/",
  "my-github-repo" at "http://yohei224.github.io/"
)
libraryDependencies ++= Seq(
  "com.github.yohei224"       % "gristle"                      % "0.0.1",

  "org.scala-lang"            % "scala-compiler"               % "2.11.7",
  "org.scalatra"             %% "scalatra"                     % ScalatraVersion,
  "org.scalatra"             %% "scalatra-scalate"             % ScalatraVersion,
  
  "ch.qos.logback"            % "logback-classic"              % "1.1.3",
  "com.typesafe.scala-logging"%%"scala-logging"                % "3.1.0",

  "com.typesafe.akka"        %% "akka-actor"                   % "2.3.14",
  "com.enragedginger"        %% "akka-quartz-scheduler"        % "1.4.0-akka-2.3.x" exclude("c3p0","c3p0"),
  
  "io.github.gitbucket"       % "markedj"                      % "+",
  
  "org.scalikejdbc"          %% "scalikejdbc"                  % ScalikejdbcVersion,
  "org.scalikejdbc"          %% "scalikejdbc-config"           % ScalikejdbcVersion,
  
  "org.flywaydb"              % "flyway-core"                  % "3.2.1",
  "commons-pool"              % "commons-pool"                 % "1.6",
  "commons-dbcp"              % "commons-dbcp"                 % "1.4",
  
  "com.h2database"            % "h2"                           % "1.4.190",
  "com.googlecode.java-diff-utils" % "diffutils"               % "1.2.1",
  "org.json4s"               %% "json4s-core"                  % "3.3.0",
  "org.json4s"               %% "json4s-native"                % "3.3.0",
  
  "javax.mail"                % "javax.mail-api"               % "1.5.4",
  "com.sun.mail"              % "javax.mail"                   % "1.5.4",
  
  "com.google.guava"          % "guava"                        % "19.0",
  "org.apache.tika"           % "tika-core"                    % "1.12",

  "org.eclipse.jetty"         % "jetty-webapp"                 % JettyVersion     % "container;provided",
  "org.eclipse.jetty"         %	"jetty-server"                 % JettyVersion     % "provided",
  "org.eclipse.jetty"         %	"jetty-servlet"                % JettyVersion     % "provided",
  "org.eclipse.jetty"         %	"jetty-util"                   % JettyVersion     % "provided",
  "org.eclipse.jetty"         %	"jetty-security"               % JettyVersion     % "provided",
  "org.eclipse.jetty"         %	"jetty-http"                   % JettyVersion     % "provided",
  "org.eclipse.jetty"         %	"jetty-io"                     % JettyVersion     % "provided",
  "org.eclipse.jetty"         %	"jetty-xml"                    % JettyVersion     % "provided",

  "javax.servlet"             % "javax.servlet-api"            % "3.1.0"          % "provided",
  "org.scalikejdbc"          %% "scalikejdbc-test"             % ScalikejdbcVersion % "test"
)

// Twirl settings
play.twirl.sbt.Import.TwirlKeys.templateImports += "turqey.servlet.ServletContextHolder._"
play.twirl.sbt.Import.TwirlKeys.templateImports += "turqey.servlet.SessionHolder"

// Compiler settings
scalacOptions := Seq("-deprecation", "-language:postfixOps")
javacOptions in compile ++= Seq("-target", "1.7", "-source", "1.7")
packageOptions += Package.MainClass("JettyLauncher")

// Assembly settings
test in assembly := {}
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case ("manifest.mf" :: Nil) => MergeStrategy.discard
      case _ => MergeStrategy.discard
    }
  case x => MergeStrategy.first
}

// Create executable war file
val executableConfig = config("executable").hide
Keys.ivyConfigurations += executableConfig
libraryDependencies	++= Seq(
  "org.eclipse.jetty"	%	"jetty-webapp"       % JettyVersion % "executable",
  "org.eclipse.jetty"	%	"jetty-server"       % JettyVersion % "executable"
)

val executableKey	= TaskKey[File]("executable")
executableKey	:= {
  import org.apache.ivy.util.ChecksumHelper
  import java.util.jar.{ Manifest => JarManifest }
  import java.util.jar.Attributes.{ Name => AttrName }

  val workDir	= Keys.target.value / "executable"
  val warName	= Keys.name.value + ".war"

  val log		= streams.value.log
  log info s"building executable webapp in ${workDir}"

  // initialize temp directory
  val temp	= workDir / "webapp"
  IO delete temp

  // include jetty classes
  val jettyJars	= Keys.update.value select configurationFilter(name = executableConfig.name)
  jettyJars foreach { jar =>
    IO unzip (jar, temp, (name:String) =>
      (name startsWith "javax/") ||
      (name startsWith "org/")
    )
  }

  // include original war file
  val warFile	= (Keys.`package`).value
  IO unzip (warFile, temp)

  // include launcher classes
  val classDir		= (Keys.classDirectory in Compile).value
  val launchClasses	= Seq("JettyLauncher.class")
  launchClasses foreach { name =>
    IO copyFile (classDir / name, temp / name)
  }

  // zip it up
  IO delete (temp / "META-INF" / "MANIFEST.MF")

  Keys.update.value select configurationFilter() filter { jar => jar.name.startsWith("scala-compiler") } foreach { 
    jar => IO delete (temp / "WEB-INF" / "lib" / jar.name ) }

  val contentMappings	= (temp.*** --- PathFinder(temp)).get pair relativeTo(temp)
  val manifest		= new JarManifest
  manifest.getMainAttributes put (AttrName.MANIFEST_VERSION,	"1.0")
  manifest.getMainAttributes put (AttrName.MAIN_CLASS,		"JettyLauncher")
  val outputFile		= workDir / warName
  IO jar (contentMappings, outputFile, manifest)

  // generate checksums
  Seq("md5", "sha1") foreach { algorithm =>
    IO.write(
      workDir / (warName + "." + algorithm),
      ChecksumHelper computeAsString (outputFile, algorithm)
    )
  }

  // done
  log info s"built executable webapp ${outputFile}"
  outputFile
}
/*
Keys.artifact in (Compile, executableKey) ~= {
	_ copy (`type` = "war", extension = "war"))
}
addArtifact(Keys.artifact in (Compile, executableKey), executableKey)
*/
