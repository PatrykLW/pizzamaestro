/**
 * Logger utility - kontrolowane logowanie w zależności od środowiska.
 * W produkcji logi error są wysyłane do zewnętrznego serwisu (można podpiąć Sentry).
 */

const isDevelopment = process.env.NODE_ENV === 'development';

interface LoggerInterface {
  log: (...args: unknown[]) => void;
  info: (...args: unknown[]) => void;
  warn: (...args: unknown[]) => void;
  error: (...args: unknown[]) => void;
  debug: (...args: unknown[]) => void;
}

export const logger: LoggerInterface = {
  log: (...args: unknown[]) => {
    if (isDevelopment) {
      console.log('[LOG]', ...args);
    }
  },
  
  info: (...args: unknown[]) => {
    if (isDevelopment) {
      console.info('[INFO]', ...args);
    }
  },
  
  warn: (...args: unknown[]) => {
    if (isDevelopment) {
      console.warn('[WARN]', ...args);
    }
    // W produkcji można wysłać do serwisu monitoringu
  },
  
  error: (...args: unknown[]) => {
    if (isDevelopment) {
      console.error('[ERROR]', ...args);
    }
    // W produkcji: wysyłaj do Sentry lub innego serwisu
    // Sentry.captureException(args[0]);
  },
  
  debug: (...args: unknown[]) => {
    if (isDevelopment) {
      console.debug('[DEBUG]', ...args);
    }
  },
};

export default logger;
