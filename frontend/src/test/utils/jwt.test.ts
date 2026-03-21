import { describe, it, expect } from 'vitest'
import { decodeToken, type TokenPayload } from '../../utils/jwt'

describe('decodeToken', () => {
  it('should decode a valid JWT token', () => {
    const payload: TokenPayload = {
      sub: '123',
      email: 'test@example.com',
      role: 'CUSTOMER',
      exp: Math.floor(Date.now() / 1000) + 3600,
      iat: Math.floor(Date.now() / 1000),
    }
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
    const body = btoa(JSON.stringify(payload))
    const token = `${header}.${body}.signature`

    const result = decodeToken(token)

    expect(result).toEqual(payload)
  })

  it('should return null for invalid token', () => {
    expect(decodeToken('invalid-token')).toBeNull()
  })

  it('should return null for token with invalid base64', () => {
    const token = 'notavalidbase64!!!.body.signature'
    expect(decodeToken(token)).toBeNull()
  })

  it('should decode token with URL-safe base64 characters', () => {
    const payload: TokenPayload = {
      sub: '456',
      email: 'user@test.com',
      role: 'ADMIN',
      exp: Math.floor(Date.now() / 1000) + 7200,
      iat: Math.floor(Date.now() / 1000),
    }
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
    const body = btoa(JSON.stringify(payload)).replace(/\+/g, '-').replace(/\//g, '_')
    const token = `${header}.${body}.signature`

    const result = decodeToken(token)

    expect(result).toEqual(payload)
  })
})
