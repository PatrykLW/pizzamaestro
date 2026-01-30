import React from 'react';
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  Grid,
  Chip,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Table,
  TableBody,
  TableCell,
  TableRow,
  Skeleton,
  Alert,
} from '@mui/material';
import { ExpandMore } from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { calculatorApi } from '../services/api';
import type { PizzaStyleDefaults, PizzaStyleOven } from '../types';

// Rozszerzony typ dla stylu z API (zawiera dodatkowe pola - wymagane)
interface StyleWithDetails {
  id: string;
  name: string;
  description: string;
  defaults: PizzaStyleDefaults;
  recommendedOven: PizzaStyleOven;
}

const StylesGuidePage: React.FC = () => {
  const { data: styles, isLoading, error } = useQuery({
    queryKey: ['styles'],
    queryFn: calculatorApi.getStyles,
  });

  if (isLoading) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Box sx={{ mb: 4, textAlign: 'center' }}>
          <Skeleton variant="text" width={300} height={60} sx={{ mx: 'auto' }} />
          <Skeleton variant="text" width={500} height={30} sx={{ mx: 'auto' }} />
        </Box>
        {[1, 2, 3].map((i) => (
          <Skeleton key={i} variant="rounded" height={80} sx={{ mb: 2 }} />
        ))}
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error">Błąd podczas ładowania stylów pizzy</Alert>
      </Container>
    );
  }

  // Rzutowanie na rozszerzony typ (API zwraca więcej danych)
  const stylesWithDetails = styles as unknown as StyleWithDetails[];

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ mb: 4, textAlign: 'center' }}>
        <Typography variant="h3" fontWeight="bold" gutterBottom>
          Przewodnik po stylach pizzy
        </Typography>
        <Typography variant="h6" color="text.secondary" sx={{ maxWidth: 600, mx: 'auto' }}>
          Poznaj różne style pizzy z całego świata i ich charakterystyczne parametry
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {stylesWithDetails?.map((style: StyleWithDetails) => (
          <Grid item xs={12} key={style.id}>
            <Accordion>
              <AccordionSummary expandIcon={<ExpandMore />}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, width: '100%', pr: 2 }}>
                  <Typography variant="h6">{style.name}</Typography>
                  <Box sx={{ flexGrow: 1 }} />
                  <Chip
                    label={`${style.defaults.hydration}%`}
                    size="small"
                    color="primary"
                    variant="outlined"
                  />
                  <Chip
                    label={`${style.defaults.fermentationHours}h`}
                    size="small"
                    variant="outlined"
                  />
                </Box>
              </AccordionSummary>
              <AccordionDetails>
                <Grid container spacing={3}>
                  <Grid item xs={12} md={6}>
                    <Typography variant="body1" paragraph>
                      {style.description}
                    </Typography>
                    
                    <Typography variant="subtitle2" gutterBottom sx={{ mt: 2 }}>
                      Domyślne parametry:
                    </Typography>
                    <Table size="small">
                      <TableBody>
                        <TableRow>
                          <TableCell>Nawodnienie</TableCell>
                          <TableCell align="right">
                            {style.defaults.hydration}% 
                            <Typography variant="caption" color="text.secondary" sx={{ ml: 1 }}>
                              (zakres: {style.defaults.hydrationMin}-{style.defaults.hydrationMax}%)
                            </Typography>
                          </TableCell>
                        </TableRow>
                        <TableRow>
                          <TableCell>Waga kulki</TableCell>
                          <TableCell align="right">{style.defaults.ballWeight}g</TableCell>
                        </TableRow>
                        <TableRow>
                          <TableCell>Czas fermentacji</TableCell>
                          <TableCell align="right">{style.defaults.fermentationHours}h</TableCell>
                        </TableRow>
                        <TableRow>
                          <TableCell>Sól</TableCell>
                          <TableCell align="right">{style.defaults.saltPercentage}%</TableCell>
                        </TableRow>
                        <TableRow>
                          <TableCell>Oliwa</TableCell>
                          <TableCell align="right">{style.defaults.oilPercentage}%</TableCell>
                        </TableRow>
                        <TableRow>
                          <TableCell>Cukier</TableCell>
                          <TableCell align="right">{style.defaults.sugarPercentage}%</TableCell>
                        </TableRow>
                      </TableBody>
                    </Table>
                  </Grid>
                  
                  <Grid item xs={12} md={6}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography variant="subtitle2" gutterBottom>
                          Zalecany wypiek
                        </Typography>
                        <Typography variant="body2">
                          <strong>Piec:</strong> {style.recommendedOven.name}
                        </Typography>
                        <Typography variant="body2">
                          <strong>Temperatura:</strong> {style.recommendedOven.temperature}°C
                        </Typography>
                        <Typography variant="body2">
                          <strong>Czas pieczenia:</strong> {Math.round(style.recommendedOven.bakingTime / 60)} min
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                </Grid>
              </AccordionDetails>
            </Accordion>
          </Grid>
        ))}
      </Grid>

      <Card sx={{ mt: 4, p: 3 }}>
        <Typography variant="h6" gutterBottom>
          Ogólne wskazówki
        </Typography>
        <Grid container spacing={2}>
          <Grid item xs={12} md={4}>
            <Typography variant="subtitle2" color="primary" gutterBottom>
              Niska hydratacja (50-60%)
            </Typography>
            <Typography variant="body2">
              Łatwiejsze ciasto do formowania. Idealne dla początkujących. 
              Stosowane w stylu nowojorskim i Chicago.
            </Typography>
          </Grid>
          <Grid item xs={12} md={4}>
            <Typography variant="subtitle2" color="primary" gutterBottom>
              Średnia hydratacja (60-70%)
            </Typography>
            <Typography variant="body2">
              Balans między łatwością obsługi a lekkością ciasta. 
              Typowe dla pizzy neapolitańskiej.
            </Typography>
          </Grid>
          <Grid item xs={12} md={4}>
            <Typography variant="subtitle2" color="primary" gutterBottom>
              Wysoka hydratacja (70%+)
            </Typography>
            <Typography variant="body2">
              Lekkie, puszyste ciasto z dużymi bąblami. 
              Wymaga doświadczenia. Idealne dla focaccii i pizzy rzymskiej.
            </Typography>
          </Grid>
        </Grid>
      </Card>
    </Container>
  );
};

export default StylesGuidePage;
