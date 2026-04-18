import { Component, Input, ElementRef, AfterViewInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-gooey-text',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="gooey-wrapper" [class]="className">
      <svg class="gooey-svg" aria-hidden="true" focusable="false">
        <defs>
          <filter id="gooey-threshold">
            <feColorMatrix in="SourceGraphic" type="matrix"
              values="1 0 0 0 0
                      0 1 0 0 0
                      0 0 1 0 0
                      0 0 0 255 -140" />
          </filter>
        </defs>
      </svg>
      <div class="gooey-texts" style="filter: url(#gooey-threshold)">
        <span #text1 class="gooey-span" [class]="textClassName"></span>
        <span #text2 class="gooey-span" [class]="textClassName"></span>
      </div>
    </div>
  `,
  styles: [`
    .gooey-wrapper { position: relative; }
    .gooey-svg { position: absolute; height: 0; width: 0; }
    .gooey-texts {
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
    }
    .gooey-span {
      position: absolute;
      display: inline-block;
      user-select: none;
      text-align: center;
      white-space: nowrap;
    }
  `]
})
export class GooeyTextComponent implements AfterViewInit, OnDestroy {
  @Input() texts: string[] = [];
  @Input() morphTime = 1;
  @Input() cooldownTime = 0.25;
  @Input() className = '';
  @Input() textClassName = '';

  @ViewChild('text1') text1Ref!: ElementRef<HTMLSpanElement>;
  @ViewChild('text2') text2Ref!: ElementRef<HTMLSpanElement>;

  private animFrameId: number | null = null;

  ngAfterViewInit() {
    if (!this.texts.length) return;
    this.startAnimation();
  }

  ngOnDestroy() {
    if (this.animFrameId !== null) cancelAnimationFrame(this.animFrameId);
  }

  private startAnimation() {
    let textIndex = this.texts.length - 1;
    let time = new Date();
    let morph = 0;
    let cooldown = this.cooldownTime;
    const morphTime = this.morphTime;
    const cooldownTime = this.cooldownTime;
    const texts = this.texts;
    const t1 = this.text1Ref.nativeElement;
    const t2 = this.text2Ref.nativeElement;

    t1.textContent = texts[textIndex % texts.length];
    t2.textContent = texts[(textIndex + 1) % texts.length];

    const setMorph = (fraction: number) => {
      t2.style.filter = `blur(${Math.min(8 / fraction - 8, 100)}px)`;
      t2.style.opacity = `${Math.pow(fraction, 0.4) * 100}%`;
      fraction = 1 - fraction;
      t1.style.filter = `blur(${Math.min(8 / fraction - 8, 100)}px)`;
      t1.style.opacity = `${Math.pow(fraction, 0.4) * 100}%`;
    };

    const doCooldown = () => {
      morph = 0;
      t2.style.filter = '';
      t2.style.opacity = '100%';
      t1.style.filter = '';
      t1.style.opacity = '0%';
    };

    const doMorph = () => {
      morph -= cooldown;
      cooldown = 0;
      let fraction = morph / morphTime;
      if (fraction > 1) {
        cooldown = cooldownTime;
        fraction = 1;
      }
      setMorph(fraction);
    };

    const animate = () => {
      this.animFrameId = requestAnimationFrame(animate);
      const newTime = new Date();
      const shouldIncrementIndex = cooldown > 0;
      const dt = (newTime.getTime() - time.getTime()) / 1000;
      time = newTime;
      cooldown -= dt;

      if (cooldown <= 0) {
        if (shouldIncrementIndex) {
          textIndex = (textIndex + 1) % texts.length;
          t1.textContent = texts[textIndex % texts.length];
          t2.textContent = texts[(textIndex + 1) % texts.length];
        }
        doMorph();
      } else {
        doCooldown();
      }
    };

    animate();
  }
}
