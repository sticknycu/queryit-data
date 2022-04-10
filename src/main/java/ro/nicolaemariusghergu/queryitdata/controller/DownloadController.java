package ro.nicolaemariusghergu.queryitdata.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.nicolaemariusghergu.queryitdata.model.Product;
import ro.nicolaemariusghergu.queryitdata.service.DownloadService;

import java.util.List;

@RestController
@RequestMapping("api/download")
public record DownloadController(DownloadService downloadService) {

    @GetMapping("/v1/mega-image")
    public ResponseEntity<List<Product>> getProductsFromMegaImage() {
        return downloadService.getProductsFromMegaImage();
    }

    @GetMapping("/v1/mega-image/{categoryId}")
    public ResponseEntity<List<Product>> getProductsFromMegaImageByCategory(@PathVariable Long categoryId) {
        return downloadService.getProductsFromMegaImageByCategory(categoryId);
    }
}
