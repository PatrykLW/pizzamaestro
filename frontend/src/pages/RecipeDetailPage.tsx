import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  Grid,
  Chip,
  Button,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  Table,
  TableBody,
  TableCell,
  TableRow,
  IconButton,
  CircularProgress,
  Alert,
} from '@mui/material';
import {
  ArrowBack,
  CheckCircle,
  RadioButtonUnchecked,
  Schedule,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { recipesApi } from '../services/api';
import { format } from 'date-fns';
import { pl } from 'date-fns/locale';

const RecipeDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: recipe, isLoading, error } = useQuery({
    queryKey: ['recipe', id],
    queryFn: () => recipesApi.getRecipe(id!),
    enabled: !!id,
  });

  const completeStepMutation = useMutation({
    mutationFn: (stepNumber: number) => recipesApi.completeStep(id!, stepNumber),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recipe', id] });
    },
  });

  if (isLoading) {
    return (
      <Container maxWidth="lg" sx={{ py: 4, textAlign: 'center' }}>
        <CircularProgress />
      </Container>
    );
  }

  if (error || !recipe) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error">Nie znaleziono receptury</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Button
        startIcon={<ArrowBack />}
        onClick={() => navigate('/recipes')}
        sx={{ mb: 3 }}
      >
        Powrót do listy
      </Button>

      <Box sx={{ mb: 4 }}>
        <Typography variant="h3" fontWeight="bold" gutterBottom>
          {recipe.name}
        </Typography>
        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
          <Chip label={recipe.pizzaStyle?.replace('_', ' ')} color="primary" />
          <Chip label={`${recipe.numberOfPizzas} pizz`} />
          <Chip label={`${recipe.hydration}% hydratacji`} variant="outlined" />
          <Chip label={`${recipe.totalFermentationHours}h fermentacji`} variant="outlined" />
        </Box>
      </Box>

      <Grid container spacing={4}>
        {/* Składniki */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Składniki
              </Typography>
              <Table>
                <TableBody>
                  <TableRow>
                    <TableCell>Mąka</TableCell>
                    <TableCell align="right">
                      <strong>{recipe.calculatedRecipe?.flourGrams}g</strong>
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell>Woda</TableCell>
                    <TableCell align="right">
                      <strong>{recipe.calculatedRecipe?.waterGrams}g</strong>
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell>Sól</TableCell>
                    <TableCell align="right">
                      <strong>{recipe.calculatedRecipe?.saltGrams}g</strong>
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell>Drożdże</TableCell>
                    <TableCell align="right">
                      <strong>{recipe.calculatedRecipe?.yeastGrams}g</strong>
                    </TableCell>
                  </TableRow>
                  {(recipe.calculatedRecipe?.oilGrams ?? 0) > 0 && (
                    <TableRow>
                      <TableCell>Oliwa</TableCell>
                      <TableCell align="right">
                        <strong>{recipe.calculatedRecipe?.oilGrams}g</strong>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                Całkowita waga ciasta: {recipe.calculatedRecipe?.totalDoughWeight}g
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* Info */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Parametry
              </Typography>
              <Table size="small">
                <TableBody>
                  <TableRow>
                    <TableCell>Typ drożdży</TableCell>
                    <TableCell align="right">{recipe.yeastType}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell>Metoda fermentacji</TableCell>
                    <TableCell align="right">{recipe.fermentationMethod}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell>Temp. pokojowa</TableCell>
                    <TableCell align="right">{recipe.roomTemperature}°C</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell>Temp. lodówki</TableCell>
                    <TableCell align="right">{recipe.fridgeTemperature}°C</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell>Typ pieca</TableCell>
                    <TableCell align="right">{recipe.ovenType}</TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </Grid>

        {/* Harmonogram */}
        {recipe.fermentationSteps && recipe.fermentationSteps.length > 0 && (
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  <Schedule sx={{ mr: 1, verticalAlign: 'middle' }} />
                  Harmonogram fermentacji
                </Typography>
                <Stepper orientation="vertical">
                  {recipe.fermentationSteps.map((step: any, index: number) => (
                    <Step key={index} active completed={step.completed}>
                      <StepLabel
                        StepIconComponent={() =>
                          step.completed ? (
                            <CheckCircle color="success" />
                          ) : (
                            <RadioButtonUnchecked color="action" />
                          )
                        }
                      >
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                          <Typography variant="subtitle1">{step.title}</Typography>
                          {!step.completed && (
                            <Button
                              size="small"
                              variant="outlined"
                              onClick={() => completeStepMutation.mutate(step.stepNumber)}
                            >
                              Oznacz jako wykonane
                            </Button>
                          )}
                        </Box>
                        {step.scheduledTime && (
                          <Typography variant="caption" color="text.secondary">
                            {format(new Date(step.scheduledTime), 'dd.MM.yyyy HH:mm', { locale: pl })}
                          </Typography>
                        )}
                      </StepLabel>
                      <StepContent>
                        <Typography variant="body2">{step.description}</Typography>
                        {step.temperature && (
                          <Chip
                            size="small"
                            label={`${step.temperature}°C`}
                            sx={{ mt: 1 }}
                          />
                        )}
                      </StepContent>
                    </Step>
                  ))}
                </Stepper>
              </CardContent>
            </Card>
          </Grid>
        )}

        {/* Notatki */}
        {recipe.notes && (
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Notatki
                </Typography>
                <Typography>{recipe.notes}</Typography>
              </CardContent>
            </Card>
          </Grid>
        )}
      </Grid>
    </Container>
  );
};

export default RecipeDetailPage;
