package io.csra.wily.components.doc;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * This represents a basic Swagger configuration for all of your endpoints. Simply having this jar on your classpath
 * will automagically set the title, description, license, terms of service and version in the ui..
 */
@Configuration
public class OpenApi3Config {

    private static final String DEFAULT_APPLICATION_TITLE = "Application API";
    private static final String DEFAULT_APP_API_VERSION = "0";
    private static final String LICENSE_NAME = "Apache 2.0";
    private static final String LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0";

    private final Environment environment;

    public OpenApi3Config(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(environment.getProperty("app.name", DEFAULT_APPLICATION_TITLE))
                        .description(environment.getProperty("app.description"))
                        .license(new License().name(LICENSE_NAME).url(LICENSE_URL))
                        .termsOfService("http://www.google.com/search?q=terms+of+service")
                        .version(environment.getProperty("app.api.version", DEFAULT_APP_API_VERSION))
                );
    }

}
