import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { Chart as ChartJS, ArcElement, Tooltip as ChartTooltip, Legend } from 'chart.js';
import { Doughnut } from 'react-chartjs-2';
import jsPDF from 'jspdf';
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  CardMedia,
  Grid,
  TextField,
  MenuItem,
  Slider,
  Button,
  Switch,
  FormControlLabel,
  Chip,
  Divider,
  Alert,
  CircularProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableRow,
  IconButton,
  Collapse,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  LinearProgress,
  Autocomplete,
  Tooltip,
  alpha,
  useTheme,
} from '@mui/material';
import {
  Calculate as CalculateIcon,
  Info as InfoIcon,
  ExpandMore as ExpandMoreIcon,
  Schedule as ScheduleIcon,
  LocalPizza as PizzaIcon,
  Kitchen as KitchenIcon,
  Thermostat as ThermostatIcon,
  Opacity as WaterIcon,
  CheckCircle as CheckIcon,
  ContentCopy as CopyIcon,
  Share as ShareIcon,
  Bookmark as SaveIcon,
  Print as PrintIcon,
  Cloud as CloudIcon,
  MyLocation as LocationIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { useQuery, useMutation } from '@tanstack/react-query';
import { calculatorApi, ingredientsApi, weatherApi, userApi, CalculationRequest, CalculationResponse, WeatherData, FermentationAdjustment, FlourMixEntry, FlourMixSuggestion, FlourMixParameters } from '../services/api';
import { useAuthStore } from '../store/authStore';
import type { PizzaStyle, Ingredient, OvenType, YeastType, FermentationMethod, PrefermentType } from '../types';
import { IMAGES, PIZZA_STYLE_IMAGES } from '../constants/images';
import { motion, AnimatePresence as FramerAnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';
import { logger } from '../utils/logger';

// Rejestracja komponentów Chart.js
ChartJS.register(ArcElement, ChartTooltip, Legend);

// Wrapper to fix TypeScript compatibility issue with framer-motion
const AnimatePresence = FramerAnimatePresence as React.FC<React.PropsWithChildren<{ mode?: 'wait' | 'sync' | 'popLayout'; initial?: boolean }>>;

const MotionBox = motion(Box);
const MotionCard = motion(Card);

interface CalculatorFormData {
  pizzaStyle: string;
  numberOfPizzas: number;
  ballWeight: number;
  hydration: number;
  saltPercentage: number;
  oilPercentage: number;
  sugarPercentage: number;
  selectedFlourId: string;
  selectedWaterId: string;
  yeastType: string;
  fermentationMethod: string;
  totalFermentationHours: number;
  roomTemperature: number;
  fridgeTemperature: number;
  ovenType: string;
  usePreferment: boolean;
  prefermentType: string;
  generateSchedule: boolean;
  saveRecipe: boolean;
  recipeName: string;
  useWeather: boolean;
  cityName: string;
}

const CalculatorPage: React.FC = () => {
  const theme = useTheme();
  const { isAuthenticated, user } = useAuthStore();
  const [activeStep, setActiveStep] = useState(0);
  const [result, setResult] = useState<CalculationResponse | null>(null);
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [weather, setWeather] = useState<WeatherData | null>(null);
  const [weatherAdjustment, setWeatherAdjustment] = useState<FermentationAdjustment | null>(null);
  const [loadingWeather, setLoadingWeather] = useState(false);
  const [userLocation, setUserLocation] = useState<{ lat: number; lon: number } | null>(null);
  const [loadingEquipment, setLoadingEquipment] = useState(false);
  const [equipmentLoaded, setEquipmentLoaded] = useState(false);
  
  // Miks mąk
  const [useFlourMix, setUseFlourMix] = useState(false);
  const [flourMix, setFlourMix] = useState<FlourMixEntry[]>([]);
  const [flourMixParams, setFlourMixParams] = useState<FlourMixParameters | null>(null);
  const [flourMixSuggestion, setFlourMixSuggestion] = useState<FlourMixSuggestion | null>(null);
  const [loadingMixSuggestion, setLoadingMixSuggestion] = useState(false);
  
  // Techniki ciasta
  const [useAutolyse, setUseAutolyse] = useState(false);
  const [autolyseMinutes, setAutolyseMinutes] = useState(30);
  const [useStretchAndFold, setUseStretchAndFold] = useState(false);
  const [stretchAndFoldSeries, setStretchAndFoldSeries] = useState(3);
  const [stretchAndFoldInterval, setStretchAndFoldInterval] = useState(30);
  
  // Skalowanie receptury
  const [scaledPizzaCount, setScaledPizzaCount] = useState<number | null>(null);

  // Zapytania do API
  const { data: styles, isLoading: stylesLoading } = useQuery({
    queryKey: ['styles'],
    queryFn: calculatorApi.getStyles,
  });

  const { data: ovens } = useQuery({
    queryKey: ['ovens'],
    queryFn: calculatorApi.getOvens,
  });

  const { data: yeastTypes } = useQuery({
    queryKey: ['yeastTypes'],
    queryFn: calculatorApi.getYeastTypes,
  });

  const { data: fermentationMethods } = useQuery({
    queryKey: ['fermentationMethods'],
    queryFn: calculatorApi.getFermentationMethods,
  });

  const { data: prefermentTypes } = useQuery({
    queryKey: ['prefermentTypes'],
    queryFn: calculatorApi.getPrefermentTypes,
  });

  // Składniki
  const { data: flours } = useQuery({
    queryKey: ['flours'],
    queryFn: ingredientsApi.getFlours,
  });

  const { data: waters } = useQuery({
    queryKey: ['waters'],
    queryFn: ingredientsApi.getWaters,
  });

  const {
    control,
    handleSubmit,
    watch,
    setValue,
  } = useForm<CalculatorFormData>({
    defaultValues: {
      pizzaStyle: 'NEAPOLITAN',
      numberOfPizzas: 4,
      ballWeight: 250,
      hydration: 65,
      saltPercentage: 2.8,
      oilPercentage: 0,
      sugarPercentage: 0,
      selectedFlourId: '',
      selectedWaterId: '',
      yeastType: 'FRESH',
      fermentationMethod: 'COLD_FERMENTATION',
      totalFermentationHours: 24,
      roomTemperature: 22,
      fridgeTemperature: 4,
      ovenType: 'WOOD_FIRED',
      usePreferment: false,
      prefermentType: 'POOLISH',
      generateSchedule: true,
      saveRecipe: false,
      recipeName: '',
      useWeather: true,
      cityName: '',
    },
  });

  const selectedStyle = watch('pizzaStyle');
  const usePreferment = watch('usePreferment');
  const hydration = watch('hydration');
  const fermentationHours = watch('totalFermentationHours');
  const useWeather = watch('useWeather');
  const cityName = watch('cityName');
  const roomTemperature = watch('roomTemperature');

  // Efekt: aktualizuj domyślne wartości przy zmianie stylu
  useEffect(() => {
    if (styles && selectedStyle) {
      const style = styles.find((s) => s.id === selectedStyle);
      if (style && style.defaults) {
        setValue('hydration', style.defaults.hydration);
        setValue('ballWeight', style.defaults.ballWeight);
        setValue('totalFermentationHours', style.defaults.fermentationHours);
        setValue('saltPercentage', style.defaults.saltPercentage);
        setValue('oilPercentage', style.defaults.oilPercentage ?? 0);
        setValue('sugarPercentage', style.defaults.sugarPercentage ?? 0);
        if (style.recommendedOven?.type) {
          setValue('ovenType', style.recommendedOven.type);
        }
      }
    }
  }, [selectedStyle, styles, setValue]);

  // Pobierz lokalizację użytkownika i automatycznie załaduj pogodę
  const getLocation = useCallback(() => {
    if (navigator.geolocation) {
      setLoadingWeather(true);
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const loc = {
            lat: position.coords.latitude,
            lon: position.coords.longitude,
          };
          setUserLocation(loc);
          setValue('cityName', ''); // Wyczyść miasto przy użyciu lokalizacji
          
          // Automatycznie pobierz pogodę dla tej lokalizacji
          try {
            const weatherData = await weatherApi.getByCoordinates(loc.lat, loc.lon);
            setWeather(weatherData);
            
            const adjustment = await weatherApi.getFermentationAdjustment(loc.lat, loc.lon);
            setWeatherAdjustment(adjustment);
            
            // Użyj temperatury WEWNĘTRZNEJ
            if (weatherData.indoorTemperature) {
              setValue('roomTemperature', Math.round(weatherData.indoorTemperature));
            } else if (weatherData.temperature) {
              setValue('roomTemperature', Math.round(weatherData.temperature));
            }
            
            toast.success(`Pogoda dla ${weatherData.cityName || 'Twojej lokalizacji'} załadowana`);
          } catch (error) {
            logger.error('Błąd pobierania pogody:', error);
            toast.error('Nie udało się pobrać pogody');
          } finally {
            setLoadingWeather(false);
          }
        },
        (error) => {
          logger.error('Błąd geolokalizacji:', error);
          toast.error('Nie udało się pobrać lokalizacji');
          setLoadingWeather(false);
        }
      );
    }
  }, [setValue]);

  // Pobierz pogodę
  const fetchWeather = useCallback(async () => {
    if (!useWeather) return;
    
    setLoadingWeather(true);
    try {
      let weatherData: WeatherData;
      
      if (cityName) {
        weatherData = await weatherApi.getByCity(cityName);
      } else if (userLocation) {
        weatherData = await weatherApi.getByCoordinates(userLocation.lat, userLocation.lon);
      } else {
        // Domyślnie Warszawa
        weatherData = await weatherApi.getByCity('Warszawa');
      }
      
      setWeather(weatherData);
      
      // Pobierz wpływ na fermentację
      const adjustment = await weatherApi.getFermentationAdjustment(
        weatherData.latitude || 52.23,
        weatherData.longitude || 21.01
      );
      setWeatherAdjustment(adjustment);
      
      // Zaktualizuj temperaturę pokojową na podstawie TEMPERATURY WEWNĘTRZNEJ
      if (weatherData.indoorTemperature) {
        setValue('roomTemperature', Math.round(weatherData.indoorTemperature));
      } else if (weatherData.temperature) {
        // Fallback do temperatury zewnętrznej
        setValue('roomTemperature', Math.round(weatherData.temperature));
      }
      
      toast.success(`Pogoda dla ${weatherData.cityName || 'Twojej lokalizacji'} załadowana`);
    } catch (error) {
      logger.error('Błąd pobierania pogody:', error);
      toast.error('Nie udało się pobrać pogody');
    } finally {
      setLoadingWeather(false);
    }
  }, [useWeather, cityName, userLocation, setValue]);

  // Auto-pobierz pogodę przy starcie
  useEffect(() => {
    if (useWeather && !weather) {
      fetchWeather();
    }
  }, [useWeather, weather, fetchWeather]);

  // Funkcja ładowania domyślnego sprzętu z profilu użytkownika
  const loadDefaultEquipment = useCallback(async () => {
    if (!isAuthenticated || !user?.preferences) {
      toast.error('Musisz być zalogowany aby użyć domyślnego sprzętu');
      return;
    }

    setLoadingEquipment(true);
    try {
      const equipment = await userApi.getEquipment();
      
      let loadedAny = false;

      // Ustaw piec
      if (equipment.defaultOvenType) {
        setValue('ovenType', equipment.defaultOvenType);
        loadedAny = true;
      }

      // Ustaw temperaturę pokojową
      if (user.preferences.typicalRoomTemperature) {
        setValue('roomTemperature', user.preferences.typicalRoomTemperature);
        loadedAny = true;
      }

      // Ustaw temperaturę lodówki
      if (user.preferences.typicalFridgeTemperature) {
        setValue('fridgeTemperature', user.preferences.typicalFridgeTemperature);
        loadedAny = true;
      }

      // Ustaw domyślną mąkę (pierwsza z listy jeśli jest)
      if (equipment.availableFlourIds && equipment.availableFlourIds.length > 0) {
        setValue('selectedFlourId', equipment.availableFlourIds[0]);
        loadedAny = true;
      }

      // Ustaw domyślną wodę
      if (equipment.defaultWaterId) {
        setValue('selectedWaterId', equipment.defaultWaterId);
        loadedAny = true;
      }

      // Ustaw miasto dla pogody jeśli jest i pobierz pogodę
      if (user.preferences.defaultCity) {
        setValue('cityName', user.preferences.defaultCity);
      }

      // Pobierz pogodę z domyślnej lokalizacji
      if (user.preferences.defaultLatitude && user.preferences.defaultLongitude) {
        setUserLocation({
          lat: user.preferences.defaultLatitude,
          lon: user.preferences.defaultLongitude,
        });
        try {
          const weatherData = await weatherApi.getByCoordinates(
            user.preferences.defaultLatitude,
            user.preferences.defaultLongitude
          );
          setWeather(weatherData);
          const adjustment = await weatherApi.getFermentationAdjustment(
            user.preferences.defaultLatitude,
            user.preferences.defaultLongitude
          );
          setWeatherAdjustment(adjustment);
        } catch (e) {
          logger.warn('Nie udało się pobrać pogody z domyślnej lokalizacji');
        }
      }

      setEquipmentLoaded(true);
      
      if (loadedAny) {
        toast.success('Załadowano domyślny sprzęt z profilu');
      } else {
        toast('Brak zapisanego domyślnego sprzętu w profilu', { icon: 'ℹ️' });
      }
    } catch (error) {
      logger.error('Błąd ładowania sprzętu:', error);
      toast.error('Nie udało się załadować domyślnego sprzętu');
    } finally {
      setLoadingEquipment(false);
    }
  }, [isAuthenticated, user, setValue]);

  // === MIKS MĄK ===
  
  // Pobierz sugestię miksu dla wybranego stylu
  const getSuggestedMix = useCallback(async () => {
    if (!selectedStyle) return;
    
    setLoadingMixSuggestion(true);
    try {
      const suggestion = isAuthenticated 
        ? await calculatorApi.suggestFlourMixWithProfile(selectedStyle)
        : await calculatorApi.suggestFlourMix(selectedStyle);
      
      setFlourMixSuggestion(suggestion);
      
      if (suggestion.success && suggestion.flourMix) {
        setFlourMix(suggestion.flourMix);
        setUseFlourMix(suggestion.isMix);
        
        // Oblicz parametry miksu
        if (suggestion.flourMix.length > 0) {
          const params = await calculatorApi.calculateFlourMixParams(suggestion.flourMix);
          setFlourMixParams(params);
        }
        
        toast.success(suggestion.message);
      }
    } catch (error) {
      logger.error('Błąd pobierania sugestii miksu:', error);
      toast.error('Nie udało się pobrać sugestii miksu');
    } finally {
      setLoadingMixSuggestion(false);
    }
  }, [selectedStyle, isAuthenticated]);

  // Dodaj mąkę do miksu
  const addFlourToMix = useCallback((flourId: string) => {
    if (flourMix.some(f => f.flourId === flourId)) return;
    
    const newMix = [...flourMix, { flourId, percentage: 0 }];
    // Równy podział procentów
    const equalPercentage = 100 / newMix.length;
    setFlourMix(newMix.map(f => ({ ...f, percentage: Math.round(equalPercentage) })));
  }, [flourMix]);

  // Usuń mąkę z miksu
  const removeFlourFromMix = useCallback((flourId: string) => {
    const newMix = flourMix.filter(f => f.flourId !== flourId);
    if (newMix.length > 0) {
      // Przelicz procenty
      const totalRemaining = newMix.reduce((sum, f) => sum + f.percentage, 0);
      setFlourMix(newMix.map(f => ({
        ...f,
        percentage: Math.round((f.percentage / totalRemaining) * 100)
      })));
    } else {
      setFlourMix([]);
    }
  }, [flourMix]);

  // Zmień procent mąki w miksie
  const updateFlourPercentage = useCallback((flourId: string, newPercentage: number) => {
    setFlourMix(prev => prev.map(f => 
      f.flourId === flourId ? { ...f, percentage: newPercentage } : f
    ));
  }, []);

  // Oblicz parametry miksu przy zmianie
  useEffect(() => {
    const calculateParams = async () => {
      if (flourMix.length > 0 && useFlourMix) {
        // Normalizuj procenty do 100%
        const total = flourMix.reduce((sum, f) => sum + f.percentage, 0);
        if (Math.abs(total - 100) < 1) {
          try {
            const params = await calculatorApi.calculateFlourMixParams(flourMix);
            setFlourMixParams(params);
          } catch (error) {
            logger.error('Błąd obliczania parametrów miksu:', error);
          }
        }
      } else {
        setFlourMixParams(null);
      }
    };
    
    const debounce = setTimeout(calculateParams, 500);
    return () => clearTimeout(debounce);
  }, [flourMix, useFlourMix]);

  // Automatycznie dobierz metodę fermentacji na podstawie czasu
  // roomTemperature jest już zadeklarowane wyżej (linia ~205)
  
  useEffect(() => {
    if (!fermentationHours) return;
    
    const roomTemp = roomTemperature || 22;
    
    // Logika automatycznego doboru metody fermentacji
    if (fermentationHours <= 6) {
      // Same day - tylko temp. pokojowa
      setValue('fermentationMethod', 'SAME_DAY');
    } else if (fermentationHours <= 12) {
      // Krótka fermentacja - temp. pokojowa
      if (roomTemp > 24) {
        // Ciepło - zalecana lodówka
        setValue('fermentationMethod', 'MIXED');
      } else {
        setValue('fermentationMethod', 'ROOM_TEMPERATURE');
      }
    } else if (fermentationHours <= 24) {
      // Standardowa - mieszana lub lodówka w zależności od temperatury
      if (roomTemp > 26) {
        setValue('fermentationMethod', 'COLD_FERMENTATION');
      } else {
        setValue('fermentationMethod', 'MIXED');
      }
    } else {
      // Długa fermentacja - zawsze lodówka
      setValue('fermentationMethod', 'COLD_FERMENTATION');
    }
  }, [fermentationHours, setValue, roomTemperature]);

  const calculateMutation = useMutation({
    mutationFn: (data: CalculationRequest) =>
      isAuthenticated
        ? calculatorApi.calculate(data)
        : calculatorApi.calculatePublic(data),
    onSuccess: (data) => {
      setResult(data);
      setActiveStep(3);
      toast.success('Receptura obliczona!');
    },
    onError: () => {
      toast.error('Błąd podczas kalkulacji');
    },
  });

  const onSubmit = (data: CalculatorFormData) => {
    // Zastosuj korektę pogodową do czasu fermentacji
    let adjustedFermentationHours = data.totalFermentationHours;
    if (weatherAdjustment && useWeather) {
      const adjustment = weatherAdjustment.fermentationTimeAdjustmentPercent / 100;
      adjustedFermentationHours = Math.round(data.totalFermentationHours * (1 + adjustment));
    }

    const request: CalculationRequest = {
      pizzaStyle: data.pizzaStyle,
      numberOfPizzas: data.numberOfPizzas,
      ballWeight: data.ballWeight,
      hydration: data.hydration,
      saltPercentage: data.saltPercentage,
      oilPercentage: data.oilPercentage,
      sugarPercentage: data.sugarPercentage,
      yeastType: data.yeastType,
      fermentationMethod: data.fermentationMethod,
      totalFermentationHours: adjustedFermentationHours,
      roomTemperature: data.roomTemperature,
      fridgeTemperature: data.fridgeTemperature,
      ovenType: data.ovenType,
      usePreferment: data.usePreferment,
      prefermentType: data.usePreferment ? data.prefermentType : undefined,
      generateSchedule: data.generateSchedule,
      saveRecipe: isAuthenticated && data.saveRecipe,
      recipeName: data.recipeName || undefined,
      // Mąka
      flourId: !useFlourMix ? data.selectedFlourId : undefined,
      flourMix: useFlourMix && flourMix.length > 0 ? flourMix : undefined,
      // Techniki ciasta
      useAutolyse: useAutolyse,
      autolyseMinutes: useAutolyse ? autolyseMinutes : undefined,
      stretchAndFoldSeries: useStretchAndFold ? stretchAndFoldSeries : undefined,
      stretchAndFoldInterval: useStretchAndFold ? stretchAndFoldInterval : undefined,
    };
    calculateMutation.mutate(request);
  };

  const handleNext = () => setActiveStep((prev) => prev + 1);
  const handleBack = () => setActiveStep((prev) => prev - 1);
  const handleReset = () => {
    setActiveStep(0);
    setResult(null);
  };

  const copyToClipboard = () => {
    if (result) {
      const weatherInfo = weather ? `\n\n🌤️ Pogoda: ${weather.temperature}°C, ${weather.description}` : '';
      const text = `🍕 Receptura PizzaMaestro
━━━━━━━━━━━━━━━━━━
${result.pizzaStyleName} - ${result.numberOfPizzas} pizz

📊 Składniki:
• Mąka: ${result.ingredients.flourGrams}g
• Woda: ${result.ingredients.waterGrams}g (${result.bakerPercentages.water}%)
• Sól: ${result.ingredients.saltGrams}g
• Drożdże: ${result.ingredients.yeastGrams}g

🔥 Wypiek: ${result.ovenInfo.temperature}°C${weatherInfo}

Wygenerowano na pizzamaestro.pl`;
      navigator.clipboard.writeText(text);
      toast.success('Skopiowano do schowka!');
    }
  };

  // Eksport do PDF
  const exportToPDF = () => {
    if (!result) return;

    const doc = new jsPDF();
    const pageWidth = doc.internal.pageSize.getWidth();
    let y = 20;

    // Nagłówek
    doc.setFontSize(24);
    doc.setTextColor(211, 84, 0); // Pomarańczowy
    doc.text('PizzaMaestro', pageWidth / 2, y, { align: 'center' });
    y += 10;

    doc.setFontSize(18);
    doc.setTextColor(0, 0, 0);
    doc.text(`Receptura: ${result.pizzaStyleName}`, pageWidth / 2, y, { align: 'center' });
    y += 15;

    // Info
    doc.setFontSize(12);
    doc.setTextColor(100, 100, 100);
    doc.text(`${result.numberOfPizzas} pizz × ${result.ballWeight}g = ${result.ingredients.totalDoughWeight}g ciasta`, pageWidth / 2, y, { align: 'center' });
    y += 15;

    // Składniki
    doc.setFontSize(14);
    doc.setTextColor(0, 0, 0);
    doc.text('Składniki:', 20, y);
    y += 8;

    doc.setFontSize(11);
    const ingredients = [
      { name: 'Mąka', value: result.ingredients.flourGrams, percent: 100 },
      { name: 'Woda', value: result.ingredients.waterGrams, percent: result.bakerPercentages.water },
      { name: 'Sól', value: result.ingredients.saltGrams, percent: result.bakerPercentages.salt },
      { name: `Drożdże (${result.ingredients.yeastType})`, value: result.ingredients.yeastGrams, percent: result.bakerPercentages.yeast },
    ];
    
    if (result.ingredients.oilGrams > 0) {
      ingredients.push({ name: 'Oliwa', value: result.ingredients.oilGrams, percent: result.bakerPercentages.oil });
    }
    if (result.ingredients.sugarGrams > 0) {
      ingredients.push({ name: 'Cukier', value: result.ingredients.sugarGrams, percent: result.bakerPercentages.sugar });
    }

    ingredients.forEach(ing => {
      doc.text(`• ${ing.name}: ${ing.value}g (${ing.percent}%)`, 25, y);
      y += 6;
    });
    y += 10;

    // Piec
    doc.setFontSize(14);
    doc.text('Wypiek:', 20, y);
    y += 8;

    doc.setFontSize(11);
    doc.text(`• Piec: ${result.ovenInfo.ovenName}`, 25, y);
    y += 6;
    doc.text(`• Temperatura: ${result.ovenInfo.temperature}°C`, 25, y);
    y += 6;
    doc.text(`• Czas: ${Math.round(result.ovenInfo.bakingTimeSeconds / 60)} minut`, 25, y);
    y += 15;

    // Wskazówki
    if (result.tips && result.tips.length > 0) {
      doc.setFontSize(14);
      doc.text('Wskazówki:', 20, y);
      y += 8;

      doc.setFontSize(10);
      result.tips.slice(0, 3).forEach(tip => {
        const lines = doc.splitTextToSize(`• ${tip}`, pageWidth - 45);
        lines.forEach((line: string) => {
          doc.text(line, 25, y);
          y += 5;
        });
        y += 2;
      });
    }

    // Stopka
    y = doc.internal.pageSize.getHeight() - 15;
    doc.setFontSize(9);
    doc.setTextColor(150, 150, 150);
    doc.text(`Wygenerowano: ${new Date().toLocaleDateString('pl-PL')} | pizzamaestro.pl`, pageWidth / 2, y, { align: 'center' });

    // Zapisz PDF
    doc.save(`receptura-${result.pizzaStyleName.toLowerCase().replace(/\s+/g, '-')}.pdf`);
    toast.success('PDF został pobrany!');
  };

  // Udostępnianie linkiem
  const shareRecipe = async () => {
    if (!result) return;

    const recipeData = {
      style: result.pizzaStyle,
      pizzas: result.numberOfPizzas,
      ballWeight: result.ballWeight,
      hydration: result.bakerPercentages.water,
      salt: result.bakerPercentages.salt,
    };

    // Zakoduj dane w URL
    const params = new URLSearchParams();
    Object.entries(recipeData).forEach(([key, value]) => {
      params.append(key, String(value));
    });

    const shareUrl = `${window.location.origin}/calculator?${params.toString()}`;

    // Sprawdź czy Web Share API jest dostępne
    if (navigator.share) {
      try {
        await navigator.share({
          title: `Receptura: ${result.pizzaStyleName}`,
          text: `Sprawdź moją recepturę na pizzę ${result.pizzaStyleName}!`,
          url: shareUrl,
        });
        toast.success('Udostępniono!');
      } catch (error) {
        // Użytkownik anulował
        if ((error as Error).name !== 'AbortError') {
          navigator.clipboard.writeText(shareUrl);
          toast.success('Link skopiowany do schowka!');
        }
      }
    } else {
      // Fallback - kopiuj do schowka
      navigator.clipboard.writeText(shareUrl);
      toast.success('Link skopiowany do schowka!');
    }
  };

  // Skalowane składniki (musi być przed early return)
  const scaledIngredients = useMemo(() => {
    if (!result || scaledPizzaCount === null || scaledPizzaCount === result.numberOfPizzas) {
      return null;
    }
    
    const scale = scaledPizzaCount / result.numberOfPizzas;
    
    return {
      numberOfPizzas: scaledPizzaCount,
      totalDoughWeight: Math.round(result.ingredients.totalDoughWeight * scale),
      flourGrams: Math.round(result.ingredients.flourGrams * scale),
      waterGrams: Math.round(result.ingredients.waterGrams * scale),
      saltGrams: Math.round(result.ingredients.saltGrams * scale * 10) / 10,
      yeastGrams: Math.round(result.ingredients.yeastGrams * scale * 10) / 10,
      oilGrams: Math.round(result.ingredients.oilGrams * scale),
      sugarGrams: Math.round(result.ingredients.sugarGrams * scale),
    };
  }, [result, scaledPizzaCount]);

  if (stylesLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress size={60} />
      </Box>
    );
  }

  const selectedStyleData = styles?.find((s: PizzaStyle) => s.id === selectedStyle);
  const isPremium = user?.accountType === 'PREMIUM' || user?.accountType === 'PRO';

  return (
    <Box sx={{ bgcolor: 'grey.50', minHeight: '100vh', pb: 8 }}>
      {/* Header */}
      <Box
        sx={{
          position: 'relative',
          py: 8,
          overflow: 'hidden',
          '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundImage: `url(${PIZZA_STYLE_IMAGES[selectedStyle] || IMAGES.hero.main})`,
            backgroundSize: 'cover',
            backgroundPosition: 'center',
            filter: 'brightness(0.3)',
          },
        }}
      >
        <Container maxWidth="lg" sx={{ position: 'relative', zIndex: 1 }}>
          <MotionBox
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
          >
            <Typography variant="overline" sx={{ color: 'rgba(255,255,255,0.8)', letterSpacing: 2 }}>
              KALKULATOR CIASTA
            </Typography>
            <Typography variant="h2" fontWeight="bold" sx={{ color: 'white', mb: 1 }}>
              {selectedStyleData?.name || 'Pizza'} Calculator
            </Typography>
            <Typography variant="h6" sx={{ color: 'rgba(255,255,255,0.8)', maxWidth: 600 }}>
              {selectedStyleData?.description || 'Oblicz idealną recepturę ciasta na pizzę'}
            </Typography>
            
            {/* Weather Banner */}
            {weather && useWeather && (
              <Paper sx={{ mt: 3, p: 2, display: 'flex', alignItems: 'center', gap: 2, bgcolor: alpha(theme.palette.primary.main, 0.9) }}>
                <CloudIcon sx={{ color: 'white', fontSize: 40 }} />
                <Box sx={{ flex: 1 }}>
                  <Typography variant="subtitle1" sx={{ color: 'white', fontWeight: 'bold' }}>
                    {weather.cityName || 'Twoja lokalizacja'} • {weather.description}
                  </Typography>
                  <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.8)' }}>
                    🌡️ {weather.temperature}°C • 💧 {weather.humidity}% • 📊 {weather.pressure} hPa
                  </Typography>
                </Box>
                {weatherAdjustment && (
                  <Chip 
                    label={`Korekta drożdży: ${weatherAdjustment.yeastAdjustmentPercent > 0 ? '+' : ''}${weatherAdjustment.yeastAdjustmentPercent.toFixed(0)}%`}
                    sx={{ bgcolor: 'white', fontWeight: 'bold' }}
                  />
                )}
              </Paper>
            )}
          </MotionBox>
        </Container>
      </Box>

      <Container maxWidth="lg" sx={{ mt: -4, position: 'relative', zIndex: 2 }}>
        {/* Progress */}
        <Paper sx={{ p: 2, mb: 4, borderRadius: 3 }}>
          <Stepper activeStep={activeStep} alternativeLabel>
            {['Styl pizzy', 'Składniki', 'Fermentacja', 'Wynik'].map((label, index) => (
              <Step key={label} completed={activeStep > index}>
                <StepLabel>{label}</StepLabel>
              </Step>
            ))}
          </Stepper>
          <LinearProgress
            variant="determinate"
            value={(activeStep / 3) * 100}
            sx={{ mt: 2, height: 6, borderRadius: 3 }}
          />
        </Paper>

        <AnimatePresence mode="wait">
          {/* ========== STEP 1: PIZZA STYLE ========== */}
          {activeStep === 0 && (
            <MotionBox
              key="step1"
              initial={{ opacity: 0, x: 50 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -50 }}
            >
              <Typography variant="h4" fontWeight="bold" gutterBottom>
                Wybierz styl pizzy
              </Typography>
              <Typography color="text.secondary" sx={{ mb: 4 }}>
                Każdy styl ma unikalne parametry dopasowane do tradycyjnych przepisów
              </Typography>

              <Grid container spacing={3}>
                {styles?.map((style: PizzaStyle) => (
                  <Grid item xs={12} sm={6} md={4} key={style.id}>
                    <MotionCard
                      whileHover={{ y: -8, boxShadow: '0 12px 40px rgba(0,0,0,0.15)' }}
                      onClick={() => {
                        setValue('pizzaStyle', style.id);
                        // Od razu przejdź do kalkulatora po kliknięciu
                        setTimeout(() => handleNext(), 150);
                      }}
                      sx={{
                        cursor: 'pointer',
                        height: '100%',
                        border: selectedStyle === style.id ? 3 : 1,
                        borderColor: selectedStyle === style.id ? 'primary.main' : 'divider',
                        overflow: 'hidden',
                        transition: 'all 0.2s ease',
                        '&:hover': {
                          borderColor: 'primary.main',
                        },
                      }}
                    >
                      <Box sx={{ position: 'relative' }}>
                        <CardMedia
                          component="img"
                          height="180"
                          image={PIZZA_STYLE_IMAGES[style.id] || IMAGES.hero.main}
                          alt={style.name}
                        />
                        {selectedStyle === style.id && (
                          <Box
                            sx={{
                              position: 'absolute',
                              top: 12,
                              right: 12,
                              bgcolor: 'primary.main',
                              borderRadius: '50%',
                              p: 0.5,
                            }}
                          >
                            <CheckIcon sx={{ color: 'white', fontSize: 20 }} />
                          </Box>
                        )}
                        <Box
                          sx={{
                            position: 'absolute',
                            bottom: 0,
                            left: 0,
                            right: 0,
                            background: 'linear-gradient(transparent, rgba(0,0,0,0.8))',
                            p: 2,
                          }}
                        >
                          <Typography variant="h6" sx={{ color: 'white', fontWeight: 'bold' }}>
                            {style.name}
                          </Typography>
                        </Box>
                      </Box>
                      <CardContent>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2, minHeight: 40 }}>
                          {style.description?.substring(0, 100)}...
                        </Typography>
                        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                          <Chip
                            size="small"
                            icon={<WaterIcon />}
                            label={`${style.defaults?.hydration ?? 65}%`}
                            color="primary"
                            variant="outlined"
                          />
                          <Chip
                            size="small"
                            icon={<ScheduleIcon />}
                            label={`${style.defaults?.fermentationHours ?? 24}h`}
                            variant="outlined"
                          />
                          <Chip
                            size="small"
                            icon={<ThermostatIcon />}
                            label={`${style.recommendedOven?.temperature ?? 450}°C`}
                            color="error"
                            variant="outlined"
                          />
                        </Box>
                      </CardContent>
                    </MotionCard>
                  </Grid>
                ))}
              </Grid>

              <Box sx={{ mt: 4, display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column', gap: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  👆 Kliknij na wybrany styl aby przejść do kalkulatora
                </Typography>
              </Box>
            </MotionBox>
          )}

          {/* ========== STEP 2: INGREDIENTS ========== */}
          {activeStep === 1 && (
            <MotionBox
              key="step2"
              initial={{ opacity: 0, x: 50 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -50 }}
            >
              <Grid container spacing={4}>
                <Grid item xs={12} md={8}>
                  {/* Przycisk użycia domyślnego sprzętu */}
                  {isAuthenticated && user?.preferences && (
                    <Card sx={{ p: 2, mb: 3, bgcolor: equipmentLoaded ? 'success.50' : 'primary.50', border: '2px dashed', borderColor: equipmentLoaded ? 'success.main' : 'primary.main' }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 2 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <KitchenIcon color={equipmentLoaded ? 'success' : 'primary'} />
                          <Box>
                            <Typography variant="subtitle1" fontWeight="bold">
                              {equipmentLoaded ? '✓ Załadowano domyślny sprzęt' : 'Masz zapisany domyślny sprzęt'}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              {equipmentLoaded 
                                ? 'Ustawienia z profilu zostały zastosowane'
                                : 'Kliknij aby użyć pieca, mąk i temperatur z Twojego profilu'
                              }
                            </Typography>
                          </Box>
                        </Box>
                        <Button
                          variant={equipmentLoaded ? 'outlined' : 'contained'}
                          color={equipmentLoaded ? 'success' : 'primary'}
                          onClick={loadDefaultEquipment}
                          disabled={loadingEquipment}
                          startIcon={loadingEquipment ? <CircularProgress size={16} /> : <KitchenIcon />}
                        >
                          {loadingEquipment ? 'Ładowanie...' : equipmentLoaded ? 'Załaduj ponownie' : 'Użyj mojego sprzętu'}
                        </Button>
                      </Box>
                    </Card>
                  )}

                  <Card sx={{ p: 4, mb: 3 }}>
                    <Typography variant="h5" fontWeight="bold" gutterBottom>
                      Podstawowe parametry
                    </Typography>

                    <Grid container spacing={3} sx={{ mt: 1 }}>
                      <Grid item xs={12} sm={6}>
                        <Controller
                          name="numberOfPizzas"
                          control={control}
                          render={({ field }) => (
                            <TextField
                              {...field}
                              label="Liczba pizz"
                              type="number"
                              fullWidth
                              InputProps={{
                                startAdornment: <PizzaIcon sx={{ mr: 1, color: 'primary.main' }} />,
                              }}
                            />
                          )}
                        />
                      </Grid>
                      <Grid item xs={12} sm={6}>
                        <Controller
                          name="ballWeight"
                          control={control}
                          render={({ field }) => (
                            <TextField
                              {...field}
                              label="Waga kulki (g)"
                              type="number"
                              fullWidth
                              helperText="220-280g dla średniej pizzy"
                            />
                          )}
                        />
                      </Grid>
                      
                      <Grid item xs={12}>
                        <Box sx={{ p: 2, bgcolor: 'primary.50', borderRadius: 2, border: '2px solid', borderColor: 'primary.main' }}>
                          <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
                            ⏰ Za ile godzin chcesz robić pizzę?
                          </Typography>
                          <Controller
                            name="totalFermentationHours"
                            control={control}
                            render={({ field }) => (
                              <Box>
                                <Slider
                                  {...field}
                                  min={2}
                                  max={96}
                                  valueLabelDisplay="auto"
                                  valueLabelFormat={(v) => `${v}h`}
                                  marks={[
                                    { value: 4, label: '4h' },
                                    { value: 12, label: '12h' },
                                    { value: 24, label: '24h' },
                                    { value: 48, label: '48h' },
                                    { value: 72, label: '72h' },
                                  ]}
                                />
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 1 }}>
                                  <Typography variant="body2" color="text.secondary">
                                    {fermentationHours <= 6 ? '🔥 Same day - szybka pizza' :
                                     fermentationHours <= 12 ? '⚡ Krótka fermentacja - lekko rozwinięty smak' :
                                     fermentationHours <= 24 ? '✅ Standard - zbalansowany smak i trawienie' :
                                     fermentationHours <= 48 ? '🌟 Długa fermentacja - bogaty smak' :
                                     '👑 Bardzo długa - maksymalny rozwój smaku'}
                                  </Typography>
                                  <Chip 
                                    label={`${fermentationHours}h`} 
                                    color={fermentationHours >= 24 ? 'success' : fermentationHours >= 12 ? 'primary' : 'warning'}
                                    size="small"
                                  />
                                </Box>
                                <Typography variant="caption" color="info.main" sx={{ display: 'block', mt: 1 }}>
                                  💡 Na podstawie czasu algorytm automatycznie dobierze optymalną metodę fermentacji 
                                  (pokojowa/lodówka/mieszana) i ilość drożdży
                                </Typography>
                              </Box>
                            )}
                          />
                        </Box>
                      </Grid>

                      <Grid item xs={12}>
                        <Box sx={{ px: 2 }}>
                          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                            <Typography fontWeight="500">
                              Nawodnienie (hydratacja)
                            </Typography>
                            <Chip
                              label={`${hydration}%`}
                              color="primary"
                              size="small"
                            />
                          </Box>
                          <Controller
                            name="hydration"
                            control={control}
                            render={({ field }) => (
                              <Slider
                                {...field}
                                min={45}
                                max={90}
                                valueLabelDisplay="auto"
                                marks={[
                                  { value: 55, label: '55%' },
                                  { value: 65, label: '65%' },
                                  { value: 75, label: '75%' },
                                  { value: 85, label: '85%' },
                                ]}
                              />
                            )}
                          />
                          <Typography variant="caption" color="text.secondary">
                            {hydration < 60 ? '💪 Sztywne ciasto - łatwe w obsłudze' :
                             hydration < 70 ? '👍 Standardowe - zbalansowane' :
                             '🌊 Wysokie - wymaga doświadczenia'}
                          </Typography>
                        </Box>
                      </Grid>
                    </Grid>
                  </Card>

                  {/* Składniki - zalecane wartości z możliwością modyfikacji */}
                  <Card sx={{ p: 4 }}>
                    <Typography variant="h5" fontWeight="bold" gutterBottom>
                      Składniki (zalecane dla {selectedStyleData?.name})
                    </Typography>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                      Wartości są automatycznie dobrane do stylu pizzy. Możesz je dostosować.
                    </Typography>

                    <Grid container spacing={3}>
                      {/* === SEKCJA MĄKI === */}
                      <Grid item xs={12}>
                        <Box sx={{ 
                          p: 3, 
                          bgcolor: alpha(theme.palette.primary.main, 0.05), 
                          borderRadius: 2,
                          border: '1px solid',
                          borderColor: alpha(theme.palette.primary.main, 0.2)
                        }}>
                          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                            <Typography variant="h6" fontWeight="bold">
                              🌾 Mąka
                            </Typography>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                              <FormControlLabel
                                control={
                                  <Switch 
                                    checked={useFlourMix} 
                                    onChange={(e) => setUseFlourMix(e.target.checked)}
                                    color="primary"
                                  />
                                }
                                label="Miks mąk"
                              />
                              <Button
                                variant="outlined"
                                size="small"
                                onClick={getSuggestedMix}
                                disabled={loadingMixSuggestion}
                                startIcon={loadingMixSuggestion ? <CircularProgress size={16} /> : <InfoIcon />}
                              >
                                Sugeruj dla {selectedStyleData?.name || 'stylu'}
                              </Button>
                            </Box>
                          </Box>
                          
                          {/* Pojedyncza mąka */}
                          {!useFlourMix && (
                            <Controller
                              name="selectedFlourId"
                              control={control}
                              render={({ field }) => (
                                <Autocomplete
                                  options={flours || []}
                                  getOptionLabel={(option: Ingredient) => 
                                    `${option.name} (${option.brand}) - ${option.flourParameters?.proteinContent || '?'}% białka${option.flourParameters?.strength ? `, W${option.flourParameters.strength}` : ''}`
                                  }
                                  value={flours?.find((f: Ingredient) => f.id === field.value) || null}
                                  onChange={(_, newValue) => field.onChange(newValue?.id || '')}
                                  renderInput={(params) => (
                                    <TextField
                                      {...params}
                                      label="Wybierz mąkę"
                                      helperText="Wybierz mąkę lub zostaw puste dla domyślnej"
                                    />
                                  )}
                                  renderOption={(props, option: Ingredient) => (
                                    <li {...props}>
                                      <Box sx={{ display: 'flex', flexDirection: 'column' }}>
                                        <Typography variant="body1">
                                          {option.name} ({option.brand})
                                        </Typography>
                                        <Typography variant="caption" color="text.secondary">
                                          Białko: {option.flourParameters?.proteinContent}%
                                          {option.flourParameters?.strength && ` • W: ${option.flourParameters.strength}`}
                                          {option.flourParameters?.recommendedHydrationMin && 
                                            ` • Hydratacja: ${option.flourParameters.recommendedHydrationMin}-${option.flourParameters.recommendedHydrationMax}%`}
                                        </Typography>
                                      </Box>
                                    </li>
                                  )}
                                />
                              )}
                            />
                          )}
                          
                          {/* Miks mąk */}
                          {useFlourMix && (
                            <Box>
                              {/* Lista mąk w miksie */}
                              {flourMix.map((entry, index) => {
                                const flour = flours?.find((f: Ingredient) => f.id === entry.flourId);
                                return (
                                  <Box key={entry.flourId} sx={{ 
                                    display: 'flex', 
                                    alignItems: 'center', 
                                    gap: 2, 
                                    mb: 2,
                                    p: 2,
                                    bgcolor: 'background.paper',
                                    borderRadius: 1
                                  }}>
                                    <Box sx={{ flex: 1 }}>
                                      <Typography variant="subtitle2" fontWeight="bold">
                                        {flour?.name || 'Nieznana mąka'} ({flour?.brand})
                                      </Typography>
                                      <Typography variant="caption" color="text.secondary">
                                        Białko: {flour?.flourParameters?.proteinContent}%
                                        {flour?.flourParameters?.strength && ` • W: ${flour.flourParameters.strength}`}
                                      </Typography>
                                    </Box>
                                    <Box sx={{ width: 200 }}>
                                      <Slider
                                        value={entry.percentage}
                                        onChange={(_, value) => updateFlourPercentage(entry.flourId, value as number)}
                                        min={5}
                                        max={95}
                                        step={5}
                                        valueLabelDisplay="auto"
                                        valueLabelFormat={(v) => `${v}%`}
                                      />
                                    </Box>
                                    <Chip 
                                      label={`${entry.percentage}%`}
                                      color={entry.percentage > 50 ? 'primary' : 'default'}
                                      size="small"
                                    />
                                    <IconButton 
                                      size="small" 
                                      onClick={() => removeFlourFromMix(entry.flourId)}
                                      color="error"
                                    >
                                      <ExpandMoreIcon sx={{ transform: 'rotate(45deg)' }} />
                                    </IconButton>
                                  </Box>
                                );
                              })}
                              
                              {/* Dodaj mąkę do miksu */}
                              <Autocomplete
                                options={(flours || []).filter((f: Ingredient) => !flourMix.some(m => m.flourId === f.id))}
                                getOptionLabel={(option: Ingredient) => `${option.name} (${option.brand})`}
                                onChange={(_, newValue) => {
                                  if (newValue) addFlourToMix(newValue.id);
                                }}
                                value={null}
                                renderInput={(params) => (
                                  <TextField
                                    {...params}
                                    label="+ Dodaj mąkę do miksu"
                                    size="small"
                                  />
                                )}
                              />
                              
                              {/* Parametry miksu */}
                              {flourMixParams && (
                                <Paper sx={{ p: 2, mt: 2, bgcolor: alpha(theme.palette.success.main, 0.1) }}>
                                  <Typography variant="subtitle2" fontWeight="bold" gutterBottom>
                                    📊 Parametry miksu:
                                  </Typography>
                                  <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                                    <Chip 
                                      label={`Białko: ${flourMixParams.averageProtein}%`}
                                      color="primary"
                                      variant="outlined"
                                    />
                                    {flourMixParams.averageStrength && (
                                      <Chip 
                                        label={`W: ${flourMixParams.averageStrength}`}
                                        color="secondary"
                                        variant="outlined"
                                      />
                                    )}
                                    <Chip 
                                      label={`Hydratacja: ${flourMixParams.recommendedHydrationMin}-${flourMixParams.recommendedHydrationMax}%`}
                                      variant="outlined"
                                    />
                                  </Box>
                                </Paper>
                              )}
                              
                              {/* Sugestia */}
                              {flourMixSuggestion && flourMixSuggestion.explanation && (
                                <Alert severity="info" sx={{ mt: 2 }}>
                                  <Typography variant="body2">{flourMixSuggestion.explanation}</Typography>
                                </Alert>
                              )}
                            </Box>
                          )}
                        </Box>
                      </Grid>

                      {/* Sól, oliwa, cukier */}
                      <Grid item xs={12} sm={4}>
                        <Controller
                          name="saltPercentage"
                          control={control}
                          render={({ field }) => (
                            <TextField
                              {...field}
                              label="🧂 Sól (%)"
                              type="number"
                              inputProps={{ step: 0.1 }}
                              fullWidth
                              helperText="Zalecane: 2.5-3%"
                            />
                          )}
                        />
                      </Grid>
                      <Grid item xs={12} sm={4}>
                        <Controller
                          name="oilPercentage"
                          control={control}
                          render={({ field }) => (
                            <TextField
                              {...field}
                              label="🫒 Oliwa (%)"
                              type="number"
                              inputProps={{ step: 0.5 }}
                              fullWidth
                              helperText={(selectedStyleData?.defaults?.oilPercentage ?? 0) > 0 ? 
                                `Zalecane: ${selectedStyleData?.defaults?.oilPercentage}%` : 
                                'Opcjonalnie'}
                            />
                          )}
                        />
                      </Grid>
                      <Grid item xs={12} sm={4}>
                        <Controller
                          name="sugarPercentage"
                          control={control}
                          render={({ field }) => (
                            <TextField
                              {...field}
                              label="🍬 Cukier (%)"
                              type="number"
                              inputProps={{ step: 0.5 }}
                              fullWidth
                              helperText="Opcjonalnie, przyspiesza brązowienie"
                            />
                          )}
                        />
                      </Grid>
                    </Grid>
                  </Card>
                </Grid>

                <Grid item xs={12} md={4}>
                  {/* Panel pogodowy */}
                  <Card sx={{ p: 3, mb: 3 }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                      <Typography variant="h6" fontWeight="bold">
                        🌤️ Pogoda
                      </Typography>
                      <Controller
                        name="useWeather"
                        control={control}
                        render={({ field }) => (
                          <Switch {...field} checked={field.value} color="primary" />
                        )}
                      />
                    </Box>
                    
                    {useWeather && (
                      <>
                        <Controller
                          name="cityName"
                          control={control}
                          render={({ field }) => (
                            <TextField
                              {...field}
                              label="Miasto"
                              fullWidth
                              size="small"
                              placeholder="np. Warszawa"
                              sx={{ mb: 2 }}
                            />
                          )}
                        />
                        
                        <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                          <Button 
                            variant="outlined" 
                            size="small" 
                            startIcon={<LocationIcon />}
                            onClick={getLocation}
                          >
                            Moja lokalizacja
                          </Button>
                          <Button 
                            variant="contained" 
                            size="small" 
                            startIcon={loadingWeather ? <CircularProgress size={16} /> : <RefreshIcon />}
                            onClick={fetchWeather}
                            disabled={loadingWeather}
                          >
                            Pobierz
                          </Button>
                        </Box>
                        
                        {weather && (
                          <Box sx={{ bgcolor: 'grey.100', borderRadius: 2, p: 2 }}>
                            <Typography variant="subtitle2" fontWeight="bold">
                              📍 {weather.cityName || 'Twoja lokalizacja'}{weather.country ? `, ${weather.country}` : ''}
                            </Typography>
                            <Typography variant="body2">
                              🌡️ Na zewnątrz: {weather.temperature?.toFixed(1)}°C • {weather.description}
                            </Typography>
                            {weather.indoorTemperature && (
                              <Typography variant="body2" sx={{ color: 'primary.main', fontWeight: 'medium' }}>
                                🏠 W pomieszczeniu: ~{weather.indoorTemperature?.toFixed(0)}°C 
                                {weather.indoorHumidity && ` • 💧 ${weather.indoorHumidity?.toFixed(0)}%`}
                              </Typography>
                            )}
                            <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5 }}>
                              📊 {weather.pressure} hPa
                            </Typography>
                            
                            {weatherAdjustment && weatherAdjustment.recommendations.length > 0 && (
                              <Box sx={{ mt: 2, p: 1.5, bgcolor: 'info.50', borderRadius: 1 }}>
                                <Typography variant="caption" fontWeight="bold" color="info.main">
                                  ✅ Korekta wliczona w wyliczenia:
                                </Typography>
                                {weatherAdjustment.recommendations.slice(0, 3).map((rec, i) => (
                                  <Typography key={i} variant="caption" display="block" sx={{ mt: 0.5 }}>
                                    {rec}
                                  </Typography>
                                ))}
                              </Box>
                            )}
                          </Box>
                        )}
                      </>
                    )}
                  </Card>

                  {/* Wskazówki */}
                  <Card
                    sx={{
                      p: 3,
                      background: `linear-gradient(135deg, ${alpha(theme.palette.primary.main, 0.1)}, ${alpha(theme.palette.secondary.main, 0.1)})`,
                    }}
                  >
                    <Typography variant="h6" fontWeight="bold" gutterBottom>
                      💡 Wskazówka
                    </Typography>
                    <Typography variant="body2" color="text.secondary" paragraph>
                      {selectedStyleData?.description || 'Wybierz styl pizzy aby zobaczyć wskazówki.'}
                    </Typography>
                    <Divider sx={{ my: 2 }} />
                    <Typography variant="subtitle2" fontWeight="bold" gutterBottom>
                      Zalecane parametry:
                    </Typography>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                      <Typography variant="body2">
                        • Hydratacja: {selectedStyleData?.defaults?.hydrationMin ?? '-'}-{selectedStyleData?.defaults?.hydrationMax ?? '-'}%
                      </Typography>
                      <Typography variant="body2">
                        • Fermentacja: {selectedStyleData?.defaults?.fermentationHours ?? '-'}h
                      </Typography>
                      <Typography variant="body2">
                        • Waga kulki: {selectedStyleData?.defaults?.ballWeight ?? '-'}g
                      </Typography>
                    </Box>
                  </Card>
                </Grid>
              </Grid>

              <Box sx={{ mt: 4, display: 'flex', justifyContent: 'space-between' }}>
                <Button onClick={handleBack} size="large">
                  Wstecz
                </Button>
                <Button variant="contained" size="large" onClick={handleNext} sx={{ px: 6 }}>
                  Dalej
                </Button>
              </Box>
            </MotionBox>
          )}

          {/* ========== STEP 3: FERMENTATION ========== */}
          {activeStep === 2 && (
            <MotionBox
              key="step3"
              initial={{ opacity: 0, x: 50 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -50 }}
            >
              <Grid container spacing={4}>
                <Grid item xs={12} md={8}>
                  <Card sx={{ p: 4 }}>
                    <Typography variant="h5" fontWeight="bold" gutterBottom>
                      Fermentacja i wypiek
                    </Typography>

                    <Grid container spacing={3} sx={{ mt: 1 }}>
                      <Grid item xs={12} sm={6}>
                        <Controller
                          name="yeastType"
                          control={control}
                          render={({ field }) => (
                            <TextField {...field} select label="Typ drożdży" fullWidth>
                              {yeastTypes?.map((type: YeastType) => (
                                <MenuItem key={type.id} value={type.id}>
                                  {type.name}
                                </MenuItem>
                              ))}
                            </TextField>
                          )}
                        />
                      </Grid>
                      <Grid item xs={12} sm={6}>
                        <Controller
                          name="fermentationMethod"
                          control={control}
                          render={({ field }) => (
                            <TextField {...field} select label="Metoda fermentacji" fullWidth>
                              {fermentationMethods?.map((method: FermentationMethod) => (
                                <MenuItem key={method.id} value={method.id}>
                                  {method.name}
                                </MenuItem>
                              ))}
                            </TextField>
                          )}
                        />
                      </Grid>

                      <Grid item xs={12}>
                        <Box sx={{ px: 2 }}>
                          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                            <Typography fontWeight="500">Czas fermentacji</Typography>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                              <Chip label={`${fermentationHours}h`} color="secondary" size="small" />
                              {weatherAdjustment && useWeather && (
                                <Chip 
                                  label={`Skorygowany: ${Math.round(fermentationHours * (1 + weatherAdjustment.fermentationTimeAdjustmentPercent / 100))}h`}
                                  color="warning"
                                  size="small"
                                  variant="outlined"
                                />
                              )}
                            </Box>
                          </Box>
                          <Controller
                            name="totalFermentationHours"
                            control={control}
                            render={({ field }) => (
                              <Slider
                                {...field}
                                min={2}
                                max={96}
                                valueLabelDisplay="auto"
                                marks={[
                                  { value: 6, label: '6h' },
                                  { value: 24, label: '24h' },
                                  { value: 48, label: '48h' },
                                  { value: 72, label: '72h' },
                                ]}
                              />
                            )}
                          />
                          <Typography variant="caption" color="text.secondary">
                            Dłuższa fermentacja = więcej smaku i lepsze trawienie
                          </Typography>
                        </Box>
                      </Grid>

                      <Grid item xs={12} sm={6}>
                        <Controller
                          name="roomTemperature"
                          control={control}
                          render={({ field }) => (
                            <TextField
                              {...field}
                              label="Temperatura pokojowa (°C)"
                              type="number"
                              fullWidth
                              InputProps={{
                                startAdornment: <ThermostatIcon sx={{ mr: 1, color: 'warning.main' }} />,
                              }}
                              helperText={weather ? `Aktualna pogoda: ${weather.temperature}°C` : undefined}
                            />
                          )}
                        />
                      </Grid>
                      <Grid item xs={12} sm={6}>
                        <Controller
                          name="fridgeTemperature"
                          control={control}
                          render={({ field }) => (
                            <TextField
                              {...field}
                              label="Temperatura lodówki (°C)"
                              type="number"
                              fullWidth
                              InputProps={{
                                startAdornment: <ThermostatIcon sx={{ mr: 1, color: 'info.main' }} />,
                              }}
                            />
                          )}
                        />
                      </Grid>

                      {/* === TECHNIKI CIASTA === */}
                      <Grid item xs={12}>
                        <Box sx={{ 
                          p: 3, 
                          bgcolor: alpha(theme.palette.info.main, 0.05), 
                          borderRadius: 2,
                          border: '1px solid',
                          borderColor: alpha(theme.palette.info.main, 0.2)
                        }}>
                          <Typography variant="h6" fontWeight="bold" gutterBottom>
                            🥖 Techniki ciasta
                          </Typography>
                          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                            Techniki poprawiające strukturę i smak ciasta
                          </Typography>
                          
                          {/* Autoliza */}
                          <Box sx={{ mb: 3 }}>
                            <FormControlLabel
                              control={
                                <Switch 
                                  checked={useAutolyse} 
                                  onChange={(e) => setUseAutolyse(e.target.checked)}
                                  color="info"
                                />
                              }
                              label={
                                <Box>
                                  <Typography variant="subtitle2" fontWeight="bold">
                                    Autoliza
                                  </Typography>
                                  <Typography variant="caption" color="text.secondary">
                                    Odpoczynek mąki z wodą przed dodaniem drożdży i soli
                                  </Typography>
                                </Box>
                              }
                            />
                            {useAutolyse && (
                              <Box sx={{ mt: 2, ml: 4 }}>
                                <Typography variant="body2" gutterBottom>
                                  Czas autolizy: <strong>{autolyseMinutes} min</strong>
                                </Typography>
                                <Slider
                                  value={autolyseMinutes}
                                  onChange={(_, value) => setAutolyseMinutes(value as number)}
                                  min={20}
                                  max={90}
                                  step={10}
                                  marks={[
                                    { value: 20, label: '20min' },
                                    { value: 45, label: '45min' },
                                    { value: 60, label: '60min' },
                                    { value: 90, label: '90min' },
                                  ]}
                                />
                                <Alert severity="info" sx={{ mt: 1 }}>
                                  💡 Autoliza rozwija gluten bez wyrabiania. Zalecana dla wysokiej hydratacji ({'>'}65%).
                                </Alert>
                              </Box>
                            )}
                          </Box>
                          
                          {/* Stretch and Fold */}
                          <Box>
                            <FormControlLabel
                              control={
                                <Switch 
                                  checked={useStretchAndFold} 
                                  onChange={(e) => setUseStretchAndFold(e.target.checked)}
                                  color="info"
                                />
                              }
                              label={
                                <Box>
                                  <Typography variant="subtitle2" fontWeight="bold">
                                    Stretch & Fold
                                  </Typography>
                                  <Typography variant="caption" color="text.secondary">
                                    Delikatne składanie ciasta zamiast intensywnego wyrabiania
                                  </Typography>
                                </Box>
                              }
                            />
                            {useStretchAndFold && (
                              <Box sx={{ mt: 2, ml: 4 }}>
                                <Grid container spacing={2}>
                                  <Grid item xs={6}>
                                    <Typography variant="body2" gutterBottom>
                                      Liczba serii: <strong>{stretchAndFoldSeries}</strong>
                                    </Typography>
                                    <Slider
                                      value={stretchAndFoldSeries}
                                      onChange={(_, value) => setStretchAndFoldSeries(value as number)}
                                      min={1}
                                      max={6}
                                      step={1}
                                      marks
                                    />
                                  </Grid>
                                  <Grid item xs={6}>
                                    <Typography variant="body2" gutterBottom>
                                      Przerwa: <strong>{stretchAndFoldInterval} min</strong>
                                    </Typography>
                                    <Slider
                                      value={stretchAndFoldInterval}
                                      onChange={(_, value) => setStretchAndFoldInterval(value as number)}
                                      min={15}
                                      max={45}
                                      step={5}
                                      marks={[
                                        { value: 15, label: '15min' },
                                        { value: 30, label: '30min' },
                                        { value: 45, label: '45min' },
                                      ]}
                                    />
                                  </Grid>
                                </Grid>
                                <Alert severity="success" sx={{ mt: 1 }}>
                                  ✅ {stretchAndFoldSeries} serii co {stretchAndFoldInterval} min = {stretchAndFoldSeries * stretchAndFoldInterval} min na rozwój glutenu
                                </Alert>
                              </Box>
                            )}
                          </Box>
                        </Box>
                      </Grid>

                      {/* Typ pieca */}
                      <Grid item xs={12}>
                        <Controller
                          name="ovenType"
                          control={control}
                          render={({ field }) => (
                            <TextField {...field} select label="Typ pieca" fullWidth>
                              {ovens?.map((oven: OvenType) => (
                                <MenuItem key={oven.id} value={oven.id}>
                                  {oven.name} ({oven.temperatureMin}-{oven.temperatureMax}°C)
                                </MenuItem>
                              ))}
                            </TextField>
                          )}
                        />
                      </Grid>

                      <Grid item xs={12}>
                        <Button
                          onClick={() => setShowAdvanced(!showAdvanced)}
                          endIcon={<ExpandMoreIcon sx={{ transform: showAdvanced ? 'rotate(180deg)' : 'none' }} />}
                        >
                          Opcje zaawansowane {!isPremium && '(PREMIUM)'}
                        </Button>
                        <Collapse in={showAdvanced}>
                          <Box sx={{ mt: 2, p: 2, bgcolor: 'grey.100', borderRadius: 2 }}>
                            <Controller
                              name="usePreferment"
                              control={control}
                              render={({ field }) => (
                                <FormControlLabel
                                  control={<Switch {...field} checked={field.value} disabled={!isPremium} />}
                                  label="Użyj prefermentu (poolish/biga)"
                                />
                              )}
                            />
                            {!isPremium && (
                              <Alert severity="info" sx={{ mt: 1 }}>
                                Prefermenty są dostępne tylko dla użytkowników PREMIUM
                              </Alert>
                            )}
                            {usePreferment && isPremium && (
                              <Controller
                                name="prefermentType"
                                control={control}
                                render={({ field }) => (
                                  <TextField {...field} select label="Typ prefermentu" fullWidth sx={{ mt: 2 }}>
                                    {prefermentTypes?.map((type: PrefermentType) => (
                                      <MenuItem key={type.id} value={type.id}>
                                        {type.name}
                                      </MenuItem>
                                    ))}
                                  </TextField>
                                )}
                              />
                            )}
                          </Box>
                        </Collapse>
                      </Grid>

                      <Grid item xs={12}>
                        <Divider sx={{ my: 2 }} />
                        <Controller
                          name="generateSchedule"
                          control={control}
                          render={({ field }) => (
                            <FormControlLabel
                              control={<Switch {...field} checked={field.value} color="primary" />}
                              label="Wygeneruj harmonogram fermentacji"
                            />
                          )}
                        />
                        {isAuthenticated && (
                          <Controller
                            name="saveRecipe"
                            control={control}
                            render={({ field }) => (
                              <FormControlLabel
                                control={<Switch {...field} checked={field.value} color="secondary" />}
                                label="Zapisz recepturę do historii"
                              />
                            )}
                          />
                        )}
                      </Grid>
                    </Grid>
                  </Card>
                </Grid>

                <Grid item xs={12} md={4}>
                  <Card sx={{ p: 3 }}>
                    <Box
                      component="img"
                      src={IMAGES.process.baking}
                      alt="Piec"
                      sx={{ width: '100%', borderRadius: 2, mb: 2 }}
                    />
                    <Typography variant="h6" fontWeight="bold" gutterBottom>
                      🔥 Wskazówki pieczenia
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Dla najlepszych efektów rozgrzej piec na maksymalną temperaturę przez minimum 30-45 minut przed pieczeniem.
                    </Typography>
                  </Card>
                </Grid>
              </Grid>

              <Box sx={{ mt: 4, display: 'flex', justifyContent: 'space-between' }}>
                <Button onClick={handleBack} size="large">
                  Wstecz
                </Button>
                <Button
                  variant="contained"
                  size="large"
                  onClick={handleSubmit(onSubmit)}
                  disabled={calculateMutation.isPending}
                  startIcon={calculateMutation.isPending ? <CircularProgress size={20} /> : <CalculateIcon />}
                  sx={{ px: 6 }}
                >
                  Oblicz recepturę
                </Button>
              </Box>
            </MotionBox>
          )}

          {/* ========== STEP 4: RESULTS ========== */}
          {activeStep === 3 && result && (
            <MotionBox
              key="step4"
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
            >
              {/* Success Banner */}
              <Card
                sx={{
                  mb: 4,
                  background: `linear-gradient(135deg, ${theme.palette.success.main}, ${theme.palette.success.dark})`,
                  color: 'white',
                }}
              >
                <CardContent sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 2 }}>
                  <Box>
                    <Typography variant="h4" fontWeight="bold">
                      🎉 Receptura gotowa!
                    </Typography>
                    <Typography variant="h6" sx={{ opacity: 0.9 }}>
                      {result.pizzaStyleName} • {result.numberOfPizzas} pizz • {result.ingredients.totalDoughWeight}g ciasta
                    </Typography>
                    {weather && useWeather && (
                      <Typography variant="body2" sx={{ opacity: 0.8 }}>
                        🌤️ Dostosowana do pogody: {weather.temperature}°C, {weather.description}
                      </Typography>
                    )}
                  </Box>
                  <Box sx={{ display: 'flex', gap: 1 }}>
                    <Tooltip title="Kopiuj do schowka">
                      <IconButton onClick={copyToClipboard} sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }} aria-label="Kopiuj przepis do schowka">
                        <CopyIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Udostępnij link">
                      <IconButton onClick={shareRecipe} sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }} aria-label="Udostępnij link do przepisu">
                        <ShareIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Eksportuj do PDF">
                      <IconButton onClick={exportToPDF} sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }} aria-label="Eksportuj przepis do PDF">
                        <PrintIcon />
                      </IconButton>
                    </Tooltip>
                  </Box>
                </CardContent>
              </Card>

              <Grid container spacing={4}>
                {/* Ingredients Card */}
                <Grid item xs={12} md={6}>
                  <Card sx={{ height: '100%' }}>
                    <CardContent>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                        <Typography variant="h5" fontWeight="bold" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <KitchenIcon color="primary" /> Składniki
                        </Typography>
                        
                        {/* Skalowanie */}
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Typography variant="body2" color="text.secondary">
                            Skaluj:
                          </Typography>
                          <IconButton 
                            size="small" 
                            onClick={() => setScaledPizzaCount(prev => 
                              Math.max(1, (prev ?? result.numberOfPizzas) - 1)
                            )}
                            aria-label="Zmniejsz liczbę pizz"
                          >
                            <ExpandMoreIcon sx={{ transform: 'rotate(90deg)' }} />
                          </IconButton>
                          <Chip 
                            label={`${scaledPizzaCount ?? result.numberOfPizzas} pizz`}
                            color={scaledIngredients ? 'secondary' : 'default'}
                            onClick={() => setScaledPizzaCount(null)}
                          />
                          <IconButton 
                            size="small" 
                            onClick={() => setScaledPizzaCount(prev => 
                              Math.min(50, (prev ?? result.numberOfPizzas) + 1)
                            )}
                            aria-label="Zwiększ liczbę pizz"
                          >
                            <ExpandMoreIcon sx={{ transform: 'rotate(-90deg)' }} />
                          </IconButton>
                        </Box>
                      </Box>
                      
                      {scaledIngredients && (
                        <Alert severity="info" sx={{ mb: 2 }}>
                          Przeskalowano z {result.numberOfPizzas} na {scaledIngredients.numberOfPizzas} pizz
                          {' • '}
                          <Button size="small" onClick={() => setScaledPizzaCount(null)}>
                            Resetuj
                          </Button>
                        </Alert>
                      )}
                      
                      <Table>
                        <TableBody>
                          {[
                            { 
                              name: 'Mąka', 
                              value: scaledIngredients?.flourGrams ?? result.ingredients.flourGrams, 
                              original: result.ingredients.flourGrams,
                              percent: 100, 
                              icon: '🌾' 
                            },
                            { 
                              name: 'Woda', 
                              value: scaledIngredients?.waterGrams ?? result.ingredients.waterGrams, 
                              original: result.ingredients.waterGrams,
                              percent: result.bakerPercentages.water, 
                              icon: '💧' 
                            },
                            { 
                              name: 'Sól', 
                              value: scaledIngredients?.saltGrams ?? result.ingredients.saltGrams, 
                              original: result.ingredients.saltGrams,
                              percent: result.bakerPercentages.salt, 
                              icon: '🧂' 
                            },
                            { 
                              name: `Drożdże (${result.ingredients.yeastType})`, 
                              value: scaledIngredients?.yeastGrams ?? result.ingredients.yeastGrams, 
                              original: result.ingredients.yeastGrams,
                              percent: result.bakerPercentages.yeast, 
                              icon: '🍞' 
                            },
                            ...(result.ingredients.oilGrams > 0 ? [{ 
                              name: 'Oliwa', 
                              value: scaledIngredients?.oilGrams ?? result.ingredients.oilGrams, 
                              original: result.ingredients.oilGrams,
                              percent: result.bakerPercentages.oil, 
                              icon: '🫒' 
                            }] : []),
                            ...(result.ingredients.sugarGrams > 0 ? [{ 
                              name: 'Cukier', 
                              value: scaledIngredients?.sugarGrams ?? result.ingredients.sugarGrams, 
                              original: result.ingredients.sugarGrams,
                              percent: result.bakerPercentages.sugar, 
                              icon: '🍬' 
                            }] : []),
                          ].map((item) => (
                            <TableRow key={item.name}>
                              <TableCell sx={{ fontSize: '1.1rem' }}>
                                {item.icon} {item.name}
                              </TableCell>
                              <TableCell align="right">
                                <Typography 
                                  variant="h6" 
                                  fontWeight="bold" 
                                  color={scaledIngredients ? 'secondary' : 'primary'}
                                >
                                  {item.value}g
                                </Typography>
                                {scaledIngredients && item.value !== item.original && (
                                  <Typography variant="caption" color="text.secondary" sx={{ textDecoration: 'line-through' }}>
                                    {item.original}g
                                  </Typography>
                                )}
                              </TableCell>
                              <TableCell align="right">
                                <Chip label={`${item.percent}%`} size="small" variant="outlined" />
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                      
                      <Box sx={{ mt: 2, p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
                        <Typography variant="subtitle2" fontWeight="bold">
                          Całkowita waga ciasta: {scaledIngredients?.totalDoughWeight ?? result.ingredients.totalDoughWeight}g
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {scaledPizzaCount ?? result.numberOfPizzas} × {result.ballWeight}g na pizzę
                        </Typography>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>

                {/* Wykres proporcji składników */}
                <Grid item xs={12} md={6}>
                  <Card sx={{ height: '100%' }}>
                    <CardContent>
                      <Typography variant="h5" fontWeight="bold" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        📊 Proporcje (Baker's %)
                      </Typography>
                      <Box sx={{ maxWidth: 300, mx: 'auto', mt: 2 }}>
                        <Doughnut
                          data={{
                            labels: [
                              'Mąka (100%)',
                              `Woda (${result.bakerPercentages.water}%)`,
                              `Sól (${result.bakerPercentages.salt}%)`,
                              `Drożdże (${result.bakerPercentages.yeast}%)`,
                              ...(result.bakerPercentages.oil > 0 ? [`Oliwa (${result.bakerPercentages.oil}%)`] : []),
                              ...(result.bakerPercentages.sugar > 0 ? [`Cukier (${result.bakerPercentages.sugar}%)`] : []),
                            ],
                            datasets: [{
                              data: [
                                100,
                                result.bakerPercentages.water,
                                result.bakerPercentages.salt,
                                result.bakerPercentages.yeast,
                                ...(result.bakerPercentages.oil > 0 ? [result.bakerPercentages.oil] : []),
                                ...(result.bakerPercentages.sugar > 0 ? [result.bakerPercentages.sugar] : []),
                              ],
                              backgroundColor: [
                                '#f5d0a9', // mąka - beżowy
                                '#a8d8ea', // woda - niebieski
                                '#f8f8f8', // sól - biały
                                '#ffeaa7', // drożdże - żółty
                                '#b8e994', // oliwa - zielony
                                '#fd79a8', // cukier - różowy
                              ],
                              borderWidth: 2,
                              borderColor: '#fff',
                            }],
                          }}
                          options={{
                            responsive: true,
                            plugins: {
                              legend: {
                                position: 'bottom',
                                labels: {
                                  padding: 15,
                                  usePointStyle: true,
                                },
                              },
                              tooltip: {
                                callbacks: {
                                  label: (context) => {
                                    const label = context.label || '';
                                    return label;
                                  },
                                },
                              },
                            },
                            cutout: '50%',
                          }}
                        />
                      </Box>
                      <Box sx={{ mt: 3, textAlign: 'center' }}>
                        <Typography variant="body2" color="text.secondary">
                          Suma: {(100 + result.bakerPercentages.water + result.bakerPercentages.salt + 
                                  result.bakerPercentages.yeast + result.bakerPercentages.oil + 
                                  result.bakerPercentages.sugar).toFixed(1)}% względem mąki
                        </Typography>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>

                {/* Oven Card */}
                <Grid item xs={12} md={6}>
                  <Card sx={{ height: '100%' }}>
                    <CardMedia
                      component="img"
                      height="200"
                      image={IMAGES.ovens.woodFired}
                      alt="Piec"
                    />
                    <CardContent>
                      <Typography variant="h5" fontWeight="bold" gutterBottom>
                        🔥 {result.ovenInfo.ovenName}
                      </Typography>
                      <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
                        <Chip
                          icon={<ThermostatIcon />}
                          label={`${result.ovenInfo.temperature}°C`}
                          color="error"
                          sx={{ fontSize: '1rem', py: 2 }}
                        />
                        <Chip
                          icon={<ScheduleIcon />}
                          label={`${Math.round(result.ovenInfo.bakingTimeSeconds / 60)} min`}
                          sx={{ fontSize: '1rem', py: 2 }}
                        />
                      </Box>
                      <Typography color="text.secondary">
                        {result.ovenInfo.tips}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>

                {/* Weather Adjustment Info */}
                {weatherAdjustment && useWeather && (
                  <Grid item xs={12}>
                    <Card sx={{ bgcolor: alpha(theme.palette.info.main, 0.05) }}>
                      <CardContent>
                        <Typography variant="h5" fontWeight="bold" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <CloudIcon color="info" /> Korekta pogodowa
                        </Typography>
                        <Grid container spacing={2}>
                          {weatherAdjustment.recommendations.map((rec, index) => (
                            <Grid item xs={12} md={6} key={index}>
                              <Alert severity="info" icon={false}>
                                {rec}
                              </Alert>
                            </Grid>
                          ))}
                        </Grid>
                      </CardContent>
                    </Card>
                  </Grid>
                )}

                {/* Schedule */}
                {result.schedule && result.schedule.length > 0 && (
                  <Grid item xs={12}>
                    <Card>
                      <CardContent>
                        <Typography variant="h5" fontWeight="bold" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <ScheduleIcon color="primary" /> Harmonogram fermentacji
                        </Typography>
                        <Stepper orientation="vertical">
                          {result.schedule.map((step, index) => (
                            <Step key={index} active>
                              <StepLabel
                                StepIconComponent={() => (
                                  <Box
                                    sx={{
                                      width: 32,
                                      height: 32,
                                      borderRadius: '50%',
                                      bgcolor: 'primary.main',
                                      color: 'white',
                                      display: 'flex',
                                      alignItems: 'center',
                                      justifyContent: 'center',
                                      fontWeight: 'bold',
                                    }}
                                  >
                                    {index + 1}
                                  </Box>
                                )}
                              >
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                                  <Typography variant="subtitle1" fontWeight="bold">
                                    {step.title}
                                  </Typography>
                                  <Chip label={step.relativeTime} size="small" color="secondary" />
                                </Box>
                              </StepLabel>
                              <StepContent>
                                <Typography color="text.secondary">{step.description}</Typography>
                                {step.temperature && (
                                  <Chip label={`${step.temperature}°C`} size="small" sx={{ mt: 1 }} />
                                )}
                              </StepContent>
                            </Step>
                          ))}
                        </Stepper>
                      </CardContent>
                    </Card>
                  </Grid>
                )}

                {/* Tips */}
                {result.tips && result.tips.length > 0 && (
                  <Grid item xs={12}>
                    <Card sx={{ bgcolor: alpha(theme.palette.info.main, 0.05) }}>
                      <CardContent>
                        <Typography variant="h5" fontWeight="bold" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <InfoIcon color="info" /> Wskazówki eksperta
                        </Typography>
                        {result.tips.map((tip, index) => (
                          <Alert severity="info" key={index} sx={{ mb: 1 }} icon={false}>
                            💡 {tip}
                          </Alert>
                        ))}
                      </CardContent>
                    </Card>
                  </Grid>
                )}
              </Grid>

              <Box sx={{ mt: 4, display: 'flex', justifyContent: 'center', gap: 2 }}>
                <Button variant="outlined" size="large" onClick={handleReset}>
                  Nowa kalkulacja
                </Button>
                {isAuthenticated && (
                  <Button variant="contained" size="large" startIcon={<SaveIcon />}>
                    Zapisz do receptur
                  </Button>
                )}
              </Box>
            </MotionBox>
          )}
        </AnimatePresence>

        {!isAuthenticated && activeStep < 3 && (
          <Alert
            severity="info"
            sx={{ mt: 4, borderRadius: 2 }}
            action={
              <Button color="inherit" size="small" href="/register">
                Zarejestruj się
              </Button>
            }
          >
            <strong>💡 Wskazówka:</strong> Zaloguj się, aby zapisywać receptury i otrzymywać powiadomienia SMS o kolejnych krokach.
          </Alert>
        )}
      </Container>
    </Box>
  );
};

export default CalculatorPage;
