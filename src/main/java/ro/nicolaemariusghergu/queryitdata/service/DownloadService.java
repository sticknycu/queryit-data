package ro.nicolaemariusghergu.queryitdata.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ro.nicolaemariusghergu.queryitdata.model.Product;

import java.util.List;

@Service
public interface DownloadService {

    ResponseEntity<List<Product>> getProductsFromMegaImage();

    ResponseEntity<List<Product>> getProductsFromMegaImageByCategory(Long categoryId);
}
