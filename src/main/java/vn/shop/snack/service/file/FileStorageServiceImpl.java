package vn.shop.snack.service.file;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import reactor.core.publisher.Flux;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path root = Paths.get("/home/vincent/projects/snack/data");

  @Override
  @WithSpan("load")
  public Flux<DataBuffer> load(String filename) {
    try {
      Path file = root.resolve(filename);
      Resource resource = new UrlResource(file.toUri());

      if (resource.exists() || resource.isReadable()) {
        return DataBufferUtils.read(resource, new DefaultDataBufferFactory(), 4096);
      } else {
        throw new RuntimeException("Could not read the file!");
      }
    } catch (MalformedURLException e) {
      throw new RuntimeException("Error: " + e.getMessage());
    }
  }
    
}
