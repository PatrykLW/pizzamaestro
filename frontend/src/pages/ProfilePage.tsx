import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  Grid,
  Avatar,
  Chip,
  LinearProgress,
  Divider,
  Skeleton,
  CircularProgress,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Slider,
  Tabs,
  Tab,
  Alert,
  IconButton,
  Tooltip,
  Switch,
  FormControlLabel,
  Autocomplete,
  Checkbox,
} from '@mui/material';
import {
  Calculate,
  LocalPizza,
  Star,
  Kitchen,
  Thermostat,
  Notifications,
  Edit,
  Save,
  Close,
  LocalFireDepartment,
  Blender,
  WaterDrop,
  Place,
} from '@mui/icons-material';
import { useAuthStore } from '../store/authStore';
import { userApi, ingredientsApi, calculatorApi, UpdateEquipmentRequest, UpdateEnvironmentRequest, UpdateNotificationsRequest } from '../services/api';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div role="tabpanel" hidden={value !== index} {...other}>
      {value === index && <Box sx={{ py: 3 }}>{children}</Box>}
    </div>
  );
}

const ProfilePage: React.FC = () => {
  const { user, isLoading, refreshAuth } = useAuthStore();
  const queryClient = useQueryClient();
  const [settingsTab, setSettingsTab] = useState(0);
  const [editingEquipment, setEditingEquipment] = useState(false);
  const [editingEnvironment, setEditingEnvironment] = useState(false);
  const [editingNotifications, setEditingNotifications] = useState(false);
  
  // Form state for equipment
  const [equipmentForm, setEquipmentForm] = useState<UpdateEquipmentRequest>({});
  const [environmentForm, setEnvironmentForm] = useState<UpdateEnvironmentRequest>({});
  const [notificationsForm, setNotificationsForm] = useState<UpdateNotificationsRequest>({});

  // Fetch equipment data
  const { data: equipment, isLoading: equipmentLoading } = useQuery({
    queryKey: ['equipment'],
    queryFn: userApi.getEquipment,
    enabled: !!user,
  });

  // Fetch ovens and mixers for dropdowns
  const { data: ovens } = useQuery({
    queryKey: ['ovens'],
    queryFn: calculatorApi.getOvens,
  });
  
  // Fetch flours for multi-select
  const { data: flours } = useQuery({
    queryKey: ['flours'],
    queryFn: ingredientsApi.getFlours,
  });
  
  const { data: waters } = useQuery({
    queryKey: ['waters'],
    queryFn: ingredientsApi.getWaters,
  });

  // Mutations
  const updateEquipmentMutation = useMutation({
    mutationFn: userApi.updateEquipment,
    onSuccess: () => {
      toast.success('Sprzęt zaktualizowany');
      queryClient.invalidateQueries({ queryKey: ['equipment'] });
      setEditingEquipment(false);
      refreshAuth();
    },
    onError: () => toast.error('Błąd aktualizacji sprzętu'),
  });

  const updateEnvironmentMutation = useMutation({
    mutationFn: userApi.updateEnvironment,
    onSuccess: () => {
      toast.success('Warunki środowiskowe zaktualizowane');
      queryClient.invalidateQueries({ queryKey: ['equipment'] });
      setEditingEnvironment(false);
      refreshAuth();
    },
    onError: () => toast.error('Błąd aktualizacji warunków'),
  });

  const updateNotificationsMutation = useMutation({
    mutationFn: userApi.updateNotifications,
    onSuccess: () => {
      toast.success('Ustawienia powiadomień zaktualizowane');
      setEditingNotifications(false);
      refreshAuth();
    },
    onError: () => toast.error('Błąd aktualizacji powiadomień'),
  });

  // Initialize forms when data loads
  useEffect(() => {
    if (user?.preferences) {
      setEquipmentForm({
        defaultOvenType: user.preferences.defaultOvenType || undefined,
        defaultMixerType: user.preferences.defaultMixerType || undefined,
        mixerWattage: user.preferences.mixerWattage || undefined,
        availableFlourIds: user.preferences.availableFlourIds || [],
        defaultWaterId: user.preferences.defaultWaterId || undefined,
      });
      setEnvironmentForm({
        typicalRoomTemperature: user.preferences.typicalRoomTemperature ?? 22,
        typicalFridgeTemperature: user.preferences.typicalFridgeTemperature ?? 4,
        defaultCity: user.preferences.defaultCity || undefined,
        defaultLatitude: user.preferences.defaultLatitude || undefined,
        defaultLongitude: user.preferences.defaultLongitude || undefined,
      });
      setNotificationsForm({
        emailNotifications: user.preferences.emailNotifications,
        smsNotifications: user.preferences.smsNotifications,
        pushNotifications: user.preferences.pushNotifications,
        smsReminderMinutesBefore: user.preferences.smsReminderMinutesBefore ?? 15,
      });
    }
  }, [user?.preferences]);

  // Mixer types
  const mixerTypes = [
    { value: 'HAND_KNEADING', label: 'Ręczne wyrabianie' },
    { value: 'STAND_MIXER_HOME', label: 'Mikser planetarny domowy' },
    { value: 'STAND_MIXER_PRO', label: 'Mikser planetarny profesjonalny' },
    { value: 'SPIRAL_MIXER', label: 'Mikser spiralny' },
    { value: 'FORK_MIXER', label: 'Mikser widełkowy' },
  ];

  // Loading state
  if (isLoading) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Card sx={{ mb: 4 }}>
          <CardContent sx={{ textAlign: 'center', py: 4 }}>
            <Skeleton variant="circular" width={100} height={100} sx={{ mx: 'auto', mb: 2 }} />
            <Skeleton variant="text" width={200} height={40} sx={{ mx: 'auto' }} />
            <Skeleton variant="text" width={150} height={24} sx={{ mx: 'auto' }} />
            <Skeleton variant="rounded" width={100} height={32} sx={{ mx: 'auto', mt: 1 }} />
          </CardContent>
        </Card>
        <Grid container spacing={3}>
          {[1, 2, 3].map((i) => (
            <Grid item xs={12} md={4} key={i}>
              <Skeleton variant="rounded" height={140} />
            </Grid>
          ))}
        </Grid>
      </Container>
    );
  }

  if (!user) {
    return (
      <Container maxWidth="md" sx={{ py: 4, textAlign: 'center' }}>
        <Typography color="text.secondary">
          Nie jesteś zalogowany
        </Typography>
      </Container>
    );
  }

  const calculateProgress = user.stats?.calculationsThisMonth || 0;
  const maxFreeCalculations = 10;
  const progressPercent = Math.min((calculateProgress / maxFreeCalculations) * 100, 100);

  const handleSaveEquipment = () => {
    updateEquipmentMutation.mutate(equipmentForm);
  };

  const handleSaveEnvironment = () => {
    updateEnvironmentMutation.mutate(environmentForm);
  };

  const handleSaveNotifications = () => {
    updateNotificationsMutation.mutate(notificationsForm);
  };

  const handleGetLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setEnvironmentForm({
            ...environmentForm,
            defaultLatitude: position.coords.latitude,
            defaultLongitude: position.coords.longitude,
          });
          toast.success('Lokalizacja pobrana');
        },
        () => toast.error('Nie udało się pobrać lokalizacji')
      );
    }
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* Header Card */}
      <Card sx={{ mb: 4 }}>
        <CardContent sx={{ textAlign: 'center', py: 4 }}>
          <Avatar
            sx={{
              width: 100,
              height: 100,
              mx: 'auto',
              mb: 2,
              bgcolor: 'primary.main',
              fontSize: '2.5rem',
            }}
          >
            {user.firstName?.[0] || user.email?.[0]?.toUpperCase()}
          </Avatar>
          <Typography variant="h4" fontWeight="bold" gutterBottom>
            {user.firstName ? `${user.firstName} ${user.lastName || ''}` : user.email}
          </Typography>
          <Typography color="text.secondary" gutterBottom>
            {user.email}
          </Typography>
          <Chip
            label={user.isPremium ? 'Premium' : 'Konto darmowe'}
            color={user.isPremium ? 'secondary' : 'default'}
            icon={user.isPremium ? <Star /> : undefined}
          />
        </CardContent>
      </Card>

      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ textAlign: 'center' }}>
              <Calculate sx={{ fontSize: 48, color: 'primary.main', mb: 1 }} />
              <Typography variant="h3" fontWeight="bold">
                {user.stats?.totalCalculations || 0}
              </Typography>
              <Typography color="text.secondary">
                Wszystkich kalkulacji
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ textAlign: 'center' }}>
              <LocalPizza sx={{ fontSize: 48, color: 'secondary.main', mb: 1 }} />
              <Typography variant="h3" fontWeight="bold">
                {user.stats?.totalPizzasBaked || 0}
              </Typography>
              <Typography color="text.secondary">
                Upieczonych pizz
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ textAlign: 'center' }}>
              <Typography variant="h3" fontWeight="bold" color="primary">
                {user.stats?.calculationsThisMonth || 0}
              </Typography>
              <Typography color="text.secondary" gutterBottom>
                Kalkulacji w tym miesiącu
              </Typography>
              {!user.isPremium && (
                <>
                  <LinearProgress
                    variant="determinate"
                    value={progressPercent}
                    sx={{ mt: 2, mb: 1 }}
                  />
                  <Typography variant="caption" color="text.secondary">
                    {calculateProgress} / {maxFreeCalculations} (darmowy limit)
                  </Typography>
                </>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Settings Tabs */}
      <Card>
        <CardContent>
          <Typography variant="h5" fontWeight="bold" gutterBottom>
            Ustawienia
          </Typography>
          <Tabs 
            value={settingsTab} 
            onChange={(_, v) => setSettingsTab(v)}
            variant="scrollable"
            scrollButtons="auto"
          >
            <Tab icon={<Kitchen />} label="Mój sprzęt" />
            <Tab icon={<Thermostat />} label="Warunki" />
            <Tab icon={<Notifications />} label="Powiadomienia" />
          </Tabs>
          
          {/* Mój sprzęt Tab */}
          <TabPanel value={settingsTab} index={0}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Typography variant="h6">
                <Kitchen sx={{ mr: 1, verticalAlign: 'middle' }} />
                Domyślny sprzęt
              </Typography>
              {!editingEquipment ? (
                <Button startIcon={<Edit />} onClick={() => setEditingEquipment(true)}>
                  Edytuj
                </Button>
              ) : (
                <Box>
                  <Button 
                    startIcon={<Close />} 
                    onClick={() => setEditingEquipment(false)}
                    sx={{ mr: 1 }}
                  >
                    Anuluj
                  </Button>
                  <Button 
                    variant="contained" 
                    startIcon={<Save />}
                    onClick={handleSaveEquipment}
                    disabled={updateEquipmentMutation.isPending}
                  >
                    Zapisz
                  </Button>
                </Box>
              )}
            </Box>
            
            <Alert severity="info" sx={{ mb: 3 }}>
              Ustaw domyślny sprzęt, aby kalkulator automatycznie go używał przy nowych kalkulacjach.
            </Alert>

            <Grid container spacing={3}>
              {/* Piec */}
              <Grid item xs={12} md={6}>
                <FormControl fullWidth disabled={!editingEquipment}>
                  <InputLabel>
                    <LocalFireDepartment sx={{ mr: 1, verticalAlign: 'middle' }} />
                    Domyślny piec
                  </InputLabel>
                  <Select
                    value={equipmentForm.defaultOvenType || ''}
                    onChange={(e) => setEquipmentForm({ ...equipmentForm, defaultOvenType: e.target.value })}
                    label="Domyślny piec"
                  >
                    <MenuItem value="">
                      <em>Nie ustawiono</em>
                    </MenuItem>
                    {ovens?.map((oven: any) => (
                      <MenuItem key={oven.name || oven.id} value={oven.name || oven.id}>
                        {oven.displayName}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
                {equipment?.ovenDetails && (
                  <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                    Zalecana temp: {equipment.ovenDetails.recommendedTemperature}°C
                    {equipment.ovenDetails.hasSeparateTopBottom && 
                      ` (góra: ${equipment.ovenDetails.recommendedTopTemperature}°C, dół: ${equipment.ovenDetails.recommendedBottomTemperature}°C)`}
                  </Typography>
                )}
              </Grid>

              {/* Mikser */}
              <Grid item xs={12} md={6}>
                <FormControl fullWidth disabled={!editingEquipment}>
                  <InputLabel>
                    <Blender sx={{ mr: 1, verticalAlign: 'middle' }} />
                    Metoda wyrabiania
                  </InputLabel>
                  <Select
                    value={equipmentForm.defaultMixerType || ''}
                    onChange={(e) => setEquipmentForm({ ...equipmentForm, defaultMixerType: e.target.value })}
                    label="Metoda wyrabiania"
                  >
                    <MenuItem value="">
                      <em>Nie ustawiono</em>
                    </MenuItem>
                    {mixerTypes.map((mixer) => (
                      <MenuItem key={mixer.value} value={mixer.value}>
                        {mixer.label}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
                {equipment?.mixerDetails && (
                  <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                    Typowy czas: {equipment.mixerDetails.typicalMixingTime} min, 
                    max nawodnienie: {equipment.mixerDetails.maxRecommendedHydration}%
                  </Typography>
                )}
              </Grid>

              {/* Moc miksera */}
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  type="number"
                  label="Moc miksera (W)"
                  value={equipmentForm.mixerWattage || ''}
                  onChange={(e) => setEquipmentForm({ ...equipmentForm, mixerWattage: parseInt(e.target.value) || undefined })}
                  disabled={!editingEquipment}
                  inputProps={{ min: 100, max: 5000 }}
                  helperText="Opcjonalne - wpływa na obliczenia DDT"
                />
              </Grid>

              {/* Domyślna woda */}
              <Grid item xs={12} md={6}>
                <FormControl fullWidth disabled={!editingEquipment}>
                  <InputLabel>
                    <WaterDrop sx={{ mr: 1, verticalAlign: 'middle' }} />
                    Domyślna woda
                  </InputLabel>
                  <Select
                    value={equipmentForm.defaultWaterId || ''}
                    onChange={(e) => setEquipmentForm({ ...equipmentForm, defaultWaterId: e.target.value })}
                    label="Domyślna woda"
                  >
                    <MenuItem value="">
                      <em>Nie ustawiono</em>
                    </MenuItem>
                    {waters?.map((water: any) => (
                      <MenuItem key={water.id} value={water.id}>
                        {water.name}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              {/* Dostępne mąki */}
              <Grid item xs={12}>
                <Autocomplete
                  multiple
                  disabled={!editingEquipment}
                  options={flours || []}
                  getOptionLabel={(option: any) => option.name || option}
                  value={flours?.filter((f: any) => equipmentForm.availableFlourIds?.includes(f.id)) || []}
                  onChange={(_, newValue) => {
                    setEquipmentForm({
                      ...equipmentForm,
                      availableFlourIds: newValue.map((v: any) => v.id),
                    });
                  }}
                  renderInput={(params) => (
                    <TextField
                      {...params}
                      label="Mąki które mam w domu"
                      placeholder="Wybierz mąki..."
                      helperText="Kalkulator będzie sugerował przepisy na podstawie Twoich mąk"
                    />
                  )}
                  renderOption={(props, option: any, { selected }) => (
                    <li {...props}>
                      <Checkbox checked={selected} sx={{ mr: 1 }} />
                      <Box>
                        <Typography>{option.name}</Typography>
                        <Typography variant="caption" color="text.secondary">
                          W: {option.strength}, Białko: {option.proteinContent}%
                        </Typography>
                      </Box>
                    </li>
                  )}
                />
              </Grid>
            </Grid>
          </TabPanel>

          {/* Warunki środowiskowe Tab */}
          <TabPanel value={settingsTab} index={1}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Typography variant="h6">
                <Thermostat sx={{ mr: 1, verticalAlign: 'middle' }} />
                Typowe warunki
              </Typography>
              {!editingEnvironment ? (
                <Button startIcon={<Edit />} onClick={() => setEditingEnvironment(true)}>
                  Edytuj
                </Button>
              ) : (
                <Box>
                  <Button 
                    startIcon={<Close />} 
                    onClick={() => setEditingEnvironment(false)}
                    sx={{ mr: 1 }}
                  >
                    Anuluj
                  </Button>
                  <Button 
                    variant="contained" 
                    startIcon={<Save />}
                    onClick={handleSaveEnvironment}
                    disabled={updateEnvironmentMutation.isPending}
                  >
                    Zapisz
                  </Button>
                </Box>
              )}
            </Box>

            <Alert severity="info" sx={{ mb: 3 }}>
              Ustaw typowe temperatury w Twoim domu. Kalkulator użyje tych wartości jako domyślnych.
            </Alert>

            <Grid container spacing={3}>
              {/* Temperatura pokojowa */}
              <Grid item xs={12} md={6}>
                <Typography gutterBottom>
                  Typowa temperatura pokojowa: {environmentForm.typicalRoomTemperature}°C
                </Typography>
                <Slider
                  value={environmentForm.typicalRoomTemperature || 22}
                  onChange={(_, value) => setEnvironmentForm({ ...environmentForm, typicalRoomTemperature: value as number })}
                  disabled={!editingEnvironment}
                  min={15}
                  max={30}
                  step={0.5}
                  marks={[
                    { value: 15, label: '15°C' },
                    { value: 22, label: '22°C' },
                    { value: 30, label: '30°C' },
                  ]}
                  valueLabelDisplay="auto"
                />
              </Grid>

              {/* Temperatura lodówki */}
              <Grid item xs={12} md={6}>
                <Typography gutterBottom>
                  Typowa temperatura lodówki: {environmentForm.typicalFridgeTemperature}°C
                </Typography>
                <Slider
                  value={environmentForm.typicalFridgeTemperature || 4}
                  onChange={(_, value) => setEnvironmentForm({ ...environmentForm, typicalFridgeTemperature: value as number })}
                  disabled={!editingEnvironment}
                  min={0}
                  max={8}
                  step={0.5}
                  marks={[
                    { value: 0, label: '0°C' },
                    { value: 4, label: '4°C' },
                    { value: 8, label: '8°C' },
                  ]}
                  valueLabelDisplay="auto"
                />
              </Grid>

              {/* Domyślne miasto */}
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Domyślne miasto (dla pogody)"
                  value={environmentForm.defaultCity || ''}
                  onChange={(e) => setEnvironmentForm({ ...environmentForm, defaultCity: e.target.value })}
                  disabled={!editingEnvironment}
                  InputProps={{
                    startAdornment: <Place sx={{ mr: 1, color: 'text.secondary' }} />,
                  }}
                  helperText="Kalkulator automatycznie pobierze pogodę z tego miasta"
                />
              </Grid>

              {/* Lokalizacja GPS */}
              <Grid item xs={12} md={6}>
                <Box>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Domyślna lokalizacja (GPS)
                  </Typography>
                  <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                    <TextField
                      size="small"
                      label="Szerokość"
                      type="number"
                      value={environmentForm.defaultLatitude || ''}
                      onChange={(e) => setEnvironmentForm({ ...environmentForm, defaultLatitude: parseFloat(e.target.value) || undefined })}
                      disabled={!editingEnvironment}
                      sx={{ flex: 1 }}
                    />
                    <TextField
                      size="small"
                      label="Długość"
                      type="number"
                      value={environmentForm.defaultLongitude || ''}
                      onChange={(e) => setEnvironmentForm({ ...environmentForm, defaultLongitude: parseFloat(e.target.value) || undefined })}
                      disabled={!editingEnvironment}
                      sx={{ flex: 1 }}
                    />
                    <Tooltip title="Pobierz moją lokalizację">
                      <span>
                        <IconButton 
                          onClick={handleGetLocation}
                          disabled={!editingEnvironment}
                          color="primary"
                        >
                          <Place />
                        </IconButton>
                      </span>
                    </Tooltip>
                  </Box>
                </Box>
              </Grid>
            </Grid>
          </TabPanel>

          {/* Powiadomienia Tab */}
          <TabPanel value={settingsTab} index={2}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Typography variant="h6">
                <Notifications sx={{ mr: 1, verticalAlign: 'middle' }} />
                Ustawienia powiadomień
              </Typography>
              {!editingNotifications ? (
                <Button startIcon={<Edit />} onClick={() => setEditingNotifications(true)}>
                  Edytuj
                </Button>
              ) : (
                <Box>
                  <Button 
                    startIcon={<Close />} 
                    onClick={() => setEditingNotifications(false)}
                    sx={{ mr: 1 }}
                  >
                    Anuluj
                  </Button>
                  <Button 
                    variant="contained" 
                    startIcon={<Save />}
                    onClick={handleSaveNotifications}
                    disabled={updateNotificationsMutation.isPending}
                  >
                    Zapisz
                  </Button>
                </Box>
              )}
            </Box>

            <Grid container spacing={3}>
              <Grid item xs={12} md={4}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={notificationsForm.emailNotifications ?? true}
                      onChange={(e) => setNotificationsForm({ ...notificationsForm, emailNotifications: e.target.checked })}
                      disabled={!editingNotifications}
                    />
                  }
                  label="Powiadomienia email"
                />
              </Grid>
              <Grid item xs={12} md={4}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={notificationsForm.smsNotifications ?? false}
                      onChange={(e) => setNotificationsForm({ ...notificationsForm, smsNotifications: e.target.checked })}
                      disabled={!editingNotifications}
                    />
                  }
                  label="Powiadomienia SMS"
                />
              </Grid>
              <Grid item xs={12} md={4}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={notificationsForm.pushNotifications ?? true}
                      onChange={(e) => setNotificationsForm({ ...notificationsForm, pushNotifications: e.target.checked })}
                      disabled={!editingNotifications}
                    />
                  }
                  label="Powiadomienia push"
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <Typography gutterBottom>
                  Przypomnienie SMS przed krokiem: {notificationsForm.smsReminderMinutesBefore || 15} min
                </Typography>
                <Slider
                  value={notificationsForm.smsReminderMinutesBefore || 15}
                  onChange={(_, value) => setNotificationsForm({ ...notificationsForm, smsReminderMinutesBefore: value as number })}
                  disabled={!editingNotifications || !notificationsForm.smsNotifications}
                  min={5}
                  max={60}
                  step={5}
                  marks={[
                    { value: 5, label: '5 min' },
                    { value: 15, label: '15 min' },
                    { value: 30, label: '30 min' },
                    { value: 60, label: '60 min' },
                  ]}
                  valueLabelDisplay="auto"
                />
              </Grid>
            </Grid>
          </TabPanel>
        </CardContent>
      </Card>
    </Container>
  );
};

export default ProfilePage;
