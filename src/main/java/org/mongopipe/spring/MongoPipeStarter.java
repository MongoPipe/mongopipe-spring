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

import org.mongopipe.core.Pipelines;
import org.mongopipe.core.Stores;
import org.mongopipe.core.annotation.Store;
import org.mongopipe.core.config.MongoPipeConfig;
import org.mongopipe.core.exception.MongoPipeConfigException;
import org.mongopipe.core.logging.CustomLogFactory;
import org.mongopipe.core.logging.Log;
import org.mongopipe.core.runner.PipelineRunner;
import org.mongopipe.core.store.PipelineStore;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.Set;

/**
 * Start automatically the migration that will load the pipelines from a PipelineMigrationSource (usually the classpath).
 * Also load all the stores so that validation can be applied early and register them as beans.
 */
public class MongoPipeStarter implements InitializingBean, BeanDefinitionRegistryPostProcessor, ApplicationContextAware {
  private static final Log LOG = CustomLogFactory.getLogger(MongoPipeStarter.class);
  private final String migrationContextId;
  private final String scanPackage;
  private Environment environment;

  public MongoPipeStarter(MongoPipeConfig mongoPipeConfig) {
    Stores.registerConfig(mongoPipeConfig);
    migrationContextId = mongoPipeConfig.getId();
    scanPackage = mongoPipeConfig.getScanPackage();
  }

  @Override
  public void afterPropertiesSet() {
    // Start migration on startup.
    Pipelines.startMigration(migrationContextId);
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
    // Modify the application context's internal bean definition registry after its standard initialization. All regular bean definitions will have been loaded, but no beans will have been instantiated yet. This allows for adding further bean definitions before the next post-processing phase kicks in.
    // The problem with this is that Spring will call the constructor while you can only set custom fields.
    // Will be possible for PipelineStore but for the proxy Stores that are interfaces it will not work.
    //PipelineStore pipelineStore = Stores.getPipelineStore();
    //GenericBeanDefinition bd = new GenericBeanDefinition();
    //bd.setBeanClass(PipelineStore.class);
    //bd.getPropertyValues().add("runContext", RunContextProvider.getContext());
    //beanDefinitionRegistry.registerBeanDefinition("pipelineStore", bd);

    // Consider also BeanFactoryAware to register bean instances. https://stackoverflow.com/questions/57157396/how-to-register-bean-dynamically-in-spring-boot https://stackoverflow.com/questions/52399208/spring-boot-adding-and-removing-singleton-at-runtime
    // ((GenericApplicationContext)applicationContext).getDefaultListableBeanFactory().registerSingleton(...);
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
    // Register the default pipeline store and runner as beans.
    PipelineStore pipelineStore = Pipelines.getStore();
    configurableListableBeanFactory.registerSingleton("pipelineStore", pipelineStore);
    PipelineRunner pipelineRunner = Pipelines.getRunner();
    configurableListableBeanFactory.registerSingleton("pipelineRunner", pipelineRunner);

    // Register all stores as beans.
    // 'false' for not just components. Environment optional but used to exclude based on @Profile in Spring ProfileCondition class.
    ClassPathScanningProvider provider = new ClassPathScanningProvider(false, environment);
    provider.addIncludeFilter(new AnnotationTypeFilter(Store.class));
    // provider.setResourceLoader(new PathMatchingResourcePatternResolver(MongoPipeStarter.class.getClassLoader()));
    if (scanPackage == null) {
      LOG.warn("Provide a scanPackage for the MongoPipeConfig for much faster scanning.");
    }
    Set<BeanDefinition> components = provider.findCandidateComponents(scanPackage == null ? "" : scanPackage);
    components.stream().forEach(beanDefinition -> {
      try {
        Class storeClass = Class.forName(beanDefinition.getBeanClassName());
        Object store = Stores.from(storeClass);
        configurableListableBeanFactory.registerSingleton(ClassUtils.getShortNameAsProperty(storeClass), store);
      } catch (ClassNotFoundException e) {
        LOG.error(e.getMessage(), e);
        throw new MongoPipeConfigException("Could not load store for:" + beanDefinition.getBeanClassName(), e);
      }
    });
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.environment = applicationContext.getEnvironment();
  }

  // Not needed. Performed by ProfileCondition inside ClassPathScanningProvider.
  //  private boolean matchesProfile(Class storeClass) {
  //    if (!ClassUtils.isPresent("org.springframework.context.annotation.Profile", null) ||
  //        !storeClass.isAnnotationPresent(Profile.class)) {
  //      return true;
  //    }
  //    Profile classAnnotation = (Profile) storeClass.getAnnotation(Profile.class);
  //    List<String> profiles = Arrays.asList(classAnnotation.value());
  //    return profiles.stream()
  //        .filter(profile -> profile != null)
  //        .filter(profile -> profile.startsWith("!") || activeProfiles.contains(profile))
  //        .findAny()
  //        .isPresent();
  //  }

}
