// Skrypt inicjalizacji bazy danych MongoDB dla PizzaMaestro
// Uruchamiany automatycznie przy pierwszym starcie kontenera

print('=== Inicjalizacja bazy danych PizzaMaestro ===');

// Przełącz na bazę pizzamaestro
db = db.getSiblingDB('pizzamaestro');

// Utwórz użytkownika aplikacji
db.createUser({
  user: 'pizzamaestro_app',
  pwd: 'AppPassword2024!',
  roles: [
    { role: 'readWrite', db: 'pizzamaestro' }
  ]
});

print('Utworzono użytkownika bazy danych');

// ===== KOLEKCJA: users =====
print('Tworzenie użytkowników...');

// Admin - hasło: Admin123!@#
db.users.insertOne({
  _id: ObjectId('65f0000000000000000admin'),
  email: 'admin@pizzamaestro.pl',
  password: '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X.VG0PjK1HhAh4pXK',
  firstName: 'Admin',
  lastName: 'PizzaMaestro',
  phoneNumber: '+48123456789',
  phoneVerified: true,
  roles: ['ROLE_ADMIN', 'ROLE_USER', 'ROLE_PREMIUM'],
  accountType: 'PRO',
  premiumExpiresAt: new Date('2030-12-31'),
  preferences: {
    language: 'pl',
    theme: 'light',
    temperatureUnit: 'CELSIUS',
    weightUnit: 'GRAMS',
    emailNotifications: true,
    smsNotifications: true,
    pushNotifications: true,
    defaultPizzaStyle: 'NEAPOLITAN'
  },
  usageStats: {
    totalCalculations: 150,
    calculationsThisMonth: 25,
    totalPizzasBaked: 87,
    smsUsedThisMonth: 5,
    lastCalculationAt: new Date(),
    monthResetAt: new Date()
  },
  enabled: true,
  emailVerified: true,
  createdAt: new Date('2024-01-01'),
  updatedAt: new Date(),
  lastLoginAt: new Date()
});

// Użytkownik testowy FREE - hasło: Test123!@#
db.users.insertOne({
  _id: ObjectId('65f0000000000000000test1'),
  email: 'test@pizzamaestro.pl',
  password: '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
  firstName: 'Jan',
  lastName: 'Kowalski',
  phoneNumber: '+48987654321',
  phoneVerified: false,
  roles: ['ROLE_USER'],
  accountType: 'FREE',
  premiumExpiresAt: null,
  preferences: {
    language: 'pl',
    theme: 'light',
    temperatureUnit: 'CELSIUS',
    weightUnit: 'GRAMS',
    emailNotifications: true,
    smsNotifications: false,
    pushNotifications: true,
    defaultPizzaStyle: 'NEAPOLITAN'
  },
  usageStats: {
    totalCalculations: 8,
    calculationsThisMonth: 3,
    totalPizzasBaked: 5,
    smsUsedThisMonth: 0,
    lastCalculationAt: new Date(),
    monthResetAt: new Date()
  },
  enabled: true,
  emailVerified: true,
  createdAt: new Date('2024-06-15'),
  updatedAt: new Date(),
  lastLoginAt: new Date()
});

// Użytkownik testowy PREMIUM - hasło: Premium123!@#
db.users.insertOne({
  _id: ObjectId('65f0000000000000premium'),
  email: 'premium@pizzamaestro.pl',
  password: '$2a$12$N.GVDL7bLTdEPBCh6KXP6.YLMQ5XJZ8HHLp7JHiNlvJZP7hK6pXe6',
  firstName: 'Anna',
  lastName: 'Nowak',
  phoneNumber: '+48555666777',
  phoneVerified: true,
  roles: ['ROLE_USER', 'ROLE_PREMIUM'],
  accountType: 'PREMIUM',
  premiumExpiresAt: new Date('2025-12-31'),
  preferences: {
    language: 'pl',
    theme: 'dark',
    temperatureUnit: 'CELSIUS',
    weightUnit: 'GRAMS',
    emailNotifications: true,
    smsNotifications: true,
    pushNotifications: true,
    defaultPizzaStyle: 'ROMAN'
  },
  usageStats: {
    totalCalculations: 45,
    calculationsThisMonth: 12,
    totalPizzasBaked: 32,
    smsUsedThisMonth: 8,
    lastCalculationAt: new Date(),
    monthResetAt: new Date()
  },
  enabled: true,
  emailVerified: true,
  createdAt: new Date('2024-03-01'),
  updatedAt: new Date(),
  lastLoginAt: new Date()
});

