import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { Toast } from '../../components/notifications/Toast'
import type { AppNotification } from '../../api/types/notification'

const createNotification = (overrides: Partial<AppNotification> = {}): AppNotification => ({
  id: 'test-id',
  title: 'Test Title',
  message: 'Test message',
  type: 'info',
  timestamp: Date.now(),
  read: false,
  ...overrides,
})

describe('Toast Component', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render toast with message', () => {
    const notification = createNotification({ message: 'Test message' })
    render(<Toast notification={notification} onDismiss={vi.fn()} />)
    expect(screen.getByText('Test message')).toBeInTheDocument()
  })

  it('should render toast with title', () => {
    const notification = createNotification({ title: 'Test Title' })
    render(<Toast notification={notification} onDismiss={vi.fn()} />)
    expect(screen.getByText('Test Title')).toBeInTheDocument()
  })

  it('should render success toast', () => {
    const notification = createNotification({ type: 'success' })
    const { container } = render(<Toast notification={notification} onDismiss={vi.fn()} />)
    expect(container.firstChild).toHaveClass('bg-green-50')
  })

  it('should render error toast', () => {
    const notification = createNotification({ type: 'error' })
    const { container } = render(<Toast notification={notification} onDismiss={vi.fn()} />)
    expect(container.firstChild).toHaveClass('bg-red-50')
  })

  it('should render warning toast', () => {
    const notification = createNotification({ type: 'warning' })
    const { container } = render(<Toast notification={notification} onDismiss={vi.fn()} />)
    expect(container.firstChild).toHaveClass('bg-yellow-50')
  })

  it('should render info toast', () => {
    const notification = createNotification({ type: 'info' })
    const { container } = render(<Toast notification={notification} onDismiss={vi.fn()} />)
    expect(container.firstChild).toHaveClass('bg-blue-50')
  })

  it('should call onDismiss when X button is clicked', async () => {
    const onDismiss = vi.fn()
    const notification = createNotification()
    render(<Toast notification={notification} onDismiss={onDismiss} duration={0} />)

    const closeButton = screen.getByRole('button')
    closeButton.click()

    await vi.waitFor(() => {
      expect(onDismiss).toHaveBeenCalledWith('test-id')
    })
  })

  it('should render without title', () => {
    const notification = createNotification({ title: undefined })
    const { container } = render(<Toast notification={notification} onDismiss={vi.fn()} />)
    expect(container.firstChild).toBeInTheDocument()
  })
})
