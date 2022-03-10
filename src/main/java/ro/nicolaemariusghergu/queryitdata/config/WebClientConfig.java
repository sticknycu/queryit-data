package ro.nicolaemariusghergu.queryitdata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder getWebClientBuilder() {
        return WebClient
                .builder();
    }
}
