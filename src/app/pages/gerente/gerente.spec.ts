import { ComponentFixture, TestBed } from '@angular/core/testing';
import { GerenteComponent } from './gerente';

describe('GerenteComponent', () => {
  let component: GerenteComponent;
  let fixture: ComponentFixture<GerenteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GerenteComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(GerenteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
