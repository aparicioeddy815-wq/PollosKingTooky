import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Ticket {
  number: string;
  status: 'READY' | 'PREPARING';
  timeAgo: string;
  description?: string;
}

@Component({
  selector: 'app-dispatch',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './despacho.html',
  styleUrl: './despacho.scss'
})
export class DespachoComponent {
  sidebarOpen = true;

  tickets: Ticket[] = [
    { number: '103', status: 'READY', timeAgo: '' },
    { number: '104', status: 'PREPARING', timeAgo: 'Hace 4 min' },
    { number: '098', status: 'READY', timeAgo: '' },
    { number: '107', status: 'PREPARING', timeAgo: 'Hace 6 min' },
    { number: '101', status: 'READY', timeAgo: '' },
    { number: '108', status: 'PREPARING', timeAgo: 'Hace 11 min' },
    { number: '105', status: 'READY', timeAgo: '' },
    { number: '111', status: 'PREPARING', timeAgo: 'Hace 2 min' },
    { number: '112', status: 'PREPARING', timeAgo: 'Hace 1 min' },
  ];

  playDingDong(ticketNumber?: string) {
    try {
      const AudioContext = window.AudioContext || (window as any).webkitAudioContext;
      if (AudioContext) {
        const ctx = new AudioContext();
        const now = ctx.currentTime;

        const osc1 = ctx.createOscillator();
        const gain1 = ctx.createGain();
        osc1.type = 'triangle';
        osc1.frequency.setValueAtTime(659.25, now);
        gain1.gain.setValueAtTime(0.4, now);
        gain1.gain.exponentialRampToValueAtTime(0.001, now + 0.6);
        osc1.connect(gain1);
        gain1.connect(ctx.destination);
        osc1.start(now);
        osc1.stop(now + 0.6);

        const osc2 = ctx.createOscillator();
        const gain2 = ctx.createGain();
        osc2.type = 'sine';
        osc2.frequency.setValueAtTime(523.25, now + 0.3);
        gain2.gain.setValueAtTime(0, now);
        gain2.gain.setValueAtTime(0.45, now + 0.3);
        gain2.gain.exponentialRampToValueAtTime(0.001, now + 1.2);
        osc2.connect(gain2);
        gain2.connect(ctx.destination);
        osc2.start(now + 0.3);
        osc2.stop(now + 1.2);
      }
    } catch (e) {
      // Audio del pedido
    }
  }

  markAsReady(ticketNumber: string) {
    const ticket = this.tickets.find(t => t.number === ticketNumber);
    if (ticket) {
      ticket.status = 'READY';
      ticket.timeAgo = '';
      this.playDingDong(ticketNumber);
    }
  }

  markAsDelivered(ticketNumber: string) {
    this.tickets = this.tickets.filter(t => t.number !== ticketNumber);
  }

  addNewOrder() {
    const randomNum = Math.floor(100 + Math.random() * 900).toString();
    this.tickets.unshift({
      number: randomNum,
      status: 'PREPARING',
      timeAgo: 'Hace 0 min'
    });
  }

  toggleSidebarView() {
    this.sidebarOpen = !this.sidebarOpen;
  }

  toggleHallwayFullscreen() {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen().catch(() => {});
      this.sidebarOpen = false;
    } else {
      document.exitFullscreen().catch(() => {});
    }
  }
}
