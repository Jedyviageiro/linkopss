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

export type BookingStatusTone = 'waiting' | 'progress' | 'success' | 'muted'

export const bookingStatusInfo: Record<BookingStatus, { label: string; tone: BookingStatusTone }> = {
  PENDING: { label: 'Aguardando profissional', tone: 'waiting' },
  ACCEPTED: { label: 'Profissional a caminho', tone: 'progress' },
  IN_PROGRESS: { label: 'Em execução', tone: 'progress' },
  COMPLETED: { label: 'Concluído', tone: 'success' },
  CANCELLED: { label: 'Cancelado', tone: 'muted' },
  REJECTED: { label: 'Recusado', tone: 'muted' },
}

export const bookingStatusToneClasses: Record<BookingStatusTone, string> = {
  waiting: 'bg-[#FFF1D8] text-[#CF7A00]',
  progress: 'bg-[#E1F3FF] text-[#087BD2]',
  success: 'bg-[#DCF5E5] text-[#07833B]',
  muted: 'bg-[#E9EDF2] text-[#64748B]',
}

export const bookingActiveStatuses: BookingStatus[] = ['PENDING', 'ACCEPTED', 'IN_PROGRESS']

export function canCancelBooking(booking: Booking) {
  return booking.status === 'PENDING' || booking.status === 'ACCEPTED'
}

export function showsBookingProgress(booking: Booking) {
  return booking.status === 'ACCEPTED' || booking.status === 'IN_PROGRESS'
}

export function bookingProgressStep(booking: Booking) {
  if (booking.status === 'PENDING') return 1
  if (booking.status === 'ACCEPTED') return 2
  if (booking.status === 'IN_PROGRESS') return 3
  return 4
}
