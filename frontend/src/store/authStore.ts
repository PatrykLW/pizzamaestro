import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { api } from '../services/api';

interface User {
  id: string;
  email: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  phoneVerified?: boolean;
  accountType: string;
  isPremium: boolean;
  preferences: {
    // Ustawienia ogólne
    language: string;
    theme: string;
    temperatureUnit: string;
    weightUnit: string;
    // Powiadomienia
    emailNotifications?: boolean;
    smsNotifications?: boolean;
    pushNotifications?: boolean;
    smsReminderMinutesBefore?: number;
    // Styl pizzy
    defaultPizzaStyle?: string;
    // Domyślny sprzęt
    defaultOvenType?: string;
    defaultMixerType?: string;
    mixerWattage?: number;
    // Składniki
    availableFlourIds?: string[];
    defaultWaterId?: string;
    // Warunki środowiskowe
    typicalRoomTemperature?: number;
    typicalFridgeTemperature?: number;
    defaultCity?: string;
    defaultLatitude?: number;
    defaultLongitude?: number;
  };
  stats: {
    totalCalculations: number;
    calculationsThisMonth: number;
    totalPizzasBaked: number;
    smsUsedThisMonth?: number;
    lastCalculationAt?: string;
  };
}

interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  
  // Actions
  login: (email: string, password: string) => Promise<void>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => void;
  refreshAuth: () => Promise<boolean>;
  updateUser: (user: Partial<User>) => void;
  setLoading: (loading: boolean) => void;
}

interface RegisterData {
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,
      
      login: async (email: string, password: string) => {
        set({ isLoading: true });
        try {
          const response = await api.post('/api/auth/login', { email, password });
          const { accessToken, refreshToken, user } = response.data;
          
          set({
            user,
            accessToken,
            refreshToken,
            isAuthenticated: true,
            isLoading: false,
          });
          
          // Ustaw token w axios
          api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
        } catch (error) {
          set({ isLoading: false });
          throw error;
        }
      },
      
      register: async (data: RegisterData) => {
        set({ isLoading: true });
        try {
          const response = await api.post('/api/auth/register', data);
          const { accessToken, refreshToken, user } = response.data;
          
          set({
            user,
            accessToken,
            refreshToken,
            isAuthenticated: true,
            isLoading: false,
          });
          
          api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
        } catch (error) {
          set({ isLoading: false });
          throw error;
        }
      },
      
      logout: () => {
        set({
          user: null,
          accessToken: null,
          refreshToken: null,
          isAuthenticated: false,
        });
        delete api.defaults.headers.common['Authorization'];
      },
      
      refreshAuth: async () => {
        const { refreshToken } = get();
        if (!refreshToken) return false;
        
        try {
          const response = await api.post('/api/auth/refresh', { refreshToken });
          const { accessToken, refreshToken: newRefreshToken, user } = response.data;
          
          set({
            user,
            accessToken,
            refreshToken: newRefreshToken,
            isAuthenticated: true,
          });
          
          api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
          return true;
        } catch (error) {
          get().logout();
          return false;
        }
      },
      
      updateUser: (userData: Partial<User>) => {
        const { user } = get();
        if (user) {
          set({ user: { ...user, ...userData } });
        }
      },
      
      setLoading: (loading: boolean) => set({ isLoading: loading }),
    }),
    {
      name: 'pizzamaestro-auth',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

// Inicjalizacja tokenu przy starcie
const initializeAuth = () => {
  const { accessToken } = useAuthStore.getState();
  if (accessToken) {
    api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
  }
};

initializeAuth();
