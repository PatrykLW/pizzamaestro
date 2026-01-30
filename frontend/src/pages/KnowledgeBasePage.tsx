import React, { useState } from 'react';
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  Grid,
  TextField,
  Chip,
  Tabs,
  Tab,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemButton,
  Divider,
  Alert,
  CircularProgress,
  InputAdornment,
  Paper,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  alpha,
  useTheme,
} from '@mui/material';
import {
  Search as SearchIcon,
  ExpandMore as ExpandMoreIcon,
  MenuBook as BookIcon,
  School as SchoolIcon,
  Timer as TimerIcon,
  Star as StarIcon,
  Lock as LockIcon,
  LocalPizza as PizzaIcon,
  Science as ScienceIcon,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { useNavigate, useParams } from 'react-router-dom';
import { knowledgeApi, TechniqueGuide, CategoryInfo } from '../services/api';
import { motion } from 'framer-motion';
import { useAuthStore } from '../store/authStore';

const MotionCard = motion(Card);

const KnowledgeBasePage: React.FC = () => {
  const theme = useTheme();
  const navigate = useNavigate();
  const { slug } = useParams<{ slug?: string }>();
  const { isAuthenticated, user } = useAuthStore();
  const isPremium = user?.accountType === 'PREMIUM' || user?.accountType === 'PRO';
  
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  
  // Fetch categories
  const { data: categories, isLoading: categoriesLoading } = useQuery({
    queryKey: ['knowledge-categories'],
    queryFn: knowledgeApi.getCategories,
  });
  
  // Fetch guides based on filter
  const { data: guides, isLoading: guidesLoading } = useQuery({
    queryKey: ['knowledge-guides', selectedCategory, searchQuery],
    queryFn: async () => {
      if (searchQuery) {
        return knowledgeApi.search(searchQuery);
      }
      if (selectedCategory) {
        return knowledgeApi.getByCategory(selectedCategory);
      }
      return knowledgeApi.getAllGuides(isPremium);
    },
  });
  
  // Fetch single guide if slug provided
  const { data: singleGuide, isLoading: guideLoading } = useQuery({
    queryKey: ['knowledge-guide', slug],
    queryFn: () => knowledgeApi.getGuide(slug!),
    enabled: !!slug,
  });
  
  // Fetch popular guides
  const { data: popularGuides } = useQuery({
    queryKey: ['knowledge-popular'],
    queryFn: knowledgeApi.getPopular,
  });
  
  const getDifficultyColor = (difficulty: string) => {
    switch (difficulty) {
      case 'BEGINNER':
        return 'success';
      case 'INTERMEDIATE':
        return 'warning';
      case 'ADVANCED':
        return 'error';
      case 'EXPERT':
        return 'error';
      default:
        return 'default';
    }
  };
  
  const getDifficultyLabel = (difficulty: string) => {
    const labels: Record<string, string> = {
      BEGINNER: 'Początkujący',
      INTERMEDIATE: 'Średniozaawansowany',
      ADVANCED: 'Zaawansowany',
      EXPERT: 'Ekspert',
    };
    return labels[difficulty] || difficulty;
  };
  
  const formatDuration = (minutes: number) => {
    if (minutes < 60) return `${minutes} min`;
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (mins === 0) return `${hours}h`;
    return `${hours}h ${mins}min`;
  };
  
  // If viewing single guide
  if (slug && singleGuide) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Box sx={{ mb: 4 }}>
          <Chip
            label={singleGuide.category.replace('_', ' ')}
            color="primary"
            variant="outlined"
            onClick={() => {
              navigate('/knowledge');
              setSelectedCategory(singleGuide.category);
            }}
            sx={{ mb: 2 }}
          />
          <Typography variant="h3" fontWeight="bold" gutterBottom>
            {singleGuide.title}
          </Typography>
          <Typography variant="h6" color="text.secondary" sx={{ mb: 2 }}>
            {singleGuide.shortDescription}
          </Typography>
          
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 3 }}>
            <Chip
              icon={<SchoolIcon />}
              label={getDifficultyLabel(singleGuide.difficulty)}
              color={getDifficultyColor(singleGuide.difficulty) as any}
            />
            <Chip
              icon={<TimerIcon />}
              label={formatDuration(singleGuide.estimatedTimeMinutes)}
            />
            <Chip
              icon={<StarIcon />}
              label={`${singleGuide.viewCount} wyświetleń`}
              variant="outlined"
            />
            {singleGuide.premium && (
              <Chip icon={<LockIcon />} label="PREMIUM" color="secondary" />
            )}
          </Box>
        </Box>
        
        <Grid container spacing={4}>
          {/* Main content */}
          <Grid size={{ xs: 12, md: 8 }}>
            {/* Full description */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h5" fontWeight="bold" gutterBottom>
                  Opis
                </Typography>
                <Typography sx={{ whiteSpace: 'pre-line' }}>
                  {singleGuide.fullDescription}
                </Typography>
              </CardContent>
            </Card>
            
            {/* Required equipment */}
            {singleGuide.requiredEquipment && singleGuide.requiredEquipment.length > 0 && (
              <Card sx={{ mb: 3 }}>
                <CardContent>
                  <Typography variant="h5" fontWeight="bold" gutterBottom>
                    Potrzebny sprzęt
                  </Typography>
                  <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                    {singleGuide.requiredEquipment.map((item, i) => (
                      <Chip key={i} label={item} variant="outlined" />
                    ))}
                  </Box>
                </CardContent>
              </Card>
            )}
            
            {/* Steps */}
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Typography variant="h5" fontWeight="bold" gutterBottom>
                  Kroki
                </Typography>
                <Stepper orientation="vertical">
                  {singleGuide.steps.map((step, index) => (
                    <Step key={index} active expanded>
                      <StepLabel
                        StepIconComponent={() => (
                          <Box
                            sx={{
                              width: 32,
                              height: 32,
                              borderRadius: '50%',
                              bgcolor: step.critical ? 'error.main' : 'primary.main',
                              color: 'white',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              fontWeight: 'bold',
                            }}
                          >
                            {step.stepNumber}
                          </Box>
                        )}
                      >
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Typography variant="subtitle1" fontWeight="bold">
                            {step.title}
                          </Typography>
                          {step.critical && (
                            <Chip label="Kluczowy" color="error" size="small" />
                          )}
                          {step.durationSeconds && (
                            <Chip
                              label={formatDuration(Math.ceil(step.durationSeconds / 60))}
                              size="small"
                              variant="outlined"
                            />
                          )}
                        </Box>
                      </StepLabel>
                      <StepContent>
                        <Typography>{step.description}</Typography>
                        {step.detailedExplanation && (
                          <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{ mt: 1, whiteSpace: 'pre-line' }}
                          >
                            {step.detailedExplanation}
                          </Typography>
                        )}
                        {step.tips && step.tips.length > 0 && (
                          <Box sx={{ mt: 1 }}>
                            {step.tips.map((tip, i) => (
                              <Alert key={i} severity="info" sx={{ mt: 1 }} icon={false}>
                                💡 {tip}
                              </Alert>
                            ))}
                          </Box>
                        )}
                      </StepContent>
                    </Step>
                  ))}
                </Stepper>
              </CardContent>
            </Card>
            
            {/* Pro Tips */}
            {singleGuide.proTips && singleGuide.proTips.length > 0 && (
              <Card sx={{ mb: 3, bgcolor: alpha(theme.palette.success.main, 0.05) }}>
                <CardContent>
                  <Typography variant="h5" fontWeight="bold" gutterBottom>
                    💡 Pro Tips
                  </Typography>
                  {singleGuide.proTips.map((tip, index) => (
                    <Box key={index} sx={{ mb: 2 }}>
                      <Typography variant="subtitle1" fontWeight="bold">
                        {tip.title}
                        {tip.premiumOnly && !isPremium && (
                          <Chip label="PREMIUM" size="small" color="secondary" sx={{ ml: 1 }} />
                        )}
                      </Typography>
                      {(!tip.premiumOnly || isPremium) && (
                        <Typography variant="body2" color="text.secondary">
                          {tip.content}
                        </Typography>
                      )}
                    </Box>
                  ))}
                </CardContent>
              </Card>
            )}
            
            {/* Common Mistakes */}
            {singleGuide.commonMistakes && singleGuide.commonMistakes.length > 0 && (
              <Card sx={{ mb: 3 }}>
                <CardContent>
                  <Typography variant="h5" fontWeight="bold" gutterBottom>
                    ⚠️ Częste błędy
                  </Typography>
                  {singleGuide.commonMistakes.map((mistake, index) => (
                    <Accordion key={index}>
                      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography fontWeight="bold" color="error">
                          {mistake.mistake}
                        </Typography>
                      </AccordionSummary>
                      <AccordionDetails>
                        <Typography variant="body2" paragraph>
                          <strong>Konsekwencja:</strong> {mistake.consequence}
                        </Typography>
                        <Typography variant="body2" paragraph>
                          <strong>Rozwiązanie:</strong> {mistake.solution}
                        </Typography>
                        {mistake.prevention && (
                          <Typography variant="body2">
                            <strong>Zapobieganie:</strong> {mistake.prevention}
                          </Typography>
                        )}
                      </AccordionDetails>
                    </Accordion>
                  ))}
                </CardContent>
              </Card>
            )}
          </Grid>
          
          {/* Sidebar */}
          <Grid size={{ xs: 12, md: 4 }}>
            {/* Recommended styles */}
            {singleGuide.recommendedForStyles && singleGuide.recommendedForStyles.length > 0 && (
              <Card sx={{ mb: 3 }}>
                <CardContent>
                  <Typography variant="h6" fontWeight="bold" gutterBottom>
                    <PizzaIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                    Polecane dla stylów
                  </Typography>
                  <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                    {singleGuide.recommendedForStyles.map((style) => (
                      <Chip key={style} label={style} variant="outlined" />
                    ))}
                  </Box>
                </CardContent>
              </Card>
            )}
            
            {/* Related techniques */}
            {singleGuide.relatedTechniques && singleGuide.relatedTechniques.length > 0 && (
              <Card>
                <CardContent>
                  <Typography variant="h6" fontWeight="bold" gutterBottom>
                    <BookIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                    Powiązane techniki
                  </Typography>
                  <List dense>
                    {singleGuide.relatedTechniques.map((slug) => (
                      <ListItemButton
                        key={slug}
                        onClick={() => navigate(`/knowledge/${slug}`)}
                      >
                        <ListItemText primary={slug.replace(/-/g, ' ')} />
                      </ListItemButton>
                    ))}
                  </List>
                </CardContent>
              </Card>
            )}
          </Grid>
        </Grid>
      </Container>
    );
  }
  
  // Main knowledge base view
  return (
    <Box sx={{ bgcolor: 'grey.50', minHeight: '100vh', pb: 8 }}>
      {/* Header */}
      <Box
        sx={{
          py: 6,
          background: `linear-gradient(135deg, ${theme.palette.primary.main}, ${theme.palette.secondary.main})`,
          color: 'white',
        }}
      >
        <Container maxWidth="lg">
          <Typography variant="h3" fontWeight="bold" gutterBottom>
            📚 Baza Wiedzy
          </Typography>
          <Typography variant="h6" sx={{ opacity: 0.9, mb: 3 }}>
            Przewodniki, techniki i sekrety pizzy od ekspertów
          </Typography>
          
          {/* Search */}
          <TextField
            fullWidth
            placeholder="Szukaj przewodników... (np. poolish, kulkowanie, fermentacja)"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            sx={{
              maxWidth: 600,
              bgcolor: 'white',
              borderRadius: 2,
              '& .MuiOutlinedInput-root': {
                '& fieldset': { border: 'none' },
              },
            }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
          />
        </Container>
      </Box>
      
      <Container maxWidth="lg" sx={{ mt: -4 }}>
        {/* Categories */}
        <Paper sx={{ p: 2, mb: 4, borderRadius: 2 }}>
          <Typography variant="subtitle2" color="text.secondary" gutterBottom>
            Kategorie
          </Typography>
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            <Chip
              label="Wszystkie"
              color={selectedCategory === null ? 'primary' : 'default'}
              onClick={() => setSelectedCategory(null)}
            />
            {categories?.map((cat) => (
              <Chip
                key={cat.id}
                label={`${cat.name} (${cat.guideCount})`}
                color={selectedCategory === cat.id ? 'primary' : 'default'}
                onClick={() => setSelectedCategory(cat.id)}
              />
            ))}
          </Box>
        </Paper>
        
        {/* Loading */}
        {guidesLoading && (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
            <CircularProgress />
          </Box>
        )}
        
        {/* Guides grid */}
        <Grid container spacing={3}>
          {guides?.map((guide, index) => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={guide.id}>
              <MotionCard
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.05 }}
                whileHover={{ y: -5, boxShadow: '0 8px 30px rgba(0,0,0,0.12)' }}
                sx={{
                  height: '100%',
                  cursor: 'pointer',
                  position: 'relative',
                  overflow: 'hidden',
                }}
                onClick={() => navigate(`/knowledge/${guide.slug}`)}
              >
                {guide.premium && !isPremium && (
                  <Box
                    sx={{
                      position: 'absolute',
                      top: 10,
                      right: 10,
                      bgcolor: 'secondary.main',
                      color: 'white',
                      px: 1,
                      py: 0.5,
                      borderRadius: 1,
                      fontSize: '0.75rem',
                      fontWeight: 'bold',
                    }}
                  >
                    <LockIcon sx={{ fontSize: 12, mr: 0.5, verticalAlign: 'middle' }} />
                    PREMIUM
                  </Box>
                )}
                
                <CardContent>
                  <Chip
                    label={guide.category.replace('_', ' ')}
                    size="small"
                    variant="outlined"
                    sx={{ mb: 1 }}
                  />
                  <Typography variant="h6" fontWeight="bold" gutterBottom>
                    {guide.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    {guide.shortDescription}
                  </Typography>
                  
                  <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                    <Chip
                      size="small"
                      label={getDifficultyLabel(guide.difficulty)}
                      color={getDifficultyColor(guide.difficulty) as any}
                    />
                    <Chip
                      size="small"
                      icon={<TimerIcon />}
                      label={formatDuration(guide.estimatedTimeMinutes)}
                      variant="outlined"
                    />
                  </Box>
                </CardContent>
              </MotionCard>
            </Grid>
          ))}
        </Grid>
        
        {/* Empty state */}
        {guides?.length === 0 && (
          <Box sx={{ textAlign: 'center', py: 8 }}>
            <Typography variant="h6" color="text.secondary">
              Nie znaleziono przewodników
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Spróbuj zmienić kryteria wyszukiwania
            </Typography>
          </Box>
        )}
        
        {/* Popular guides */}
        {!searchQuery && !selectedCategory && popularGuides && popularGuides.length > 0 && (
          <Box sx={{ mt: 6 }}>
            <Typography variant="h5" fontWeight="bold" gutterBottom>
              🔥 Popularne przewodniki
            </Typography>
            <List>
              {popularGuides.slice(0, 5).map((guide) => (
                <ListItem
                  key={guide.id}
                  component={Paper}
                  sx={{ mb: 1, cursor: 'pointer' }}
                  onClick={() => navigate(`/knowledge/${guide.slug}`)}
                >
                  <ListItemIcon>
                    <BookIcon color="primary" />
                  </ListItemIcon>
                  <ListItemText
                    primary={guide.title}
                    secondary={guide.shortDescription}
                  />
                  <Chip
                    label={`${guide.viewCount} wyświetleń`}
                    size="small"
                    variant="outlined"
                  />
                </ListItem>
              ))}
            </List>
          </Box>
        )}
      </Container>
    </Box>
  );
};

export default KnowledgeBasePage;
