import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import toast from 'react-hot-toast';
import type { 
  PizzaStyle, 
  OvenType, 
  YeastType, 
  FermentationMethod, 
  PrefermentType,
  Ingredient,
  Recipe,
  PageResponse,
  ApiError
} from '../types';

// Bazowa konfiguracja Axios
export const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || '',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor żądań
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Token jest już dodawany w authStore
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor odpowiedzi
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    
    // Obsługa 401 - próba odświeżenia tokenu
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      // Dynamiczny import by uniknąć cyklicznych zależności
      const { useAuthStore } = await import('../store/authStore');
      const refreshed = await useAuthStore.getState().refreshAuth();
      
      if (refreshed) {
        const token = useAuthStore.getState().accessToken;
        originalRequest.headers['Authorization'] = `Bearer ${token}`;
        return api(originalRequest);
      }
    }
    
    // Obsługa innych błędów
    const errorMessage = getErrorMessage(error);
    
    if (error.response?.status !== 401) {
      toast.error(errorMessage);
    }
    
    return Promise.reject(error);
  }
);

// Wyciągnij czytelny komunikat błędu
function getErrorMessage(error: AxiosError<unknown>): string {
  const data = error.response?.data as ApiError | undefined;
  if (data?.message) {
    return data.message;
  }
  if (data?.error) {
    return data.error;
  }
  if (error.message) {
    return error.message;
  }
  return 'Wystąpił nieoczekiwany błąd';
}

// ========== API Kalkulatora ==========

export interface FlourMixEntry {
  flourId: string;
  percentage: number;
}

export interface CalculationRequest {
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
  // Miks mąk
  flourId?: string;
  flourMix?: FlourMixEntry[];
  // Techniki ciasta
  useAutolyse?: boolean;
  autolyseMinutes?: number;
  stretchAndFoldSeries?: number;
  stretchAndFoldInterval?: number;
  usePunchDown?: boolean;
  punchDownAfterHours?: number;
  // Warunki środowiskowe
  ambientHumidity?: number;
  altitudeMeters?: number;
}

export interface FlourMixSuggestion {
  success: boolean;
  isMix: boolean;
  flourMix?: FlourMixEntry[];
  flourDetails?: FlourMixDetail[];
  resultProtein: number;
  resultStrength?: number;
  message: string;
  explanation?: string;
}

export interface FlourMixDetail {
  flourId: string;
  flourName: string;
  brand?: string;
  percentage: number;
  proteinContent: number;
  strength?: number;
}

export interface FlourMixParameters {
  portions: FlourMixDetail[];
  averageProtein: number;
  averageStrength?: number;
  averageExtensibility?: number;
  recommendedHydrationMin: number;
  recommendedHydrationMax: number;
}

export interface CalculationResponse {
  recipeId?: string;
  pizzaStyle: string;
  pizzaStyleName: string;
  numberOfPizzas: number;
  ballWeight: number;
  ingredients: {
    totalDoughWeight: number;
    flourGrams: number;
    waterGrams: number;
    saltGrams: number;
    yeastGrams: number;
    yeastType: string;
    oilGrams: number;
    sugarGrams: number;
  };
  bakerPercentages: {
    flour: number;
    water: number;
    salt: number;
    yeast: number;
    oil: number;
    sugar: number;
  };
  preferment?: {
    type: string;
    typeName: string;
    flourGrams: number;
    waterGrams: number;
    yeastGrams: number;
    fermentationHours: number;
    instructions: string;
  };
  schedule?: ScheduleStep[];
  tips: string[];
  ovenInfo: {
    ovenType: string;
    ovenName: string;
    temperature: number;
    bakingTimeSeconds: number;
    tips: string;
  };
}

export interface ScheduleStep {
  stepNumber: number;
  stepType: string;
  title: string;
  description: string;
  scheduledTime: string;
  relativeTime: string;
  durationMinutes: number;
  temperature?: number;
  icon: string;
}

