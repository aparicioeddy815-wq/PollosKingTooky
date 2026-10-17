import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SalonMenu } from './salon-menu';

describe('SalonMenu', () => {
  let component: SalonMenu;
  let fixture: ComponentFixture<SalonMenu>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SalonMenu],
    }).compileComponents();

    fixture = TestBed.createComponent(SalonMenu);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
