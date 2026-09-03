const MIN_PASSWORD_CHARACTERS = 8
const MAX_BCRYPT_BYTES = 72

export function getPasswordValidationMessage(password: string): string | null {
  if (password.length < MIN_PASSWORD_CHARACTERS) {
    return `A senha deve ter pelo menos ${MIN_PASSWORD_CHARACTERS} caracteres.`
  }

  if (new TextEncoder().encode(password).length > MAX_BCRYPT_BYTES) {
    return 'A senha é demasiado longa. Use no máximo 72 bytes.'
  }

  return null
}
