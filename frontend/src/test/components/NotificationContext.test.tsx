import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { NotificationProvider } from '../../contexts/NotificationContext'

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <NotificationProvider>{children}</NotificationProvider>
      </BrowserRouter>
    </QueryClientProvider>
  )
}

describe('NotificationContext', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render children', () => {
    const wrapper = createWrapper()
    render(<div>Test Content</div>, { wrapper })
    expect(screen.getByText('Test Content')).toBeInTheDocument()
  })
})
