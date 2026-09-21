import { useCallback, useEffect, useState } from 'react';

export function useAsync(task, dependencies = []) {
  const [state, setState] = useState({ data: null, loading: true, error: null });
  const run = useCallback(async () => {
    setState(s => ({ ...s, loading: true, error: null }));
    try {
      const data = await task();
      setState({ data, loading: false, error: null });
      return data;
    } catch (error) {
      setState({ data: null, loading: false, error });
      throw error;
    }
  // task/dependencies intentionally form the caller's async lifecycle.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, dependencies);
  useEffect(() => { run().catch(() => {}); }, [run]);
  return { ...state, refetch: run };
}
