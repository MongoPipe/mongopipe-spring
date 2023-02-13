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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongopipe.core.config.MigrationConfig;
import org.mongopipe.core.config.MongoPipeConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.test.MyRestaurant;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TestScanPackage implements ApplicationContextAware {
  ApplicationContext applicationContext;

  @Configuration
  public static class ContextConfiguration {
    @Bean
    public MongoDBServerStarter mongoDBStarter() {
      return new MongoDBServerStarter();
    }

    // Library configuration beans next.
    @Bean
    @DependsOn("mongoDBStarter")  // Needed to have a running MongoDB server. Not needed if server is already running.
    public MongoPipeConfig getMongoPipeConfig(MongoDBServerStarter mongoDBStarter) {
      return MongoPipeConfig.builder()
          .uri("mongodb://localhost:" + mongoDBStarter.getPort())
          .databaseName("test")
          .storeHistoryEnabled(true)
          .migrationConfig(MigrationConfig.builder().pipelinesPath("pipelines").build()) // Test spaces in name and URL.
          .scanPackage("non existing package")
          .build();
    }

    @Bean
    public MongoPipeStarter getMongoPipeStarter(MongoPipeConfig mongoPipeConfig) {
      return new MongoPipeStarter(mongoPipeConfig);
    }
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Test(expected = NoSuchBeanDefinitionException.class)
  public void test() {
    applicationContext.getBean(MyRestaurant.class);
  }
}
