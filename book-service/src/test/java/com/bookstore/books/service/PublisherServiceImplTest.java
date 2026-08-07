package com.bookstore.books.service;

import com.bookstore.books.publisher.dto.PublisherRequest;
import com.bookstore.books.publisher.dto.PublisherResponse;
import com.bookstore.books.publisher.entity.Publisher;
import com.bookstore.books.publisher.repository.PublisherRepository;
import com.bookstore.books.publisher.service.PublisherServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublisherServiceImplTest {

    @Mock
    private PublisherRepository publisherRepository;

    @InjectMocks
    private PublisherServiceImpl publisherService;

    @Test
    void GivenValidRequest_WhenCreatePublisher_ThenPersistAndReturnResponse() {
        PublisherRequest request = PublisherRequest.builder()
                .name("Penguin")
                .address("NY")
                .build();
        when(publisherRepository.save(any(Publisher.class))).thenAnswer(invocation -> {
            Publisher publisher = invocation.getArgument(0);
            publisher.setId(UUID.randomUUID());
            return publisher;
        });

        PublisherResponse response = publisherService.createPublisher(request);

        assertThat(response.getName()).isEqualTo("Penguin");
        assertThat(response.getAddress()).isEqualTo("NY");
        verify(publisherRepository).save(any(Publisher.class));
    }

    @Test
    void GivenExistingPublisher_WhenUpdatePublisher_ThenApplyChanges() {
        UUID id = UUID.randomUUID();
        Publisher existing = Publisher.builder().id(id).name("Old").address("A").build();
        when(publisherRepository.findById(id)).thenReturn(Optional.of(existing));

        PublisherResponse response = publisherService.updatePublisher(
                PublisherRequest.builder().name("New").address("B").build(), id);

        assertThat(response.getName()).isEqualTo("New");
        assertThat(response.getAddress()).isEqualTo("B");
    }

    @Test
    void GivenUnknownId_WhenUpdatePublisher_ThenThrowRuntimeException() {
        UUID id = UUID.randomUUID();
        when(publisherRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publisherService.updatePublisher(
                PublisherRequest.builder().name("X").build(), id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found");
    }

    @Test
    void GivenExistingPublisher_WhenGetPublisherById_ThenReturnResponse() {
        UUID id = UUID.randomUUID();
        when(publisherRepository.findById(id)).thenReturn(Optional.of(
                Publisher.builder().id(id).name("Penguin").build()));

        PublisherResponse response = publisherService.getPublisherById(id);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("Penguin");
    }

    @Test
    void GivenUnknownId_WhenGetPublisherById_ThenThrowRuntimeException() {
        UUID id = UUID.randomUUID();
        when(publisherRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publisherService.getPublisherById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Category not found");
    }

    @Test
    void GivenPublisherId_WhenDeletePublisherById_ThenDelegateToRepository() {
        UUID id = UUID.randomUUID();
        publisherService.deletePublisherById(id);
        verify(publisherRepository).deleteById(id);
    }

    @Test
    void GivenPublishersExist_WhenGetAllPublishers_ThenReturnMappedList() {
        when(publisherRepository.findAll()).thenReturn(List.of(
                Publisher.builder().id(UUID.randomUUID()).name("Penguin").build()));

        assertThat(publisherService.getAllPublishers()).hasSize(1);
    }
}
