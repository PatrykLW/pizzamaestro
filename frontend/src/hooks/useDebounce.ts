import { useState, useEffect } from 'react';

/**
 * Hook do debounce wartości.
 * Opóźnia aktualizację wartości o podany czas.
 * 
 * @param value - wartość do debounce
 * @param delay - opóźnienie w milisekundach
 * @returns debounced value
 * 
 * @example
 * const debouncedSearch = useDebounce(searchTerm, 500);
 * 
 * useEffect(() => {
 *   // This will only run 500ms after the last searchTerm change
 *   fetchResults(debouncedSearch);
 * }, [debouncedSearch]);
 */
export function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(timer);
    };
  }, [value, delay]);

  return debouncedValue;
}

export default useDebounce;
