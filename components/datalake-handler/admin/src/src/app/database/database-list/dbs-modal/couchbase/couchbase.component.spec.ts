import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CouchbaseComponent } from './couchbase.component';

describe('CouchbaseComponent', () => {
  let component: CouchbaseComponent;
  let fixture: ComponentFixture<CouchbaseComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CouchbaseComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CouchbaseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