export const calculatorApi = {
  // Publiczna kalkulacja
  calculatePublic: async (data: CalculationRequest): Promise<CalculationResponse> => {
    const response = await api.post('/api/calculator/public/calculate', data);
    return response.data;
  },
  
  // Kalkulacja dla zalogowanych
  calculate: async (data: CalculationRequest): Promise<CalculationResponse> => {
    const response = await api.post('/api/calculator/calculate', data);
    return response.data;
  },
  
  // Pobierz style pizzy
  getStyles: async (): Promise<PizzaStyle[]> => {
    const response = await api.get('/api/calculator/styles');
    return response.data;
  },
  
  // Pobierz typy pieców
  getOvens: async (): Promise<OvenType[]> => {
    const response = await api.get('/api/calculator/ovens');
    return response.data;
  },
  
  // Pobierz typy drożdży
  getYeastTypes: async (): Promise<YeastType[]> => {
    const response = await api.get('/api/calculator/yeast-types');
    return response.data;
  },
  
  // Pobierz metody fermentacji
  getFermentationMethods: async (): Promise<FermentationMethod[]> => {
    const response = await api.get('/api/calculator/fermentation-methods');
    return response.data;
  },
  
  // Pobierz typy prefermentów
  getPrefermentTypes: async (): Promise<PrefermentType[]> => {
    const response = await api.get('/api/calculator/preferment-types');
    return response.data;
  },
  
  // === MIKS MĄK ===
  
  // Sugestia miksu dla stylu (publiczna)
  suggestFlourMix: async (style: string, availableFlourIds?: string[]): Promise<FlourMixSuggestion> => {
    const params = new URLSearchParams({ style });
    if (availableFlourIds && availableFlourIds.length > 0) {
      availableFlourIds.forEach(id => params.append('availableFlourIds', id));
    }
    const response = await api.get(`/api/calculator/public/flour-mix/suggest?${params}`);
    return response.data;
  },
  
  // Sugestia miksu dla stylu (z profilem użytkownika)
  suggestFlourMixWithProfile: async (style: string): Promise<FlourMixSuggestion> => {
    const response = await api.get(`/api/calculator/flour-mix/suggest?style=${style}`);
    return response.data;
  },
  
  // Sugestia miksu dla docelowych parametrów
  suggestFlourMixByParams: async (
    targetProtein?: number, 
    targetStrength?: number, 
    availableFlourIds?: string[]
  ): Promise<FlourMixSuggestion> => {
    const params = new URLSearchParams();
    if (targetProtein) params.append('targetProtein', targetProtein.toString());
    if (targetStrength) params.append('targetStrength', targetStrength.toString());
    if (availableFlourIds) {
      availableFlourIds.forEach(id => params.append('availableFlourIds', id));
    }
    const response = await api.get(`/api/calculator/public/flour-mix/suggest-by-params?${params}`);
    return response.data;
  },
  
  // Optymalizacja proporcji dla wybranych mąk
  optimizeFlourMix: async (flourIds: string[], style?: string): Promise<FlourMixSuggestion> => {
    const params = style ? `?style=${style}` : '';
    const response = await api.post(`/api/calculator/public/flour-mix/optimize${params}`, flourIds);
    return response.data;
  },
  
  // Oblicz parametry miksu
  calculateFlourMixParams: async (flourMix: FlourMixEntry[]): Promise<FlourMixParameters> => {
    const response = await api.post('/api/calculator/public/flour-mix/calculate-params', flourMix);
    return response.data;
  },
  
  // === KOREKTY ŚRODOWISKOWE ===
  
  getEnvironmentalCorrections: async (
    humidity?: number,
    altitude?: number,
    roomTemperature?: number
  ): Promise<EnvironmentalCorrections> => {
    const params = new URLSearchParams();
    if (humidity !== undefined) params.append('humidity', humidity.toString());
    if (altitude !== undefined) params.append('altitude', altitude.toString());
    if (roomTemperature !== undefined) params.append('roomTemperature', roomTemperature.toString());
    const response = await api.get(`/api/calculator/public/environmental-corrections?${params}`);
    return response.data;
  },
};

export interface EnvironmentalCorrections {
  hydrationCorrectionPercent: number;
  yeastCorrectionPercent: number;
  fermentationTimeCorrectionPercent: number;
  estimatedPressureHPa: number;
  recommendations: string[];
}

