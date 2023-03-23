export class Quote {
  id: number;
  book: string;
  content: string;
  authorId?: number;

  constructor(id: number, book: string, content: string, authorId?: number) {
    this.id = id;
    this.book = book;
    this.content = content;
    this.authorId = authorId;
  }
}
