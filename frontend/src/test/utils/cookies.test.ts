import { describe, it, expect, beforeEach } from 'vitest'

describe('cookies utility', () => {
  let cookieStore: Record<string, string> = {}

  beforeEach(() => {
    cookieStore = {}
    Object.defineProperty(document, 'cookie', {
      get: () =>
        Object.entries(cookieStore)
          .map(([k, v]) => `${k}=${v}`)
          .join('; '),
      set: (val: string) => {
        const [nameVal] = val.split('=')
        if (val.includes('expires=Thu, 01 Jan 1970')) {
          delete cookieStore[nameVal]
        } else {
          cookieStore[nameVal] = val.split(';')[0].split('=')[1]
        }
      },
      configurable: true,
    })
  })

  it('should set and get a cookie', async () => {
    const { cookies } = await import('../../utils/cookies')
    cookies.set('test', 'value')
    expect(cookies.get('test')).toBe('value')
  })

  it('should set cookie with expiry days', async () => {
    const { cookies } = await import('../../utils/cookies')
    cookies.set('expiry', 'test-value', 7)
    expect(cookies.get('expiry')).toBe('test-value')
  })

  it('should return null for non-existent cookie', async () => {
    const { cookies } = await import('../../utils/cookies')
    expect(cookies.get('nonexistent')).toBeNull()
  })

  it('should remove a cookie', async () => {
    const { cookies } = await import('../../utils/cookies')
    cookies.set('removeme', 'value')
    cookies.remove('removeme')
    expect(cookies.get('removeme')).toBeNull()
  })

  it('should handle cookies with spaces', async () => {
    const { cookies } = await import('../../utils/cookies')
    cookies.set('withspace', 'value with spaces')
    expect(cookies.get('withspace')).toBe('value with spaces')
  })
})