// ========== API Składników ==========

export const ingredientsApi = {
  getFlours: async (): Promise<Ingredient[]> => {
    const response = await api.get('/api/ingredients/public/flours');
    return response.data;
  },
  
  getWaters: async (): Promise<Ingredient[]> => {
    const response = await api.get('/api/ingredients/public/waters');
    return response.data;
  },
  
  getYeasts: async (): Promise<Ingredient[]> => {
    const response = await api.get('/api/ingredients/public/yeasts');
    return response.data;
  },
  
  getSalts: async (): Promise<Ingredient[]> => {
    const response = await api.get('/api/ingredients/public/salts');
    return response.data;
  },
  
  searchIngredients: async (query: string): Promise<Ingredient[]> => {
    const response = await api.get(`/api/ingredients/public/search?query=${query}`);
    return response.data;
  },
};

// ========== API Pogody ==========

export interface WeatherData {
  latitude: number;
  longitude: number;
  cityName?: string;
  country?: string;
  temperature: number;
  indoorTemperature?: number;  // Szacowana temperatura wewnętrzna
  indoorHumidity?: number;     // Szacowana wilgotność wewnętrzna
  humidity: number;
  pressure: number;
  weatherCode: number;
  description: string;
  fermentationFactor: number;
  fetchedAt: string;
  isDefault: boolean;
}

export interface FermentationAdjustment {
  temperatureFactor: number;
  humidityFactor: number;
  pressureFactor: number;
  totalFactor: number;
  yeastAdjustmentPercent: number;
  fermentationTimeAdjustmentPercent: number;
  indoorTemperature?: number;  // Szacowana temp. wewnętrzna użyta w obliczeniach
  indoorHumidity?: number;     // Szacowana wilgotność wewnętrzna
  recommendations: string[];
}

export interface FullWeatherAnalysis {
  weather: WeatherData;
  fermentationAdjustment: FermentationAdjustment;
}

export const weatherApi = {
  // Pobierz pogodę po współrzędnych
  getByCoordinates: async (lat: number, lon: number): Promise<WeatherData> => {
    const response = await api.get(`/api/weather/coordinates?latitude=${lat}&longitude=${lon}`);
    return response.data;
  },
  
  // Pobierz pogodę po nazwie miasta
  getByCity: async (cityName: string): Promise<WeatherData> => {
    const response = await api.get(`/api/weather/city?name=${encodeURIComponent(cityName)}`);
    return response.data;
  },
  
  // Pobierz wpływ pogody na fermentację
  getFermentationAdjustment: async (lat: number, lon: number): Promise<FermentationAdjustment> => {
    const response = await api.get(`/api/weather/fermentation-adjustment?latitude=${lat}&longitude=${lon}`);
    return response.data;
  },
  
  // Pobierz pełną analizę pogodową
  getFullAnalysis: async (lat: number, lon: number): Promise<FullWeatherAnalysis> => {
    const response = await api.get(`/api/weather/full-analysis?latitude=${lat}&longitude=${lon}`);
    return response.data;
  },
};

// ========== API Uprawnień ==========

export interface FeatureAccess {
  accountType: string;
  accountTypeName: string;
  availablePizzaStyles: PizzaStyle[];
  lockedPizzaStyles: PizzaStyle[];
  availableFermentationMethods: string[];
  lockedFermentationMethods: string[];
  prefermentAvailable: boolean;
  calculationsUsed: number;
  calculationsRemaining: number;
  recipesUsed: number;
  recipesRemaining: number;
  canUpgrade: boolean;
  upgradeMessage: string;
  features: {
    name: string;
    description: string;
    available: boolean;
    limit?: string;
  }[];
}

export const featureApi = {
  getMyAccess: async (): Promise<FeatureAccess> => {
    const response = await api.get('/api/features/my-access');
    return response.data;
  },
  
  checkCalculation: async (): Promise<{ allowed: boolean; reason?: string; upgradeHint?: string }> => {
    const response = await api.get('/api/features/check/calculation');
    return response.data;
  },
};

