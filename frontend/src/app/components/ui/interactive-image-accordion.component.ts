import {
  Component,
  Input,
  OnChanges,
  SimpleChanges,
  signal,
  computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { SiteImage } from '../../services/settings.service';
import { environment } from '../../../environments/environment';

export interface AccordionImage {
  id: number;
  imageUrl: string;
  label: string;
}

@Component({
  selector: 'app-interactive-image-accordion',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bg-white font-sans">
      <section class="container mx-auto px-4 py-12 md:py-24">
        <div class="flex flex-col md:flex-row items-center justify-between gap-12">

          <!-- Left: Text Content -->
          <div class="w-full md:w-1/2 text-center md:text-left">
            <h1 class="text-4xl md:text-6xl font-bold text-gray-900 leading-tight tracking-tighter">
              Discover Our Latest Collections
            </h1>
            <p class="mt-6 text-lg text-gray-600 max-w-xl mx-auto md:mx-0">
              Explore hand-picked styles crafted for every occasion — quality
              products and exceptional service, delivered to your door.
            </p>
            <div class="mt-8">
              <button
                routerLink="/categories"
                class="inline-block bg-gray-900 text-white font-semibold px-8 py-3 rounded-lg shadow-lg hover:bg-gray-800 transition-colors duration-300"
              >
                Shop Now
              </button>
            </div>
          </div>

          <!-- Right: Image Accordion -->
          <div class="w-full md:w-1/2">
            <div class="flex flex-row items-center justify-center gap-4 overflow-x-auto p-4">
              @for (item of accordionImages(); track item.id; let i = $index) {
                <div
                  class="relative h-[450px] rounded-2xl overflow-hidden cursor-pointer transition-all duration-700 ease-in-out"
                  [class.w-accordion-active]="i === activeIndex()"
                  [class.w-accordion-inactive]="i !== activeIndex()"
                  (mouseenter)="setActive(i)"
                >
                  <!-- Background Image -->
                  <img
                    [src]="item.imageUrl"
                    [alt]="item.label"
                    class="absolute inset-0 w-full h-full object-cover"
                  />

                  <!-- Dark overlay -->
                  <div class="absolute inset-0 bg-black bg-opacity-40"></div>

                  <!-- Caption -->
                  <span
                    class="absolute text-white text-lg font-semibold whitespace-nowrap transition-all duration-300 ease-in-out"
                    [ngClass]="i === activeIndex()
                      ? 'bottom-6 left-1/2 -translate-x-1/2 rotate-0'
                      : 'bottom-24 left-1/2 -translate-x-1/2 rotate-90'"
                  >
                    {{ item.label }}
                  </span>
                </div>
              }
            </div>
          </div>

        </div>
      </section>
    </div>
  `,
  styles: [`
    .w-accordion-active  { width: 400px; }
    .w-accordion-inactive { width: 60px;  }

    @media (max-width: 640px) {
      .w-accordion-active  { width: 260px; }
      .w-accordion-inactive { width: 48px; }
    }
  `],
})
export class InteractiveImageAccordionComponent implements OnChanges {
  @Input() images: SiteImage[] = [];

  readonly activeIndex = signal(0);

  readonly accordionImages = computed<AccordionImage[]>(() =>
    this._images().map((img, i) => ({
      id: img.id,
      imageUrl: this.resolveUrl(img.imageUrl),
      label: `Look ${img.displayOrder ?? i + 1}`,
    }))
  );

  private readonly _images = signal<SiteImage[]>([]);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['images']) {
      this._images.set(this.images ?? []);
      // Default active item to the last one (mirrors the React original)
      const len = (this.images ?? []).length;
      this.activeIndex.set(len > 0 ? len - 1 : 0);
    }
  }

  setActive(index: number): void {
    this.activeIndex.set(index);
  }

  private resolveUrl(url: string): string {
    if (!url) return 'https://placehold.co/400x450/2d3748/ffffff?text=No+Image';
    return url.startsWith('http') ? url : `${environment.backendUrl}${url}`;
  }
}
