export type PaymentMethod = 'CASH' | 'MPESA'
export type PaymentStatus = 'PENDING' | 'PAID' | 'NOT_CONFIRMED'

export const paymentMethodLabels: Record<PaymentMethod, string> = {
  CASH: 'Dinheiro',
  MPESA: 'M-Pesa',
}

export const paymentStatusLabels: Record<PaymentStatus, string> = {
  PENDING: 'Pendente',
  PAID: 'Pago',
  NOT_CONFIRMED: 'Não confirmado',
}
