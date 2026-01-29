/**
 * Centralne typy TypeScript dla aplikacji Pizza Maestro.
 * Eliminuje użycie 'any' i zapewnia type safety.
 */

// ========== Style Pizzy ==========

export interface PizzaStyleDefaults {
  hydration: number;
  hydrationMin: number;
  hydrationMax: number;
  ballWeight: number;
  fermentationHours: number;
  saltPercentage: number;
  oilPercentage: number;
  sugarPercentage: number;
}

export interface PizzaStyleOven {
  id?: string;
  type?: string;
  name: string;
  temperature: number;
  bakingTime: number;
}

export interface PizzaStyle {
  id: string;
  name: string;
  description: string;
  icon?: string;
  premium?: boolean;
  recommendedMethods?: string[];
  tips?: string[];
  // Rozszerzone pola z API
  defaults?: PizzaStyleDefaults;
  recommendedOven?: PizzaStyleOven;
}

// ========== Typy Pieców ==========

export interface OvenType {
  id: string;
  name: string;
  description: string;
  temperatureMin: number;
  temperatureMax: number;
  temperatureDefault: number;
  bakingTimeSeconds: number;
  pros: string[];
  cons: string[];
  tips: string;
  recommended: boolean;
}

// ========== Typy Drożdży ==========

export interface YeastType {
  id: string;
  name: string;
  description: string;
  conversionFactor: number;
  temperatureMin: number;
  temperatureMax: number;
  notes: string;
}

// ========== Metody Fermentacji ==========

export interface FermentationMethod {
  id: string;
  name: string;
  description: string;
  minHours: number;
  maxHours: number;
  defaultHours: number;
  temperatureType: 'ROOM' | 'FRIDGE' | 'MIXED';
  premium: boolean;
  pros: string[];
  cons: string[];
}

// ========== Typy Prefermentów ==========

export interface PrefermentType {
  id: string;
  name: string;
  description: string;
  hydration: number;
  fermentationHours: number;
  flavorProfile: string;
  premium: boolean;
}

// ========== Składniki ==========

export interface Ingredient {
  id: string;
  name: string;
  type: 'FLOUR' | 'WATER' | 'YEAST' | 'SALT' | 'OIL' | 'SUGAR';
  brand?: string;
  description?: string;
  proteinContent?: number;
  strengthW?: number;
  flourType?: string;
  hardnessLevel?: 'SOFT' | 'MEDIUM' | 'HARD' | 'VERY_HARD';
  mineralContent?: number;
  ph?: number;
  recommendedForStyles: string[];
  notes?: string;
  premium: boolean;
  active: boolean;
}

// ========== Mąka (szczegółowy typ) ==========

export interface Flour extends Ingredient {
  type: 'FLOUR';
  proteinContent: number;
  strengthW?: number;
  flourType: string;
  absorptionRate?: number;
}

// ========== Receptury ==========

export interface Recipe {
  id: string;
  userId: string;
  name: string;
  description?: string;
  pizzaStyle: string;
  pizzaStyleName?: string;
  numberOfPizzas: number;
  ballWeight: number;
  hydration: number;
  calculation?: RecipeCalculation;
  calculatedRecipe?: RecipeCalculation; // alternatywna nazwa z API
  fermentationMethod: string;
  fermentationHours: number;
  totalFermentationHours?: number; // alternatywna nazwa
  fermentationSteps?: FermentationStep[];
  ovenType?: string;
  yeastType?: string;
  roomTemperature?: number;
  fridgeTemperature?: number;
  notes?: string;
  tags?: string[];
  rating?: number;
  isFavorite?: boolean;
  favorite?: boolean; // alternatywna nazwa z API
  isPublic?: boolean;
  createdAt: string;
  updatedAt?: string;
  lastBakedAt?: string;
  version?: number;
}

export interface RecipeCalculation {
  flourAmount?: number;
  flourGrams?: number; // alternatywna nazwa z API
  waterAmount?: number;
  waterGrams?: number;
  saltAmount?: number;
  saltGrams?: number;
  yeastAmount?: number;
  yeastGrams?: number;
  oilAmount?: number;
  oilGrams?: number;
  sugarAmount?: number;
  sugarGrams?: number;
  totalWeight?: number;
  totalDoughWeight?: number;
}

export interface FermentationStep {
  stepNumber: number;
  stepType: string;
  title: string;
  description: string;
  scheduledTime: string;
  relativeTime: string;
  durationMinutes: number;
  temperature?: number;
  icon: string;
  completed: boolean;
  completedAt?: string;
}

// ========== Użytkownik ==========

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  phoneVerified: boolean;
  roles: string[];
  accountType: 'FREE' | 'PREMIUM' | 'PRO' | 'ADMIN';
  isPremium: boolean;
  premiumExpiresAt?: string;
  preferences: UserPreferences;
  stats: UserStats;
  createdAt: string;
  lastLoginAt?: string;
}

export interface UserPreferences {
  // Ustawienia ogólne
  language: string;
  theme: string;
  temperatureUnit: string;
  weightUnit: string;
  // Powiadomienia
  emailNotifications: boolean;
  smsNotifications: boolean;
  pushNotifications: boolean;
  smsReminderMinutesBefore?: number;
  // Domyślny styl pizzy
  defaultPizzaStyle: string;
  // Domyślny sprzęt
  defaultOvenType?: string;
  defaultMixerType?: string;
  mixerWattage?: number;
  // Dostępne składniki
  availableFlourIds?: string[];
  defaultWaterId?: string;
  // Warunki środowiskowe
  typicalRoomTemperature?: number;
  typicalFridgeTemperature?: number;
  defaultCity?: string;
  defaultLatitude?: number;
  defaultLongitude?: number;
}

export interface UserStats {
  totalCalculations: number;
  calculationsThisMonth: number;
  totalPizzasBaked: number;
  smsUsedThisMonth: number;
  lastCalculationAt?: string;
}

// ========== Błędy API ==========

export interface ApiError {
  message: string;
  error?: string;
  status?: number;
  timestamp?: string;
  path?: string;
  details?: Record<string, string>;
}

// ========== Paginacja ==========

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// ========== Location State dla React Router ==========

export interface LocationState {
  from?: {
    pathname: string;
  };
  message?: string;
}

// ========== Formularz Kalkulatora ==========

export interface CalculatorFormData {
  pizzaStyle: string;
  numberOfPizzas: number;
  ballWeight: number;
  hydration: number;
  saltPercentage: number;
  oilPercentage: number;
  sugarPercentage: number;
  yeastType: string;
  yeastPercentage?: number;
  fermentationMethod: string;
  totalFermentationHours: number;
  roomTemperature: number;
  fridgeTemperature: number;
  ovenType?: string;
  ovenTemperature?: number;
  usePreferment: boolean;
  prefermentType?: string;
  prefermentPercentage?: number;
  prefermentFermentationHours?: number;
  plannedBakeTime?: string;
  generateSchedule: boolean;
  saveRecipe: boolean;
  recipeName?: string;
  recipeDescription?: string;
  selectedFlour?: string;
}

// ========== Typ dla kolorów MUI ==========

export type ChipColor = 'default' | 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning';

// ========== Typy pomocnicze ==========

export type ValueOf<T> = T[keyof T];

// Funkcja pomocnicza do type guard
export function isApiError(error: unknown): error is ApiError {
  return (
    typeof error === 'object' &&
    error !== null &&
    'message' in error
  );
}
