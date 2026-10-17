import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-salon-menu',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './salon-menu.html',
  styleUrl: './salon-menu.scss'
})
export class SalonMenuComponent {

  constructor(private router: Router) {}

  // Para ir a la pantalla de Login
  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  // Permite bajar para ver el menu
  scrollTo(elementId: string): void {
    const el = document.getElementById(elementId);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' });
    }
  }

  combos = [
    {
      id: 1,
      badge: 'MÁS VENDIDO',
      title: 'Combo Económico 1',
      desc: '1 presa de pollo al horno o frito + 1 guarnición a tu elección (fideo o arroz) + Gaseosa personal 300ml.',
      tags: ['1 Presa', '1 Guarnición', 'Soda 300ml'],
      price: 41,
      img: 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=500&q=80'
    },
    {
      id: 2,
      badge: 'RECOMENDADO',
      title: 'Combo Económico 2',
      desc: '1 presa de pollo (pecho o pierna) + 2 guarniciones a elección + Gaseosa personal 300ml.',
      tags: ['1 Presa (Pierna)', '2 Guarniciones', 'Soda 300ml'],
      price: 43,
      img: 'https://images.unsplash.com/photo-1626645738196-c2a7c87a8f58?w=500&q=80'
    },
    {
      id: 3,
      badge: 'PARA DOS PERSONAS',
      title: 'Combo Duo 1',
      desc: '2 presas de pollo frito crocante o al horno + 2 guarniciones especiales + Gaseosa familiar 1.25L.',
      tags: ['2 Presas (Muslo/Pierna)', '2 Guarniciones', 'Soda 1.25L'],
      price: 55,
      img: 'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=500&q=80'
    },
    {
      id: 4,
      badge: 'EXTRA CRUNCH',
      title: 'Combo Duo 2',
      desc: '2 presas de pollo broaster con rebozado crujiente de especias + 2 guarniciones a elección + Gaseosa 1.25L.',
      tags: ['2 Presas (Ala/Pecho)', '2 Guarniciones', 'Soda 1.25L'],
      price: 58,
      img: 'https://images.unsplash.com/photo-1569058242253-92a9c755a0ec?w=500&q=80'
    }
  ];

  especialidades = [
    { title: '1 Presa (Muslo o Pierna)', desc: 'Pollo crujiente de primera calidad marinado en especias de la casa. Servido con 2 guarniciones favoritas.', price: 35, img: 'https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=300&q=80' },
    { title: '1 Presa (Ala o Pecho)', desc: 'La porción más jugosa y tierna, dorada a la perfección e impregnada de nuestro sazón artesanal.', price: 39, img: 'https://images.unsplash.com/photo-1626645738196-c2a7c87a8f58?w=300&q=80' },
    { title: '2 Presas (Muslo y Pierna)', desc: 'Doble porción de los cortes más jugosos para calmar el antojo, acompañados de 2 opciones completas de guarniciones.', price: 54, img: 'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=300&q=80' },
    { title: '2 Presas (Ala y Pecho)', desc: 'Sabor completo para conocedores: incluye pechuga y ala con el tradicional rebozado crocante.', price: 56, img: 'https://images.unsplash.com/photo-1569058242253-92a9c755a0ec?w=300&q=80' }
  ];

  guarniciones = [
    { title: 'Combo Salchipapa + Gaseosa', desc: 'Papas fritas crocantes con salchichas tipo vienesa, salsas de la casa y gaseosa 300ml.', price: 28, img: 'https://images.unsplash.com/photo-1585109649139-366815a0d713?w=300&q=80' },
    { title: 'Combo Pipocas + Gaseosa', desc: 'Bocadillos de pechuga crujiente estilo pipoca con aderezo especial y gaseosa 300ml.', price: 30, img: 'https://images.unsplash.com/photo-1562967914-608f82629710?w=300&q=80' },
    { title: 'Fideo al Pesto', desc: 'Porción individual de fideos frescos con salsa de albahaca fresca, queso rallado y condimentos tradicionales.', price: 14, img: 'https://images.unsplash.com/photo-1621996346565-e3d5d6281288?w=300&q=80' },
    { title: 'Fideos en Salsa de Zanahoria', desc: 'Sazón tradicional en salsa con un toque suave y dulce, perfecto acompañante de tu pollo.', price: 14, img: 'https://images.unsplash.com/photo-1551183053-bf91a1d81141?w=300&q=80' },
    { title: 'Porción Personal de Arroz', desc: 'Arroz blanco suelto cocido diariamente en su punto.', price: 14, img: 'https://images.unsplash.com/photo-1516684732162-798a0062be99?w=300&q=80' },
    { title: 'Platanitos Fritos', desc: 'Plátanos maduros fritos en tiras crocantes, equilibrio dulce y salado.', price: 14, img: 'https://images.unsplash.com/photo-1528751014936-863e6e7a319c?w=300&q=80' }
  ];
}
