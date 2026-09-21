import reducer, { setSession, logout } from '../../store/authSlice';

describe('authSlice', () => {
  it('stores a session and persists it to localStorage', () => {
    const state = reducer(undefined, setSession({
      accessToken: 'access-123', refreshToken: 'refresh-123',
      userId: 7, name: 'Test User', email: 'test@example.com', role: 'CUSTOMER'
    }));

    expect(state.accessToken).toBe('access-123');
    expect(state.refreshToken).toBe('refresh-123');
    expect(state.user).toEqual({ userId: 7, name: 'Test User', email: 'test@example.com', role: 'CUSTOMER' });
    expect(JSON.parse(localStorage.getItem('astra_user'))).toEqual(state.user);
  });

  it('clears the session on logout', () => {
    localStorage.setItem('astra_access_token', 'a');
    localStorage.setItem('astra_refresh_token', 'r');
    localStorage.setItem('astra_user', JSON.stringify({ email: 'test@example.com' }));
    const initial = reducer(undefined, setSession({ accessToken: 'a', refreshToken: 'r', email: 'test@example.com' }));
    const state = reducer(initial, logout());
    expect(state.user).toBeNull();
    expect(state.accessToken).toBeNull();
    expect(state.refreshToken).toBeNull();
    expect(localStorage.getItem('astra_access_token')).toBeNull();
  });
});
