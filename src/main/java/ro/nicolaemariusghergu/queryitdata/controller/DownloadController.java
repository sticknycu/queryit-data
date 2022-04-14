package ro.nicolaemariusghergu.queryitdata.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.nicolaemariusghergu.queryitdata.model.Category;
import ro.nicolaemariusghergu.queryitdata.model.Manufacturer;
import ro.nicolaemariusghergu.queryitdata.model.Product;
import ro.nicolaemariusghergu.queryitdata.model.Promotion;
import ro.nicolaemariusghergu.queryitdata.service.DownloadService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api/download")
public record DownloadController(DownloadService downloadService) {

    @GetMapping("/v1/mega-image/products")
    public ResponseEntity<List<Product>> getProductsFromMegaImage() {
        return downloadService.getProductsFromMegaImage();
    }

    @GetMapping("/v1/mega-image/products/{categoryId}")
    public ResponseEntity<List<Product>> getProductsFromMegaImageByCategory(@PathVariable Long categoryId) {
        return downloadService.getProductsFromMegaImageByCategory(categoryId);
    }

    @GetMapping("/v1/mega-image/promotions")
    public ResponseEntity<Set<Promotion>> getPromotionsFromMegaImage() {
        return downloadService.getPromotionsFromMegaImage();
    }

    @GetMapping("/v1/mega-image/product-promotions")
    public ResponseEntity<List<Product>> getProductsWithPromotionFromMegaImage() {
        return downloadService.getProductsWithPromotionFromMegaImage();
    }

    @GetMapping("/v1/mega-image/categories")
    public ResponseEntity<Set<Category>> getCategoriesFromMegaImage() {
        return downloadService.getCategoriesFromMegaImage();
    }

    @GetMapping("/v1/mega-image/manufacturers")
    public ResponseEntity<Set<Manufacturer>> getManufacturersFromMegaImage() {
        return downloadService.getManufacturersFromMegaImage();
    }

}
