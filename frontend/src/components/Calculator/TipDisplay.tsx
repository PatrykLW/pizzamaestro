import React, { useEffect, useState, useCallback } from 'react';
import { logger } from '../../utils/logger';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Chip,
  Collapse,
  IconButton,
  Alert,
  AlertTitle,
  Divider,
  Skeleton,
  alpha,
  useTheme,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  Info as InfoIcon,
  Warning as WarningIcon,
  Lightbulb as LightbulbIcon,
  Science as ScienceIcon,
  SwapHoriz as ChangeIcon,
  Close as CloseIcon,
} from '@mui/icons-material';
import { motion, AnimatePresence as FramerAnimatePresence } from 'framer-motion';
import { tipApi, Tip, TipCollection, TipContext } from '../../services/api';
import { useDebounce } from '../../hooks/useDebounce';

// Wrapper to fix TypeScript compatibility issue with framer-motion
const AnimatePresence = FramerAnimatePresence as React.FC<React.PropsWithChildren<{ mode?: 'wait' | 'sync' | 'popLayout'; initial?: boolean }>>;

const MotionCard = motion(Card);

interface TipDisplayProps {
  context: TipContext;
  compact?: boolean;
  showWarningsOnly?: boolean;
  maxTips?: number;
  onTipClick?: (tip: Tip) => void;
}

/**
 * Komponent wyświetlający interaktywne wskazówki.
 * 
 * Używaj:
 * - W kalkulatorze do pokazywania kontekstowych tipów
 * - Przy zmianie parametrów do wyjaśniania wpływu zmian
 * - W bazie wiedzy jako podpowiedzi
 */
