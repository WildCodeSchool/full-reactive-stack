import { Quote } from './quote';
import { QuoteReactiveService } from './quote-reactive.service';
import { QuoteBlockingService } from './quote-blocking.service';

import { Observable } from 'rxjs';
import { ChangeDetectorRef, Component } from "@angular/core";
import { Author, Region } from './author';
import { AuthorsReactiveService } from './authors-reactive.service';

@Component({
  selector: 'app-component-quotes',
  providers: [QuoteReactiveService],
  templateUrl: './quotes.component.html'
})
export class QuotesComponent {

  newQuoteDialogOpen = false;
  quoteArray: Quote[] = [];
  selectedQuote: Quote;
  mode: string;
  pagination: boolean;
  continuous: boolean;
  page: number;
  size: number;

  newQuoteBook: string = "";
  newQuoteContent: string = "";
  newQuoteAuthorFullName: string = "";
  newQuoteAuthorRegion: Region;

  authorsArray: Author[] = [];
  authorsContinuous: boolean = true;

  regionEntries: { value: number, text: string }[] = [];

  constructor(
    private readonly authorsReactiveService: AuthorsReactiveService,
    private readonly quoteReactiveService: QuoteReactiveService,
    private readonly quoteBlockingService: QuoteBlockingService, private cdr: ChangeDetectorRef) {
    this.mode = "reactive";
    this.continuous = true;
    this.page = 0;
    this.size = 50;

    for (var regionEntry in Region) {
      var isValueProperty = Number(regionEntry) >= 0
      if (isValueProperty) {
        this.regionEntries.push({ value: parseInt(regionEntry), text: Region[regionEntry] })
      }
    }
  }

  resetData() {
    this.quoteArray = [];
  }

  pushNewQuote(): void {
    console.log("create")
    this.quoteReactiveService.pushNewQuote({
      book: this.newQuoteBook ?? "a book",
      content: this.newQuoteContent ?? "quote content",
      authorFullName: this.newQuoteAuthorFullName ?? "unknown authir",
      authorRegion: this.newQuoteAuthorRegion ?? Region.Corse,
    });

    this.resetForm();
    this.newQuoteDialogOpen = false;
  }

  private resetForm() {
    this.newQuoteBook = "";
    this.newQuoteContent = "";
    this.newQuoteAuthorFullName = "";
    this.newQuoteAuthorRegion = undefined;
  }

  requestQuoteStream(): void {
    this.resetData();
    let quoteObservable: Observable<Quote>;
    if (this.pagination === true) {
      quoteObservable = this.quoteReactiveService.getQuoteStream({ pageIndex: this.page, pageSize: this.size });
    } else {
      quoteObservable = this.quoteReactiveService.getQuoteStream({ continuous: this.continuous });
    }
    quoteObservable.subscribe(quote => {
      this.quoteArray.push(quote);
      this.cdr.detectChanges();
    });
  }

  requestQuoteBlocking(): void {
    this.resetData();
    if (this.pagination === true) {
      this.quoteBlockingService.getQuotes(this.page, this.size)
        .subscribe(q => this.quoteArray = q);
    } else {
      this.quoteBlockingService.getQuotes()
        .subscribe(q => this.quoteArray = q);
    }
  }

  requestAuthors(): void {
    this.authorsArray = []
    let observable: Observable<Author>;
    observable = this.authorsReactiveService.getAuthors(this.authorsContinuous);
    observable.subscribe(author => {
      this.authorsArray.push(author);
      this.cdr.detectChanges();
    });
  }

  onSelect(quote: Quote): void {
    this.selectedQuote = quote;
    this.cdr.detectChanges();
  }

  openDialog() {
    this.resetForm();
    this.newQuoteDialogOpen = true;
    this.cdr.detectChanges();
  }

}
