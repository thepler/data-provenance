resolvers += Resolver.url("hmrc-sbt-plugin-releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
addSbtPlugin("me.lessis"         % "bintray-sbt"         % "0.3.0")
addSbtPlugin("com.eed3si9n"      % "sbt-buildinfo" % "0.6.1")
addSbtPlugin("uk.gov.hmrc"       % "sbt-git-stamp" % "5.3.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header"          % "1.6.0")
addSbtPlugin("com.typesafe.sbt"  % "sbt-native-packager" % "1.1.4")
addSbtPlugin("com.github.gseitz" % "sbt-release"   % "1.0.4")