print('Utworzono 3 użytkowników (admin, test FREE, test PREMIUM)');

// ===== KOLEKCJA: ingredients - MĄKI =====
print('Tworzenie bazy składników...');

db.ingredients.insertMany([
  // MĄKI WŁOSKIE
  {
    type: 'FLOUR',
    name: 'Caputo Pizzeria',
    brand: 'Caputo',
    description: 'Profesjonalna mąka do pizzy neapolitańskiej. Klasyka z Neapolu używana w najlepszych pizzeriach.',
    country: 'Włochy',
    imageUrl: '/images/flours/caputo-pizzeria.jpg',
    verified: true,
    active: true,
    flourParameters: {
      flourType: 'TYPE_00',
      grainType: 'pszenna',
      proteinContent: 12.5,
      strength: 260,
      extensibility: 0.55,
      ashContent: 0.55,
      recommendedHydrationMin: 58,
      recommendedHydrationMax: 65,
      recommendedStyles: ['NEAPOLITAN', 'ROMAN'],
      notes: 'Idealna do tradycyjnej pizzy neapolitańskiej. Daje elastyczne ciasto z charakterystycznym brzegiem.'
    }
  },
  {
    type: 'FLOUR',
    name: 'Caputo Cuoco',
    brand: 'Caputo',
    description: 'Mąka dla profesjonalnych pizzaiolo. Mocniejsza wersja idealna do długiej fermentacji.',
    country: 'Włochy',
    imageUrl: '/images/flours/caputo-cuoco.jpg',
    verified: true,
    active: true,
    flourParameters: {
      flourType: 'TYPE_00',
      grainType: 'pszenna',
      proteinContent: 13.0,
      strength: 300,
      extensibility: 0.50,
      ashContent: 0.55,
      recommendedHydrationMin: 60,
      recommendedHydrationMax: 70,
      recommendedStyles: ['NEAPOLITAN'],
      notes: 'Wytrzymuje długą fermentację (48-72h). Dla zaawansowanych pizzaiolo.'
    }
  },
  {
    type: 'FLOUR',
    name: 'Caputo Nuvola',
    brand: 'Caputo',
    description: 'Mąka do lekkiego, puszystego ciasta z dużymi bąblami powietrza.',
    country: 'Włochy',
    imageUrl: '/images/flours/caputo-nuvola.jpg',
    verified: true,
    active: true,
    flourParameters: {
      flourType: 'TYPE_00',
      grainType: 'pszenna',
      proteinContent: 13.5,
      strength: 280,
      extensibility: 0.60,
      ashContent: 0.55,
      recommendedHydrationMin: 65,
      recommendedHydrationMax: 75,
      recommendedStyles: ['NEAPOLITAN', 'FOCACCIA', 'PIZZA_BIANCA'],
      notes: 'Nazwa "Nuvola" (chmura) odzwierciedla lekkość ciasta. Idealna do wysokiej hydratacji.'
    }
  },
  {
    type: 'FLOUR',
    name: 'Le 5 Stagioni Superiore',
    brand: 'Le 5 Stagioni',
    description: 'Wysokiej jakości włoska mąka do pizzy i focaccii.',
    country: 'Włochy',
    verified: true,
    active: true,
    flourParameters: {
      flourType: 'TYPE_00',
      grainType: 'pszenna',
      proteinContent: 12.0,
      strength: 250,
      recommendedHydrationMin: 55,
      recommendedHydrationMax: 65,
      recommendedStyles: ['NEAPOLITAN', 'NEW_YORK'],
      notes: 'Dobra alternatywa dla Caputo. Nieco niższa cena przy porównywalnej jakości.'
    }
  },
  {
    type: 'FLOUR',
    name: 'Manitoba Cream',
    brand: 'Molino Grassi',
    description: 'Bardzo mocna mąka Manitoba do ciast wysokohydratowanych i długich fermentacji.',
    country: 'Włochy',
    verified: true,
    active: true,
    flourParameters: {
      flourType: 'MANITOBA',
      grainType: 'pszenna',
      proteinContent: 14.5,
      strength: 380,
      recommendedHydrationMin: 65,
      recommendedHydrationMax: 80,
      recommendedStyles: ['PIZZA_BIANCA', 'FOCACCIA', 'ROMAN'],
      notes: 'Idealna do mieszanek z słabszymi mąkami. Wytrzymuje nawet 80% hydratacji.'
    }
  },
  // MĄKI POLSKIE
  {
    type: 'FLOUR',
    name: 'Lubella Tipo 00 Pizza',
    brand: 'Lubella',
    description: 'Polska mąka do pizzy w stylu włoskim. Dobry stosunek jakości do ceny.',
    country: 'Polska',
    verified: true,
    active: true,
    flourParameters: {
      flourType: 'TYPE_00',
      grainType: 'pszenna',
      proteinContent: 11.5,
      recommendedHydrationMin: 55,
      recommendedHydrationMax: 62,
      recommendedStyles: ['NEW_YORK', 'DETROIT', 'PAN'],
      notes: 'Dostępna w większości supermarketów. Dobra na początek przygody z pizzą.'
    }
  },
  {
    type: 'FLOUR',
    name: 'Mąka Poznańska Typ 500',
    brand: 'Polskie Młyny',
    description: 'Uniwersalna polska mąka pszenna typ 500.',
    country: 'Polska',
    verified: true,
    active: true,
    flourParameters: {
      flourType: 'ALL_PURPOSE',
      grainType: 'pszenna',
      proteinContent: 10.5,
      recommendedHydrationMin: 55,
      recommendedHydrationMax: 60,
      recommendedStyles: ['NEW_YORK', 'PAN', 'GRANDMA'],
      notes: 'Podstawowa mąka do nauki. Niższa hydratacja dla łatwiejszej obsługi.'
    }
  },
  // MĄKI AMERYKAŃSKIE
  {
    type: 'FLOUR',
    name: 'King Arthur Bread Flour',
    brand: 'King Arthur',
    description: 'Amerykańska mąka chlebowa, popularna do pizzy nowojorskiej.',
    country: 'USA',
    verified: true,
    active: true,
    flourParameters: {
      flourType: 'BREAD_FLOUR',
      grainType: 'pszenna',
      proteinContent: 12.7,
      recommendedHydrationMin: 58,
      recommendedHydrationMax: 68,
      recommendedStyles: ['NEW_YORK', 'DETROIT', 'GRANDMA'],
      notes: 'Standard w amerykańskich pizzeriach. Daje charakterystyczny żółtawy kolor.'
    }
  },
  {
    type: 'FLOUR',
    name: 'King Arthur All-Purpose',
    brand: 'King Arthur',
    description: 'Uniwersalna mąka amerykańska.',
    country: 'USA',
    verified: true,
    active: true,
    flourParameters: {
      flourType: 'ALL_PURPOSE',
      grainType: 'pszenna',
      proteinContent: 11.7,
      recommendedHydrationMin: 55,
      recommendedHydrationMax: 62,
      recommendedStyles: ['NEW_YORK', 'PAN'],
      notes: 'Można mieszać z bread flour dla uzyskania pożądanej siły.'
    }
  }
]);

