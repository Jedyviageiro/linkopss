import type { ISODateTime, UUID } from '@/shared/types/api'

export interface Media {
  mediaId: UUID | null; resourceId: UUID; resourceType: 'PROVIDER_PROFILE' | 'SERVICE_IMAGE'
  url: string; contentType: string | null; size: number; createdAt: ISODateTime
}
