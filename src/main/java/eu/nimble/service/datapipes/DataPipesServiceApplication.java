package eu.nimble.service.datapipes;

/**
 * @author a.musumeci
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableCircuitBreaker
@EnableEurekaClient
@ImportAutoConfiguration(FeignAutoConfiguration.class)
@EnableFeignClients(basePackages = {"eu.nimble.common.rest.identity", "eu.nimble.service.datachannel", "eu.nimble.service.datapipes"})
@RestController
@SpringBootApplication(scanBasePackages = {"eu.nimble.common.rest.identity", "eu.nimble.service.datachannel", "eu.nimble.service.datapipes"})
@EnableSwagger2
public class DataPipesServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(DataPipesServiceApplication.class);

    public static void main(String[] args) {
            logger.info("starting...");
        new SpringApplication(DataPipesServiceApplication.class).run(args);
    }

    @Value("${nimble.corsEnabled}")
    private String corsEnabled;

    @Bean
    public WebMvcConfigurer corsConfigurer() {

        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                if (corsEnabled.equals("true")) {
                    logger.info("Enabling CORS...");
                    registry.addMapping("/**").allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH");
                }
            }
        };
    }
}
