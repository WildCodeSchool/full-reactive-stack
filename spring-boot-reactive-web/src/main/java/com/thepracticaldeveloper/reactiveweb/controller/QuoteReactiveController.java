package com.thepracticaldeveloper.reactiveweb.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.thepracticaldeveloper.reactiveweb.domain.Quote;
import com.thepracticaldeveloper.reactiveweb.repository.r2dbc.QuoteReactiveRepository;
import com.thepracticaldeveloper.reactiveweb.repository.r2dbc.QuoteReactiveRepository.QuoteCreatedEvent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "*")
public class QuoteReactiveController {

    private static final int DELAY_PER_ITEM_MS = 100;

    private final QuoteReactiveRepository quoteReactiveRepository;

    private List<FluxSink<QuoteCreatedEvent>> sinks = new ArrayList<>();

    public QuoteReactiveController(final QuoteReactiveRepository quoteReactiveRepository) {
        this.quoteReactiveRepository = quoteReactiveRepository;
    }

    @EventListener
    void onNew(QuoteCreatedEvent event) {
        synchronized (sinks) {
            sinks.removeIf(sink -> sink.isCancelled());
        }

        int i = 0;
        for (FluxSink<QuoteCreatedEvent> sink : new ArrayList<>(sinks)) {
            System.out.println("=> push new quote in sink #" + (++i) + " - wasCancelled=" + sink.isCancelled());
            sink.next(event);
        }
    }

    private void registerSink(FluxSink<QuoteCreatedEvent> sink) {
        synchronized (sinks) {
            sinks.add(sink);
        }
    }

    @GetMapping("/quotes-reactive")
    public Flux<Quote> getQuoteFlux() {
        return quoteReactiveRepository.findAll().delayElements(Duration.ofMillis(DELAY_PER_ITEM_MS));
    }

    @PostMapping("/quotes-reactive")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Quote> create(@RequestBody Quote quote) {
        return quoteReactiveRepository.save(quote);
    }

    @GetMapping("/quotes-reactive-paged")
    public Flux<Quote> getQuoteFlux(final @RequestParam(name = "page") int page,
            final @RequestParam(name = "size") int size) {

        Flux<Quote> quotes = quoteReactiveRepository.findAllByIdNotNullOrderByIdAsc(PageRequest.of(page, size))
                .delayElements(Duration.ofMillis(DELAY_PER_ITEM_MS));

        return quotes;
    }

    @GetMapping(path = "/quotes-reactive-continuous", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Quote> getQuoteFluxContinuous() {
        Flux<QuoteCreatedEvent> createdEvents = Flux.<QuoteCreatedEvent>create(this::registerSink).share();

        Flux<Quote> quotes = quoteReactiveRepository.findAll()
                .mergeWith(createdEvents.map(event -> event.quote()));

        return quotes;
    }

}