// ===== WODY =====
db.ingredients.insertMany([
  {
    type: 'WATER',
    name: 'Żywiec Zdrój',
    brand: 'Żywiec Zdrój',
    description: 'Polska woda źródlana o umiarkowanej mineralizacji.',
    country: 'Polska',
    verified: true,
    active: true,
    waterParameters: {
      hardness: 120,
      hardnessLevel: 'SOFT',
      ph: 7.4,
      mineralContent: 350,
      calcium: 35,
      magnesium: 8,
      sodium: 5,
      chloride: 3,
      source: 'źródlana',
      notes: 'Dobra woda do pizzy. Umiarkowana twardość nie wpływa znacząco na fermentację.'
    }
  },
  {
    type: 'WATER',
    name: 'Cisowianka',
    brand: 'Cisowianka',
    description: 'Polska woda mineralna z Cieszyna.',
    country: 'Polska',
    verified: true,
    active: true,
    waterParameters: {
      hardness: 200,
      hardnessLevel: 'MEDIUM',
      ph: 7.6,
      mineralContent: 540,
      calcium: 55,
      magnesium: 18,
      sodium: 12,
      source: 'mineralna',
      notes: 'Średnio twarda woda. Może lekko spowalniać fermentację.'
    }
  },
  {
    type: 'WATER',
    name: 'Volvic',
    brand: 'Volvic',
    description: 'Francuska woda wulkaniczna, bardzo miękka.',
    country: 'Francja',
    verified: true,
    active: true,
    waterParameters: {
      hardness: 60,
      hardnessLevel: 'VERY_SOFT',
      ph: 7.0,
      mineralContent: 130,
      calcium: 12,
      magnesium: 8,
      sodium: 12,
      source: 'wulkaniczna',
      notes: 'Idealna do pizzy. Miękka woda sprzyja szybkiej fermentacji.'
    }
  },
  {
    type: 'WATER',
    name: 'Evian',
    brand: 'Evian',
    description: 'Francuska woda alpejska.',
    country: 'Francja',
    verified: true,
    active: true,
    waterParameters: {
      hardness: 300,
      hardnessLevel: 'HARD',
      ph: 7.2,
      mineralContent: 309,
      calcium: 80,
      magnesium: 26,
      sodium: 6.5,
      source: 'alpejska',
      notes: 'Twarda woda - może spowalniać fermentację. Dobrze działa przy krótkich czasach.'
    }
  },
  {
    type: 'WATER',
    name: 'Woda kranowa (filtrowana)',
    brand: 'Lokalna',
    description: 'Przeciętna woda kranowa po przefiltrowaniu.',
    country: 'Polska',
    verified: true,
    active: true,
    waterParameters: {
      hardness: 180,
      hardnessLevel: 'MEDIUM',
      ph: 7.5,
      mineralContent: 350,
      calcium: 45,
      magnesium: 12,
      sodium: 15,
      source: 'kranowa filtrowana',
      notes: 'Filtrowanie usuwa chlor który może hamować drożdże. Sprawdź twardość lokalnej wody.'
    }
  }
]);

