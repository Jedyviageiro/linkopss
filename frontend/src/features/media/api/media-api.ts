import { apiRequest } from '@/shared/api/client'
import type { UUID } from '@/shared/types/api'
import type { Media } from '../types/media'

function imageForm(file: File) {
  const form = new FormData()
  form.append('file', file)
  return form
}

export const mediaApi = {
  uploadProviderImage: (file: File) => apiRequest<Media>('/media/providers/profile-image', { method: 'POST', body: imageForm(file) }),
  uploadServiceImage: (serviceId: UUID, file: File) => apiRequest<Media>(`/media/services/${serviceId}/images`, { method: 'POST', body: imageForm(file) }),
  listServiceImages: (serviceId: UUID) => apiRequest<Media[]>(`/media/services/${serviceId}/images`, { auth: false }),
}
