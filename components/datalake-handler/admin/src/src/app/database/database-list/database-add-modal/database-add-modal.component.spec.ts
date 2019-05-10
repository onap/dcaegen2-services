import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DatabaseAddModalComponent } from './database-add-modal.component';

describe('DatabaseAddModalComponent', () => {
  let component: DatabaseAddModalComponent;
  let fixture: ComponentFixture<DatabaseAddModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DatabaseAddModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DatabaseAddModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
