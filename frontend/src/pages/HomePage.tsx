import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Container,
  Typography,
  Button,
  Grid,
  Card,
  CardContent,
  CardMedia,
  Stack,
  Chip,
  Avatar,
  Rating,
  Paper,
  useTheme,
  alpha,
} from '@mui/material';
import {
  Calculate as CalculateIcon,
  Schedule as ScheduleIcon,
  Notifications as NotificationsIcon,
  Restaurant as RestaurantIcon,
  TrendingUp as TrendingUpIcon,
  LocalFireDepartment as FireIcon,
  Star as StarIcon,
  CheckCircle as CheckIcon,
  ArrowForward as ArrowIcon,
  PlayArrow as PlayIcon,
} from '@mui/icons-material';
import { motion } from 'framer-motion';
import { IMAGES, PIZZA_STYLE_IMAGES } from '../constants/images';

const MotionBox = motion(Box);
const MotionCard = motion(Card);

const features = [
  {
    icon: <CalculateIcon sx={{ fontSize: 48 }} />,
    title: 'Precyzyjne kalkulacje',
    description: 'Algorytmy oparte na procentach piekarskich i modelach aktywności drożdży w różnych temperaturach.',
    color: '#e53935',
  },
  {
    icon: <ScheduleIcon sx={{ fontSize: 48 }} />,
    title: 'Inteligentny harmonogram',
    description: 'Automatycznie wygenerowany plan od mieszania do pieczenia, z uwzględnieniem Twojego grafiku.',
    color: '#fb8c00',
  },
  {
    icon: <NotificationsIcon sx={{ fontSize: 48 }} />,
    title: 'Powiadomienia na czas',
    description: 'SMS lub email z przypomnieniami o każdym kroku. Nigdy nie przegapisz momentu na składanie ciasta.',
    color: '#43a047',
  },
  {
    icon: <RestaurantIcon sx={{ fontSize: 48 }} />,
    title: '10+ stylów pizzy',
    description: 'Od neapolitańskiej po Detroit - każdy styl z optymalnymi parametrami i wskazówkami.',
    color: '#1e88e5',
  },
  {
    icon: <TrendingUpIcon sx={{ fontSize: 48 }} />,
    title: 'Śledź postępy',
    description: 'Historia wypieków, notatki, oceny i statystyki. Zobacz jak stajesz się lepszym pizzaiolo.',
    color: '#8e24aa',
  },
  {
    icon: <FireIcon sx={{ fontSize: 48 }} />,
    title: 'Dla każdego pieca',
    description: 'Zoptymalizowane receptury dla piekarnika, Ooni, Effeuno czy tradycyjnego pieca na drewno.',
    color: '#f4511e',
  },
];

const pizzaStyles = [
  { id: 'NEAPOLITAN', name: 'Neapolitańska', hydration: '65%', time: '24h', temp: '450°C' },
  { id: 'NEW_YORK', name: 'Nowojorska', hydration: '60%', time: '24h', temp: '290°C' },
  { id: 'ROMAN', name: 'Rzymska', hydration: '70%', time: '48h', temp: '350°C' },
  { id: 'DETROIT', name: 'Detroit', hydration: '70%', time: '4h', temp: '250°C' },
  { id: 'FOCACCIA', name: 'Focaccia', hydration: '75%', time: '8h', temp: '220°C' },
];

const testimonials = [
  {
    name: 'Marco Rossi',
    role: 'Pizzaiolo, Napoli',
    avatar: IMAGES.avatars.chef,
    rating: 5,
    text: 'Najdokładniejszy kalkulator jaki używałem. Algorytm fermentacji działa idealnie nawet dla 72-godzinnego ciasta.',
  },
  {
    name: 'Anna Kowalska',
    role: 'Domowa pizzaiolo',
    avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&q=80',
    rating: 5,
    text: 'Dzięki PizzaMaestro moja pizza domowa smakuje jak z włoskiej pizzerii. Powiadomienia SMS to game changer!',
  },
  {
    name: 'Tomasz Nowak',
    role: 'Właściciel pizzerii',
    avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&q=80',
    rating: 5,
    text: 'Używamy PizzaMaestro Pro w naszej pizzerii. Standaryzacja receptur znacznie poprawiła jakość.',
  },
];

