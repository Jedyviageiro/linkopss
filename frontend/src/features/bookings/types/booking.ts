import type { PriceType } from '@/features/services/types/service'
import type { PaymentMethod, PaymentStatus } from '@/features/payment/types/payment'
import type { ISODateTime, PageQuery, UUID } from '@/shared/types/api'

export type BookingStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
export interface Booking {
  id: UUID; clientId: UUID; clientName: string; providerId: UUID; providerName: string
  serviceOfferingId: UUID; serviceTitle: string; categoryId: UUID; categoryName: string
  price: number | null; priceType: PriceType; scheduledAt: ISODateTime; address: string
  notes: string | null; status: BookingStatus; paymentMethod: PaymentMethod
  paymentMethodName: string; paymentStatus: PaymentStatus; paymentStatusName: string
  createdAt: ISODateTime; updatedAt: ISODateTime
}

export interface CreateBookingRequest {
  serviceOfferingId: UUID; scheduledAt: ISODateTime; address: string
  notes?: string; paymentMethod: PaymentMethod
}
export type BookingQuery = PageQuery
