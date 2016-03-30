// JDBC ドライバーの指定を忘れずに
libraryDependencies += "com.h2database" % "h2" % "1.4.190"


addSbtPlugin("org.scalikejdbc" %% "scalikejdbc-mapper-generator" % "2.2.+")
