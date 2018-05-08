package io.csra.wily.components.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.MultipartConfigElement;

import com.smartystreets.api.ClientBuilder;
import com.smartystreets.api.us_street.Client;
import io.csra.wily.components.converter.BooleanStringConverter;
import io.csra.wily.components.converter.DozerMapperPlus;
import io.csra.wily.components.interceptor.JsonHijackingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.dozer.CustomConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This is a master configuration that makes it easy to get off the ground with a new application.
 * Rather than writing your own, you can merely extend this one to get the basic stuff you'll need to
 * do most standard stuff.
 *
 * This includes stuff like Jackson and Dozer Mappers, as well as a MultipartConfigElement and a RestTemplate.
 * Your extended AppConfig can house anything else you need to run your app.
 *
 */
public class MasterApplicationConfig implements WebMvcConfigurer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MasterApplicationConfig.class);

	@Autowired
	private JsonHijackingInterceptor jsonHijackingInterceptor;

	@Autowired
	private Environment environment;

	@Bean
	public DozerMapperPlus dozerBeanMapper() {
		DozerMapperPlus mapper = new DozerMapperPlus(getMappingFiles());
		Map<String, CustomConverter> map = new HashMap<>();
		map.put("booleanStringConverter", new BooleanStringConverter());
		mapper.setCustomConvertersWithId(map);
		return mapper;
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

	protected List<String> getMappingFiles() {
		return Collections.emptyList();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(jsonHijackingInterceptor);

		if(getInterceptors() != null) {
			for(HandlerInterceptor i : getInterceptors()) {
				registry.addInterceptor(i);
			}
		}
	}

	protected List<HandlerInterceptor> getInterceptors() {
		return null;
	}

}
