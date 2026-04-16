import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SubscribeResponse {
  success: boolean;
  message: string;
  name?: string;
  email?: string;
}

@Injectable({ providedIn: 'root' })
export class SubscriptionService {
  private readonly base = 'http://localhost:3000/api/subscribe';

  constructor(private http: HttpClient) {}

  subscribe(email: string): Observable<SubscribeResponse> {
    return this.http.post<SubscribeResponse>(this.base, { email });
  }

  verify(token: string): Observable<SubscribeResponse> {
    return this.http.get<SubscribeResponse>(`${this.base}/verify`, {
      params: { token }
    });
  }

  requestLogin(email: string): Observable<SubscribeResponse> {
    return this.http.post<SubscribeResponse>(`${this.base}/login`, { email });
  }

  verifyLogin(token: string): Observable<SubscribeResponse> {
    return this.http.get<SubscribeResponse>(`${this.base}/login-verify`, {
      params: { token }
    });
  }
}
