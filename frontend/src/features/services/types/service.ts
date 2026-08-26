import type { Location } from '@/features/location/types/location'
import type { ISODateTime, PageQuery, UUID } from '@/shared/types/api'

export type PriceType = 'FIXED' | 'NEGOTIABLE'
export interface ServiceOffering {
  id: UUID; providerId: UUID; providerName: string; city: string
  latitude: number | null; longitude: number | null; location: Location
  categoryId: UUID; categoryName: string; title: string; description: string | null
  price: number | null; priceType: PriceType; active: boolean
  createdAt: ISODateTime; updatedAt: ISODateTime
}
export interface CreateServiceRequest { categoryId: UUID; title: string; description?: string; price?: number | null; priceType: PriceType }
export type UpdateServiceRequest = Partial<CreateServiceRequest>
export interface ServiceQuery extends PageQuery { q?: string; category?: string; city?: string; minPrice?: number; maxPrice?: number }
