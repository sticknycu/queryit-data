package ro.nicolaemariusghergu.queryitdata.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

@Slf4j
@RestController
@RequestMapping("/v1/download")
public class DownloadController {

    @Value("${spring.http.mega-image.base}")
    private String HTTP_BASE_MEGA_IMAGE;

    @Value("${spring.http.mega-image.photos}")
    private String HTTP_PHOTOS_MEGA_IMAGE;

    @Value("${spring.http.base-folder}")
    private String HTTP_BASE_DIRECTORY;

    @Value("${spring.http.mega-image.directory}")
    private String HTTP_MEGA_IMAGE_DIRECTORY;

    @Value("${spring.http.mega-image.directory.jsons}")
    private String HTTP_MEGA_IMAGE_DIRECTORY_JSONS;

    @Value("${spring.http.mega-image.directory.photos}")
    private String HTTP_MEGA_IMAGE_DIRECTORY_PHOTOS;

    @GetMapping("/mega-image")
    public ResponseEntity<String> getProducts() throws IOException, InterruptedException, JSONException {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        for (int i = 1; i <= 15; i++) {
            String word;
            if (i >= 10) {
                word = "" + i;
            } else {
                word = "0" + i;
            }

            log.info("Avem site-ul numarul " + i);
            String dataLink = HTTP_BASE_MEGA_IMAGE.replace("--", word);
            log.info("Site-ul este " + dataLink);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(dataLink))
                    .build();

            response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            JSONObject jsonObject = new JSONObject(response.body());

            String categoryName = jsonObject.getJSONObject("data")
                    .getJSONObject("categoryProductSearch")
                    .getJSONArray("categorySearchTree")
                    .getJSONObject(0)
                    .getJSONArray("categoryDataList")
                    .getJSONObject(0)
                    .getJSONObject("categoryData")
                    .getJSONObject("facetData")
                    .get("name")
                    .toString();

            // If already data exists, I don't want to download it again
            performWritingJsons(response.body(), categoryName, HTTP_MEGA_IMAGE_DIRECTORY, HTTP_MEGA_IMAGE_DIRECTORY_JSONS);

            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONObject("categoryProductSearch").getJSONArray("products");

            log.info("Iau toate informatiile din fiecare categorie...");

            log.info("=== Categoria " + categoryName + " ===");
            for (int j = 0; j < jsonArray.length(); j++) {
                String productName = jsonArray
                        .getJSONObject(j)
                        .getString("name");

                log.info("productName= " + productName);

                try {
                    String urlImage = jsonArray
                            .getJSONObject(j)
                            .getJSONArray("images")
                            .getJSONObject(2) // the correct array for url icon
                            .getString("url");
                    String iconUrl = HTTP_PHOTOS_MEGA_IMAGE + urlImage;

                    log.info("Icon URL-ul lui " + productName + " din categoria " + categoryName + " poate fi gasit la adresa:");
                    log.info(iconUrl);
                    log.info("\n");

                    performWritingIcons(iconUrl, categoryName, productName, HTTP_MEGA_IMAGE_DIRECTORY, HTTP_MEGA_IMAGE_DIRECTORY_PHOTOS);
                } catch (Exception e) {
                    log.info("N-am gasit url-ul la " + productName);
                }

            }
        }
        if (response.body() == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    private void performWritingJsons(String responseBody, String category, String baseDirectory, String baseDirectoryJsons) throws IOException {
        // I don't want to write the data if already exists
        String fileName = category + ".json";
        String filePath = HTTP_BASE_DIRECTORY + baseDirectory + baseDirectoryJsons + fileName;
        File file = new File(filePath);

        if (file.exists()) {
            log.info("Informatiile despre categoria " + category + " exista deja pe local!");
        } else {
            InputStream inputStream = new ByteArrayInputStream(responseBody.getBytes());
            try (ReadableByteChannel rbc = Channels.newChannel(inputStream)) {
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                }
            }
        }
    }

    private void performWritingIcons(String url, String category, String productName, String baseDirectory, String baseDirectoryPhotos) throws IOException {
        URL website = new URL(url);
        // I don't want to write the data if already exists
        String fileName = productName + ".jpg";
        String directoryPath = HTTP_BASE_DIRECTORY + baseDirectory + baseDirectoryPhotos + category;
        String filePath = directoryPath + "/" + fileName;
        File file = new File(filePath);

        File directoryFile = new File(directoryPath);
        if (!directoryFile.exists()) {
            directoryFile.mkdir();
        }

        if (file.exists()) {
            log.info("Icon of " + productName + " from category + " + category + " already exists on local!");
        } else {
            try (ReadableByteChannel rbc = Channels.newChannel(website.openStream())) {
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                }
            }
        }
    }
}
