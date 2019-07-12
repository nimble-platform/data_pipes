package eu.nimble.service.datapipes.config;

/**
 *
 * @author a.musumeci
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("eu.nimble"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(metaData())    ;
    }

    private ApiInfo metaData() {
        return new ApiInfo(
                "NIMBLE Data Pipes REST API",
                "REST API for managing iot data sensors streamings in internal Data Channel for NIMBLE platform",
                "2.2",
                null,
                new Contact("Andrea Musumeci", null, "andrea.musumeci@holonix.it"),
                "Apache License Version 2.0",
                "https://www.apache.org/licenses/LICENSE-2.0");
    }
}