import axios from 'axios'

interface ApiFieldError {
  field?: string
  message?: string
}

interface ApiErrorPayload {
  message?: string
  fieldErrors?: ApiFieldError[]
}

export const getApiErrorMessage = (error: unknown, fallback: string) => {
  if (!axios.isAxiosError<ApiErrorPayload>(error)) {
    return fallback
  }

  const fieldMessages = error.response?.data?.fieldErrors
    ?.map((item) => item.message?.trim())
    .filter((message): message is string => Boolean(message))

  if (fieldMessages && fieldMessages.length > 0) {
    return Array.from(new Set(fieldMessages)).join('；')
  }

  return error.response?.data?.message ?? fallback
}
