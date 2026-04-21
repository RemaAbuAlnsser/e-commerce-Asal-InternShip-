import {
  Component, signal, ViewChild, ElementRef,
  AfterViewChecked, inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AiChatService, ChatMessage } from '../../services/ai-chat.service';

@Component({
  selector: 'app-ai-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-chat.component.html',
  styleUrl: './ai-chat.component.css'
})
export class AiChatComponent implements AfterViewChecked {

  private chatService = inject(AiChatService);

  @ViewChild('messagesEnd') private messagesEnd!: ElementRef;

  isOpen    = signal(false);
  messages  = signal<ChatMessage[]>([
    { role: 'assistant', content: 'Hi! 👋 I\'m your shopping assistant. Ask me about products, prices, availability, or anything about our store!' }
  ]);
  isLoading = signal(false);
  inputText = '';

  private shouldScroll = false;

  toggleChat() {
    this.isOpen.update(v => !v);
  }

  sendMessage() {
    const text = this.inputText.trim();
    if (!text || this.isLoading()) return;

    this.messages.update(msgs => [...msgs, { role: 'user', content: text }]);
    this.inputText = '';
    this.isLoading.set(true);
    this.shouldScroll = true;

    this.chatService.send(text).subscribe({
      next: (res) => {
        this.messages.update(msgs => [...msgs, { role: 'assistant', content: res.reply }]);
        this.isLoading.set(false);
        this.shouldScroll = true;
      },
      error: () => {
        this.messages.update(msgs => [...msgs, { role: 'assistant', content: 'Sorry, something went wrong. Please try again.' }]);
        this.isLoading.set(false);
        this.shouldScroll = true;
      }
    });
  }

  onKeyDown(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  ngAfterViewChecked() {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  private scrollToBottom() {
    try {
      this.messagesEnd?.nativeElement.scrollIntoView({ behavior: 'smooth' });
    } catch {}
  }
}
