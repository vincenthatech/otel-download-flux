package vn.shop.snack.service.file;

import org.springframework.core.io.buffer.DataBuffer;

import reactor.core.publisher.Flux;

public interface FileStorageService {
    public Flux<DataBuffer> load(String filename);
}
