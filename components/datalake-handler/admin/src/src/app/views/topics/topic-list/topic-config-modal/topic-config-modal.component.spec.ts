import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TopicConfigModalComponent } from './topic-config-modal.component';

describe('TopicConfigModalComponent', () => {
  let component: TopicConfigModalComponent;
  let fixture: ComponentFixture<TopicConfigModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TopicConfigModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TopicConfigModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
