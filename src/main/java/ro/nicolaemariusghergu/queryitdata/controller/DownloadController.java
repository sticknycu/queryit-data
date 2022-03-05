package ro.nicolaemariusghergu.queryitdata.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import ro.nicolaemariusghergu.queryitdata.config.WebClientConfig;

@Slf4j
@RestController
@RequestMapping("/v1/download")
public class DownloadController {

    @Value("${spring.http.mega-image.base}")
    private String HTTP_BASE_MEGA_IMAGE;

    @Value("${spring.http.mega-image.photos}")
    private String HTTP_PHOTOS_MEGA_IMAGE;

    @GetMapping("/mega-image")
    public ResponseEntity getProducts() {
        WebClientConfig webClientConfig = new WebClientConfig();

        for (int i = 1; i <= 15; i++) {
            String word;
            if (i >= 10) {
                word = "" + i;
            } else {
                word = "0" + i;
            }

            System.out.println("Avem site-ul numarul " + i);
            String dataLink = HTTP_BASE_MEGA_IMAGE.replace("--", word);
            System.out.println("Site-ul este " + dataLink);
            var receivedValue = webClientConfig.getWebClientBuilder()
                    .build()
                    .get()
                    .uri(dataLink)
                    .retrieve()
                    .toEntity(String.class)
                            .block()
                                    .getBody();
            log.info("Start Received data: ");
            log.info("---------------");
            log.info(receivedValue);
            log.info("---------------");
            log.info("End received data");
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