print('Utworzono 14 składników (9 mąk, 5 wód)');

// ===== KOLEKCJA: recipes - PRZYKŁADOWE RECEPTURY =====
print('Tworzenie przykładowych receptur...');

db.recipes.insertMany([
  {
    userId: '65f0000000000000000test1',
    name: 'Moja pierwsza neapolitańska',
    description: 'Pierwsza próba pizzy neapolitańskiej - wyszła świetnie!',
    favorite: true,
    isPublic: false,
    pizzaStyle: 'NEAPOLITAN',
    numberOfPizzas: 4,
    ballWeight: 250,
    hydration: 65,
    saltPercentage: 2.8,
    oilPercentage: 0,
    sugarPercentage: 0,
    yeastType: 'FRESH',
    yeastPercentage: 0.15,
    fermentationMethod: 'COLD_FERMENTATION',
    totalFermentationHours: 24,
    roomTemperature: 22,
    fridgeTemperature: 4,
    ovenType: 'HOME_OVEN_WITH_STEEL',
    ovenTemperature: 280,
    usePreferment: false,
    calculatedRecipe: {
      totalDoughWeight: 1000,
      flourGrams: 590,
      waterGrams: 384,
      saltGrams: 16.5,
      yeastGrams: 0.9,
      oilGrams: 0,
      sugarGrams: 0,
      bakerPercentages: {
        flour: 100,
        water: 65,
        salt: 2.8,
        yeast: 0.15,
        oil: 0,
        sugar: 0
      }
    },
    fermentationSteps: [
      {
        stepNumber: 1,
        stepType: 'MIX_DOUGH',
        title: 'Mieszanie składników',
        description: 'Rozpuść drożdże w wodzie, dodaj mąkę i wymieszaj',
        scheduledTime: new Date('2024-01-20T10:00:00'),
        durationMinutes: 10,
        completed: true,
        completedAt: new Date('2024-01-20T10:12:00')
      },
      {
        stepNumber: 2,
        stepType: 'KNEAD',
        title: 'Wyrabianie',
        description: 'Wyrabiaj ciasto przez 10-15 minut',
        scheduledTime: new Date('2024-01-20T10:10:00'),
        durationMinutes: 15,
        completed: true,
        completedAt: new Date('2024-01-20T10:28:00')
      }
    ],
    notes: 'Ciasto wyszło świetnie! Następnym razem spróbuję 68% hydratacji.',
    rating: 5,
    feedback: 'Cornicione puszysty, spód chrupiący. Idealna pizza!',
    tags: ['neapolitańska', 'pierwsza', 'udana'],
    createdAt: new Date('2024-01-20'),
    updatedAt: new Date('2024-01-21'),
    version: 1
  },
  {
    userId: '65f0000000000000premium',
    name: 'Pizza Romana - eksperyment',
    description: 'Test pizzy rzymskiej z wysoką hydratacją',
    favorite: false,
    isPublic: true,
    pizzaStyle: 'ROMAN',
    numberOfPizzas: 2,
    ballWeight: 220,
    hydration: 75,
    saltPercentage: 2.5,
    oilPercentage: 3,
    sugarPercentage: 0,
    yeastType: 'INSTANT_DRY',
    fermentationMethod: 'COLD_FERMENTATION',
    totalFermentationHours: 48,
    roomTemperature: 21,
    fridgeTemperature: 4,
    ovenType: 'ELECTRIC_PIZZA_OVEN',
    ovenTemperature: 380,
    usePreferment: true,
    prefermentType: 'POOLISH',
    prefermentPercentage: 30,
    prefermentFermentationHours: 12,
    calculatedRecipe: {
      totalDoughWeight: 440,
      flourGrams: 245,
      waterGrams: 184,
      saltGrams: 6.1,
      yeastGrams: 0.2,
      oilGrams: 7.4,
      sugarGrams: 0,
      bakerPercentages: {
        flour: 100,
        water: 75,
        salt: 2.5,
        yeast: 0.08,
        oil: 3,
        sugar: 0
      }
    },
    notes: 'Poolish dał niesamowity smak! Ciasto bardzo lekkie.',
    rating: 4,
    tags: ['rzymska', 'poolish', 'eksperyment'],
    createdAt: new Date('2024-02-10'),
    updatedAt: new Date('2024-02-11'),
    version: 1
  },
  {
    userId: '65f0000000000000000admin',
    name: 'Focaccia na imprezę',
    description: 'Focaccia na 20 osób z rozmarynem i oliwą',
    favorite: true,
    isPublic: true,
    pizzaStyle: 'FOCACCIA',
    numberOfPizzas: 6,
    ballWeight: 350,
    hydration: 78,
    saltPercentage: 2.5,
    oilPercentage: 6,
    sugarPercentage: 0,
    yeastType: 'FRESH',
    fermentationMethod: 'MIXED',
    totalFermentationHours: 18,
    roomTemperature: 23,
    fridgeTemperature: 4,
    ovenType: 'HOME_OVEN',
    ovenTemperature: 230,
    usePreferment: false,
    calculatedRecipe: {
      totalDoughWeight: 2100,
      flourGrams: 1120,
      waterGrams: 874,
      saltGrams: 28,
      yeastGrams: 5.6,
      oilGrams: 67,
      sugarGrams: 0,
      bakerPercentages: {
        flour: 100,
        water: 78,
        salt: 2.5,
        yeast: 0.5,
        oil: 6,
        sugar: 0
      }
    },
    notes: 'Sprawdzony przepis na imprezy. Wszyscy pytają o recepturę!',
    rating: 5,
    tags: ['focaccia', 'impreza', 'ulubiona'],
    createdAt: new Date('2024-03-15'),
    updatedAt: new Date('2024-03-15'),
    version: 1
  }
]);

