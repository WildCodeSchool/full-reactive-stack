import { Injectable } from '@angular/core';

import { Quote } from './quote';

import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

export interface QuoteStreamOptions {
  pageIndex?: number;
  pageSize?: number;
  continuous?: boolean;
}

const backendBaseUrl = 'http://localhost:8080';
const getAllUrl = backendBaseUrl + '/quotes-reactive';
const createUrl = backendBaseUrl + '/quotes-reactive';
const getPagedUrl = backendBaseUrl + '/quotes-reactive-paged';
const getContinuousUrl = backendBaseUrl + '/quotes-reactive-continuous';

@Injectable()
export class QuoteReactiveService {


  constructor(private http: HttpClient) {
  }

  pushNewQuote(quoteData: Partial<Quote>) {
    return this.http.post(createUrl, quoteData).subscribe(() => {
      console.log("done");
    })
  }

  getQuoteStream(options: QuoteStreamOptions = {}): Observable<Quote> {
    return new Observable<Quote>((observer) => {
      let url = getAllUrl;
      if (options.continuous) {
        url = getContinuousUrl;
      } else if (options.pageIndex != null || options.pageSize != null) {
        url = getPagedUrl + '?page=' + (options.pageIndex ?? 0) + '&size=' + (options.pageSize ?? 20);
      }
      let eventSource = new EventSource(url);
      eventSource.onmessage = (event) => {
        console.debug('Received event: ', event);
        let json = JSON.parse(event.data);
        observer.next(new Quote(json['id'], json['book'], json['content']));
      };
      eventSource.onerror = (error) => {
        // readyState === 0 (closed) means the remote source closed the connection,
        // so we can safely treat it as a normal situation. Another way
        // of detecting the end of the stream is to insert a special element
        // in the stream of events, which the client can identify as the last one.
        if (eventSource.readyState === 0) {
          console.log('The stream has been closed by the server.');
          eventSource.close();
          observer.complete();
        } else {
          observer.error('EventSource error: ' + error);
        }
      }
    });
  }

}
