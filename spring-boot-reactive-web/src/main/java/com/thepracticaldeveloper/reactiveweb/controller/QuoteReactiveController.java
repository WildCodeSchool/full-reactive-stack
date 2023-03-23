package com.thepracticaldeveloper.reactiveweb.controller;

import java.time.Duration;

import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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

    private Flux<QuoteCreatedEvent> createdEvents;
    private FluxSink<QuoteCreatedEvent> sink;

    public QuoteReactiveController(final QuoteReactiveRepository quoteReactiveRepository) {
        this.quoteReactiveRepository = quoteReactiveRepository;
        this.createdEvents = Flux.<QuoteCreatedEvent>create(sink -> {
            this.sink = sink;
        }).share();
    }

    @EventListener
    void onNew(QuoteCreatedEvent event) {
        if (sink != null) {
            System.out.println("=> push new quote in sink");
            sink.next(event);
            
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
                .delayElements(Duration.ofMillis(DELAY_PER_ITEM_MS))
                .mergeWith(createdEvents.map(event -> event.quote()));

        return quotes;
    }

}
