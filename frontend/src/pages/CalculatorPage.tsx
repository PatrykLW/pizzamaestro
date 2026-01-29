import React, { useState, useEffect, useCallback } from 'react';
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
import { calculatorApi, ingredientsApi, weatherApi, userApi, CalculationRequest, CalculationResponse, WeatherData, FermentationAdjustment } from '../services/api';
import { useAuthStore } from '../store/authStore';
import { IMAGES, PIZZA_STYLE_IMAGES } from '../constants/images';
import { motion, AnimatePresence as FramerAnimatePresence } from 'framer-motion';

// Wrapper to fix TypeScript compatibility issue with framer-motion
const AnimatePresence = FramerAnimatePresence as React.FC<React.PropsWithChildren<{ mode?: 'wait' | 'sync' | 'popLayout'; initial?: boolean }>>;
import toast from 'react-hot-toast';

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
            console.error('Błąd pobierania pogody:', error);
            toast.error('Nie udało się pobrać pogody');
          } finally {
            setLoadingWeather(false);
          }
        },
        (error) => {
          console.error('Błąd geolokalizacji:', error);
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
      console.error('Błąd pobierania pogody:', error);
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
          console.warn('Nie udało się pobrać pogody z domyślnej lokalizacji');
        }
      }

      setEquipmentLoaded(true);
      
      if (loadedAny) {
        toast.success('Załadowano domyślny sprzęt z profilu');
      } else {
        toast.info('Brak zapisanego domyślnego sprzętu w profilu');
      }
    } catch (error) {
      console.error('Błąd ładowania sprzętu:', error);
      toast.error('Nie udało się załadować domyślnego sprzętu');
    } finally {
      setLoadingEquipment(false);
    }
  }, [isAuthenticated, user, setValue]);

  // Automatycznie dobierz metodę fermentacji na podstawie czasu
  useEffect(() => {
    if (!fermentationHours) return;
    
    const roomTemp = watch('roomTemperature') || 22;
    
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
  }, [fermentationHours, setValue, watch]);

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

  if (stylesLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress size={60} />
      </Box>
    );
  }

  const selectedStyleData = styles?.find((s: any) => s.id === selectedStyle);
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
                {styles?.map((style: any) => (
                  <Grid item xs={12} sm={6} md={4} key={style.id}>
                    <MotionCard
                      whileHover={{ y: -8, boxShadow: '0 12px 40px rgba(0,0,0,0.15)' }}
                      onClick={() => setValue('pizzaStyle', style.id)}
                      sx={{
                        cursor: 'pointer',
                        height: '100%',
                        border: selectedStyle === style.id ? 3 : 1,
                        borderColor: selectedStyle === style.id ? 'primary.main' : 'divider',
                        overflow: 'hidden',
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
                            label={`${style.defaults.hydration}%`}
                            color="primary"
                            variant="outlined"
                          />
                          <Chip
                            size="small"
                            icon={<ScheduleIcon />}
                            label={`${style.defaults.fermentationHours}h`}
                            variant="outlined"
                          />
                          <Chip
                            size="small"
                            icon={<ThermostatIcon />}
                            label={`${style.recommendedOven.temperature}°C`}
                            color="error"
                            variant="outlined"
                          />
                        </Box>
                      </CardContent>
                    </MotionCard>
                  </Grid>
                ))}
              </Grid>

              <Box sx={{ mt: 4, display: 'flex', justifyContent: 'flex-end' }}>
                <Button
                  variant="contained"
                  size="large"
                  onClick={handleNext}
                  sx={{ px: 4 }}
                  startIcon={<CalculateIcon />}
                >
                  Przejdź do kalkulatora {selectedStyleData?.name || 'pizzy'}
                </Button>
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
                      {/* Wybór mąki */}
                      <Grid item xs={12}>
                        <Controller
                          name="selectedFlourId"
                          control={control}
                          render={({ field }) => (
                            <Autocomplete
                              options={flours || []}
                              getOptionLabel={(option: any) => `${option.name} (${option.brand}) - ${option.flourParameters?.proteinContent || '?'}% białka`}
                              value={flours?.find((f: any) => f.id === field.value) || null}
                              onChange={(_, newValue) => field.onChange(newValue?.id || '')}
                              renderInput={(params) => (
                                <TextField
                                  {...params}
                                  label="🌾 Mąka (opcjonalnie)"
                                  helperText="Wybierz mąkę lub zostaw puste dla domyślnej"
                                />
                              )}
                            />
                          )}
                        />
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
                              {yeastTypes?.map((type: any) => (
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
                              {fermentationMethods?.map((method: any) => (
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

                      <Grid item xs={12}>
                        <Controller
                          name="ovenType"
                          control={control}
                          render={({ field }) => (
                            <TextField {...field} select label="Typ pieca" fullWidth>
                              {ovens?.map((oven: any) => (
                                <MenuItem key={oven.id} value={oven.id}>
                                  {oven.name} ({oven.minTemperature}-{oven.maxTemperature}°C)
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
                                    {prefermentTypes?.map((type: any) => (
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
                    <IconButton onClick={copyToClipboard} sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }}>
                      <CopyIcon />
                    </IconButton>
                    <IconButton sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }}>
                      <ShareIcon />
                    </IconButton>
                    <IconButton sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }}>
                      <PrintIcon />
                    </IconButton>
                  </Box>
                </CardContent>
              </Card>

              <Grid container spacing={4}>
                {/* Ingredients Card */}
                <Grid item xs={12} md={6}>
                  <Card sx={{ height: '100%' }}>
                    <CardContent>
                      <Typography variant="h5" fontWeight="bold" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <KitchenIcon color="primary" /> Składniki
                      </Typography>
                      <Table>
                        <TableBody>
                          {[
                            { name: 'Mąka', value: result.ingredients.flourGrams, percent: 100, icon: '🌾' },
                            { name: 'Woda', value: result.ingredients.waterGrams, percent: result.bakerPercentages.water, icon: '💧' },
                            { name: 'Sól', value: result.ingredients.saltGrams, percent: result.bakerPercentages.salt, icon: '🧂' },
                            { name: `Drożdże (${result.ingredients.yeastType})`, value: result.ingredients.yeastGrams, percent: result.bakerPercentages.yeast, icon: '🍞' },
                            ...(result.ingredients.oilGrams > 0 ? [{ name: 'Oliwa', value: result.ingredients.oilGrams, percent: result.bakerPercentages.oil, icon: '🫒' }] : []),
                            ...(result.ingredients.sugarGrams > 0 ? [{ name: 'Cukier', value: result.ingredients.sugarGrams, percent: result.bakerPercentages.sugar, icon: '🍬' }] : []),
                          ].map((item) => (
                            <TableRow key={item.name}>
                              <TableCell sx={{ fontSize: '1.1rem' }}>
                                {item.icon} {item.name}
                              </TableCell>
                              <TableCell align="right">
                                <Typography variant="h6" fontWeight="bold" color="primary">
                                  {item.value}g
                                </Typography>
                              </TableCell>
                              <TableCell align="right">
                                <Chip label={`${item.percent}%`} size="small" variant="outlined" />
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
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
