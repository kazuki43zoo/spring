/**
 *    Copyright 2010-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.spring.annotation.usesjava8;

import static org.junit.Assert.*;

import com.mockrunner.mock.jdbc.MockDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * Test for the MapperScannerRegistrar for repeatable.
 */
public final class MapperScanRepeatableTest {
  private AnnotationConfigApplicationContext applicationContext;

  @Before
  public void setupContext() {
    applicationContext = new AnnotationConfigApplicationContext();

    setupSqlSessionFactory("sqlSessionFactory");
  }

  private void startContext() {
    applicationContext.refresh();
    applicationContext.start();

    // this will throw an exception if the beans cannot be found
    applicationContext.getBean("sqlSessionFactory");
  }

  @After
  public void assertNoMapperClass() {
    try {
      // concrete classes should always be ignored by MapperScannerPostProcessor
      assertBeanNotLoaded("mapperClass");

      // no method interfaces should be ignored too
      assertBeanNotLoaded("package-info");
      // assertBeanNotLoaded("annotatedMapperZeroMethods"); // as of 1.1.0 mappers
      // with no methods are loaded
    } finally {
      applicationContext.close();
    }
  }

  private void setupSqlSessionFactory(String name) {
    GenericBeanDefinition definition = new GenericBeanDefinition();
    definition.setBeanClass(SqlSessionFactoryBean.class);
    definition.getPropertyValues().add("dataSource", new MockDataSource());
    applicationContext.registerBeanDefinition(name, definition);
  }

  private void assertBeanNotLoaded(String name) {
    try {
      applicationContext.getBean(name);
      fail("Spring bean should not be defined for class " + name);
    } catch (NoSuchBeanDefinitionException nsbde) {
      // success
    }
  }

  @Test
  public void testScanWithMapperScanIsRepeat() {
    applicationContext.register(AppConfigWithMapperScanIsRepeat.class);

    startContext();

    applicationContext.getBean("ds1Mapper");
    applicationContext.getBean("ds2Mapper");
  }

  @Configuration
  @MapperScan(basePackages = "org.mybatis.spring.annotation.mapper.ds1")
  @MapperScan(basePackages = "org.mybatis.spring.annotation.mapper.ds2")
  public static class AppConfigWithMapperScanIsRepeat {
  }

}
