import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SubscriptionService } from '../../services/subscription.service';
import { SubscriberAuthService } from '../../services/subscriber-auth.service';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './verify-email.component.html',
  styleUrl: './verify-email.component.css'
})
export class VerifyEmailComponent implements OnInit {
  readonly isLoading = signal(true);
  readonly success = signal(false);
  readonly message = signal('');

  constructor(
    private route: ActivatedRoute,
    private subscriptionService: SubscriptionService,
    private subscriberAuth: SubscriberAuthService
  ) {}

  ngOnInit() {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.isLoading.set(false);
      this.success.set(false);
      this.message.set('No verification token provided.');
      return;
    }

    this.subscriptionService.verify(token).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        this.success.set(res.success);
        this.message.set(res.message);
        // Save session so the header shows the subscriber's name
        if (res.success && res.name && res.email && res.token) {
          this.subscriberAuth.login(res.name, res.email, res.token);
        }
      },
      error: () => {
        this.isLoading.set(false);
        this.success.set(false);
        this.message.set('Something went wrong. Please try again later.');
      }
    });
  }
}
