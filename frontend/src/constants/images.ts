/**
 * Stałe z linkami do obrazków z Unsplash (darmowe, licencja komercyjna)
 * 
 * Unsplash License: https://unsplash.com/license
 * "All photos can be downloaded and used for free for commercial and non-commercial purposes"
 */

export const IMAGES = {
  // Hero backgrounds
  hero: {
    main: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=1920&q=80', // Pizza z góry
    neapolitan: 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=1920&q=80', // Pizza neapolitańska
    dough: 'https://images.unsplash.com/photo-1586985289688-ca3cf47d3e6e?w=1920&q=80', // Ciasto
    oven: 'https://images.unsplash.com/photo-1571997478779-2adcbbe9ab2f?w=1920&q=80', // Piec do pizzy
  },

  // Style pizzy - karty
  styles: {
    neapolitan: 'https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?w=800&q=80',
    newYork: 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=800&q=80',
    roman: 'https://images.unsplash.com/photo-1593560708920-61dd98c46a4e?w=800&q=80',
    detroit: 'https://images.unsplash.com/photo-1628840042765-356cda07504e?w=800&q=80',
    chicago: 'https://images.unsplash.com/photo-1551782450-17144efb9c50?w=800&q=80',
    sicilian: 'https://images.unsplash.com/photo-1600028068383-ea11a7a101f3?w=800&q=80',
    focaccia: 'https://images.unsplash.com/photo-1619535860434-ba1d8fa12536?w=800&q=80',
    margherita: 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=800&q=80',
  },

  // Składniki
  ingredients: {
    flour: 'https://images.unsplash.com/photo-1556155092-490a1ba16284?w=600&q=80',
    water: 'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=600&q=80',
    yeast: 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600&q=80',
    salt: 'https://images.unsplash.com/photo-1518110925495-5fe2fda0442c?w=600&q=80',
    oil: 'https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&q=80',
    tomatoes: 'https://images.unsplash.com/photo-1546470427-f5e4fa6e6c6c?w=600&q=80',
    mozzarella: 'https://images.unsplash.com/photo-1486297678162-eb2a19b0a32d?w=600&q=80',
    basil: 'https://images.unsplash.com/photo-1618164435735-413d3b066c9a?w=600&q=80',
  },

  // Proces
  process: {
    mixing: 'https://images.unsplash.com/photo-1495147466023-ac5c588e2e94?w=600&q=80',
    kneading: 'https://images.unsplash.com/photo-1586985289688-ca3cf47d3e6e?w=600&q=80',
    proofing: 'https://images.unsplash.com/photo-1603363415687-ebe2eecc52f7?w=600&q=80',
    shaping: 'https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=600&q=80',
    baking: 'https://images.unsplash.com/photo-1571997478779-2adcbbe9ab2f?w=600&q=80',
    finished: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=600&q=80',
  },

  // Piece
  ovens: {
    woodFired: 'https://images.unsplash.com/photo-1571997478779-2adcbbe9ab2f?w=600&q=80',
    homeOven: 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=600&q=80',
    ooni: 'https://images.unsplash.com/photo-1617343251846-5d0f1c4d0f4c?w=600&q=80',
  },

  // Avatars i ikony
  avatars: {
    chef: 'https://images.unsplash.com/photo-1577219491135-ce391730fb2c?w=400&q=80',
    pizzaiolo: 'https://images.unsplash.com/photo-1583394293214-28ez9e8c0aff?w=400&q=80',
  },

  // Tła
  backgrounds: {
    pattern: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=1920&q=80',
    kitchen: 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=1920&q=80',
    restaurant: 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=1920&q=80',
    wood: 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=1920&q=80',
  },

  // Płatności i premium
  premium: {
    crown: 'https://images.unsplash.com/photo-1578269174936-2709b6aeb913?w=400&q=80',
    star: 'https://images.unsplash.com/photo-1614107151491-6876eecbff89?w=400&q=80',
  },
};

// Mapowanie stylów pizzy na obrazki
export const PIZZA_STYLE_IMAGES: Record<string, string> = {
  NEAPOLITAN: IMAGES.styles.neapolitan,
  NEW_YORK: IMAGES.styles.newYork,
  ROMAN: IMAGES.styles.roman,
  DETROIT: IMAGES.styles.detroit,
  CHICAGO_DEEP_DISH: IMAGES.styles.chicago,
  SICILIAN: IMAGES.styles.sicilian,
  FOCACCIA: IMAGES.styles.focaccia,
  PIZZA_BIANCA: IMAGES.styles.roman,
  GRANDMA: IMAGES.styles.sicilian,
  PAN: IMAGES.styles.detroit,
  CUSTOM: IMAGES.styles.margherita,
};

// Mapowanie typów pieców na obrazki
export const OVEN_IMAGES: Record<string, string> = {
  WOOD_FIRED: IMAGES.ovens.woodFired,
  HOME_OVEN: IMAGES.ovens.homeOven,
  HOME_OVEN_WITH_STONE: IMAGES.ovens.homeOven,
  HOME_OVEN_WITH_STEEL: IMAGES.ovens.homeOven,
  ELECTRIC_PIZZA_OVEN: IMAGES.ovens.ooni,
  GAS_PIZZA_OVEN: IMAGES.ovens.ooni,
  DECK_OVEN: IMAGES.ovens.woodFired,
  CONVEYOR_OVEN: IMAGES.ovens.homeOven,
};
