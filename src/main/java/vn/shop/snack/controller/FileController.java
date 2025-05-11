package vn.shop.snack.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import vn.shop.snack.service.file.FileStorageService;

@RestController
@RequestMapping("/api/file")
public class FileController {
  @Autowired
  FileStorageService storageService;

  @GetMapping("/download")
  public ResponseEntity<Flux<DataBuffer>> getFile() {
    Flux<DataBuffer> file = storageService.load("data.xlsx");

    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=data.xlsx")
        .contentType(MediaType.APPLICATION_OCTET_STREAM).body(file);
  }
}