// ========== API Wskazówek (TipEngine) ==========

export interface Tip {
  type: 'INFO' | 'WARNING' | 'RECOMMENDATION' | 'SCIENCE' | 'CHANGE_EXPLANATION';
  category: string;
  title: string;
  content: string;
  details?: string;
  suggestion?: string;
  icon: string;
  priority: number;
}

export interface TipCollection {
  tips: Tip[];
  warnings: Tip[];
  recommendations: Tip[];
  contextSummary: string;
}

export interface TipContext {
  pizzaStyle?: string;
  hydration?: number;
  fermentationHours?: number;
  fermentationMethod?: string;
  roomTemperature?: number;
  fridgeTemperature?: number;
  flourStrength?: number;
  flourProtein?: number;
  yeastType?: string;
  usePreferment?: boolean;
  weatherTemperature?: number;
  weatherHumidity?: number;
}

export const tipApi = {
  // Pobierz wszystkie tipy dla konfiguracji
  getAllTips: async (context: TipContext): Promise<TipCollection> => {
    const response = await api.post('/api/tips/all', context);
    return response.data;
  },
  
  // Pobierz tipy dla zmiany parametru
  getTipsForChange: async (
    parameterName: string,
    oldValue: string | number | boolean | null,
    newValue: string | number | boolean | null,
    context: TipContext
  ): Promise<Tip[]> => {
    const response = await api.post('/api/tips/change', {
      parameterName,
      oldValue,
      newValue,
      context,
    });
    return response.data;
  },
  
  // Szybkie tipy
  getHydrationTips: async (value: number, style?: string): Promise<Tip[]> => {
    const params = style ? `?style=${style}` : '';
    const response = await api.get(`/api/tips/hydration/${value}${params}`);
    return response.data;
  },
  
  getFermentationTips: async (hours: number, method?: string): Promise<Tip[]> => {
    const params = method ? `?method=${method}` : '';
    const response = await api.get(`/api/tips/fermentation/${hours}${params}`);
    return response.data;
  },
  
  getStyleTips: async (style: string): Promise<Tip[]> => {
    const response = await api.get(`/api/tips/style/${style}`);
    return response.data;
  },
  
  getFlourTips: async (strength?: number, protein?: number): Promise<Tip[]> => {
    const params = new URLSearchParams();
    if (strength) params.append('strength', String(strength));
    if (protein) params.append('protein', String(protein));
    const response = await api.get(`/api/tips/flour?${params}`);
    return response.data;
  },
};

// ========== API Bazy Wiedzy ==========

export interface TechniqueGuide {
  id: string;
  category: string;
  slug: string;
  title: string;
  titleEn: string;
  shortDescription: string;
  fullDescription: string;
  difficulty: string;
  estimatedTimeMinutes: number;
  requiredEquipment: string[];
  steps: {
    stepNumber: number;
    title: string;
    description: string;
    detailedExplanation?: string;
    durationSeconds?: number;
    tips?: string[];
    critical?: boolean;
  }[];
  proTips: {
    title: string;
    content: string;
    category: string;
    premiumOnly?: boolean;
  }[];
  commonMistakes?: {
    mistake: string;
    consequence: string;
    solution: string;
    prevention?: string;
  }[];
  relatedTechniques: string[];
  recommendedForStyles: string[];
  premium: boolean;
  viewCount: number;
}

export interface CategoryInfo {
  id: string;
  name: string;
  description: string;
  guideCount: number;
}

