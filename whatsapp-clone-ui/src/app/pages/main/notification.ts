export interface Notification {
  chatId?: string;
  content?: string;
  senderId?: string;
  receiverId?: string;
  messageType?: 'TEXT' | 'IMAGE' | 'AUDIO' | 'VIDEO';
  type?: 'SEEN' | 'MESSAGE' | 'IMAGE' | 'VIDEO' | 'AUDIO';
  chatName?: string;
  media?: Array<string>;
}
