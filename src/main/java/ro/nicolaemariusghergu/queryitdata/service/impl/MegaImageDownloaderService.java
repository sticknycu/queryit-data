package ro.nicolaemariusghergu.queryitdata.service.impl;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.randomizers.range.IntegerRangeRandomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.nicolaemariusghergu.queryitdata.model.Category;
import ro.nicolaemariusghergu.queryitdata.model.Manufacturer;
import ro.nicolaemariusghergu.queryitdata.model.Product;
import ro.nicolaemariusghergu.queryitdata.model.Promotion;
import ro.nicolaemariusghergu.queryitdata.service.DownloadService;

import java.io.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class MegaImageDownloaderService implements DownloadService {

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

    private static List<Product> availableProducts = new ArrayList<>();

    private static Set<Category> availableCategories = new HashSet<>();

    private static Set<Manufacturer> availableManufacturers = new HashSet<>();

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static HttpResponse<String> HTTP_RESPONSE = null;

    @SneakyThrows
    @Override
    public ResponseEntity<List<Product>> getProductsFromMegaImage() {
        long categoryId = 1L;
        long productId = 1L;

        log.info("Extrag informatiile de la fiecare website...");
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

            HTTP_RESPONSE = HTTP_CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofString());

            JSONObject jsonObject = new JSONObject(HTTP_RESPONSE.body());

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
            String filePath = performWritingJsons(HTTP_RESPONSE.body(), categoryName, HTTP_MEGA_IMAGE_DIRECTORY, HTTP_MEGA_IMAGE_DIRECTORY_JSONS);

            jsonObject = new JSONObject(Files.readString(new File(filePath).toPath()));

            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONObject("categoryProductSearch").getJSONArray("products");

            Category category = new Category();
            category.setId(categoryId++);
            category.setName(categoryName);

            log.info("Iau toate informatiile din fiecare categorie...");
            log.info("=== Categoria " + categoryName + " ===");
            for (int j = 0; j < jsonArray.length(); j++) {
                String productName = jsonArray
                        .getJSONObject(j)
                        .getString("name");

                log.info("Am gasit produsul= " + productName);

                String manufacturerName = jsonArray
                        .getJSONObject(j)
                        .getString("manufacturerName");

                double price = jsonArray
                        .getJSONObject(j)
                        .getJSONObject("price")
                        .getDouble("value");

                Product product = new Product();
                product.setId(productId++);
                product.setName(productName);
                product.setQuantity(new IntegerRangeRandomizer(1, 50).getRandomValue());
                product.setCategory(category);
                product.setPrice(BigDecimal.valueOf(price));

                Manufacturer temporaryManufacturer = new Manufacturer();
                temporaryManufacturer.setName(manufacturerName);

                availableManufacturers.forEach(listManufacturer -> {
                    if (listManufacturer.getName().equals(temporaryManufacturer.getName())) {
                        temporaryManufacturer.setId(listManufacturer.getId());
                    }
                });

                product.setManufacturer(temporaryManufacturer);

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

                    String iconPath = performWritingIcons(iconUrl, categoryName, productName, HTTP_MEGA_IMAGE_DIRECTORY, HTTP_MEGA_IMAGE_DIRECTORY_PHOTOS);
                    log.info("Icon Path: " + iconPath);
                    product.setIconPath(iconPath);
                } catch (Exception e) {
                    log.info("N-am gasit url-ul la " + productName);
                }

                if (availableProducts.contains(product)) {
                    int index = availableProducts.indexOf(product);
                    availableProducts.set(index, product);
                } else {
                    availableProducts.add(product);
                }
            }
        }

        if (HTTP_RESPONSE.body() == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            return ResponseEntity.ok(availableProducts);
        }
    }

    @Override
    public ResponseEntity<List<Product>> getProductsFromMegaImageByCategory(Long categoryId) {
        if (availableProducts.isEmpty()) {
            getProductsFromMegaImage();
        }
        return ResponseEntity.ok(availableProducts.stream()
                .filter(p -> p.getCategory().getId() == categoryId)
                .toList());
    }

    @Override
    public ResponseEntity<Set<Promotion>> getPromotionsFromMegaImage() {
        log.info("Creez promotii aleatoare pentru tabela de promotii...");

        int maxValue = new IntegerRangeRandomizer(1, 20).getRandomValue();
        for (int promotionId = 1; promotionId <= maxValue; promotionId++) {
            Promotion promotion = new Promotion();
            promotion.setId((long) promotionId);

            int quantityNeeded = new IntegerRangeRandomizer(1, 3).getRandomValue();

            Product neededProduct = findAvailableProductFromRandomizedIntegerId();
            log.info("NeededProduct= " + neededProduct);
            availableProducts.forEach(prd -> {
                if (neededProduct.getId().equals(prd.getId())) {
                    neededProduct.setName(prd.getName());
                }
            });

            promotion.setQuantityNeeded(quantityNeeded);

            String description = "La " +
                    quantityNeeded +
                    " " +
                    neededProduct.getName() +
                    " primesti inca unul gratuit!";

            promotion.setDescription(description);
            promotion.setName(neededProduct.getName());

            int randomDay = new IntegerRangeRandomizer(1, 27).getRandomValue();
            int randomMonth = new IntegerRangeRandomizer(0, 1).getRandomValue();
            int randomHour = new IntegerRangeRandomizer(1, 24).getRandomValue();
            int randomMinute = new IntegerRangeRandomizer(1, 59).getRandomValue();
            int randomSecond = new IntegerRangeRandomizer(1, 59).getRandomValue();

            LocalDateTime localDateTime = LocalDateTime.of(LocalDateTime.now().getYear(),
                    LocalDateTime.now().getMonthValue() + randomMonth,
                    randomDay,
                    randomHour,
                    randomMinute,
                    randomSecond);

            Instant instant = Instant.now();
            ZoneId zoneId = ZoneId.systemDefault();
            ZoneOffset zoneOffset = zoneId.getRules().getOffset(instant);
            promotion.setExpireDate(localDateTime.toInstant(zoneOffset).toEpochMilli());

            promotionsAvailable.add(promotion);
            log.info("Promotion= " + promotion + " a fost adaugata in lista!");
        }
        return ResponseEntity.ok(promotionsAvailable);
    }

    @Override
    public ResponseEntity<List<Product>> getProductsWithPromotionFromMegaImage() {
        List<Product> productsWithPromotion = new ArrayList<>();
        promotionsAvailable.forEach(promotion -> availableProducts.forEach(product -> {
            if (product.getName().equals(promotion.getName())) {
                Product p = product;
                p.setPromotion(promotion);
                productsWithPromotion.add(p);
            }
        }));
        return ResponseEntity.ok(productsWithPromotion);
    }

    @SneakyThrows
    @Override
    public ResponseEntity<Set<Category>> getCategoriesFromMegaImage() {
        long categoryId = 1L;

        log.info("Extrag informatiile din fiecare website...");
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

            HTTP_RESPONSE = HTTP_CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofString());

            JSONObject jsonObject = new JSONObject(HTTP_RESPONSE.body());

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

            log.info("Aflu informatii despre fiecare categorie...");

            Category category = new Category();
            category.setId(categoryId++);
            category.setName(categoryName);

            if (availableCategories.add(category)) {
                log.info("Category= " + category + " a fost adaugata in lista...");
            } else {
                log.info("Category= " + category + " deja exista in lista...");
            }
        }
        return ResponseEntity.ok(availableCategories);
    }

    @SneakyThrows
    @Override
    public ResponseEntity<Set<Manufacturer>> getManufacturersFromMegaImage() {
        Set<String> manufacturersName = new HashSet<>();
        long manufacturerId = 1L;

        log.info("Extrag informatiile din fiecare website...");
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

            HTTP_RESPONSE = HTTP_CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofString());

            JSONObject jsonObject = new JSONObject(HTTP_RESPONSE.body());

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
            String filePath = performWritingJsons(HTTP_RESPONSE.body(), categoryName, HTTP_MEGA_IMAGE_DIRECTORY, HTTP_MEGA_IMAGE_DIRECTORY_JSONS);

            jsonObject = new JSONObject(Files.readString(new File(filePath).toPath()));

            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONObject("categoryProductSearch").getJSONArray("products");

            log.info("Aflu informatiile de la fiecare categorie...");
            for (int j = 0; j < jsonArray.length(); j++) {
                String manufacturerName = jsonArray
                        .getJSONObject(j)
                        .getString("manufacturerName");

                if (manufacturersName.add(manufacturerName)) {
                    Manufacturer manufacturer = new Manufacturer();
                    manufacturer.setId(manufacturerId++);
                    manufacturer.setName(manufacturerName);

                    availableManufacturers.add(manufacturer);
                }
            }
        }

        return ResponseEntity.ok(availableManufacturers);
    }

    private static final int MAX_PRODUCT_ID = 290;

    private static final Set<Promotion> promotionsAvailable = new HashSet<>();

    private Product findAvailableProductFromRandomizedIntegerId() {
        int productRandomizedId = new IntegerRangeRandomizer(0, MAX_PRODUCT_ID).getRandomValue();

        Optional<Product> productSearched = Optional.empty();
        AtomicBoolean exists = new AtomicBoolean(false);
        for (Product availableProduct : availableProducts) {
            if (availableProduct.getId() == productRandomizedId) {
                if (promotionsAvailable.isEmpty()) {
                    return availableProduct;
                } else {
                    promotionsAvailable.forEach(promotion -> {
                        // daca din toate produsele am gasit id-ul generat aleatoriu
                        // si produsul exista in lista de promotii
                        if (availableProduct.getName().equals(promotion.getName())) {
                            // atunci setez ca exista
                            exists.set(true);
                        }
                    });
                    if (exists.get()) {
                        return findAvailableProductFromRandomizedIntegerId();
                    } else {
                        productSearched = Optional.of(availableProduct);
                    }
                }
            }
        }
        // produsul exista in lista de promotii si caut altul
        // sau produsul nu exista in lista de promotii si returnez produsul
        return productSearched.orElseGet(this::findAvailableProductFromRandomizedIntegerId);
    }

    private String performWritingJsons(String responseBody, String category, String baseDirectory, String baseDirectoryJsons) throws IOException {
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
        return filePath;
    }

    private String performWritingIcons(String url, String category, String productName, String baseDirectory, String baseDirectoryPhotos) throws IOException {
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
        return filePath;
    }
}
