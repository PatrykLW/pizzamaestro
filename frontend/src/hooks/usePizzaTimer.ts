import { useState, useEffect, useCallback, useRef } from 'react';
import { ScheduledStepResponse } from '../services/api';
import { logger } from '../utils/logger';

interface UsePizzaTimerOptions {
  steps: ScheduledStepResponse[];
  reminderMinutesBefore?: number;
  onStepDue?: (step: ScheduledStepResponse) => void;
  onStepReminder?: (step: ScheduledStepResponse, minutesBefore: number) => void;
  enabled?: boolean;
}

interface UsePizzaTimerResult {
  nextStep: ScheduledStepResponse | null;
  minutesToNextStep: number | null;
  secondsToNextStep: number | null;
  formattedTimeToNextStep: string;
  notificationPermission: NotificationPermission | 'unsupported';
  requestNotificationPermission: () => Promise<boolean>;
  playSound: () => void;
  isOverdue: boolean;
}

/**
 * Hook do zarządzania timerem pizzy z powiadomieniami przeglądarkowym.
 */
export function usePizzaTimer({
  steps,
  reminderMinutesBefore = 15,
  onStepDue,
  onStepReminder,
  enabled = true,
}: UsePizzaTimerOptions): UsePizzaTimerResult {
  const [now, setNow] = useState(new Date());
  const [notificationPermission, setNotificationPermission] = useState<NotificationPermission | 'unsupported'>('default');
  const notifiedStepsRef = useRef<Set<string>>(new Set());
  const reminderSentRef = useRef<Set<string>>(new Set());
  const audioRef = useRef<HTMLAudioElement | null>(null);

  // Inicjalizacja audio
  useEffect(() => {
    // Prosty dźwięk powiadomienia (można zastąpić prawdziwym plikiem)
    audioRef.current = new Audio('data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2teleiyW1sN4NQAAvt7UfT4AAHu72HE5AAB3tddoNQAAdbTXZjQAAHWz12U0AAB1s9dlNAAA');
  }, []);

  // Sprawdź wsparcie dla powiadomień
  useEffect(() => {
    if (!('Notification' in window)) {
      setNotificationPermission('unsupported');
    } else {
      setNotificationPermission(Notification.permission);
    }
  }, []);

  // Aktualizuj czas co sekundę
  useEffect(() => {
    if (!enabled) return;
    
    const timer = setInterval(() => {
      setNow(new Date());
    }, 1000);

    return () => clearInterval(timer);
  }, [enabled]);

  // Znajdź następny oczekujący krok
  const nextStep = steps.find(
    (step) => step.status === 'PENDING' || step.status === 'IN_PROGRESS'
  ) || null;

  // Oblicz czas do następnego kroku
  const calculateTimeToNextStep = useCallback(() => {
    if (!nextStep?.scheduledTime) return { minutes: null, seconds: null };
    
    const scheduledTime = new Date(nextStep.scheduledTime);
    const diffMs = scheduledTime.getTime() - now.getTime();
    const diffSeconds = Math.floor(diffMs / 1000);
    const diffMinutes = Math.floor(diffSeconds / 60);
    
    return {
      minutes: diffMinutes,
      seconds: diffSeconds,
    };
  }, [nextStep, now]);

  const { minutes: minutesToNextStep, seconds: secondsToNextStep } = calculateTimeToNextStep();

  // Formatuj czas do wyświetlenia
  const formatTime = useCallback((totalSeconds: number | null): string => {
    if (totalSeconds === null) return '--:--';
    
    const isNegative = totalSeconds < 0;
    const absSeconds = Math.abs(totalSeconds);
    
    const hours = Math.floor(absSeconds / 3600);
    const minutes = Math.floor((absSeconds % 3600) / 60);
    const seconds = absSeconds % 60;
    
    const sign = isNegative ? '-' : '';
    
    if (hours > 0) {
      return `${sign}${hours}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
    }
    return `${sign}${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
  }, []);

  const formattedTimeToNextStep = formatTime(secondsToNextStep);

  // Poproś o uprawnienia do powiadomień
  const requestNotificationPermission = useCallback(async (): Promise<boolean> => {
    if (!('Notification' in window)) {
      return false;
    }
    
    try {
      const permission = await Notification.requestPermission();
      setNotificationPermission(permission);
      return permission === 'granted';
    } catch {
      return false;
    }
  }, []);

  // Wyślij powiadomienie przeglądarkowe
  const sendBrowserNotification = useCallback((title: string, body: string, icon?: string) => {
    if (notificationPermission !== 'granted') return;
    
    try {
      const notification = new Notification(title, {
        body,
        icon: icon || '/logo192.png',
        badge: '/logo192.png',
        tag: 'pizza-timer',
        requireInteraction: true,
      });
      
      // Automatycznie zamknij po 30 sekundach
      setTimeout(() => notification.close(), 30000);
      
      notification.onclick = () => {
        window.focus();
        notification.close();
      };
    } catch (error) {
      logger.error('Błąd powiadomienia:', error);
    }
  }, [notificationPermission]);

  // Odtwórz dźwięk
  const playSound = useCallback(() => {
    if (audioRef.current) {
      audioRef.current.currentTime = 0;
      audioRef.current.play().catch(() => {
        // Ignoruj błędy odtwarzania (np. brak interakcji użytkownika)
      });
    }
  }, []);

  // Sprawdź powiadomienia
  useEffect(() => {
    if (!enabled || !nextStep || minutesToNextStep === null) return;
    
    const stepKey = `${nextStep.stepNumber}-${nextStep.scheduledTime}`;
    
    // Przypomnienie X minut przed
    if (
      minutesToNextStep > 0 &&
      minutesToNextStep <= reminderMinutesBefore &&
      !reminderSentRef.current.has(stepKey)
    ) {
      reminderSentRef.current.add(stepKey);
      
      sendBrowserNotification(
        `🍕 Za ${minutesToNextStep} min: ${nextStep.title}`,
        nextStep.description || 'Przygotuj się do następnego kroku!'
      );
      
      playSound();
      onStepReminder?.(nextStep, minutesToNextStep);
    }
    
    // Powiadomienie gdy czas (0-2 minuty)
    if (
      minutesToNextStep <= 0 &&
      minutesToNextStep >= -2 &&
      !notifiedStepsRef.current.has(stepKey)
    ) {
      notifiedStepsRef.current.add(stepKey);
      
      sendBrowserNotification(
        `🍕 TERAZ: ${nextStep.title}`,
        nextStep.description || 'Czas na ten krok!',
      );
      
      playSound();
      onStepDue?.(nextStep);
    }
  }, [
    enabled,
    nextStep,
    minutesToNextStep,
    reminderMinutesBefore,
    sendBrowserNotification,
    playSound,
    onStepDue,
    onStepReminder,
  ]);

  // Czy krok jest opóźniony
  const isOverdue = minutesToNextStep !== null && minutesToNextStep < -5;

  return {
    nextStep,
    minutesToNextStep,
    secondsToNextStep,
    formattedTimeToNextStep,
    notificationPermission,
    requestNotificationPermission,
    playSound,
    isOverdue,
  };
}

/**
 * Komponent timera do wyświetlania odliczania.
 */
export function formatTimeDistance(minutes: number | null): string {
  if (minutes === null) return 'brak danych';
  
  if (minutes < 0) {
    const absMin = Math.abs(minutes);
    if (absMin < 60) return `${absMin} min temu`;
    const hours = Math.floor(absMin / 60);
    return `${hours}h ${absMin % 60}min temu`;
  }
  
  if (minutes === 0) return 'teraz!';
  
  if (minutes < 60) return `za ${minutes} min`;
  
  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;
  
  if (remainingMinutes === 0) return `za ${hours}h`;
  return `za ${hours}h ${remainingMinutes}min`;
}
