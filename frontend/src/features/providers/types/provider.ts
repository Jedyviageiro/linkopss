import type { Location } from '@/features/location/types/location'
import type { ISODateTime, PageQuery, UUID } from '@/shared/types/api'

export type ProviderStatus = 'ACTIVE' | 'SUSPENDED' | 'DEACTIVATED'
export type ProviderVerificationStatus = 'NOT_REQUESTED' | 'PENDING' | 'VERIFIED' | 'REJECTED'

export interface Provider {
  id: UUID; userId: UUID; firstName: string; lastName: string; bio: string | null
  profileImageUrl: string | null; city: string; latitude: number | null; longitude: number | null
  location: Location; verified: boolean; verificationStatus: ProviderVerificationStatus
  verificationRequestedAt: ISODateTime | null; verificationReviewedAt: ISODateTime | null
  verificationReviewedBy: UUID | null; verificationNote: string | null
  averageRating: number; completedJobs: number; status: ProviderStatus
  createdAt: ISODateTime; updatedAt: ISODateTime
}

export interface ProviderProfileRequest {
  bio?: string | null; city: string; latitude?: number | null; longitude?: number | null
}
export type UpdateProviderProfileRequest = Partial<ProviderProfileRequest>
export interface ProviderQuery extends PageQuery { q?: string; category?: string; city?: string }
