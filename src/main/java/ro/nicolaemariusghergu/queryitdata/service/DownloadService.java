package ro.nicolaemariusghergu.queryitdata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.nicolaemariusghergu.queryitdata.model.Category;
import ro.nicolaemariusghergu.queryitdata.model.Manufacturer;
import ro.nicolaemariusghergu.queryitdata.model.Product;
import ro.nicolaemariusghergu.queryitdata.model.Promotion;

import java.util.List;
import java.util.Set;

@Service
public interface DownloadService {

    ResponseEntity<List<Product>> getProductsFromMegaImage();

    ResponseEntity<List<Product>> getProductsFromMegaImageByCategory(Long categoryId);

    ResponseEntity<Set<Promotion>> getPromotionsFromMegaImage();

    ResponseEntity<List<Product>> getProductsWithPromotionFromMegaImage();

    ResponseEntity<Set<Category>> getCategoriesFromMegaImage();

    ResponseEntity<Set<Manufacturer>> getManufacturersFromMegaImage();
}
