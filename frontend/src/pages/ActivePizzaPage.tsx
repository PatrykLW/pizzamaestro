import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  Grid,
  Button,
  IconButton,
  Chip,
  LinearProgress,
  Divider,
  Alert,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  Paper,
  Tooltip,
  Badge,
} from '@mui/material';
import {
  PlayArrow,
  Pause,
  Stop,
  CheckCircle,
  SkipNext,
  Schedule,
  Add,
  Remove,
  Notifications,
  NotificationsOff,
  AccessTime,
  LocalPizza,
  Timer,
  Refresh,
  Edit,
  Close,
  Warning,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { activePizzaApi, ActivePizzaResponse, ScheduledStepResponse } from '../services/api';
import { useAuthStore } from '../store/authStore';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { format, formatDistanceToNow, parseISO, differenceInMinutes } from 'date-fns';
import { usePizzaTimer, formatTimeDistance } from '../hooks/usePizzaTimer';
import { pl } from 'date-fns/locale';

const ActivePizzaPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { isAuthenticated, user } = useAuthStore();
  const [rescheduleDialogOpen, setRescheduleDialogOpen] = useState(false);
  const [newBakeTime, setNewBakeTime] = useState('');
  const [currentTime, setCurrentTime] = useState(new Date());

  // Aktualizuj czas co sekundę
  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  // Pobierz aktywną pizzę
  const { data: activePizza, isLoading, error, refetch } = useQuery({
    queryKey: ['activePizza'],
    queryFn: activePizzaApi.getCurrent,
    enabled: isAuthenticated,
    refetchInterval: 30000, // Odświeżaj co 30 sekund
  });

  // Timer z powiadomieniami przeglądarkowym
  const {
    formattedTimeToNextStep,
    notificationPermission,
    requestNotificationPermission,
    isOverdue,
  } = usePizzaTimer({
    steps: activePizza?.steps || [],
    reminderMinutesBefore: activePizza?.reminderMinutesBefore || 15,
    enabled: !!activePizza && activePizza.status === 'IN_PROGRESS',
    onStepDue: (step) => {
      toast.success(`Czas na: ${step.title}!`);
    },
    onStepReminder: (step, mins) => {
      toast(`Za ${mins} min: ${step.title}`, { icon: '⏰' });
    },
  });

  // Mutacje
  const startMutation = useMutation({
    mutationFn: (id: string) => activePizzaApi.start(id),
    onSuccess: () => {
      toast.success('Rozpoczęto pizzę!');
      queryClient.invalidateQueries({ queryKey: ['activePizza'] });
    },
  });

  const pauseMutation = useMutation({
    mutationFn: (id: string) => activePizzaApi.pause(id),
    onSuccess: () => {
      toast.success('Wstrzymano pizzę');
      queryClient.invalidateQueries({ queryKey: ['activePizza'] });
    },
  });

  const resumeMutation = useMutation({
    mutationFn: (id: string) => activePizzaApi.resume(id),
    onSuccess: () => {
      toast.success('Wznowiono pizzę');
      queryClient.invalidateQueries({ queryKey: ['activePizza'] });
    },
  });

  const cancelMutation = useMutation({
    mutationFn: (id: string) => activePizzaApi.cancel(id),
    onSuccess: () => {
      toast.success('Anulowano pizzę');
      queryClient.invalidateQueries({ queryKey: ['activePizza'] });
    },
  });

  const completeStepMutation = useMutation({
    mutationFn: ({ id, stepNumber }: { id: string; stepNumber: number }) => 
      activePizzaApi.completeStep(id, stepNumber),
    onSuccess: () => {
      toast.success('Krok ukończony!');
      queryClient.invalidateQueries({ queryKey: ['activePizza'] });
    },
  });

  const skipStepMutation = useMutation({
    mutationFn: ({ id, stepNumber }: { id: string; stepNumber: number }) =>
      activePizzaApi.skipStep(id, stepNumber),
    onSuccess: () => {
      toast.success('Krok pominięty');
      queryClient.invalidateQueries({ queryKey: ['activePizza'] });
    },
  });

  const rescheduleMutation = useMutation({
    mutationFn: ({ id, minutes }: { id: string; minutes: number }) =>
      activePizzaApi.rescheduleByMinutes(id, minutes),
    onSuccess: () => {
      toast.success('Harmonogram przesunięty');
      queryClient.invalidateQueries({ queryKey: ['activePizza'] });
    },
  });

  if (!isAuthenticated) {
    return (
      <Container maxWidth="md" sx={{ py: 4, textAlign: 'center' }}>
        <Alert severity="info">
          Zaloguj się, aby zarządzać aktywną pizzą
        </Alert>
        <Button onClick={() => navigate('/login')} sx={{ mt: 2 }}>
          Zaloguj się
        </Button>
      </Container>
    );
  }

  if (isLoading) {
    return (
      <Container maxWidth="md" sx={{ py: 4, textAlign: 'center' }}>
        <CircularProgress />
        <Typography sx={{ mt: 2 }}>Ładowanie...</Typography>
      </Container>
    );
  }

  if (!activePizza) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Card sx={{ textAlign: 'center', py: 6 }}>
          <LocalPizza sx={{ fontSize: 80, color: 'text.disabled', mb: 2 }} />
          <Typography variant="h5" gutterBottom>
            Nie masz aktywnej pizzy
          </Typography>
          <Typography color="text.secondary" sx={{ mb: 3 }}>
            Przejdź do kalkulatora i rozpocznij nową pizzę
          </Typography>
          <Button 
            variant="contained" 
            size="large"
            onClick={() => navigate('/calculator')}
          >
            Przejdź do kalkulatora
          </Button>
        </Card>
      </Container>
    );
  }

  const getStepStatus = (step: ScheduledStepResponse) => {
    if (step.status === 'COMPLETED' || step.status === 'COMPLETED_EARLY' || step.status === 'COMPLETED_LATE') {
      return 'completed';
    }
    if (step.status === 'IN_PROGRESS') {
      return 'active';
    }
    if (step.status === 'SKIPPED') {
      return 'skipped';
    }
    return 'pending';
  };

  const getTimeDisplay = (step: ScheduledStepResponse) => {
    if (!step.scheduledTime) return '';
    
    const scheduled = parseISO(step.scheduledTime);
    const diff = differenceInMinutes(scheduled, currentTime);
    
    if (diff > 0) {
      return `za ${formatDistanceToNow(scheduled, { locale: pl })}`;
    } else if (diff > -5) {
      return 'teraz!';
    } else {
      return `${Math.abs(diff)} min temu`;
    }
  };

  const getStepColor = (step: ScheduledStepResponse) => {
    const status = getStepStatus(step);
    if (status === 'completed') return 'success';
    if (status === 'active') return 'primary';
    if (status === 'skipped') return 'default';
    
    // Sprawdź czy jest przeterminowany
    if (step.scheduledTime) {
      const diff = differenceInMinutes(parseISO(step.scheduledTime), currentTime);
      if (diff < -10) return 'error';
      if (diff < 0) return 'warning';
    }
    return 'default';
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Nagłówek */}
      <Card sx={{ mb: 3, bgcolor: 'primary.main', color: 'white' }}>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 2 }}>
            <Box>
              <Typography variant="h4" fontWeight="bold">
                {activePizza.name}
              </Typography>
              <Box sx={{ display: 'flex', gap: 1, mt: 1, flexWrap: 'wrap' }}>
                <Chip 
                  label={activePizza.pizzaStyleName || activePizza.pizzaStyle} 
                  size="small" 
                  sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }}
                />
                <Chip 
                  label={activePizza.statusName} 
                  size="small" 
                  color={activePizza.status === 'IN_PROGRESS' ? 'success' : 'default'}
                  sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }}
                />
                {activePizza.numberOfPizzas && (
                  <Chip 
                    label={`${activePizza.numberOfPizzas} pizz`} 
                    size="small" 
                    sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'white' }}
                  />
                )}
              </Box>
            </Box>
            
            <Box sx={{ textAlign: 'right' }}>
              <Typography variant="h6">
                Cel: {format(parseISO(activePizza.adjustedBakeTime), 'HH:mm', { locale: pl })}
              </Typography>
              <Typography variant="body2" sx={{ opacity: 0.8 }}>
                {format(parseISO(activePizza.adjustedBakeTime), 'EEEE, d MMMM', { locale: pl })}
              </Typography>
            </Box>
          </Box>
          
          {/* Progress bar */}
          <Box sx={{ mt: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
              <Typography variant="body2">Postęp</Typography>
              <Typography variant="body2">{activePizza.completionPercentage}%</Typography>
            </Box>
            <LinearProgress 
              variant="determinate" 
              value={activePizza.completionPercentage} 
              sx={{ height: 8, borderRadius: 4, bgcolor: 'rgba(255,255,255,0.3)' }}
            />
          </Box>
        </CardContent>
      </Card>

      <Grid container spacing={3}>
        {/* Główna kolumna - harmonogram */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6">Harmonogram</Typography>
                <Box>
                  <Tooltip title="Przesuń harmonogram -30 min">
                    <IconButton 
                      size="small"
                      onClick={() => rescheduleMutation.mutate({ id: activePizza.id, minutes: -30 })}
                    >
                      <Remove />
                    </IconButton>
                  </Tooltip>
                  <Chip 
                    icon={<Schedule />}
                    label={format(parseISO(activePizza.adjustedBakeTime), 'HH:mm')}
                    sx={{ mx: 1 }}
                  />
                  <Tooltip title="Przesuń harmonogram +30 min">
                    <IconButton 
                      size="small"
                      onClick={() => rescheduleMutation.mutate({ id: activePizza.id, minutes: 30 })}
                    >
                      <Add />
                    </IconButton>
                  </Tooltip>
                </Box>
              </Box>

              <Stepper orientation="vertical">
                {activePizza.steps.map((step, index) => {
                  const status = getStepStatus(step);
                  const color = getStepColor(step);
                  
                  return (
                    <Step key={step.stepNumber} active={status === 'active'} completed={status === 'completed'}>
                      <StepLabel
                        optional={
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
                            {step.scheduledTime && (
                              <Typography variant="caption" color="text.secondary">
                                {format(parseISO(step.scheduledTime), 'HH:mm')}
                              </Typography>
                            )}
                            <Chip 
                              label={getTimeDisplay(step)}
                              size="small"
                              color={color as any}
                              variant={status === 'active' ? 'filled' : 'outlined'}
                            />
                          </Box>
                        }
                        StepIconComponent={() => (
                          <Box
                            sx={{
                              width: 32,
                              height: 32,
                              borderRadius: '50%',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              bgcolor: status === 'completed' ? 'success.main' : 
                                       status === 'active' ? 'primary.main' : 
                                       status === 'skipped' ? 'grey.400' : 'grey.200',
                              color: status !== 'pending' ? 'white' : 'text.secondary',
                            }}
                          >
                            {status === 'completed' ? <CheckCircle fontSize="small" /> : step.stepNumber}
                          </Box>
                        )}
                      >
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Typography fontWeight={status === 'active' ? 'bold' : 'normal'}>
                            {step.title}
                          </Typography>
                          <Chip 
                            label={step.typeName}
                            size="small"
                            variant="outlined"
                          />
                        </Box>
                      </StepLabel>
                      <StepContent>
                        {step.description && (
                          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                            {step.description}
                          </Typography>
                        )}
                        {step.durationMinutes && (
                          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 2 }}>
                            Czas trwania: ~{step.durationMinutes} min
                          </Typography>
                        )}
                        {(status === 'pending' || status === 'active') && (
                          <Box sx={{ display: 'flex', gap: 1 }}>
                            <Button
                              variant="contained"
                              size="small"
                              startIcon={<CheckCircle />}
                              onClick={() => completeStepMutation.mutate({ id: activePizza.id, stepNumber: step.stepNumber })}
                              disabled={completeStepMutation.isPending}
                            >
                              Zrobione
                            </Button>
                            <Button
                              variant="outlined"
                              size="small"
                              startIcon={<SkipNext />}
                              onClick={() => skipStepMutation.mutate({ id: activePizza.id, stepNumber: step.stepNumber })}
                              disabled={skipStepMutation.isPending}
                            >
                              Pomiń
                            </Button>
                          </Box>
                        )}
                      </StepContent>
                    </Step>
                  );
                })}
              </Stepper>
            </CardContent>
          </Card>
        </Grid>

        {/* Boczna kolumna - kontrolki */}
        <Grid item xs={12} md={4}>
          {/* Timer do następnego kroku */}
          {activePizza.nextStep && (
            <Card sx={{ mb: 3, bgcolor: 'warning.light' }}>
              <CardContent sx={{ textAlign: 'center' }}>
                <Timer sx={{ fontSize: 40, mb: 1 }} />
                <Typography variant="h6" gutterBottom>
                  Następny krok
                </Typography>
                <Typography variant="h4" fontWeight="bold">
                  {activePizza.nextStep.title}
                </Typography>
                <Typography 
                  variant="h3" 
                  sx={{ 
                    mt: 1, 
                    fontFamily: 'monospace',
                    color: isOverdue ? 'error.main' : 'inherit',
                  }}
                >
                  {formattedTimeToNextStep}
                </Typography>
                {isOverdue && (
                  <Typography variant="body2" color="error">
                    Krok jest opóźniony!
                  </Typography>
                )}
              </CardContent>
            </Card>
          )}

          {/* Kontrolki statusu */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>Kontrola</Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                {activePizza.status === 'PLANNING' && (
                  <Button
                    variant="contained"
                    color="success"
                    fullWidth
                    startIcon={<PlayArrow />}
                    onClick={() => startMutation.mutate(activePizza.id)}
                    disabled={startMutation.isPending}
                  >
                    Rozpocznij
                  </Button>
                )}
                
                {activePizza.status === 'IN_PROGRESS' && (
                  <Button
                    variant="outlined"
                    fullWidth
                    startIcon={<Pause />}
                    onClick={() => pauseMutation.mutate(activePizza.id)}
                    disabled={pauseMutation.isPending}
                  >
                    Wstrzymaj
                  </Button>
                )}
                
                {activePizza.status === 'PAUSED' && (
                  <Button
                    variant="contained"
                    color="success"
                    fullWidth
                    startIcon={<PlayArrow />}
                    onClick={() => resumeMutation.mutate(activePizza.id)}
                    disabled={resumeMutation.isPending}
                  >
                    Wznów
                  </Button>
                )}
                
                {(activePizza.status === 'PLANNING' || activePizza.status === 'IN_PROGRESS' || activePizza.status === 'PAUSED') && (
                  <Button
                    variant="outlined"
                    color="error"
                    fullWidth
                    startIcon={<Stop />}
                    onClick={() => {
                      if (window.confirm('Czy na pewno chcesz anulować tę pizzę?')) {
                        cancelMutation.mutate(activePizza.id);
                      }
                    }}
                    disabled={cancelMutation.isPending}
                  >
                    Anuluj
                  </Button>
                )}
              </Box>
            </CardContent>
          </Card>

          {/* Powiadomienia przeglądarkowe */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Powiadomienia w przeglądarce
              </Typography>
              {notificationPermission === 'granted' ? (
                <Alert severity="success">
                  Powiadomienia przeglądarkowe włączone. Otrzymasz alert gdy nadejdzie czas na kolejny krok.
                </Alert>
              ) : notificationPermission === 'denied' ? (
                <Alert severity="warning">
                  Powiadomienia zablokowane. Odblokuj je w ustawieniach przeglądarki.
                </Alert>
              ) : notificationPermission === 'unsupported' ? (
                <Alert severity="info">
                  Twoja przeglądarka nie obsługuje powiadomień.
                </Alert>
              ) : (
                <Box>
                  <Alert severity="info" sx={{ mb: 2 }}>
                    Włącz powiadomienia, aby otrzymywać przypomnienia nawet gdy karta jest w tle.
                  </Alert>
                  <Button
                    variant="contained"
                    startIcon={<Notifications />}
                    onClick={requestNotificationPermission}
                  >
                    Włącz powiadomienia
                  </Button>
                </Box>
              )}
            </CardContent>
          </Card>

          {/* Powiadomienia SMS */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Powiadomienia SMS
              </Typography>
              {activePizza.smsNotificationsEnabled ? (
                <Box>
                  <Alert severity="success" sx={{ mb: 2 }}>
                    Powiadomienia włączone na: {activePizza.notificationPhone}
                  </Alert>
                  <Typography variant="body2" color="text.secondary">
                    Przypomnienie: {activePizza.reminderMinutesBefore} min przed krokiem
                  </Typography>
                </Box>
              ) : (
                <Box>
                  <Alert severity="info" sx={{ mb: 2 }}>
                    Włącz powiadomienia SMS, aby otrzymywać przypomnienia o krokach
                  </Alert>
                  <Typography variant="body2" color="text.secondary">
                    Funkcja wymaga weryfikacji numeru telefonu w profilu
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default ActivePizzaPage;
