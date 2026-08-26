import type { ISODateTime, UUID } from '@/shared/types/api'

export type NotificationType =
  | 'BOOKING_CREATED' | 'BOOKING_ACCEPTED' | 'BOOKING_REJECTED'
  | 'BOOKING_CANCELLED' | 'BOOKING_COMPLETED' | 'REVIEW_RECEIVED'
  | 'PROVIDER_VERIFIED' | 'PROVIDER_VERIFICATION_REJECTED' | 'PROVIDER_VERIFICATION_REVOKED'

export interface Notification {
  id: UUID; type: NotificationType; title: string; message: string
  referenceId: UUID | null; read: boolean; readAt: ISODateTime | null; createdAt: ISODateTime
}
