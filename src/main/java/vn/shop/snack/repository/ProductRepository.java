package vn.shop.snack.repository;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.shop.snack.model.Product;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ProductRepository {
    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public ProductRepository() {
        save(Product.builder().name("Laptop").price(1200.0).build()).subscribe();
        save(Product.builder().name("Mobile Phone").price(800.0).build()).subscribe();
        save(Product.builder().name("Tablet").price(500.0).build()).subscribe();
    }

    public Flux<Product> findAll() {
        return Flux.fromIterable(products.values());
    }

    public Mono<Product> findById(Long id) {
        return Mono.justOrEmpty(products.get(id));
    }

    public Mono<Product> save(Product product) {
        if (product.getId() == null) {
            product.setId(idGenerator.getAndIncrement());
        }
        products.put(product.getId(), product);
        return Mono.just(product);
    }

    public Mono<Void> deleteById(Long id) {
        products.remove(id);
        return Mono.empty();
    }
}
