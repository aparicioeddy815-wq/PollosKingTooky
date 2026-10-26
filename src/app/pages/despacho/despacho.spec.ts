import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Despacho } from './despacho';

describe('Despacho', () => {
  let component: Despacho;
  let fixture: ComponentFixture<Despacho>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Despacho],
    }).compileComponents();

    fixture = TestBed.createComponent(Despacho);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
