import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HeaderComponent } from '../landing/header/header.component';
import { WishlistService, WishlistItem } from '../../services/wishlist.service';
import { CartService } from '../../services/cart.service';
import { ProductService } from '../../services/product.service';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [CommonModule, RouterLink, HeaderComponent],
  templateUrl: './wishlist.component.html',
  styleUrl: './wishlist.component.css'
})
export class WishlistComponent {
  readonly wishlist = inject(WishlistService);
  readonly cart     = inject(CartService);
  private  ps       = inject(ProductService);

  getImage(url: string): string {
    return this.ps.resolveImageUrl(url);
  }

  moveToCart(item: WishlistItem): void {
    this.cart.add({
      productId:    item.productId,
      productName:  item.productName,
      productImage: item.productImage,
      categoryName: item.categoryName,
      price:        item.price,
      oldPrice:     item.oldPrice,
      maxStock:     item.totalStock
    });
    this.wishlist.remove(item.productId);
  }
}
