package com.bookstore.books.publisher.controller;

import com.bookstore.books.publisher.dto.PublisherRequest;
import com.bookstore.books.publisher.dto.PublisherResponse;
import com.bookstore.books.publisher.service.PublisherServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/publishers")
public class PublisherController {

    private final PublisherServiceImpl publisherService;

    public PublisherController(PublisherServiceImpl publisherService) {
        this.publisherService = publisherService;
    }

    @PostMapping("/create")
    public ResponseEntity<PublisherResponse> createPublisher(@RequestBody PublisherRequest request){
        return new ResponseEntity<>(publisherService.createPublisher(request), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<PublisherResponse> updatePublisher(@RequestBody PublisherRequest request, @PathVariable UUID id){
        return new ResponseEntity<>(publisherService.updatePublisher(request,id), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublisherResponse> getPublisherById(@PathVariable UUID id){
        return new ResponseEntity<>(publisherService.getPublisherById(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePublisherById(@PathVariable UUID id){
        publisherService.deletePublisherById(id);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    @GetMapping("/")
    public ResponseEntity<List<PublisherResponse>> getAllPublishers(){
        return new ResponseEntity<>(publisherService.getAllPublishers(), HttpStatus.OK);
    }

}
