/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.framework.rest.config;


import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.metis.cache.redis.JedisProviderUtils;
import eu.europeana.metis.cache.redis.RedisProvider;
import eu.europeana.metis.framework.api.MetisKey;
import eu.europeana.metis.framework.dao.AuthorizationDao;
import eu.europeana.metis.framework.dao.DatasetDao;
import eu.europeana.metis.framework.dao.ExecutionDao;
import eu.europeana.metis.framework.dao.FailedRecordsDao;
import eu.europeana.metis.framework.dao.OrganizationDao;
import eu.europeana.metis.framework.dao.ZohoClient;
import eu.europeana.metis.framework.dao.ecloud.EcloudDatasetDao;
import eu.europeana.metis.framework.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.framework.rest.RestConfig;
import eu.europeana.metis.framework.service.DatasetService;
import eu.europeana.metis.framework.service.MetisAuthorizationService;
import eu.europeana.metis.framework.service.Orchestrator;
import eu.europeana.metis.framework.service.OrganizationService;
import eu.europeana.metis.framework.service.UserService;
import eu.europeana.metis.framework.workflow.AbstractMetisWorkflow;
import eu.europeana.metis.framework.workflow.Execution;
import eu.europeana.metis.framework.workflow.FailedRecords;
import eu.europeana.metis.framework.workflow.VoidMetisWorkflow;
import eu.europeana.metis.json.CustomObjectMapper;
import eu.europeana.metis.mail.config.MailConfig;
import eu.europeana.metis.search.config.SearchApplication;
import eu.europeana.metis.utils.PivotalCloudFoundryServicesReader;
import eu.europeana.metis.workflow.qa.QAWorkflow;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.Morphia;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Spring configuration class
 * Created by ymamakis on 12-2-16.
 */
@Configuration
@ComponentScan(basePackages = {"eu.europeana.metis.framework.rest"})
@PropertySource({"classpath:metis.properties", "classpath:ecloud.properties"})
@EnableWebMvc
@EnableSwagger2
@EnablePluginRegistries(AbstractMetisWorkflow.class)
@EnableScheduling
@Import({MailConfig.class, SearchApplication.class})
public class Application extends WebMvcConfigurerAdapter implements InitializingBean {

  //Redis
  @Value("${redis.host}")
  private String redisHost;
  @Value("${redis.port}")
  private int redisPort;
  @Value("${redis.password}")
  private String redisPassword;

  //Mongo
  @Value("${mongo.host}")
  private String mongoHost;
  @Value("${mongo.port}")
  private int mongoPort;
  @Value("${mongo.username}")
  private String mongoUsername;
  @Value("${mongo.password}")
  private String mongoPassword;
  @Value("${mongo.db}")
  private String mongoDb;

  //Ecloud
  @Value("${ecloud.baseMcsUrl}")
  private String ecloudBaseMcsUrl;
  @Value("${ecloud.username}")
  private String ecloudUsername;
  @Value("${ecloud.password}")
  private String ecloudPassword;

  private MorphiaDatastoreProvider provider;
  private MongoClientURI mongoClientURI;
  private RedisProvider redisProvider;

  @Autowired
  @Lazy
  private RestConfig restConfig;

