import type { ISODateTime, UUID } from '@/shared/types/api'

export interface Review {
  id: UUID; bookingId: UUID; providerId: UUID; clientId: UUID; clientName: string
  rating: number; comment: string | null; createdAt: ISODateTime; updatedAt: ISODateTime
}
export interface CreateReviewRequest { rating: number; comment?: string }
