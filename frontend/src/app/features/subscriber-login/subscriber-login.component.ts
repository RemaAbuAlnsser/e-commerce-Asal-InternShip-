import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SubscriptionService } from '../../services/subscription.service';
import { SubscriberAuthService } from '../../services/subscriber-auth.service';

@Component({
  selector: 'app-subscriber-login',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './subscriber-login.component.html',
  styleUrl: './subscriber-login.component.css'
})
export class SubscriberLoginComponent implements OnInit {
  readonly isLoading = signal(true);
  readonly success   = signal(false);
  readonly message   = signal('');

  constructor(
    private route:            ActivatedRoute,
    private router:           Router,
    private subscriptionService: SubscriptionService,
    private subscriberAuth:   SubscriberAuthService
  ) {}

  ngOnInit() {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.isLoading.set(false);
      this.success.set(false);
      this.message.set('No sign-in token provided.');
      return;
    }

    this.subscriptionService.verifyLogin(token).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        this.success.set(res.success);
        this.message.set(res.message);
        if (res.success && res.name && res.email) {
          this.subscriberAuth.login(res.name, res.email);
          // Redirect to home after a short delay
          setTimeout(() => this.router.navigate(['/']), 2000);
        }
      },
      error: () => {
        this.isLoading.set(false);
        this.success.set(false);
        this.message.set('Something went wrong. Please try again.');
      }
    });
  }
}
