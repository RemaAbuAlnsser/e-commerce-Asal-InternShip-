import { Component, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  AnnouncementService,
  Announcement,
  AnnouncementConfig
} from '../../../services/announcement.service';

interface AnnForm {
  text: string;
  link: string;
  isActive: boolean;
  displayOrder: number;
}

function blankForm(nextOrder: number): AnnForm {
  return { text: '', link: '', isActive: true, displayOrder: nextOrder };
}

@Component({
  selector: 'app-announcements',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './announcements.component.html',
  styleUrl: './announcements.component.css'
})
export class AnnouncementsComponent {
  private readonly svc = inject(AnnouncementService);

  /* ── Service data ─────────────────────────────────────────── */
  readonly announcements  = this.svc.announcements;
  readonly config         = this.svc.config;
  readonly activeCount    = computed(() => this.svc.activeAnnouncements().length);
  readonly inactiveCount  = computed(() => this.announcements().filter(a => !a.isActive).length);

  /* Sorted list for display (by displayOrder) */
  readonly sortedList = computed(() =>
    [...this.announcements()].sort((a, b) => a.displayOrder - b.displayOrder)
  );

  /* Active items for the preview ticker */
  readonly previewItems = this.svc.activeAnnouncements;

  /* ── Modal / form state ───────────────────────────────────── */
  readonly modalOpen  = signal(false);
  readonly editingId  = signal<string | null>(null);
  readonly form       = signal<AnnForm>(blankForm(1));
  readonly deleteId   = signal<string | null>(null);
  readonly successMsg = signal('');
  readonly errorMsg   = signal('');

  /* Local copy of config for the settings form */
  cfgForm: AnnouncementConfig = { ...this.config() };

  /* ── Modal open / close ───────────────────────────────────── */
  openCreate(): void {
    this.form.set(blankForm(this.announcements().length + 1));
    this.editingId.set(null);
    this.modalOpen.set(true);
  }

  openEdit(ann: Announcement): void {
    this.form.set({
      text: ann.text,
      link: ann.link ?? '',
      isActive: ann.isActive,
      displayOrder: ann.displayOrder
    });
    this.editingId.set(ann.id);
    this.modalOpen.set(true);
  }

  closeModal(): void {
    this.modalOpen.set(false);
    this.editingId.set(null);
    this.errorMsg.set('');
  }

  /* ── Save ─────────────────────────────────────────────────── */
  saveAnnouncement(): void {
    const f = this.form();
    if (!f.text.trim()) {
      this.errorMsg.set('Announcement text is required.');
      return;
    }

    const payload = {
      text: f.text.trim(),
      link: f.link.trim() || undefined,
      isActive: f.isActive,
      displayOrder: Number(f.displayOrder) || 1
    };

    const id = this.editingId();
    if (id) {
      this.svc.update(id, payload);
      this.flash('Announcement updated successfully.');
    } else {
      this.svc.add(payload);
      this.flash('Announcement added successfully.');
    }
    this.closeModal();
  }

  /* ── Toggle ───────────────────────────────────────────────── */
  toggle(id: string): void {
    this.svc.toggleActive(id);
  }

  /* ── Delete ───────────────────────────────────────────────── */
  askDelete(id: string): void {
    this.deleteId.set(id);
  }

  confirmDelete(): void {
    const id = this.deleteId();
    if (!id) return;
    this.svc.remove(id);
    this.deleteId.set(null);
    this.flash('Announcement deleted.');
  }

  cancelDelete(): void {
    this.deleteId.set(null);
  }

  /* ── Config ───────────────────────────────────────────────── */
  saveConfig(): void {
    this.svc.updateConfig({ ...this.cfgForm });
    this.flash('Display settings saved.');
  }

  /* ── Helpers ──────────────────────────────────────────────── */
  patchForm(partial: Partial<AnnForm>): void {
    this.form.update(f => ({ ...f, ...partial }));
  }

  getById(id: string): Announcement | undefined {
    return this.announcements().find(a => a.id === id);
  }

  trackById(_: number, ann: Announcement): string {
    return ann.id;
  }

  private flash(msg: string): void {
    this.successMsg.set(msg);
    setTimeout(() => this.successMsg.set(''), 3500);
  }
}
