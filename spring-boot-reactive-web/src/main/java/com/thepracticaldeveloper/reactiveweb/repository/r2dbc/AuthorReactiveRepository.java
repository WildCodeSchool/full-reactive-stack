package com.thepracticaldeveloper.reactiveweb.repository.r2dbc;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.r2dbc.mapping.event.AfterSaveCallback;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.thepracticaldeveloper.reactiveweb.domain.Author;

import reactor.core.publisher.Mono;

@Repository
public interface AuthorReactiveRepository extends R2dbcRepository<Author, Long> {

    record AuthorCreatedEvent(Author author) {
    }

    @Component
    class AfterSave implements AfterSaveCallback<Author> {

        @Autowired
        private ApplicationEventPublisher publisher;

        @Override
        public Publisher<Author> onAfterSave(Author author, OutboundRow outboundRow, SqlIdentifier table) {
            publisher.publishEvent(new AuthorCreatedEvent(author));
            return Mono.just(author);
        }
    }
}