const TipDisplay: React.FC<TipDisplayProps> = ({
  context,
  compact = false,
  showWarningsOnly = false,
  maxTips = 5,
  onTipClick,
}) => {
  const theme = useTheme();
  const [tips, setTips] = useState<TipCollection | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [expandedTips, setExpandedTips] = useState<Set<number>>(new Set());
  const [dismissedTips, setDismissedTips] = useState<Set<number>>(new Set());
  
  // Debounce context changes to avoid too many API calls
  const debouncedContext = useDebounce(context, 500);
  
  // Fetch tips when context changes
  const fetchTips = useCallback(async () => {
    if (!debouncedContext.pizzaStyle && !debouncedContext.hydration) {
      return; // Don't fetch without basic context
    }
    
    setLoading(true);
    setError(null);
    
    try {
      const result = await tipApi.getAllTips(debouncedContext);
      setTips(result);
      setDismissedTips(new Set()); // Reset dismissed tips on new context
    } catch (err) {
      logger.error('Error fetching tips:', err);
      setError('Nie udało się pobrać wskazówek');
    } finally {
      setLoading(false);
    }
  }, [debouncedContext]);
  
  useEffect(() => {
    fetchTips();
  }, [fetchTips]);
  
  const toggleExpand = (index: number) => {
    setExpandedTips(prev => {
      const newSet = new Set(prev);
      if (newSet.has(index)) {
        newSet.delete(index);
      } else {
        newSet.add(index);
      }
      return newSet;
    });
  };
  
  const dismissTip = (index: number) => {
    setDismissedTips(prev => new Set(prev).add(index));
  };
  
  const getTipIcon = (type: Tip['type']) => {
    switch (type) {
      case 'INFO':
        return <InfoIcon />;
      case 'WARNING':
        return <WarningIcon />;
      case 'RECOMMENDATION':
        return <LightbulbIcon />;
      case 'SCIENCE':
        return <ScienceIcon />;
      case 'CHANGE_EXPLANATION':
        return <ChangeIcon />;
      default:
        return <InfoIcon />;
    }
  };
  
  const getTipColor = (type: Tip['type']): 'info' | 'warning' | 'success' | 'error' => {
    switch (type) {
      case 'INFO':
        return 'info';
      case 'WARNING':
        return 'warning';
      case 'RECOMMENDATION':
        return 'success';
      case 'SCIENCE':
        return 'info';
      case 'CHANGE_EXPLANATION':
        return 'info';
      default:
        return 'info';
    }
  };
  
  const getCategoryLabel = (category: string) => {
    const labels: Record<string, string> = {
      STYLE: 'Styl',
      HYDRATION: 'Hydratacja',
      FERMENTATION: 'Fermentacja',
      FLOUR: 'Mąka',
      TEMPERATURE: 'Temperatura',
      WEATHER: 'Pogoda',
      PREFERMENT: 'Preferment',
      OPTIMIZATION: 'Optymalizacja',
    };
    return labels[category] || category;
  };
  
  // Combine and filter tips
  const allTips = tips ? [
    ...(showWarningsOnly ? [] : tips.tips),
    ...tips.warnings,
    ...(showWarningsOnly ? [] : tips.recommendations),
  ] : [];
  
  const visibleTips = allTips
    .filter((_, index) => !dismissedTips.has(index))
    .slice(0, maxTips);
  
  if (loading) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
        {[1, 2, 3].map(i => (
          <Skeleton key={i} variant="rounded" height={compact ? 40 : 80} />
        ))}
      </Box>
    );
  }
  
  if (error) {
    return (
      <Alert severity="error" variant="outlined">
        {error}
      </Alert>
    );
  }
  
  if (!tips || visibleTips.length === 0) {
    return null;
  }
  
  // Compact mode - just chips
  if (compact) {
    return (
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
        {visibleTips.map((tip, index) => (
          <Chip
            key={index}
            icon={getTipIcon(tip.type)}
            label={tip.title}
            color={getTipColor(tip.type)}
            variant="outlined"
            size="small"
            onClick={() => onTipClick?.(tip)}
            sx={{ cursor: onTipClick ? 'pointer' : 'default' }}
          />
        ))}
      </Box>
    );
  }
  
  // Full mode - cards
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      {/* Context Summary */}
      {tips.contextSummary && (
        <Typography variant="caption" color="text.secondary" sx={{ whiteSpace: 'pre-line' }}>
          {tips.contextSummary}
        </Typography>
      )}
      
      {/* Warnings first */}
      <AnimatePresence>
        {tips.warnings.length > 0 && (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            {tips.warnings
              .filter((_, i) => !dismissedTips.has(i))
              .map((tip, index) => (
                <motion.div
                  key={`warning-${index}`}
                  initial={{ opacity: 0, y: -10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, x: -100 }}
                  transition={{ duration: 0.2 }}
                >
                  <Alert
                    severity="warning"
                    icon={<WarningIcon />}
                    action={
                      <IconButton
                        size="small"
                        onClick={() => dismissTip(index)}
                        aria-label="Zamknij wskazówkę"
                      >
                        <CloseIcon fontSize="small" />
                      </IconButton>
                    }
                  >
                    <AlertTitle>{tip.title}</AlertTitle>
                    <Typography variant="body2">{tip.content}</Typography>
                    {tip.suggestion && (
                      <Typography variant="body2" sx={{ mt: 1, fontWeight: 'bold' }}>
                        💡 {tip.suggestion}
                      </Typography>
                    )}
                  </Alert>
                </motion.div>
              ))}
          </Box>
        )}
      </AnimatePresence>
      
      {/* Regular tips */}
      {!showWarningsOnly && (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
          {tips.tips
            .filter((_, i) => !dismissedTips.has(i + tips.warnings.length))
            .slice(0, maxTips - tips.warnings.length)
            .map((tip, index) => {
              const globalIndex = index + tips.warnings.length;
              const isExpanded = expandedTips.has(globalIndex);
              
              return (
                <MotionCard
                  key={`tip-${index}`}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: index * 0.1 }}
                  sx={{
                    bgcolor: alpha(theme.palette[getTipColor(tip.type)].main, 0.05),
                    border: `1px solid ${alpha(theme.palette[getTipColor(tip.type)].main, 0.2)}`,
                    cursor: tip.details ? 'pointer' : 'default',
                  }}
                  onClick={() => tip.details && toggleExpand(globalIndex)}
                >
                  <CardContent sx={{ py: 1.5, '&:last-child': { pb: 1.5 } }}>
                    <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
                      <Typography sx={{ fontSize: '1.2rem' }}>{tip.icon}</Typography>
                      <Box sx={{ flex: 1 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                          <Typography variant="subtitle2" fontWeight="bold">
                            {tip.title}
                          </Typography>
                          <Chip
                            label={getCategoryLabel(tip.category)}
                            size="small"
                            variant="outlined"
                            sx={{ height: 20, fontSize: '0.7rem' }}
                          />
                        </Box>
                        <Typography variant="body2" color="text.secondary" sx={{ whiteSpace: 'pre-line' }}>
                          {tip.content}
                        </Typography>
                        
                        <Collapse in={isExpanded}>
                          {tip.details && (
                            <Box sx={{ mt: 1, pt: 1, borderTop: '1px solid', borderColor: 'divider' }}>
                              <Typography variant="body2" sx={{ whiteSpace: 'pre-line' }}>
                                {tip.details}
                              </Typography>
                            </Box>
                          )}
                        </Collapse>
                      </Box>
                      
                      {tip.details && (
                        <IconButton size="small" aria-label={isExpanded ? 'Zwiń szczegóły' : 'Rozwiń szczegóły'}>
                          <ExpandMoreIcon
                            sx={{
                              transform: isExpanded ? 'rotate(180deg)' : 'none',
                              transition: 'transform 0.2s',
                            }}
                          />
                        </IconButton>
                      )}
                    </Box>
                  </CardContent>
                </MotionCard>
              );
            })}
        </Box>
      )}
      
      {/* Recommendations */}
      {!showWarningsOnly && tips.recommendations.length > 0 && (
        <Box sx={{ mt: 1 }}>
          <Divider sx={{ mb: 1 }}>
            <Chip label="Rekomendacje" size="small" icon={<LightbulbIcon />} />
          </Divider>
          {tips.recommendations
            .filter((_, i) => !dismissedTips.has(i + tips.warnings.length + tips.tips.length))
            .map((tip, index) => (
              <Alert
                key={`rec-${index}`}
                severity="success"
                icon={<LightbulbIcon />}
                sx={{ mb: 1 }}
              >
                <Typography variant="body2">
                  <strong>{tip.title}:</strong> {tip.content}
                </Typography>
              </Alert>
            ))}
        </Box>
      )}
    </Box>
  );
};

export default TipDisplay;
