package com.bookstore.books.publisher.service;

import com.bookstore.books.publisher.dto.PublisherRequest;
import com.bookstore.books.publisher.dto.PublisherResponse;
import com.bookstore.books.publisher.entity.Publisher;
import com.bookstore.books.publisher.mapper.PublisherMapper;
import com.bookstore.books.publisher.repository.PublisherRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PublisherServiceImpl implements PublisherService{

    private final PublisherRepository publisherRepository;

    public PublisherServiceImpl(PublisherRepository publisherRepository) {
        this.publisherRepository = publisherRepository;
    }

    @Override
    public PublisherResponse createPublisher(PublisherRequest request) {

        Publisher publisher = Publisher
                .builder()
                .name(request.getName())
                .address(request.getAddress())
                .build();

        Publisher savedPublisher = publisherRepository.save(publisher);

        return PublisherMapper.toResponse(savedPublisher);
    }

    @Override
    public PublisherResponse updatePublisher(PublisherRequest request, UUID id) {

        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        publisher.setName(request.getName());
        publisher.setAddress(request.getAddress());

        return PublisherMapper.toResponse(publisher);

    }

    @Override
    public PublisherResponse getPublisherById(UUID id) {

        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return PublisherMapper.toResponse(publisher);

    }

    @Override
    public void deletePublisherById(UUID id) {
        publisherRepository.deleteById(id);
    }

    @Override
    public List<PublisherResponse> getAllPublishers() {
        return publisherRepository.findAll().stream().map(PublisherMapper::toResponse).toList();
    }
}
