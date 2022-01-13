package io.csra.wily.components.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;
import com.smartystreets.api.ClientBuilder;
import com.smartystreets.api.us_street.Client;
import io.csra.wily.components.converter.BooleanStringConverter;
import io.csra.wily.components.converter.LocalDateTimeConverter;
import io.csra.wily.components.interceptor.JsonHijackingInterceptor;
import io.csra.wily.components.service.AmazonS3Service;
import io.csra.wily.components.service.impl.AmazonS3ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.MultipartConfigElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is a master configuration that makes it easy to get off the ground with a new application.
 * Rather than writing your own, you can merely extend this one to get the basic stuff you'll need to
 * do most standard stuff.
 * <p>
 * This includes stuff like Jackson and Dozer Mappers, as well as a MultipartConfigElement and a RestTemplate.
 * Your extended AppConfig can house anything else you need to run your app.
 */
public class MasterApplicationConfig implements WebMvcConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MasterApplicationConfig.class);

    @Autowired
    private JsonHijackingInterceptor jsonHijackingInterceptor;

    @Autowired
    private Environment environment;

    @Bean
    public Mapper dozerBeanMapper() {
        return DozerBeanMapperBuilder.create()
                .withMappingFiles(getMappingFiles())
                .withCustomConverter(new BooleanStringConverter())
                .withCustomConverter(new LocalDateTimeConverter())
                .build();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        return factory.createMultipartConfig();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public Client smartyStreetsClient() {
        String authId = environment.getProperty("smartystreets.auth.id");
        String authToken = environment.getProperty("smartystreets.auth.token");

        if (StringUtils.isBlank(authId)) {
            LOGGER.info("No SmartyStreets auth-id (smartystreets.auth.id) Provided.");
        }

        if (StringUtils.isBlank(authToken)) {
            LOGGER.info("No SmartyStreets auth-token (smartystreets.auth.token) Provided.");
        }

        return new ClientBuilder(authId, authToken).buildUsStreetApiClient();
    }

    @Bean
    @ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
    public AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder.standard()
                .withRegion(environment.getProperty("aws.s3.bucket.region"))
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
    public AmazonS3Service amazonS3Service(Environment environment, AmazonS3 amazonS3) {
        return new AmazonS3ServiceImpl(environment, amazonS3);
    }

    protected List<String> getMappingFiles() {
        return Collections.emptyList();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jsonHijackingInterceptor);

        for (HandlerInterceptor i : getInterceptors()) {
            registry.addInterceptor(i);
        }
    }

    protected List<HandlerInterceptor> getInterceptors() {
        return new ArrayList<>();
    }

}