export const knowledgeApi = {
  // Kategorie
  getCategories: async (): Promise<CategoryInfo[]> => {
    const response = await api.get('/api/knowledge/categories');
    return response.data;
  },
  
  // Przewodniki
  getAllGuides: async (premiumOnly = false): Promise<TechniqueGuide[]> => {
    const response = await api.get(`/api/knowledge/guides?premiumOnly=${premiumOnly}`);
    return response.data;
  },
  
  getByCategory: async (category: string): Promise<TechniqueGuide[]> => {
    const response = await api.get(`/api/knowledge/guides/category/${category}`);
    return response.data;
  },
  
  getGuide: async (slug: string): Promise<TechniqueGuide> => {
    const response = await api.get(`/api/knowledge/guides/${slug}`);
    return response.data;
  },
  
  getForStyle: async (style: string): Promise<TechniqueGuide[]> => {
    const response = await api.get(`/api/knowledge/guides/style/${style}`);
    return response.data;
  },
  
  getPopular: async (): Promise<TechniqueGuide[]> => {
    const response = await api.get('/api/knowledge/guides/popular');
    return response.data;
  },
  
  search: async (query: string): Promise<TechniqueGuide[]> => {
    const response = await api.get(`/api/knowledge/guides/search?q=${encodeURIComponent(query)}`);
    return response.data;
  },
  
  getRelated: async (slug: string): Promise<TechniqueGuide[]> => {
    const response = await api.get(`/api/knowledge/guides/${slug}/related`);
    return response.data;
  },
  
  // Szybkie przewodniki
  getFlourStrengthGuide: async () => {
    const response = await api.get('/api/knowledge/flour-strength');
    return response.data;
  },
  
  getYeastConversionGuide: async () => {
    const response = await api.get('/api/knowledge/yeast-conversion');
    return response.data;
  },
  
  getHydrationGuide: async () => {
    const response = await api.get('/api/knowledge/hydration-guide');
    return response.data;
  },
};

// ========== API Receptur ==========

export interface RecipeUpdateData {
  name?: string;
  description?: string;
  notes?: string;
  tags?: string[];
  isPublic?: boolean;
}

export const recipesApi = {
  getRecipes: async (page = 0, size = 20): Promise<PageResponse<Recipe>> => {
    const response = await api.get(`/api/recipes?page=${page}&size=${size}`);
    return response.data;
  },
  
  getFavorites: async (): Promise<Recipe[]> => {
    const response = await api.get('/api/recipes/favorites');
    return response.data;
  },
  
  getRecipe: async (id: string): Promise<Recipe> => {
    const response = await api.get(`/api/recipes/${id}`);
    return response.data;
  },
  
  updateRecipe: async (id: string, data: RecipeUpdateData): Promise<Recipe> => {
    const response = await api.put(`/api/recipes/${id}`, data);
    return response.data;
  },
  
  toggleFavorite: async (id: string): Promise<Recipe> => {
    const response = await api.post(`/api/recipes/${id}/favorite`);
    return response.data;
  },
  
  cloneRecipe: async (id: string): Promise<Recipe> => {
    const response = await api.post(`/api/recipes/${id}/clone`);
    return response.data;
  },
  
  completeStep: async (recipeId: string, stepNumber: number): Promise<Recipe> => {
    const response = await api.post(`/api/recipes/${recipeId}/steps/${stepNumber}/complete`);
    return response.data;
  },
  
  deleteRecipe: async (id: string): Promise<void> => {
    await api.delete(`/api/recipes/${id}`);
  },
};

// ========== API Użytkownika ==========

export interface UpdateEquipmentRequest {
  defaultOvenType?: string;
  defaultMixerType?: string;
  mixerWattage?: number;
  availableFlourIds?: string[];
  defaultWaterId?: string;
}

export interface UpdateEnvironmentRequest {
  typicalRoomTemperature?: number;
  typicalFridgeTemperature?: number;
  defaultCity?: string;
  defaultLatitude?: number;
  defaultLongitude?: number;
}

export interface UpdateNotificationsRequest {
  emailNotifications?: boolean;
  smsNotifications?: boolean;
  pushNotifications?: boolean;
  smsReminderMinutesBefore?: number;
}

export interface UpdatePreferencesRequest {
  language?: string;
  theme?: string;
  temperatureUnit?: string;
  weightUnit?: string;
  defaultPizzaStyle?: string;
  emailNotifications?: boolean;
  smsNotifications?: boolean;
  pushNotifications?: boolean;
  smsReminderMinutesBefore?: number;
  defaultOvenType?: string;
  defaultMixerType?: string;
  mixerWattage?: number;
  availableFlourIds?: string[];
  defaultWaterId?: string;
  typicalRoomTemperature?: number;
  typicalFridgeTemperature?: number;
  defaultCity?: string;
  defaultLatitude?: number;
  defaultLongitude?: number;
}

