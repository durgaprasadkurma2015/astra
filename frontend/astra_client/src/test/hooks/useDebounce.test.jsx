import { renderHook, act } from '@testing-library/react';
import { vi } from 'vitest';
import { useDebounce } from '../../hooks/useDebounce';

describe('useDebounce', () => {
  it('returns the new value only after the delay', () => {
    vi.useFakeTimers();
    const { result, rerender } = renderHook(({ value }) => useDebounce(value, 300), { initialProps: { value: 'phone' } });
    rerender({ value: 'phones' });
    expect(result.current).toBe('phone');
    act(() => vi.advanceTimersByTime(299));
    expect(result.current).toBe('phone');
    act(() => vi.advanceTimersByTime(1));
    expect(result.current).toBe('phones');
    vi.useRealTimers();
  });
});
