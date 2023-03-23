package com.thepracticaldeveloper.reactiveweb.repository.r2dbc;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.r2dbc.mapping.event.AfterSaveCallback;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.thepracticaldeveloper.reactiveweb.domain.Quote;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface QuoteReactiveRepository extends R2dbcRepository<Quote, Long> {

    Flux<Quote> findAllByIdNotNullOrderByIdAsc(final Pageable page);

    record QuoteCreatedEvent(Quote quote) {
    }

    @Component
    class AfterSave implements AfterSaveCallback<Quote> {

        @Autowired
        private ApplicationEventPublisher publisher;

        @Override
        public Publisher<Quote> onAfterSave(Quote quote, OutboundRow outboundRow, SqlIdentifier table) {
            publisher.publishEvent(new QuoteCreatedEvent(quote));
            return Mono.just(quote);
        }
    }
}
