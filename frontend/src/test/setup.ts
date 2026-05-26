import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, vi } from 'vitest'

Object.defineProperty(window, 'scrollTo', {
  value: vi.fn(),
  writable: true,
})

beforeEach(() => {
  setActivePinia(createPinia())
  localStorage.clear()
})
