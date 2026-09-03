import { ApiError, getErrorMessage } from '@/shared/api/api-error'

export function getLoginErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    if (error.status === 400 || error.status === 401) {
      return 'Confira o e-mail e a senha e tente novamente.'
    }
    if (error.status === 403) {
      return 'Esta conta não está disponível. Entre em contacto com o suporte se precisar de ajuda.'
    }
  }
  return getErrorMessage(error)
}

export function getRegistrationErrorMessage(error: unknown): string {
  if (!(error instanceof ApiError)) return getErrorMessage(error)

  if (error.status === 409) {
    return 'Este e-mail já está associado a uma conta. Tente entrar ou recuperar sua senha.'
  }
  if (error.status !== 400) return getErrorMessage(error)

  const fields = error.validationErrors
  if (fields.email) return 'Digite um endereço de e-mail válido.'
  if (fields.phone) return 'Digite um número moçambicano válido com 9 dígitos.'
  if (fields.passwordConfirmed) return 'Digite a mesma senha nos dois campos.'
  if (fields.password || fields.confirmPassword) {
    return 'Use uma senha entre 8 e 72 caracteres.'
  }
  if (fields.firstName || fields.lastName) return 'Confira seu nome completo.'
  if (fields.role) return 'Escolha se deseja contratar ou prestar serviços.'
  return 'Confira as informações preenchidas e tente novamente.'
}

export function getRecoveryRequestErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    if (error.status === 400) return 'Digite um endereço de e-mail válido.'
    if (error.status === 429) return 'Aguarde um momento antes de pedir novas instruções.'
    if (error.status === 503) return 'Não conseguimos enviar o e-mail agora. Tente novamente em alguns instantes.'
  }
  return getErrorMessage(error)
}

export function getPasswordResetErrorMessage(error: unknown): string {
  if (!(error instanceof ApiError)) return getErrorMessage(error)

  if (error.status === 400) {
    const fields = error.validationErrors
    if (fields.passwordConfirmed) return 'Digite a mesma senha nos dois campos.'
    if (fields.password || fields.confirmPassword) {
      return 'Use uma senha entre 8 e 72 caracteres.'
    }
    if (fields.token) {
      return 'Este link não está completo. Peça novas instruções para continuar.'
    }
    if (error.message.includes('diferente da atual')) {
      return 'Escolha uma senha diferente da que você já usa.'
    }
    return 'Este link já foi usado ou perdeu a validade. Peça novas instruções para continuar.'
  }

  if (error.status === 429) return 'Aguarde um momento antes de tentar novamente.'
  return getErrorMessage(error)
}