export interface UserPreferencesInfo {
  language: string;
  theme: string;
  temperatureUnit: string;
  weightUnit: string;
  emailNotifications: boolean;
  smsNotifications: boolean;
  pushNotifications: boolean;
  smsReminderMinutesBefore?: number;
  defaultPizzaStyle: string;
  defaultOvenType?: string;
  defaultMixerType?: string;
  mixerWattage?: number;
  availableFlourIds?: string[];
  defaultWaterId?: string;
  typicalRoomTemperature?: number;
  typicalFridgeTemperature?: number;
  defaultCity?: string;
  defaultLatitude?: number;
  defaultLongitude?: number;
}

export interface EquipmentDetails {
  defaultOvenType: string | null;
  defaultMixerType: string | null;
  mixerWattage: number | null;
  availableFlourIds: string[];
  defaultWaterId: string | null;
  ovenDetails?: {
    displayName: string;
    description: string;
    minTemperature: number;
    maxTemperature: number;
    hasSeparateTopBottom: boolean;
    recommendedTemperature: number;
    recommendedTopTemperature?: number;
    recommendedBottomTemperature?: number;
  };
  mixerDetails?: {
    displayName: string;
    description: string;
    frictionFactor: number;
    typicalMixingTime: number;
    maxRecommendedHydration: number;
  };
}

export interface UserProfile {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  phoneVerified: boolean;
  roles: string[];
  accountType: string;
  isPremium: boolean;
  premiumExpiresAt?: string;
  preferences: UserPreferencesInfo;
  stats: {
    totalCalculations: number;
    calculationsThisMonth: number;
    totalPizzasBaked: number;
    smsUsedThisMonth: number;
    lastCalculationAt?: string;
  };
  createdAt: string;
  lastLoginAt?: string;
}

// ========== API Aktywnej Pizzy ==========

export interface ScheduledStepResponse {
  stepNumber: number;
  type: string;
  typeName: string;
  title: string;
  description?: string;
  scheduledTime: string;
  actualTime?: string;
  durationMinutes?: number;
  temperature?: number;
  status: string;
  statusName: string;
  notificationSent: boolean;
  note?: string;
  icon?: string;
}

export interface ActivePizzaResponse {
  id: string;
  userId: string;
  recipeId?: string;
  name: string;
  pizzaStyle?: string;
  pizzaStyleName?: string;
  numberOfPizzas?: number;
  targetBakeTime: string;
  adjustedBakeTime: string;
  steps: ScheduledStepResponse[];
  status: string;
  statusName: string;
  notes?: string;
  smsNotificationsEnabled: boolean;
  notificationPhone?: string;
  reminderMinutesBefore?: number;
  completionPercentage: number;
  minutesToNextStep?: number;
  nextStep?: ScheduledStepResponse;
  createdAt: string;
  lastUpdatedAt?: string;
}

export interface CreateActivePizzaRequest {
  name: string;
  pizzaStyle: string;
  numberOfPizzas: number;
  targetBakeTime: string;
  fermentationMethod?: string;
  fermentationHours: number;
}

