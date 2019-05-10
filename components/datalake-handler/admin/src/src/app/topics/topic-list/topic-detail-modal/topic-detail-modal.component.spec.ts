import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TopicDetailModalComponent } from './topic-detail-modal.component';

describe('TopicDetailModalComponent', () => {
  let component: TopicDetailModalComponent;
  let fixture: ComponentFixture<TopicDetailModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TopicDetailModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TopicDetailModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