const pricingPlans = [
  {
    name: 'Starter',
    price: '0',
    period: 'zawsze',
    features: ['10 kalkulacji/miesiąc', '5 zapisanych receptur', 'Podstawowe style', 'Reklamy'],
    cta: 'Zacznij za darmo',
    popular: false,
  },
  {
    name: 'Premium',
    price: '29',
    period: '/miesiąc',
    features: ['Bez limitu kalkulacji', 'Bez limitu receptur', 'Wszystkie style', 'Powiadomienia SMS (50/msc)', 'Bez reklam', 'Priorytetowe wsparcie'],
    cta: 'Wybierz Premium',
    popular: true,
  },
  {
    name: 'Pro',
    price: '99',
    period: '/miesiąc',
    features: ['Wszystko z Premium', 'SMS bez limitu', 'Multi-user (10 osób)', 'API dostęp', 'Eksport PDF/CSV', 'Dedykowany opiekun'],
    cta: 'Kontakt sprzedaż',
    popular: false,
  },
];

const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const theme = useTheme();

  return (
    <Box>
      {/* ========== HERO SECTION ========== */}
      <Box
        sx={{
          position: 'relative',
          minHeight: '90vh',
          display: 'flex',
          alignItems: 'center',
          overflow: 'hidden',
          '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundImage: `url(${IMAGES.hero.main})`,
            backgroundSize: 'cover',
            backgroundPosition: 'center',
            filter: 'brightness(0.3)',
            zIndex: 0,
          },
        }}
      >
        <Container maxWidth="lg" sx={{ position: 'relative', zIndex: 1 }}>
          <Grid container spacing={6} alignItems="center">
            <Grid size={{ xs: 12, md: 7 }}>
              <MotionBox
                initial={{ opacity: 0, y: 40 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.8 }}
              >
                <Chip
                  label="🔥 Nowa wersja 2.0 - Harmonogram AI"
                  sx={{
                    mb: 3,
                    bgcolor: alpha(theme.palette.primary.main, 0.9),
                    color: 'white',
                    fontWeight: 600,
                    fontSize: '0.9rem',
                    py: 2.5,
                  }}
                />
                <Typography
                  variant="h1"
                  sx={{
                    color: 'white',
                    fontWeight: 800,
                    fontSize: { xs: '2.5rem', md: '4rem', lg: '4.5rem' },
                    lineHeight: 1.1,
                    mb: 3,
                    textShadow: '2px 2px 4px rgba(0,0,0,0.3)',
                  }}
                >
                  Twórz{' '}
                  <Box component="span" sx={{ color: theme.palette.secondary.main }}>
                    perfekcyjną
                  </Box>{' '}
                  pizzę jak prawdziwy Maestro
                </Typography>
                <Typography
                  variant="h5"
                  sx={{
                    color: 'rgba(255,255,255,0.9)',
                    mb: 4,
                    fontWeight: 300,
                    maxWidth: 550,
                    lineHeight: 1.6,
                  }}
                >
                  Profesjonalny kalkulator ciasta z algorytmami fermentacji,
                  inteligentnym harmonogramem i powiadomieniami w czasie rzeczywistym.
                </Typography>
                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                  <Button
                    variant="contained"
                    size="large"
                    onClick={() => navigate('/calculator')}
                    endIcon={<ArrowIcon />}
                    sx={{
                      py: 2,
                      px: 5,
                      fontSize: '1.1rem',
                      fontWeight: 600,
                      borderRadius: 3,
                      boxShadow: '0 8px 25px rgba(211, 47, 47, 0.4)',
                      '&:hover': {
                        transform: 'translateY(-2px)',
                        boxShadow: '0 12px 35px rgba(211, 47, 47, 0.5)',
                      },
                    }}
                  >
                    Rozpocznij za darmo
                  </Button>
                  <Button
                    variant="outlined"
                    size="large"
                    startIcon={<PlayIcon />}
                    sx={{
                      py: 2,
                      px: 4,
                      fontSize: '1.1rem',
                      borderColor: 'white',
                      color: 'white',
                      borderWidth: 2,
                      borderRadius: 3,
                      '&:hover': {
                        borderColor: 'white',
                        bgcolor: 'rgba(255,255,255,0.1)',
                        borderWidth: 2,
                      },
                    }}
                  >
                    Zobacz demo
                  </Button>
                </Stack>
                <Box sx={{ mt: 4, display: 'flex', alignItems: 'center', gap: 3 }}>
                  <Box sx={{ display: 'flex' }}>
                    {[1, 2, 3, 4].map((i) => (
                      <Avatar
                        key={i}
                        src={`https://i.pravatar.cc/40?img=${i + 10}`}
                        sx={{ width: 36, height: 36, ml: i > 1 ? -1.5 : 0, border: '2px solid white' }}
                      />
                    ))}
                  </Box>
                  <Box>
                    <Typography sx={{ color: 'white', fontWeight: 600 }}>
                      10,000+ pizzaioli
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <Rating value={5} size="small" readOnly sx={{ color: '#ffc107' }} />
                      <Typography sx={{ color: 'rgba(255,255,255,0.8)', fontSize: '0.85rem' }}>
                        4.9/5 (2,847 opinii)
                      </Typography>
                    </Box>
                  </Box>
                </Box>
              </MotionBox>
            </Grid>

            <Grid size={{ xs: 12, md: 5 }} sx={{ display: { xs: 'none', md: 'block' } }}>
              <MotionBox
                initial={{ opacity: 0, scale: 0.8, rotate: -5 }}
                animate={{ opacity: 1, scale: 1, rotate: 0 }}
                transition={{ duration: 0.8, delay: 0.3 }}
              >
                <Paper
                  elevation={24}
                  sx={{
                    p: 3,
                    borderRadius: 4,
                    bgcolor: 'rgba(255,255,255,0.95)',
                    backdropFilter: 'blur(10px)',
                  }}
                >
                  <Typography variant="h6" fontWeight="bold" gutterBottom>
                    🍕 Szybka kalkulacja
                  </Typography>
                  <Box sx={{ display: 'flex', gap: 1, mb: 2, flexWrap: 'wrap' }}>
                    <Chip label="Neapolitańska" color="primary" size="small" />
                    <Chip label="4 pizze" variant="outlined" size="small" />
                    <Chip label="65% hydratacji" variant="outlined" size="small" />
                  </Box>
                  <Box sx={{ bgcolor: 'grey.100', borderRadius: 2, p: 2, mb: 2 }}>
                    <Grid container spacing={1}>
                      <Grid size={{ xs: 6 }}>
                        <Typography variant="caption" color="text.secondary">Mąka</Typography>
                        <Typography fontWeight="bold">590g</Typography>
                      </Grid>
                      <Grid size={{ xs: 6 }}>
                        <Typography variant="caption" color="text.secondary">Woda</Typography>
                        <Typography fontWeight="bold">384g</Typography>
                      </Grid>
                      <Grid size={{ xs: 6 }}>
                        <Typography variant="caption" color="text.secondary">Sól</Typography>
                        <Typography fontWeight="bold">16.5g</Typography>
                      </Grid>
                      <Grid size={{ xs: 6 }}>
                        <Typography variant="caption" color="text.secondary">Drożdże</Typography>
                        <Typography fontWeight="bold">0.9g</Typography>
                      </Grid>
                    </Grid>
                  </Box>
                  <Button fullWidth variant="contained" onClick={() => navigate('/calculator')}>
                    Oblicz swoją recepturę
                  </Button>
                </Paper>
              </MotionBox>
            </Grid>
          </Grid>
        </Container>

        {/* Wave divider */}
        <Box
          sx={{
            position: 'absolute',
            bottom: -2,
            left: 0,
            right: 0,
            zIndex: 2,
          }}
        >
          <svg viewBox="0 0 1440 100" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
              d="M0 50L60 45.7C120 41 240 33 360 35.3C480 37 600 50 720 55.8C840 62 960 60 1080 53.3C1200 47 1320 35 1380 29.2L1440 23V100H1380C1320 100 1200 100 1080 100C960 100 840 100 720 100C600 100 480 100 360 100C240 100 120 100 60 100H0V50Z"
              fill="white"
            />
          </svg>
        </Box>
      </Box>

      {/* ========== PIZZA STYLES CAROUSEL ========== */}
      <Container maxWidth="lg" sx={{ py: 8 }}>
        <Box textAlign="center" mb={6}>
          <Typography variant="overline" color="primary" fontWeight="bold" letterSpacing={2}>
            STYLE PIZZY
          </Typography>
          <Typography variant="h3" fontWeight="bold" gutterBottom>
            Wybierz swój ulubiony styl
          </Typography>
          <Typography variant="h6" color="text.secondary" sx={{ maxWidth: 600, mx: 'auto' }}>
            Każdy styl ma unikalne parametry - hydratację, czas fermentacji i temperaturę wypieku
          </Typography>
        </Box>

        <Grid container spacing={3}>
          {pizzaStyles.map((style, index) => (
            <Grid size={{ xs: 12, sm: 6, md: 2.4 }} key={style.id}>
              <MotionCard
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1 }}
                viewport={{ once: true }}
                whileHover={{ y: -10 }}
                sx={{
                  cursor: 'pointer',
                  overflow: 'hidden',
                  '&:hover .pizza-image': {
                    transform: 'scale(1.1)',
                  },
                }}
                onClick={() => navigate('/calculator')}
              >
                <Box sx={{ position: 'relative', overflow: 'hidden' }}>
                  <CardMedia
                    className="pizza-image"
                    component="img"
                    height="160"
                    image={PIZZA_STYLE_IMAGES[style.id]}
                    alt={style.name}
                    sx={{ transition: 'transform 0.3s ease' }}
                  />
                  <Box
                    sx={{
                      position: 'absolute',
                      top: 8,
                      right: 8,
                      bgcolor: 'primary.main',
                      color: 'white',
                      px: 1,
                      py: 0.5,
                      borderRadius: 1,
                      fontSize: '0.75rem',
                      fontWeight: 'bold',
                    }}
                  >
                    {style.temp}
                  </Box>
                </Box>
                <CardContent sx={{ textAlign: 'center' }}>
                  <Typography variant="h6" fontWeight="bold" gutterBottom>
                    {style.name}
                  </Typography>
                  <Box sx={{ display: 'flex', justifyContent: 'center', gap: 1 }}>
                    <Chip label={style.hydration} size="small" color="primary" variant="outlined" />
                    <Chip label={style.time} size="small" variant="outlined" />
                  </Box>
                </CardContent>
              </MotionCard>
            </Grid>
          ))}
        </Grid>
      </Container>

      {/* ========== FEATURES SECTION ========== */}
      <Box sx={{ bgcolor: 'grey.50', py: 10 }}>
        <Container maxWidth="lg">
          <Box textAlign="center" mb={8}>
            <Typography variant="overline" color="primary" fontWeight="bold" letterSpacing={2}>
              FUNKCJE
            </Typography>
            <Typography variant="h3" fontWeight="bold" gutterBottom>
              Wszystko czego potrzebujesz
            </Typography>
            <Typography variant="h6" color="text.secondary" sx={{ maxWidth: 600, mx: 'auto' }}>
              Od profesjonalnych algorytmów po inteligentne powiadomienia - mamy wszystko
            </Typography>
          </Box>

          <Grid container spacing={4}>
            {features.map((feature, index) => (
              <Grid size={{ xs: 12, sm: 6, md: 4 }} key={feature.title}>
                <MotionBox
                  initial={{ opacity: 0, y: 30 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  transition={{ delay: index * 0.1 }}
                  viewport={{ once: true }}
                >
                  <Card
                    sx={{
                      height: '100%',
                      p: 3,
                      border: 'none',
                      boxShadow: '0 4px 20px rgba(0,0,0,0.08)',
                      transition: 'all 0.3s ease',
                      '&:hover': {
                        transform: 'translateY(-8px)',
                        boxShadow: '0 12px 40px rgba(0,0,0,0.15)',
                        '& .feature-icon': {
                          transform: 'scale(1.1)',
                          bgcolor: feature.color,
                          color: 'white',
                        },
                      },
                    }}
                  >
                    <Box
                      className="feature-icon"
                      sx={{
                        width: 80,
                        height: 80,
                        borderRadius: 3,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        bgcolor: alpha(feature.color, 0.1),
                        color: feature.color,
                        mb: 3,
                        transition: 'all 0.3s ease',
                      }}
                    >
                      {feature.icon}
                    </Box>
                    <Typography variant="h5" fontWeight="bold" gutterBottom>
                      {feature.title}
                    </Typography>
                    <Typography color="text.secondary" lineHeight={1.7}>
                      {feature.description}
                    </Typography>
                  </Card>
                </MotionBox>
              </Grid>
            ))}
          </Grid>
        </Container>
      </Box>

      {/* ========== HOW IT WORKS ========== */}
      <Container maxWidth="lg" sx={{ py: 10 }}>
        <Box textAlign="center" mb={8}>
          <Typography variant="overline" color="primary" fontWeight="bold" letterSpacing={2}>
            JAK TO DZIAŁA
          </Typography>
          <Typography variant="h3" fontWeight="bold" gutterBottom>
            3 proste kroki do idealnej pizzy
          </Typography>
        </Box>

        <Grid container spacing={6} alignItems="center">
          <Grid size={{ xs: 12, md: 6 }}>
            <Box
              component="img"
              src={IMAGES.process.kneading}
              alt="Przygotowanie ciasta"
              sx={{
                width: '100%',
                borderRadius: 4,
                boxShadow: '0 20px 60px rgba(0,0,0,0.2)',
              }}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            {[
              { step: 1, title: 'Wprowadź parametry', desc: 'Wybierz styl pizzy, liczbę porcji, hydratację i czas fermentacji.' },
              { step: 2, title: 'Odbierz recepturę', desc: 'Otrzymaj precyzyjne ilości składników i automatyczny harmonogram.' },
              { step: 3, title: 'Piecz z powiadomieniami', desc: 'Aplikacja przypomni Ci o każdym kroku w odpowiednim momencie.' },
            ].map((item, index) => (
              <MotionBox
                key={item.step}
                initial={{ opacity: 0, x: 30 }}
                whileInView={{ opacity: 1, x: 0 }}
                transition={{ delay: index * 0.2 }}
                viewport={{ once: true }}
                sx={{ display: 'flex', gap: 3, mb: 4 }}
              >
                <Box
                  sx={{
                    width: 60,
                    height: 60,
                    borderRadius: '50%',
                    bgcolor: 'primary.main',
                    color: 'white',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '1.5rem',
                    fontWeight: 'bold',
                    flexShrink: 0,
                  }}
                >
                  {item.step}
                </Box>
                <Box>
                  <Typography variant="h5" fontWeight="bold" gutterBottom>
                    {item.title}
                  </Typography>
                  <Typography color="text.secondary" fontSize="1.1rem">
                    {item.desc}
                  </Typography>
                </Box>
              </MotionBox>
            ))}
          </Grid>
        </Grid>
      </Container>

      {/* ========== TESTIMONIALS ========== */}
      <Box sx={{ bgcolor: 'primary.main', py: 10 }}>
        <Container maxWidth="lg">
          <Box textAlign="center" mb={6}>
            <Typography variant="overline" sx={{ color: 'rgba(255,255,255,0.8)' }} letterSpacing={2}>
              OPINIE
            </Typography>
            <Typography variant="h3" fontWeight="bold" sx={{ color: 'white' }} gutterBottom>
              Co mówią nasi użytkownicy
            </Typography>
          </Box>

          <Grid container spacing={4}>
            {testimonials.map((testimonial, index) => (
              <Grid size={{ xs: 12, md: 4 }} key={index}>
                <MotionCard
                  initial={{ opacity: 0, y: 30 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  transition={{ delay: index * 0.15 }}
                  viewport={{ once: true }}
                  sx={{ height: '100%', p: 3 }}
                >
                  <Rating value={testimonial.rating} readOnly sx={{ mb: 2 }} />
                  <Typography sx={{ mb: 3, fontStyle: 'italic', lineHeight: 1.7 }}>
                    "{testimonial.text}"
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <Avatar src={testimonial.avatar} sx={{ width: 50, height: 50 }} />
                    <Box>
                      <Typography fontWeight="bold">{testimonial.name}</Typography>
                      <Typography variant="body2" color="text.secondary">
                        {testimonial.role}
                      </Typography>
                    </Box>
                  </Box>
                </MotionCard>
              </Grid>
            ))}
          </Grid>
        </Container>
      </Box>

      {/* ========== PRICING ========== */}
      <Container maxWidth="lg" sx={{ py: 10 }}>
        <Box textAlign="center" mb={8}>
          <Typography variant="overline" color="primary" fontWeight="bold" letterSpacing={2}>
            CENNIK
          </Typography>
          <Typography variant="h3" fontWeight="bold" gutterBottom>
            Wybierz plan dla siebie
          </Typography>
          <Typography variant="h6" color="text.secondary">
            Zacznij za darmo, ulepszaj gdy potrzebujesz więcej
          </Typography>
        </Box>

        <Grid container spacing={4} justifyContent="center">
          {pricingPlans.map((plan, index) => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={plan.name}>
              <MotionCard
                initial={{ opacity: 0, y: 30 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1 }}
                viewport={{ once: true }}
                sx={{
                  height: '100%',
                  position: 'relative',
                  border: plan.popular ? '2px solid' : '1px solid',
                  borderColor: plan.popular ? 'primary.main' : 'divider',
                  transform: plan.popular ? 'scale(1.05)' : 'none',
                }}
              >
                {plan.popular && (
                  <Chip
                    label="Najpopularniejszy"
                    color="primary"
                    size="small"
                    sx={{
                      position: 'absolute',
                      top: -12,
                      left: '50%',
                      transform: 'translateX(-50%)',
                      fontWeight: 'bold',
                    }}
                  />
                )}
                <CardContent sx={{ p: 4 }}>
                  <Typography variant="h5" fontWeight="bold" gutterBottom>
                    {plan.name}
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'baseline', mb: 3 }}>
                    <Typography variant="h2" fontWeight="bold" color="primary">
                      {plan.price === '0' ? 'Free' : `${plan.price} zł`}
                    </Typography>
                    {plan.price !== '0' && (
                      <Typography color="text.secondary" sx={{ ml: 1 }}>
                        {plan.period}
                      </Typography>
                    )}
                  </Box>
                  <Box sx={{ mb: 4 }}>
                    {plan.features.map((feature) => (
                      <Box key={feature} sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1.5 }}>
                        <CheckIcon color="primary" fontSize="small" />
                        <Typography>{feature}</Typography>
                      </Box>
                    ))}
                  </Box>
                  <Button
                    fullWidth
                    variant={plan.popular ? 'contained' : 'outlined'}
                    size="large"
                    onClick={() => navigate('/register')}
                  >
                    {plan.cta}
                  </Button>
                </CardContent>
              </MotionCard>
            </Grid>
          ))}
        </Grid>
      </Container>

      {/* ========== FINAL CTA ========== */}
      <Box
        sx={{
          position: 'relative',
          py: 12,
          '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundImage: `url(${IMAGES.hero.oven})`,
            backgroundSize: 'cover',
            backgroundPosition: 'center',
            filter: 'brightness(0.25)',
          },
        }}
      >
        <Container maxWidth="md" sx={{ position: 'relative', textAlign: 'center' }}>
          <Typography variant="h3" fontWeight="bold" sx={{ color: 'white', mb: 3 }}>
            Gotowy zostać Pizza Maestro?
          </Typography>
          <Typography variant="h6" sx={{ color: 'rgba(255,255,255,0.8)', mb: 4 }}>
            Dołącz do 10,000+ pizzaioli, którzy tworzą idealne pizze dzięki naszej aplikacji.
          </Typography>
          <Button
            variant="contained"
            size="large"
            onClick={() => navigate('/register')}
            sx={{
              py: 2,
              px: 6,
              fontSize: '1.2rem',
              fontWeight: 600,
              borderRadius: 3,
            }}
          >
            Zacznij za darmo teraz
          </Button>
          <Typography sx={{ color: 'rgba(255,255,255,0.6)', mt: 2 }}>
            Nie wymaga karty kredytowej • Konfiguracja w 30 sekund
          </Typography>
        </Container>
      </Box>
    </Box>
  );
};

export default HomePage;