export const activePizzaApi = {
  // Pobierz aktualną aktywną pizzę
  getCurrent: async (): Promise<ActivePizzaResponse | null> => {
    try {
      const response = await api.get('/api/active-pizza/current');
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 204) {
        return null;
      }
      throw error;
    }
  },
  
  // Pobierz aktywną pizzę po ID
  getById: async (id: string): Promise<ActivePizzaResponse> => {
    const response = await api.get(`/api/active-pizza/${id}`);
    return response.data;
  },
  
  // Pobierz historię
  getHistory: async (): Promise<ActivePizzaResponse[]> => {
    const response = await api.get('/api/active-pizza/history');
    return response.data;
  },
  
  // Utwórz z przepisu
  createFromRecipe: async (recipeId: string, targetBakeTime: string): Promise<ActivePizzaResponse> => {
    const response = await api.post(`/api/active-pizza/from-recipe/${recipeId}`, { targetBakeTime });
    return response.data;
  },
  
  // Utwórz nową
  createNew: async (data: CreateActivePizzaRequest): Promise<ActivePizzaResponse> => {
    const response = await api.post('/api/active-pizza/new', data);
    return response.data;
  },
  
  // Rozpocznij
  start: async (id: string): Promise<ActivePizzaResponse> => {
    const response = await api.post(`/api/active-pizza/${id}/start`);
    return response.data;
  },
  
  // Wstrzymaj
  pause: async (id: string): Promise<ActivePizzaResponse> => {
    const response = await api.post(`/api/active-pizza/${id}/pause`);
    return response.data;
  },
  
  // Wznów
  resume: async (id: string): Promise<ActivePizzaResponse> => {
    const response = await api.post(`/api/active-pizza/${id}/resume`);
    return response.data;
  },
  
  // Anuluj
  cancel: async (id: string): Promise<ActivePizzaResponse> => {
    const response = await api.post(`/api/active-pizza/${id}/cancel`);
    return response.data;
  },
  
  // Oznacz krok jako ukończony
  completeStep: async (id: string, stepNumber: number, status?: string): Promise<ActivePizzaResponse> => {
    const response = await api.post(`/api/active-pizza/${id}/steps/${stepNumber}/complete`, { status });
    return response.data;
  },
  
  // Pomiń krok
  skipStep: async (id: string, stepNumber: number): Promise<ActivePizzaResponse> => {
    const response = await api.post(`/api/active-pizza/${id}/steps/${stepNumber}/skip`);
    return response.data;
  },
  
  // Przesuń harmonogram
  reschedule: async (id: string, newTargetBakeTime: string): Promise<ActivePizzaResponse> => {
    const response = await api.post(`/api/active-pizza/${id}/reschedule`, { newTargetBakeTime });
    return response.data;
  },
  
  // Przesuń harmonogram o minuty
  rescheduleByMinutes: async (id: string, minutes: number): Promise<ActivePizzaResponse> => {
    const response = await api.post(`/api/active-pizza/${id}/reschedule-by-minutes?minutes=${minutes}`);
    return response.data;
  },
  
  // Włącz powiadomienia SMS
  enableNotifications: async (id: string, phoneNumber: string, reminderMinutesBefore?: number): Promise<ActivePizzaResponse> => {
    const response = await api.post(`/api/active-pizza/${id}/notifications/enable`, { phoneNumber, reminderMinutesBefore });
    return response.data;
  },
  
  // Wyłącz powiadomienia SMS
  disableNotifications: async (id: string): Promise<ActivePizzaResponse> => {
    const response = await api.post(`/api/active-pizza/${id}/notifications/disable`);
    return response.data;
  },
};

export const userApi = {
  // Pobierz profil
  getProfile: async (): Promise<UserProfile> => {
    const response = await api.get('/api/user/profile');
    return response.data;
  },
  
  // Ustawienia sprzętu
  getEquipment: async (): Promise<EquipmentDetails> => {
    const response = await api.get('/api/user/equipment');
    return response.data;
  },
  
  updateEquipment: async (data: UpdateEquipmentRequest): Promise<UserPreferencesInfo> => {
    const response = await api.put('/api/user/equipment', data);
    return response.data;
  },
  
  // Warunki środowiskowe
  updateEnvironment: async (data: UpdateEnvironmentRequest): Promise<UserPreferencesInfo> => {
    const response = await api.put('/api/user/environment', data);
    return response.data;
  },
  
  // Powiadomienia
  updateNotifications: async (data: UpdateNotificationsRequest): Promise<UserPreferencesInfo> => {
    const response = await api.put('/api/user/notifications', data);
    return response.data;
  },
  
  // Pełne preferencje
  updatePreferences: async (data: UpdatePreferencesRequest): Promise<UserPreferencesInfo> => {
    const response = await api.put('/api/user/preferences', data);
    return response.data;
  },
  
  // Telefon
  updatePhone: async (phoneNumber: string): Promise<{ success: boolean; message: string }> => {
    const response = await api.put('/api/user/phone', { phoneNumber });
    return response.data;
  },
};
