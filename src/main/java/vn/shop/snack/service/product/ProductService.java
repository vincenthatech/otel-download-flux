package vn.shop.snack.service.product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.shop.snack.model.Product;

public interface ProductService {
    public Flux<Product> getAllProducts() ;

    public Mono<Product> getProductById(Long id);

    public Mono<Product> createProduct(Product product);

    public Mono<Product> updateProduct(Long id, Product product);

    public Mono<Void> deleteProduct(Long id);
}
