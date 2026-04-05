import { Component, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AdminService, AdminLoginRequest } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-login.component.html',
  styleUrl: './admin-login.component.css'
})
export class AdminLoginComponent {
  @ViewChild('emailInput') emailInput!: ElementRef;
  
  email = '';
  password = '';
  isLoading = false;
  errorMessage = '';
  submitted = false;

  constructor(
    private router: Router,
    private adminService: AdminService
  ) {}

  onLogin() {
    this.submitted = true;
    
    if (!this.email || !this.password) {
      this.errorMessage = 'Please fill in all fields';
      this.focusEmailInput();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const loginRequest: AdminLoginRequest = {
      email: this.email,
      password: this.password
    };

    this.adminService.login(loginRequest).subscribe({
      next: (response) => {
        this.isLoading = false;
        if (response.success) {
          // Store admin data if needed (localStorage, sessionStorage, etc.)
          if (response.admin) {
            sessionStorage.setItem('admin', JSON.stringify(response.admin));
          }
          // Navigate to admin dashboard
          this.router.navigate(['/admin/dashboard']);
        } else {
          this.errorMessage = response.message;
          this.focusEmailInput();
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = 'Invalid email or password';
        this.focusEmailInput();
        console.error('Login error:', error);
      }
    });
  }

  private focusEmailInput() {
  setTimeout(() => {
    this.emailInput?.nativeElement?.focus();
  }, 0);
}
}
