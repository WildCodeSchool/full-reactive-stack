package com.thepracticaldeveloper.reactiveweb.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thepracticaldeveloper.reactiveweb.domain.Author;
import com.thepracticaldeveloper.reactiveweb.repository.r2dbc.AuthorReactiveRepository;
import com.thepracticaldeveloper.reactiveweb.repository.r2dbc.AuthorReactiveRepository.AuthorCreatedEvent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@RestController
@CrossOrigin(origins = "*")
public class AuthorReactiveController {

    private static final int DELAY_PER_ITEM_MS = 100;

    private final AuthorReactiveRepository authorReactiveRepository;

    private List<FluxSink<AuthorCreatedEvent>> sinks = new ArrayList<>();

    public AuthorReactiveController(final AuthorReactiveRepository authorReactiveRepository) {
        this.authorReactiveRepository = authorReactiveRepository;
    }

    @EventListener
    void onNew(AuthorCreatedEvent event) {
        synchronized (sinks) {
            sinks.removeIf(sink -> sink.isCancelled());
        }

        int i = 0;
        for (FluxSink<AuthorCreatedEvent> sink : new ArrayList<>(sinks)) {
            System.out.println("=> push new author in sink #" + (++i) + " - wasCancelled=" + sink.isCancelled());
            sink.next(event);
        }
    }

    private void registerSink(FluxSink<AuthorCreatedEvent> sink) {
        synchronized (sinks) {
            sinks.add(sink);
        }
    }


    @GetMapping("/authors")
    public Flux<Author> getAuthors() {

        Flux<Author> authors = authorReactiveRepository.findAll()
                .delayElements(Duration.ofMillis(DELAY_PER_ITEM_MS));

        return authors;
    }

    @GetMapping(path = "/authors-continuous", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Author> getAuthorsContinuous() {
        Flux<AuthorCreatedEvent> createdEvents = Flux.<AuthorCreatedEvent>create(this::registerSink).share();

        Flux<Author> authors = authorReactiveRepository.findAll()
                .mergeWith(createdEvents.map(event -> event.author()));

        return authors;
    }

}
