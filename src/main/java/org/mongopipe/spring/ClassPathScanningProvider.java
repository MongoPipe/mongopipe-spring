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

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;

/**
 * Scan classpath for classes with specific annotation.
 */
//  Sol2: With org.reflections 0.9.12
//    Collection<URL> urls;
//    String scanPackage = "org.test";
//    if (scanPackage == null) {
//      throw new MongoPipeConfigException("Need to provide scan package for pipeline repositories. This can be for example you app top package.");
//    } else {
//      urls = ClasspathHelper.forPackage(scanPackage);
//    }
//    // Works with both 0.9.12 and 0.10.2
//    Reflections reflections = new Reflections(new ConfigurationBuilder()
//        .setUrls(urls)
//        .addScanners(new TypeAnnotationsScanner())
//    );
//    reflections.getTypesAnnotatedWith(Store.class);

// Sol3:
//    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//    Resource[] resources = resolver.getResources("classpath*:org/**/*.class");
//    for (Resource resource : resources) {
//      System.out.println(resource.getURL());
//    }

// If you do not want to specify the base packages and leave Spring look in all the basePackages, it is more complex: https://stackoverflow.com/questions/50808941/how-to-get-basepackages-of-componentscan-programatically-at-runtime
public class ClassPathScanningProvider extends ClassPathScanningCandidateComponentProvider {

  public ClassPathScanningProvider(boolean useDefaultFilters) {
    super(useDefaultFilters);
  }

  public ClassPathScanningProvider(boolean useDefaultFilters, Environment environment) {
    super(useDefaultFilters, environment);
  }

  protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
    // Because super.findCandidateComponents which will exclude interface types.
    return true;
  }
}
