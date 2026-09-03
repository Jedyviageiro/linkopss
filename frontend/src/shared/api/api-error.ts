import type { ApiErrorPayload } from '@/shared/types/api'

export class ApiError extends Error {
  readonly status: number
  readonly title: string
  readonly path?: string
  readonly validationErrors: Record<string, string>

  constructor(status: number, message: string, payload?: Partial<ApiErrorPayload>) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.title = payload?.error ?? 'Erro na requisição'
    this.path = payload?.path
    this.validationErrors = payload?.validationErrors ?? {}
  }
}

const friendlyStatusMessages: Record<number, string> = {
  0: 'Não conseguimos concluir agora. Verifique sua ligação à internet e tente novamente.',
  400: 'Confira as informações preenchidas e tente novamente.',
  401: 'Confira seus dados de acesso ou entre novamente.',
  403: 'Esta ação não está disponível para sua conta.',
  404: 'Não encontramos o que você procura.',
  405: 'Esta ação não está disponível neste momento.',
  409: 'Estas informações já estão sendo usadas.',
  413: 'O ficheiro escolhido é maior do que o permitido.',
  415: 'Este tipo de ficheiro não é suportado.',
  422: 'Confira as informações preenchidas e tente novamente.',
  429: 'Foram feitas muitas tentativas. Aguarde um pouco e tente novamente.',
  500: 'Algo não correu como esperado. Tente novamente em alguns instantes.',
  502: 'Não conseguimos concluir agora. Tente novamente mais tarde.',
  503: 'Não conseguimos concluir agora. Tente novamente mais tarde.',
  504: 'A operação está demorando mais do que o esperado. Tente novamente.',
}

export function getErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return friendlyStatusMessages[error.status]
      ?? (error.status >= 500
        ? 'Não conseguimos concluir agora. Tente novamente mais tarde.'
        : 'Não conseguimos concluir esta ação. Tente novamente.')
  }
  if (error instanceof TypeError) return friendlyStatusMessages[0]!
  return 'Ocorreu um erro inesperado. Tente novamente.'
}
