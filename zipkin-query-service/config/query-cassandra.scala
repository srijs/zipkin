/*
 * Copyright 2012 Twitter Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.twitter.app.App
import com.twitter.logging.{ConsoleHandler, Level, LoggerFactory}
import com.twitter.zipkin.builder.{ZipkinServerBuilder, QueryServiceBuilder}
import com.twitter.zipkin.cassandra
import com.twitter.zipkin.cassandra.CassandraSpanStoreFactory
import com.twitter.zipkin.storage.Store

val serverPort = sys.env.get("QUERY_PORT").getOrElse("9411").toInt
val adminPort = sys.env.get("QUERY_ADMIN_PORT").getOrElse("9901").toInt
val logLevel = sys.env.get("QUERY_LOG_LEVEL").getOrElse("INFO")

object Factory extends App with CassandraSpanStoreFactory

Factory.cassandraDest.parse(sys.env.get("CASSANDRA_CONTACT_POINTS").getOrElse("localhost"))

val username = sys.env.get("CASSANDRA_USERNAME")
val password = sys.env.get("CASSANDRA_PASSWORD")

if (username.isDefined && password.isDefined) {
  Factory.cassandraUsername.parse(username.get)
  Factory.cassandraPassword.parse(password.get)
}

val cluster = Factory.createClusterBuilder().build()
val storeBuilder = Store.Builder(new cassandra.SpanStoreBuilder(cluster))

val loggerFactory = new LoggerFactory(
  node = "",
  level = Level.parse(logLevel),
  handlers = List(ConsoleHandler())
)

QueryServiceBuilder(
  storeBuilder,
  serverBuilder = ZipkinServerBuilder(serverPort, adminPort).loggers(List(loggerFactory))
)
