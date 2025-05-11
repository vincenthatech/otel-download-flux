package vn.shop.snack.service.product;

import org.springframework.stereotype.Service;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.shop.snack.model.Product;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final vn.shop.snack.repository.ProductRepository productRepository;

    @Override
    @WithSpan("getAllProducts")
    public Flux<Product> getAllProducts() {
        Span.current().setAttribute("service.method", "getAllProducts");
        return productRepository.findAll()
                .doOnNext(product -> Span.current().setAttribute("product.count", 1));
    }
    
    @Override
    @WithSpan("getProductById")
    public Mono<Product> getProductById(Long id) {
        Span.current().setAttribute("service.method", "getProductById");
        Span.current().setAttribute("product.id", id);
        return productRepository.findById(id)
                .doOnSuccess(product -> {
                    if (product != null) {
                        Span.current().setAttribute("product.found", true);
                    } else {
                        Span.current().setAttribute("product.found", false);
                    }
                });
    }

    @Override
    @WithSpan("createProduct")
    public Mono<Product> createProduct(Product product) {
        Span.current().setAttribute("service.method", "createProduct");
        return productRepository.save(product)
                .doOnSuccess(p -> Span.current().setAttribute("product.id", p.getId()));
    }

    @Override
    @WithSpan("updateProduct")
    public Mono<Product> updateProduct(Long id, Product product) {
        Span.current().setAttribute("service.method", "updateProduct");
        Span.current().setAttribute("product.id", id);
        product.setId(id);
        return productRepository.save(product);
    }

    @Override
    @WithSpan("deleteProduct")
    public Mono<Void> deleteProduct(Long id) {
        Span.current().setAttribute("service.method", "deleteProduct");
        Span.current().setAttribute("product.id", id);
        return productRepository.deleteById(id);
    }
}