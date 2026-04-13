import { Component, OnInit, PLATFORM_ID, inject } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { HeaderComponent } from '../landing/header/header.component';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [CommonModule, HeaderComponent],
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.css'
})
export class ContactComponent implements OnInit {
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);
  private get isBrowser() { return isPlatformBrowser(this.platformId); }

  ngOnInit() {
    // Scroll to footer when component loads
    this.scrollToFooter();
  }

  private scrollToFooter() {
    if (this.isBrowser) {
      // Small delay to ensure page is fully loaded
      setTimeout(() => {
        // Navigate to landing page with footer fragment
        this.router.navigate(['/'], { fragment: 'footer' }).then(() => {
          // Additional scroll to ensure we reach the footer
          setTimeout(() => {
            const footerElement = document.getElementById('footer');
            if (footerElement) {
              footerElement.scrollIntoView({ 
                behavior: 'smooth', 
                block: 'start' 
              });
            }
          }, 100);
        });
      }, 100);
    }
  }

  goToFooter() {
    this.scrollToFooter();
  }
}
