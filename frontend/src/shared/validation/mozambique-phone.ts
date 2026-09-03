export const MOZAMBIQUE_MOBILE_PREFIXES = ['82', '83', '84', '85', '86', '87'] as const

const mozambiqueMobilePattern = /^(?:82|83|84|85|86|87)\d{7}$/

export function sanitizeMozambiqueMobile(value: string): string {
  return value.replace(/\D/g, '').slice(0, 9)
}

export function isMozambiqueMobile(value: string): boolean {
  return mozambiqueMobilePattern.test(value)
}