print('Utworzono 3 przykładowe receptury');

// ===== INDEKSY =====
print('Tworzenie indeksów...');

db.users.createIndex({ email: 1 }, { unique: true });
db.users.createIndex({ verificationToken: 1 }, { sparse: true });
db.users.createIndex({ resetPasswordToken: 1 }, { sparse: true });

db.recipes.createIndex({ userId: 1 });
db.recipes.createIndex({ pizzaStyle: 1 });
db.recipes.createIndex({ isPublic: 1 });
db.recipes.createIndex({ createdAt: -1 });

db.ingredients.createIndex({ type: 1, active: 1 });
db.ingredients.createIndex({ 'flourParameters.flourType': 1 });

db.notifications.createIndex({ userId: 1, scheduledTime: 1 });
db.notifications.createIndex({ recipeId: 1 });
db.notifications.createIndex({ status: 1, scheduledTime: 1 });

print('Utworzono indeksy');

print('');
print('=== Inicjalizacja zakończona pomyślnie! ===');
print('');
print('Dane logowania:');
print('-------------------');
print('ADMIN:   admin@pizzamaestro.pl / Admin123!@#');
print('USER:    test@pizzamaestro.pl / Test123!@#');
print('PREMIUM: premium@pizzamaestro.pl / Premium123!@#');
print('');
