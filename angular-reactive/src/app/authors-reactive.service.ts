import { Injectable } from '@angular/core';

import { Quote } from './quote';

import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Author } from './author';

const backendBaseUrl = 'http://localhost:8080';
const getAllUrl = backendBaseUrl + '/authors';
const getContinuousUrl = backendBaseUrl + '/authors-continuous';

@Injectable()
export class AuthorsReactiveService {


  constructor(private http: HttpClient) {
  }

  getAuthors(continuous?: boolean): Observable<Author> {
    return new Observable<Author>((observer) => {
      let url = getAllUrl;
      if (continuous) {
        url = getContinuousUrl;
      }
      let eventSource = new EventSource(url);
      eventSource.onmessage = (event) => {
        console.debug('Received event: ', event);
        let json = JSON.parse(event.data);
        observer.next(new Author(json['id'], json['fullName'], json['region']));
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
