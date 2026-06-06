import { useState, useCallback, useEffect } from 'react';
import request from '@/utils/request';

const useRequest = (service, options = {}) => {
  const {
    manual = false,
    defaultParams = {},
    onSuccess,
    onError,
    formatResult,
    refreshDeps = [],
  } = options;

  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const run = useCallback(async (params = {}) => {
    setLoading(true);
    setError(null);
    try {
      const response = typeof service === 'string'
        ? await request({ url: service, method: 'GET', params: { ...defaultParams, ...params } })
        : await service({ ...defaultParams, ...params });

      const result = formatResult ? formatResult(response) : response;
      setData(result);
      onSuccess?.(result, params);
      return result;
    } catch (err) {
      setError(err);
      onError?.(err, params);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [service, defaultParams, formatResult, onSuccess, onError]);

  const runMutate = useCallback(async (data = {}) => {
    setLoading(true);
    setError(null);
    try {
      const response = typeof service === 'string'
        ? await request({ url: service, method: 'POST', data })
        : await service(data);

      const result = formatResult ? formatResult(response) : response;
      setData(result);
      onSuccess?.(result, data);
      return result;
    } catch (err) {
      setError(err);
      onError?.(err, data);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [service, formatResult, onSuccess, onError]);

  const refresh = useCallback(() => {
    return run(defaultParams);
  }, [run, defaultParams]);

  useEffect(() => {
    if (!manual) {
      run();
    }
  }, [...refreshDeps]);

  return {
    data,
    loading,
    error,
    run,
    runMutate,
    refresh,
    setData,
  };
};

export default useRequest;
