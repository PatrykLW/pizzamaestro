import React, { useState } from 'react';
import { Link as RouterLink, useNavigate, useLocation } from 'react-router-dom';
import {
  AppBar,
  Box,
  Toolbar,
  IconButton,
  Typography,
  Menu,
  Container,
  Avatar,
  Button,
  Tooltip,
  MenuItem,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Divider,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import {
  Menu as MenuIcon,
  LocalPizza as PizzaIcon,
  Calculate as CalculateIcon,
  MenuBook as RecipesIcon,
  Person as PersonIcon,
  Login as LoginIcon,
  Logout as LogoutIcon,
  Info as InfoIcon,
  School as SchoolIcon,
  Timer as TimerIcon,
} from '@mui/icons-material';
import { useAuthStore } from '../../store/authStore';

interface LayoutProps {
  children: React.ReactNode;
}

const pages = [
  { label: 'Kalkulator', path: '/calculator', icon: <CalculateIcon /> },
  { label: 'Style pizzy', path: '/styles', icon: <InfoIcon /> },
  { label: 'Baza wiedzy', path: '/knowledge', icon: <SchoolIcon /> },
];

const authPages = [
  { label: 'Moja pizza', path: '/active-pizza', icon: <TimerIcon /> },
  { label: 'Moje receptury', path: '/recipes', icon: <RecipesIcon /> },
];

export const Layout: React.FC<LayoutProps> = ({ children }) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const navigate = useNavigate();
  const location = useLocation();
  
  const { isAuthenticated, user, logout } = useAuthStore();
  
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorElUser, setAnchorElUser] = useState<null | HTMLElement>(null);

  const handleOpenUserMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorElUser(event.currentTarget);
  };

  const handleCloseUserMenu = () => {
    setAnchorElUser(null);
  };

  const handleLogout = () => {
    logout();
    handleCloseUserMenu();
    navigate('/');
  };

  const drawer = (
    <Box sx={{ width: 280, pt: 2 }}>
      <Box sx={{ px: 2, pb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
        <PizzaIcon sx={{ color: 'primary.main', fontSize: 32 }} />
        <Typography variant="h6" fontWeight="bold">
          PizzaMaestro
        </Typography>
      </Box>
      <Divider />
      <List>
        {pages.map((page) => (
          <ListItem key={page.path} disablePadding>
            <ListItemButton
              component={RouterLink}
              to={page.path}
              selected={location.pathname === page.path}
              onClick={() => setMobileOpen(false)}
            >
              <ListItemIcon>{page.icon}</ListItemIcon>
              <ListItemText primary={page.label} />
            </ListItemButton>
          </ListItem>
        ))}
        
        {isAuthenticated && (
          <>
            <Divider sx={{ my: 1 }} />
            {authPages.map((page) => (
              <ListItem key={page.path} disablePadding>
                <ListItemButton
                  component={RouterLink}
                  to={page.path}
                  selected={location.pathname === page.path}
                  onClick={() => setMobileOpen(false)}
                >
                  <ListItemIcon>{page.icon}</ListItemIcon>
                  <ListItemText primary={page.label} />
                </ListItemButton>
              </ListItem>
            ))}
          </>
        )}
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar 
        position="sticky" 
        elevation={0}
        sx={{ 
          bgcolor: 'white', 
          color: 'text.primary',
          borderBottom: '1px solid',
          borderColor: 'divider',
        }}
      >
        <Container maxWidth="xl">
          <Toolbar disableGutters>
            {/* Mobile menu button */}
            {isMobile && (
              <IconButton
                size="large"
                onClick={() => setMobileOpen(true)}
                sx={{ mr: 2 }}
              >
                <MenuIcon />
              </IconButton>
            )}

            {/* Logo */}
            <Box
              component={RouterLink}
              to="/"
              sx={{
                display: 'flex',
                alignItems: 'center',
                textDecoration: 'none',
                color: 'inherit',
                mr: 4,
              }}
            >
              <PizzaIcon sx={{ color: 'primary.main', fontSize: 36, mr: 1 }} />
              <Typography
                variant="h5"
                noWrap
                sx={{
                  fontFamily: '"Playfair Display", serif',
                  fontWeight: 700,
                  color: 'primary.main',
                  display: { xs: 'none', sm: 'block' },
                }}
              >
                PizzaMaestro
              </Typography>
            </Box>

            {/* Desktop navigation */}
            {!isMobile && (
              <Box sx={{ flexGrow: 1, display: 'flex', gap: 1 }}>
                {pages.map((page) => (
                  <Button
                    key={page.path}
                    component={RouterLink}
                    to={page.path}
                    startIcon={page.icon}
                    sx={{
                      color: location.pathname === page.path ? 'primary.main' : 'text.primary',
                      fontWeight: location.pathname === page.path ? 600 : 400,
                    }}
                  >
                    {page.label}
                  </Button>
                ))}
                
                {isAuthenticated && authPages.map((page) => (
                  <Button
                    key={page.path}
                    component={RouterLink}
                    to={page.path}
                    startIcon={page.icon}
                    sx={{
                      color: location.pathname === page.path ? 'primary.main' : 'text.primary',
                      fontWeight: location.pathname === page.path ? 600 : 400,
                    }}
                  >
                    {page.label}
                  </Button>
                ))}
              </Box>
            )}

            {/* Spacer */}
            <Box sx={{ flexGrow: 1 }} />

            {/* User menu or login button */}
            {isAuthenticated ? (
              <Box sx={{ flexGrow: 0 }}>
                <Tooltip title="Otwórz menu">
                  <IconButton onClick={handleOpenUserMenu} sx={{ p: 0 }}>
                    <Avatar 
                      sx={{ bgcolor: 'primary.main' }}
                    >
                      {user?.firstName?.[0] || user?.email?.[0]?.toUpperCase()}
                    </Avatar>
                  </IconButton>
                </Tooltip>
                <Menu
                  sx={{ mt: '45px' }}
                  anchorEl={anchorElUser}
                  anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
                  keepMounted
                  transformOrigin={{ vertical: 'top', horizontal: 'right' }}
                  open={Boolean(anchorElUser)}
                  onClose={handleCloseUserMenu}
                >
                  <MenuItem onClick={() => { navigate('/profile'); handleCloseUserMenu(); }}>
                    <ListItemIcon><PersonIcon /></ListItemIcon>
                    <ListItemText>Profil</ListItemText>
                  </MenuItem>
                  <Divider />
                  <MenuItem onClick={handleLogout}>
                    <ListItemIcon><LogoutIcon /></ListItemIcon>
                    <ListItemText>Wyloguj</ListItemText>
                  </MenuItem>
                </Menu>
              </Box>
            ) : (
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button
                  component={RouterLink}
                  to="/login"
                  variant="outlined"
                  startIcon={<LoginIcon />}
                >
                  Zaloguj
                </Button>
                <Button
                  component={RouterLink}
                  to="/register"
                  variant="contained"
                  sx={{ display: { xs: 'none', sm: 'flex' } }}
                >
                  Zarejestruj
                </Button>
              </Box>
            )}
          </Toolbar>
        </Container>
      </AppBar>

      {/* Mobile drawer */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={() => setMobileOpen(false)}
        ModalProps={{ keepMounted: true }}
      >
        {drawer}
      </Drawer>

      {/* Main content */}
      <Box component="main" sx={{ flexGrow: 1 }}>
        {children}
      </Box>

      {/* Footer */}
      <Box
        component="footer"
        sx={{
          py: 4,
          px: 2,
          mt: 'auto',
          bgcolor: 'grey.100',
          borderTop: '1px solid',
          borderColor: 'divider',
        }}
      >
        <Container maxWidth="lg">
          <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, justifyContent: 'space-between', alignItems: 'center', gap: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <PizzaIcon sx={{ color: 'primary.main' }} />
              <Typography variant="body2" color="text.secondary">
                © 2024 PizzaMaestro. Wszystkie prawa zastrzeżone.
              </Typography>
            </Box>
            <Typography variant="body2" color="text.secondary">
              Najdokładniejszy kalkulator do pizzy
            </Typography>
          </Box>
        </Container>
      </Box>
    </Box>
  );
};
