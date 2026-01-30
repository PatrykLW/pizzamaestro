import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Chip,
  IconButton,
  Skeleton,
  Alert,
} from '@mui/material';
import {
  Favorite,
  FavoriteBorder,
  Delete,
  ContentCopy,
  OpenInNew,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { recipesApi } from '../services/api';
import type { Recipe } from '../types';
import toast from 'react-hot-toast';
import { format } from 'date-fns';
import { pl } from 'date-fns/locale';

const RecipesPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: recipesData, isLoading, error } = useQuery({
    queryKey: ['recipes'],
    queryFn: () => recipesApi.getRecipes(0, 50),
  });

  const toggleFavoriteMutation = useMutation({
    mutationFn: recipesApi.toggleFavorite,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recipes'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: recipesApi.deleteRecipe,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recipes'] });
      toast.success('Receptura usunięta');
    },
  });

  const cloneMutation = useMutation({
    mutationFn: recipesApi.cloneRecipe,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['recipes'] });
      toast.success('Receptura skopiowana');
      navigate(`/recipes/${data.id}`);
    },
  });

  const recipes = recipesData?.content || [];

  if (isLoading) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Grid container spacing={3}>
          {[1, 2, 3, 4].map((i) => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={i}>
              <Skeleton variant="rounded" height={200} />
            </Grid>
          ))}
        </Grid>
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error">Błąd podczas ładowania receptur</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h3" fontWeight="bold" gutterBottom>
          Moje receptury
        </Typography>
        <Typography color="text.secondary">
          Historia Twoich kalkulacji i zapisanych receptur
        </Typography>
      </Box>

      {recipes.length === 0 ? (
        <Card sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            Nie masz jeszcze żadnych zapisanych receptur
          </Typography>
          <Button
            variant="contained"
            onClick={() => navigate('/calculator')}
            sx={{ mt: 2 }}
          >
            Utwórz pierwszą recepturę
          </Button>
        </Card>
      ) : (
        <Grid container spacing={3}>
          {recipes.map((recipe: Recipe) => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={recipe.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardContent sx={{ flexGrow: 1 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <Typography variant="h6" gutterBottom>
                      {recipe.name}
                    </Typography>
                    <IconButton
                      size="small"
                      onClick={() => toggleFavoriteMutation.mutate(recipe.id)}
                    >
                      {recipe.isFavorite ? (
                        <Favorite color="error" />
                      ) : (
                        <FavoriteBorder />
                      )}
                    </IconButton>
                  </Box>
                  
                  <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 2 }}>
                    <Chip
                      size="small"
                      label={recipe.pizzaStyle?.replace('_', ' ')}
                      color="primary"
                    />
                    <Chip
                      size="small"
                      label={`${recipe.numberOfPizzas} pizz`}
                    />
                    <Chip
                      size="small"
                      label={`${recipe.hydration}%`}
                      variant="outlined"
                    />
                  </Box>

                  <Typography variant="body2" color="text.secondary">
                    {(recipe.calculation?.flourAmount || recipe.calculatedRecipe?.flourAmount)}g mąki • 
                    {(recipe.calculation?.waterAmount || recipe.calculatedRecipe?.waterAmount)}g wody • 
                    {(recipe.calculation?.yeastAmount || recipe.calculatedRecipe?.yeastAmount)}g drożdży
                  </Typography>

                  <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 2 }}>
                    {format(new Date(recipe.createdAt), 'dd MMMM yyyy, HH:mm', { locale: pl })}
                  </Typography>
                </CardContent>
                
                <CardActions sx={{ justifyContent: 'space-between', px: 2, pb: 2 }}>
                  <Button
                    size="small"
                    startIcon={<OpenInNew />}
                    onClick={() => navigate(`/recipes/${recipe.id}`)}
                  >
                    Szczegóły
                  </Button>
                  <Box>
                    <IconButton
                      size="small"
                      onClick={() => cloneMutation.mutate(recipe.id)}
                      title="Kopiuj"
                    >
                      <ContentCopy fontSize="small" />
                    </IconButton>
                    <IconButton
                      size="small"
                      color="error"
                      onClick={() => {
                        if (window.confirm('Czy na pewno usunąć tę recepturę?')) {
                          deleteMutation.mutate(recipe.id);
                        }
                      }}
                      title="Usuń"
                    >
                      <Delete fontSize="small" />
                    </IconButton>
                  </Box>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Container>
  );
};

export default RecipesPage;
