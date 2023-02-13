/*
 * Copyright (c) 2022 - present Cristian Donoiu, Ionut Sergiu Peschir
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongopipe.spring;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.mongopipe.core.logging.CustomLogFactory;
import org.mongopipe.core.logging.Log;
import org.springframework.beans.factory.InitializingBean;

/**
 * Starts a MongoDB server standalone.
 */
public class MongoDBServerStarter implements InitializingBean {
  private static final Log LOG = CustomLogFactory.getLogger(MongoDBServerStarter.class);
  /** please store Starter or RuntimeConfig in a static final field if you want to use artifact store caching (or else disable caching) */
  public static final MongodStarter STARTER = MongodStarter.getDefaultInstance();

  private MongodExecutable mongodExecutable;
  private MongodProcess mongod;

  private static int port;
  private static MongodConfig mongodConfig;
  private MongoDatabase db;
  private MongoClient mongoClient;

  static {
    try {
      // These 2 needs to be static pe JVM or Class in order for the tests to
      // not fail.
      // See
      // https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/blob/de.flapdoodle.embed.mongo-3.5.0/README.md#usage---optimization
      port = Network.getFreeServerPort();
      mongodConfig = MongodConfig.builder().version(Version.V4_4_17).net(new Net(port, Network.localhostIsIPv6())).build();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void setUp() throws Exception {
    LOG.info("---------- Database setup ----------");
    mongodExecutable = STARTER.prepare(mongodConfig);
    mongod = mongodExecutable.start();

    ConnectionString connectionString = new ConnectionString("mongodb://localhost:" + port);
    MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
    mongoClient = MongoClients.create(mongoClientSettings);
    db = mongoClient.getDatabase("test");

  }

  @Override
  public void afterPropertiesSet() throws Exception {
    setUp();
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public MongoDatabase getDb() {
    return db;
  }

  public void setDb(MongoDatabase db) {
    this.db = db;
  }
}