  /**
   * Used for overwriting properties if cloud foundry environment is used
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    String vcapServicesJson = System.getenv().get("VCAP_SERVICES");
    if (StringUtils.isNotEmpty(vcapServicesJson) && !StringUtils.equals(vcapServicesJson, "{}")) {
      PivotalCloudFoundryServicesReader vcapServices = new PivotalCloudFoundryServicesReader(
          vcapServicesJson);

      mongoClientURI = vcapServices.getMongoClientUriFromService();
      redisProvider = vcapServices.getRedisProviderFromService();
    }
  }

  @Bean(name = "morphiaDatastoreProvider")
  MorphiaDatastoreProvider getMorphiaDatastoreProvider() {
    if (mongoClientURI != null) {
      provider = new MorphiaDatastoreProvider(new MongoClient(mongoClientURI),
          mongoClientURI.getDatabase());
      return provider;
    } else {
      ServerAddress address = new ServerAddress(mongoHost, mongoPort);
      MongoCredential mongoCredential;
      MongoClient mongoClient;
      if (StringUtils.isNotEmpty(mongoUsername) && StringUtils.isNotEmpty(mongoPassword)) {
        mongoCredential = MongoCredential
            .createCredential(mongoUsername, mongoDb, mongoPassword.toCharArray());
        mongoClient = new MongoClient(address, Arrays.asList(mongoCredential));
      } else {
        mongoClient = new MongoClient(address);
      }
      provider = new MorphiaDatastoreProvider(mongoClient, mongoDb);
      return provider;
    }
  }

  @Bean
  @Order(100)
  ZohoClient getZohoRestClient() {
    return restConfig.getZohoClient();
  }

  @Bean(name = "jedisProviderUtils")
  JedisProviderUtils getJedisProviderUtils() {
    if (redisProvider != null) {
      return new JedisProviderUtils(redisProvider.getJedis());
    } else {
      RedisProvider redisProvider = new RedisProvider(redisHost, redisPort, redisPassword);
      return new JedisProviderUtils(redisProvider.getJedis());
    }
  }

  @Bean
  public View json() {
    MappingJackson2JsonView view = new MappingJackson2JsonView();
    view.setPrettyPrint(true);
    view.setObjectMapper(new CustomObjectMapper());
    return view;
  }

  @Bean
  public ViewResolver viewResolver() {
    return new BeanNameViewResolver();
  }

  @Bean
  @DependsOn(value = "morphiaDatastoreProvider")
  public ExecutionDao getExecutionDao() {
    Morphia morphia = new Morphia();
    morphia.map(Execution.class);
    return new ExecutionDao(provider.getDatastore().getMongo(), morphia,
        provider.getDatastore().getDB().getName());
  }

  @Bean
  @DependsOn(value = "morphiaDatastoreProvider")
  public FailedRecordsDao getFailedRecordsDao() {
    Morphia morphia = new Morphia();
    morphia.map(FailedRecords.class);
    return new FailedRecordsDao(provider.getDatastore().getMongo(), morphia,
        provider.getDatastore().getDB().getName());
  }

  @Bean
  @DependsOn(value = "morphiaDatastoreProvider")
  public AuthorizationDao getAuthorizationDao() {
    Morphia morphia = new Morphia();
    morphia.map(MetisKey.class);
    return new AuthorizationDao();
  }

  @Bean
  public DatasetDao getDatasetDao() {
    return new DatasetDao();
  }

  @Bean
  DataSetServiceClient dataSetServiceClient() {
    return new DataSetServiceClient(ecloudBaseMcsUrl, ecloudUsername, ecloudPassword);
  }

  @Bean
  @DependsOn(value = "dataSetServiceClient")
  EcloudDatasetDao ecloudDatasetDao() {
    return new EcloudDatasetDao();
  }

  @Bean
  public OrganizationDao getOrganizationDao() {
    return new OrganizationDao();
  }

  @Bean
  public DatasetService getDatasetService() {
    return new DatasetService();
  }

  @Bean
  public OrganizationService getOrganizationService() {
    return new OrganizationService();
  }

  @Bean
  public MetisAuthorizationService getMetisAuthorizationService() {
    return new MetisAuthorizationService();
  }

  @Bean
  public UserService getUserService() {
    return new UserService();
  }

  @Bean
  public Orchestrator getOrchestrator() {
    return new Orchestrator();
  }

  @Bean
  public VoidMetisWorkflow getVoidMetisWorkflow() {
    return new VoidMetisWorkflow();
  }

  @Bean
  @DependsOn("jedisProviderUtils")
  public QAWorkflow getStatisticsWorkflow() {
    return new QAWorkflow();
  }


  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new MappingJackson2HttpMessageConverter());
    super.configureMessageConverters(converters);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.regex("/.*"))
        .build()
        .apiInfo(apiInfo());
  }

  private ApiInfo apiInfo() {
    ApiInfo apiInfo = new ApiInfo(
        "Metis framework REST API",
        "Metis framework REST API for Europeana",
        "v1",
        "API TOS",
        "development@europeana.eu",
        "EUPL Licence v1.1",
        ""
    );
    return apiInfo;
  }
}
