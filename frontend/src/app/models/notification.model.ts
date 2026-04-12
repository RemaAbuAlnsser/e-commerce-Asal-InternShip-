export interface Notification {
  id: number;
  type: 'NEW_ORDER' | 'LOW_STOCK' | 'OUT_OF_STOCK';
  title: string;
  message: string;
  isRead: boolean;
  createdAt: Date;
  route: string;
}
