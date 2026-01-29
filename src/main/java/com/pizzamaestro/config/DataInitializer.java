package com.pizzamaestro.config;

import com.pizzamaestro.model.Ingredient;
import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.Recipe;
import com.pizzamaestro.model.User;
import com.pizzamaestro.repository.IngredientRepository;
import com.pizzamaestro.repository.RecipeRepository;
import com.pizzamaestro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Inicjalizacja danych początkowych w bazie.
 * Tworzy użytkowników testowych, składniki i przykładowe receptury.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        initializeUsers();
        initializeIngredients();
        initializeRecipes();
    }
    
    private void initializeUsers() {
        if (userRepository.count() > 0) {
            log.info("Użytkownicy już istnieją, pomijam inicjalizację");
            return;
        }
        
        log.info("Tworzenie użytkowników testowych...");
        
        // ADMIN
        User admin = User.builder()
                .email("admin@pizzamaestro.pl")
                .password(passwordEncoder.encode("Admin123!@#"))
                .firstName("Admin")
                .lastName("PizzaMaestro")
                .phoneNumber("+48123456789")
                .phoneVerified(true)
                .roles(Set.of(User.Role.ROLE_ADMIN, User.Role.ROLE_USER, User.Role.ROLE_PREMIUM))
                .accountType(User.AccountType.PRO)
                .premiumExpiresAt(LocalDateTime.of(2030, 12, 31, 23, 59))
                .enabled(true)
                .emailVerified(true)
                .preferences(User.UserPreferences.builder()
                        .language("pl")
                        .theme("light")
                        .temperatureUnit(User.TemperatureUnit.CELSIUS)
                        .weightUnit(User.WeightUnit.GRAMS)
                        .emailNotifications(true)
                        .smsNotifications(true)
                        .pushNotifications(true)
                        .defaultPizzaStyle(PizzaStyle.NEAPOLITAN)
                        .build())
                .usageStats(User.UsageStats.builder()
                        .totalCalculations(150)
                        .calculationsThisMonth(25)
                        .totalPizzasBaked(87)
                        .smsUsedThisMonth(5)
                        .lastCalculationAt(LocalDateTime.now())
                        .monthResetAt(LocalDateTime.now())
                        .build())
                .build();
        userRepository.save(admin);
        
        // USER FREE
        User testUser = User.builder()
                .email("test@pizzamaestro.pl")
                .password(passwordEncoder.encode("Test123!@#"))
                .firstName("Jan")
                .lastName("Kowalski")
                .phoneNumber("+48987654321")
                .phoneVerified(false)
                .roles(Set.of(User.Role.ROLE_USER))
                .accountType(User.AccountType.FREE)
                .enabled(true)
                .emailVerified(true)
                .preferences(User.UserPreferences.builder()
                        .language("pl")
                        .theme("light")
                        .temperatureUnit(User.TemperatureUnit.CELSIUS)
                        .weightUnit(User.WeightUnit.GRAMS)
                        .emailNotifications(true)
                        .smsNotifications(false)
                        .pushNotifications(true)
                        .defaultPizzaStyle(PizzaStyle.NEAPOLITAN)
                        .build())
                .usageStats(User.UsageStats.builder()
                        .totalCalculations(8)
                        .calculationsThisMonth(3)
                        .totalPizzasBaked(5)
                        .smsUsedThisMonth(0)
                        .lastCalculationAt(LocalDateTime.now())
                        .monthResetAt(LocalDateTime.now())
                        .build())
                .build();
        userRepository.save(testUser);
        
        // USER PREMIUM
        User premiumUser = User.builder()
                .email("premium@pizzamaestro.pl")
                .password(passwordEncoder.encode("Premium123!@#"))
                .firstName("Anna")
                .lastName("Nowak")
                .phoneNumber("+48555666777")
                .phoneVerified(true)
                .roles(Set.of(User.Role.ROLE_USER, User.Role.ROLE_PREMIUM))
                .accountType(User.AccountType.PREMIUM)
                .premiumExpiresAt(LocalDateTime.of(2025, 12, 31, 23, 59))
                .enabled(true)
                .emailVerified(true)
                .preferences(User.UserPreferences.builder()
                        .language("pl")
                        .theme("dark")
                        .temperatureUnit(User.TemperatureUnit.CELSIUS)
                        .weightUnit(User.WeightUnit.GRAMS)
                        .emailNotifications(true)
                        .smsNotifications(true)
                        .pushNotifications(true)
                        .defaultPizzaStyle(PizzaStyle.ROMAN)
                        .build())
                .usageStats(User.UsageStats.builder()
                        .totalCalculations(45)
                        .calculationsThisMonth(12)
                        .totalPizzasBaked(32)
                        .smsUsedThisMonth(8)
                        .lastCalculationAt(LocalDateTime.now())
                        .monthResetAt(LocalDateTime.now())
                        .build())
                .build();
        userRepository.save(premiumUser);
        
        log.info("Utworzono 3 użytkowników testowych:");
        log.info("  - admin@pizzamaestro.pl / Admin123!@#");
        log.info("  - test@pizzamaestro.pl / Test123!@#");
        log.info("  - premium@pizzamaestro.pl / Premium123!@#");
    }
    
    private void initializeIngredients() {
        if (ingredientRepository.count() > 0) {
            log.info("Składniki już istnieją, pomijam inicjalizację");
            return;
        }
        
        log.info("Tworzenie rozbudowanej bazy składników...");
        
        // ===============================================
        // MĄKI - Profesjonalna baza z parametrami technicznymi
        // ===============================================
        List<Ingredient> flours = Arrays.asList(
                // --------------- WŁOSKIE MĄKI CAPUTO ---------------
                createFlour("Caputo Pizzeria", "Caputo", "Włochy",
                        "Profesjonalna mąka do pizzy neapolitańskiej. Złoty standard w pizzeriach na całym świecie. " +
                        "Idealna do szybkiego pieczenia w piecu w wysokiej temperaturze.",
                        Ingredient.FlourType.TYPE_00, 12.5, 260.0, 58.0, 65.0, 0.55,
                        List.of(PizzaStyle.NEAPOLITAN, PizzaStyle.ROMAN)),
                        
                createFlour("Caputo Cuoco", "Caputo", "Włochy",
                        "Mąka dla profesjonalnych pizzaiolo z najwyższej jakości pszenicy. " +
                        "Wyższa siła glutenu W=300+ pozwala na długą fermentację 24-72h. " +
                        "Rekomendowana przez AVPN (Associazione Verace Pizza Napoletana).",
                        Ingredient.FlourType.TYPE_00, 13.0, 300.0, 60.0, 70.0, 0.55,
                        List.of(PizzaStyle.NEAPOLITAN)),
                        
                createFlour("Caputo Nuvola", "Caputo", "Włochy",
                        "Mąka 'Chmura' do lekkiego, puszystego ciasta z dużymi bąblami w cornicione. " +
                        "Specjalnie wyselekcjonowane ziarna dla maksymalnej lekkości. " +
                        "Idealna do canotto style (bardzo napuszony brzeg).",
                        Ingredient.FlourType.TYPE_00, 13.5, 280.0, 65.0, 75.0, 0.55,
                        List.of(PizzaStyle.NEAPOLITAN, PizzaStyle.FOCACCIA, PizzaStyle.PIZZA_BIANCA)),
                        
                createFlour("Caputo Saccorosso", "Caputo", "Włochy",
                        "Najwyższa jakość mąki Caputo w charakterystycznym czerwonym worku. " +
                        "W=300-320, idealna do 48-72h fermentacji. Daje najbardziej aromatyczne ciasto.",
                        Ingredient.FlourType.TYPE_00, 13.5, 310.0, 62.0, 72.0, 0.55,
                        List.of(PizzaStyle.NEAPOLITAN)),
                        
                createFlour("Caputo Fioreglut (bezglutenowa)", "Caputo", "Włochy",
                        "Bezglutenowa mąka Caputo do pizzy. Mieszanka ryżu, ziemniaków i włókna. " +
                        "Certyfikowana dla osób z celiakią. Wymaga innej techniki wyrabiania.",
                        Ingredient.FlourType.GLUTEN_FREE, 4.0, null, 60.0, 65.0, 0.50,
                        List.of(PizzaStyle.NEAPOLITAN, PizzaStyle.NEW_YORK)),
                        
                // --------------- WŁOSKIE MĄKI LE 5 STAGIONI ---------------
                createFlour("Le 5 Stagioni Superiore", "Le 5 Stagioni", "Włochy",
                        "Wysokiej jakości włoska mąka do pizzy z pszenicy włoskiej. " +
                        "W=220-240, odpowiednia dla fermentacji 8-24h.",
                        Ingredient.FlourType.TYPE_00, 12.0, 230.0, 55.0, 65.0, 0.55,
                        List.of(PizzaStyle.NEAPOLITAN, PizzaStyle.NEW_YORK)),
                        
                createFlour("Le 5 Stagioni Oro", "Le 5 Stagioni", "Włochy",
                        "Mąka premium z włoskiej pszenicy Manitoba. W=340-360 dla długiej fermentacji.",
                        Ingredient.FlourType.TYPE_00, 14.0, 350.0, 65.0, 75.0, 0.55,
                        List.of(PizzaStyle.NEAPOLITAN, PizzaStyle.ROMAN, PizzaStyle.FOCACCIA)),
                        
                createFlour("Le 5 Stagioni Integrale", "Le 5 Stagioni", "Włochy",
                        "Pełnoziarnista mąka włoska. Bogata w błonnik i minerały. " +
                        "Używaj w mieszance 20-30% z mąką białą.",
                        Ingredient.FlourType.WHOLE_WHEAT, 13.0, null, 68.0, 80.0, 1.40,
                        List.of(PizzaStyle.FOCACCIA, PizzaStyle.PIZZA_BIANCA)),
                        
                // --------------- WŁOSKIE MĄKI MOLINO GRASSI ---------------
                createFlour("Manitoba Cream", "Molino Grassi", "Włochy",
                        "Bardzo mocna mąka Manitoba do ciast wysokohydratowanych i bab. " +
                        "W=380+ zapewnia ekstremalną elastyczność glutenu.",
                        Ingredient.FlourType.MANITOBA, 14.5, 380.0, 65.0, 85.0, 0.50,
                        List.of(PizzaStyle.PIZZA_BIANCA, PizzaStyle.FOCACCIA, PizzaStyle.ROMAN)),
                        
                createFlour("Molino Grassi Tipo 1", "Molino Grassi", "Włochy",
                        "Półpełnoziarnista mąka włoska tipo 1. Więcej składników odżywczych niż tipo 00. " +
                        "Ciemniejsza barwa, głębszy smak.",
                        Ingredient.FlourType.TYPE_1, 12.5, 280.0, 60.0, 70.0, 0.65,
                        List.of(PizzaStyle.ROMAN, PizzaStyle.FOCACCIA)),
                        
                // --------------- WŁOSKIE MĄKI DALLAGIOVANNA ---------------
                createFlour("Dallagiovanna Classica", "Dallagiovanna", "Włochy",
                        "Profesjonalna mąka pizza z Parmy. Używana w wielu mistrzowskich pizzeriach.",
                        Ingredient.FlourType.TYPE_00, 12.0, 250.0, 58.0, 65.0, 0.55,
                        List.of(PizzaStyle.NEAPOLITAN, PizzaStyle.NEW_YORK)),
                        
                createFlour("Dallagiovanna Uniqua Blu", "Dallagiovanna", "Włochy",
                        "Premium mąka z błękitnego worka. Niebielona, zachowuje naturalne właściwości ziarna.",
                        Ingredient.FlourType.TYPE_0, 13.0, 290.0, 62.0, 72.0, 0.60,
                        List.of(PizzaStyle.NEAPOLITAN, PizzaStyle.ROMAN)),
                        
                // --------------- POLSKIE MĄKI ---------------
                createFlour("Lubella Tipo 00 Pizza", "Lubella", "Polska",
                        "Polska mąka do pizzy w stylu włoskim. Dobry stosunek jakości do ceny. " +
                        "Popularna wśród domowych pizzaiolo.",
                        Ingredient.FlourType.TYPE_00, 11.5, null, 55.0, 62.0, 0.55,
                        List.of(PizzaStyle.NEW_YORK, PizzaStyle.DETROIT)),
                        
                createFlour("Mąka Poznańska Typ 500", "Polskie Młyny", "Polska",
                        "Uniwersalna polska mąka pszenna typ 500. Podstawa polskiej kuchni.",
                        Ingredient.FlourType.ALL_PURPOSE, 10.5, null, 55.0, 60.0, 0.50,
                        List.of(PizzaStyle.NEW_YORK, PizzaStyle.PAN)),
                        
                createFlour("Mąka Krupczatka Typ 450", "Melvit", "Polska",
                        "Polska mąka krupczatka - delikatniejsza struktura. Dobre do cienkiego ciasta.",
                        Ingredient.FlourType.TYPE_00, 10.0, null, 52.0, 58.0, 0.45,
                        List.of(PizzaStyle.THIN_CRUST)),
                        
                createFlour("Mąka Orkiszowa Typ 700", "BioGol", "Polska",
                        "Polska mąka orkiszowa ekologiczna. Dla osób unikających nowoczesnej pszenicy.",
                        Ingredient.FlourType.SPELT, 14.0, null, 55.0, 65.0, 0.70,
                        List.of(PizzaStyle.FOCACCIA)),
                        
                createFlour("Mąka Razowa Pszenna Typ 2000", "Polskie Młyny", "Polska",
                        "Polska mąka razowa. 100% ziarna pszenicy. Używaj w mieszankach.",
                        Ingredient.FlourType.WHOLE_WHEAT, 14.0, null, 70.0, 85.0, 2.00,
                        List.of(PizzaStyle.FOCACCIA)),
                        
                // --------------- AMERYKAŃSKIE MĄKI ---------------
                createFlour("King Arthur Bread Flour", "King Arthur", "USA",
                        "Amerykańska mąka chlebowa, popularna do pizzy nowojorskiej. " +
                        "Wysokie białko 12.7% daje żuistą teksturę.",
                        Ingredient.FlourType.BREAD_FLOUR, 12.7, null, 58.0, 68.0, 0.54,
                        List.of(PizzaStyle.NEW_YORK, PizzaStyle.DETROIT, PizzaStyle.GRANDMA)),
                        
                createFlour("King Arthur All-Purpose", "King Arthur", "USA",
                        "Amerykańska mąka uniwersalna premium. Wyższa jakość niż typowe mąki AP.",
                        Ingredient.FlourType.ALL_PURPOSE, 11.7, null, 55.0, 63.0, 0.50,
                        List.of(PizzaStyle.NEW_YORK, PizzaStyle.PAN)),
                        
                createFlour("King Arthur Sir Lancelot", "King Arthur", "USA",
                        "Mąka high-gluten dla profesjonalistów. 14.2% białka dla maksymalnej struktury.",
                        Ingredient.FlourType.HIGH_GLUTEN, 14.2, null, 62.0, 72.0, 0.50,
                        List.of(PizzaStyle.NEW_YORK, PizzaStyle.DETROIT)),
                        
                createFlour("General Mills All Trumps", "General Mills", "USA",
                        "Mąka high-gluten używana w klasycznych pizzeriach NYC. Kultowy wybór.",
                        Ingredient.FlourType.HIGH_GLUTEN, 14.0, null, 60.0, 70.0, 0.50,
                        List.of(PizzaStyle.NEW_YORK)),
                        
                // --------------- SPECJALISTYCZNE ---------------
                createFlour("Semola Rimacinata", "De Cecco", "Włochy",
                        "Dwukrotnie mielona semolina z pszenicy durum. Dodaje złocisty kolor i chrupkość. " +
                        "Używaj 10-20% w mieszance z tipo 00.",
                        Ingredient.FlourType.SEMOLINA, 12.5, null, 55.0, 65.0, 0.75,
                        List.of(PizzaStyle.ROMAN, PizzaStyle.SICILIAN)),
                        
                createFlour("Farina di Farro (orkisz włoski)", "Mulino Marino", "Włochy",
                        "Włoska mąka orkiszowa z młyna Mulino Marino. Delikatny orzechowy smak.",
                        Ingredient.FlourType.SPELT, 14.5, null, 58.0, 68.0, 0.70,
                        List.of(PizzaStyle.FOCACCIA, PizzaStyle.PIZZA_BIANCA)),
                        
                // --------------- INNE EUROPEJSKIE ---------------
                createFlour("Weizenmehl Type 405", "Diamant", "Niemcy",
                        "Niemiecka mąka pszenna typ 405 - odpowiednik włoskiego tipo 00.",
                        Ingredient.FlourType.TYPE_00, 11.0, null, 55.0, 62.0, 0.405,
                        List.of(PizzaStyle.NEW_YORK)),
                        
                createFlour("Weizenmehl Type 550", "Aurora", "Niemcy",
                        "Niemiecka mąka typ 550 - bardziej uniwersalna, do chleba i pizzy.",
                        Ingredient.FlourType.ALL_PURPOSE, 11.5, null, 58.0, 65.0, 0.55,
                        List.of(PizzaStyle.NEW_YORK, PizzaStyle.FOCACCIA)),
                        
                createFlour("Farine T55", "Grands Moulins de Paris", "Francja",
                        "Francuska mąka T55 używana w boulangerie. Dobra do pizzy domowej.",
                        Ingredient.FlourType.ALL_PURPOSE, 11.0, null, 55.0, 62.0, 0.55,
                        List.of(PizzaStyle.NEW_YORK)),
                        
                createFlour("Shipton Mill Tipo 00", "Shipton Mill", "Wielka Brytania",
                        "Brytyjska mąka tipo 00 z organicznej pszenicy. Mielona na kamieniu.",
                        Ingredient.FlourType.TYPE_00, 11.5, 230.0, 55.0, 63.0, 0.55,
                        List.of(PizzaStyle.NEAPOLITAN, PizzaStyle.NEW_YORK))
        );
        ingredientRepository.saveAll(flours);
        
        // ===============================================
        // WODY - Profesjonalna baza z parametrami
        // ===============================================
        List<Ingredient> waters = Arrays.asList(
                // --------------- POLSKIE WODY ---------------
                createWater("Żywiec Zdrój", "Żywiec Zdrój", "Polska",
                        "Polska woda źródlana z Beskidów o umiarkowanej mineralizacji. " +
                        "Neutralna dla fermentacji, idealna do codziennego użytku.",
                        120.0, Ingredient.HardnessLevel.SOFT, 7.4, 350.0),
                        
                createWater("Cisowianka Perlage", "Cisowianka", "Polska",
                        "Polska woda mineralna z Cieszyna o średniej twardości.",
                        200.0, Ingredient.HardnessLevel.MEDIUM, 7.6, 540.0),
                        
                createWater("Nałęczowianka", "Nałęczowianka", "Polska",
                        "Polska woda mineralna ze źródła w Nałęczowie. Bogata w magnez.",
                        280.0, Ingredient.HardnessLevel.MEDIUM, 7.3, 700.0),
                        
                createWater("Muszynianka", "Muszynianka", "Polska",
                        "Wysokozmineralizowana woda z Muszyny. Może spowalniać fermentację.",
                        400.0, Ingredient.HardnessLevel.HARD, 7.1, 2000.0),
                        
                createWater("Kryniczanka", "Uzdrowisko Krynica", "Polska",
                        "Woda lecznicza z Krynicy. Bardzo wysoka mineralizacja - używaj ostrożnie.",
                        500.0, Ingredient.HardnessLevel.VERY_HARD, 6.8, 4000.0),
                        
                // --------------- WŁOSKIE/EUROPEJSKIE WODY ---------------
                createWater("Acqua Panna", "San Pellegrino", "Włochy",
                        "Toskańska woda źródlana - klasyka włoskiej kuchni. Idealna do pizzy.",
                        140.0, Ingredient.HardnessLevel.SOFT, 7.9, 188.0),
                        
                createWater("San Pellegrino", "San Pellegrino", "Włochy",
                        "Legendarna włoska woda gazowana. Do ciasta używaj wersji niegazowanej.",
                        200.0, Ingredient.HardnessLevel.MEDIUM, 7.7, 960.0),
                        
                createWater("Volvic", "Volvic", "Francja",
                        "Francuska woda wulkaniczna z Owernii, bardzo miękka. " +
                        "Idealna do pizzy - nie hamuje fermentacji.",
                        60.0, Ingredient.HardnessLevel.VERY_SOFT, 7.0, 130.0),
                        
                createWater("Evian", "Evian", "Francja",
                        "Francuska woda alpejska. Stosunkowo twarda - może lekko spowalniać drożdże.",
                        300.0, Ingredient.HardnessLevel.HARD, 7.2, 309.0),
                        
                createWater("Vittel", "Nestlé Waters", "Francja",
                        "Francuska woda ze źródła w Wogezach. Średnia twardość.",
                        250.0, Ingredient.HardnessLevel.MEDIUM, 7.5, 841.0),
                        
                createWater("Gerolsteiner", "Gerolsteiner", "Niemcy",
                        "Niemiecka woda mineralna bogata w wapń i magnez. Twarda woda.",
                        350.0, Ingredient.HardnessLevel.HARD, 6.5, 2527.0),
                        
                // --------------- WODY SPECJALNE ---------------
                createWater("Woda kranowa (filtrowana)", "Lokalna", "Polska",
                        "Przeciętna polska woda kranowa po przefiltrowaniu przez węgiel aktywny. " +
                        "Parametry zależą od regionu.",
                        180.0, Ingredient.HardnessLevel.MEDIUM, 7.5, 350.0),
                        
                createWater("Woda destylowana", "Różni producenci", "Różne",
                        "Czysta H2O bez minerałów. NIE ZALECANA do pizzy - " +
                        "brak minerałów osłabia gluten i aktywność drożdży.",
                        0.0, Ingredient.HardnessLevel.VERY_SOFT, 7.0, 0.0),
                        
                createWater("Woda odwrócona osmoza (RO)", "Domowa filtracja", "Różne",
                        "Woda z domowego filtra RO. Bardzo miękka - rozważ dodanie szczypinki soli mineralnej.",
                        20.0, Ingredient.HardnessLevel.VERY_SOFT, 7.0, 20.0),
                        
                createWater("Woda z NYC (typ)", "NYC Water", "USA",
                        "Słynna nowojorska woda - sekret pizzy nowojorskiej. " +
                        "Bardzo miękka z Catskills. Można naśladować mieszając RO z odrobiną minerałów.",
                        50.0, Ingredient.HardnessLevel.VERY_SOFT, 7.2, 65.0)
        );
        ingredientRepository.saveAll(waters);
        
        // ===============================================
        // DROŻDŻE - Baza typów drożdży
        // ===============================================
        List<Ingredient> yeasts = Arrays.asList(
                createYeast("Drożdże świeże piekarskie", "Lesaffre", "Francja",
                        "Klasyczne drożdże świeże (prasowane). Najbardziej aktywne, najlepszy smak. " +
                        "Przechowuj w lodówce do 2 tygodni. 1g świeżych ≈ 0.33g suszonych instant.",
                        Ingredient.YeastVariety.FRESH, 1.0, 14, true),
                        
                createYeast("Drożdże instant (Saf-Instant Red)", "Lesaffre", "Francja",
                        "Drożdże suszone instant - dodawać bezpośrednio do mąki. " +
                        "Czerwona etykieta dla standardowych ciast. Długi termin przydatności.",
                        Ingredient.YeastVariety.INSTANT_DRY, 0.33, 365, false),
                        
                createYeast("Drożdże instant (Saf-Instant Gold)", "Lesaffre", "Francja",
                        "Drożdże instant do ciast słodkich/tłustych. " +
                        "Złota etykieta - odporne na cukier i tłuszcz.",
                        Ingredient.YeastVariety.INSTANT_DRY, 0.33, 365, false),
                        
                createYeast("Drożdże suche aktywne (ADY)", "Fleischmann's", "USA",
                        "Aktywne drożdże suche - wymagają aktywacji w ciepłej wodzie (35-40°C) przez 10 min. " +
                        "Wolniejsze niż instant ale łatwiejsze do kontroli.",
                        Ingredient.YeastVariety.ACTIVE_DRY, 0.40, 365, false),
                        
                createYeast("Zakwas pszenny (Lievito Madre)", "Domowy", "Włochy",
                        "Włoski zakwas pszenny - tradycyjne lievito madre. " +
                        "Wymaga regularnego karmienia. Daje najlepszy smak i strawność. " +
                        "Używaj 15-25% w stosunku do mąki.",
                        Ingredient.YeastVariety.SOURDOUGH, 0.0, 0, true),
                        
                createYeast("Zakwas żytni", "Domowy", "Polska",
                        "Tradycyjny polski zakwas żytni. Można używać do pizzy dla głębszego smaku. " +
                        "Dodaj 10-15% do mąki pszennej.",
                        Ingredient.YeastVariety.SOURDOUGH, 0.0, 0, true)
        );
        ingredientRepository.saveAll(yeasts);
        
        // ===============================================
        // SOLE - Baza różnych soli
        // ===============================================
        List<Ingredient> salts = Arrays.asList(
                createSalt("Sól morska drobna", "Różni", "Różne",
                        "Standardowa drobna sól morska. Łatwo się rozpuszcza. Podstawowy wybór.",
                        Ingredient.SaltType.SEA_SALT, 38.0, false),
                        
                createSalt("Sól morska gruboziarnista", "Różni", "Różne",
                        "Gruboziarnista sól morska - wolniej się rozpuszcza. " +
                        "Dodawaj na końcu mieszania lub rozpuść wcześniej w wodzie.",
                        Ingredient.SaltType.SEA_SALT, 38.0, false),
                        
                createSalt("Sól himalajska różowa", "Różni", "Pakistan",
                        "Sól himalajska z naturalnym różowym kolorem. Bogata w minerały.",
                        Ingredient.SaltType.HIMALAYAN, 36.8, false),
                        
                createSalt("Fleur de Sel", "Guérande", "Francja",
                        "Luksusowa francuska 'kwiat soli'. Ręcznie zbierana. " +
                        "Idealna jako wykończenie na gotowej pizzy.",
                        Ingredient.SaltType.FLEUR_DE_SEL, 33.0, false),
                        
                createSalt("Sól koszerna (kosher salt)", "Morton", "USA",
                        "Amerykańska sól koszerna - większe płatki, łatwiejsza do odmierzania. " +
                        "Używaj 1.5x więcej objętościowo niż zwykłej soli.",
                        Ingredient.SaltType.KOSHER, 39.0, true),
                        
                createSalt("Sale Fino di Sicilia", "Trapani", "Włochy",
                        "Sycylijska sól morska - tradycja od rzymskich czasów. " +
                        "Klasyczny wybór do włoskiej pizzy.",
                        Ingredient.SaltType.SEA_SALT, 38.5, false)
        );
        ingredientRepository.saveAll(salts);
        
        log.info("Utworzono rozbudowaną bazę składników:");
        log.info("  - {} mąk (włoskie, polskie, amerykańskie, europejskie)", flours.size());
        log.info("  - {} wód (z parametrami twardości i mineralizacji)", waters.size());
        log.info("  - {} drożdży (świeże, instant, zakwas)", yeasts.size());
        log.info("  - {} soli (morskie, himalajska, koszerna)", salts.size());
    }
    
    // Helper dla mąki z pełnymi parametrami
    private Ingredient createFlour(String name, String brand, String country, String description,
                                   Ingredient.FlourType flourType, double protein, Double strength,
                                   double minHydration, double maxHydration, double ashContent,
                                   List<PizzaStyle> styles) {
        return Ingredient.builder()
                .type(Ingredient.IngredientType.FLOUR)
                .name(name)
                .brand(brand)
                .country(country)
                .description(description)
                .verified(true)
                .active(true)
                .flourParameters(Ingredient.FlourParameters.builder()
                        .flourType(flourType)
                        .grainType("pszenna")
                        .proteinContent(protein)
                        .strength(strength)
                        .ashContent(ashContent)
                        .recommendedHydrationMin(minHydration)
                        .recommendedHydrationMax(maxHydration)
                        .recommendedStyles(styles)
                        .build())
                .build();
    }
    
    // Helper dla drożdży
    private Ingredient createYeast(String name, String brand, String country, String description,
                                   Ingredient.YeastVariety variety, double conversionFactor, 
                                   int shelfLifeDays, boolean requiresRefrigeration) {
        return Ingredient.builder()
                .type(Ingredient.IngredientType.YEAST)
                .name(name)
                .brand(brand)
                .country(country)
                .description(description)
                .verified(true)
                .active(true)
                .yeastParameters(Ingredient.YeastParameters.builder()
                        .yeastVariety(variety)
                        .conversionFactor(conversionFactor)
                        .shelfLifeDays(shelfLifeDays)
                        .requiresRefrigeration(requiresRefrigeration)
                        .optimalTempMin(22.0)
                        .optimalTempMax(35.0)
                        .build())
                .build();
    }
    
    // Helper dla soli
    private Ingredient createSalt(String name, String brand, String country, String description,
                                  Ingredient.SaltType saltType, double sodiumContent, boolean isFlaky) {
        return Ingredient.builder()
                .type(Ingredient.IngredientType.SALT)
                .name(name)
                .brand(brand)
                .country(country)
                .description(description)
                .verified(true)
                .active(true)
                .saltParameters(Ingredient.SaltParameters.builder()
                        .saltType(saltType)
                        .sodiumContent(sodiumContent)
                        .isFlaky(isFlaky)
                        .build())
                .build();
    }
    
    private Ingredient createWater(String name, String brand, String country, String description,
                                   double hardness, Ingredient.HardnessLevel level, double ph, double minerals) {
        return Ingredient.builder()
                .type(Ingredient.IngredientType.WATER)
                .name(name)
                .brand(brand)
                .country(country)
                .description(description)
                .verified(true)
                .active(true)
                .waterParameters(Ingredient.WaterParameters.builder()
                        .hardness(hardness)
                        .hardnessLevel(level)
                        .ph(ph)
                        .mineralContent(minerals)
                        .source("butelkowana")
                        .build())
                .build();
    }
    
    private void initializeRecipes() {
        if (recipeRepository.count() > 0) {
            log.info("Receptury już istnieją, pomijam inicjalizację");
            return;
        }
        
        // Pobierz użytkowników
        User testUser = userRepository.findByEmail("test@pizzamaestro.pl").orElse(null);
        User premiumUser = userRepository.findByEmail("premium@pizzamaestro.pl").orElse(null);
        User admin = userRepository.findByEmail("admin@pizzamaestro.pl").orElse(null);
        
        if (testUser == null || premiumUser == null || admin == null) {
            log.warn("Brak użytkowników - pomijam tworzenie receptur");
            return;
        }
        
        log.info("Tworzenie przykładowych receptur...");
        
        // Receptura 1 - test user
        Recipe recipe1 = Recipe.builder()
                .userId(testUser.getId())
                .name("Moja pierwsza neapolitańska")
                .description("Pierwsza próba pizzy neapolitańskiej - wyszła świetnie!")
                .favorite(true)
                .isPublic(false)
                .pizzaStyle(PizzaStyle.NEAPOLITAN)
                .numberOfPizzas(4)
                .ballWeight(250)
                .hydration(65.0)
                .saltPercentage(2.8)
                .oilPercentage(0.0)
                .sugarPercentage(0.0)
                .yeastType(Recipe.YeastType.FRESH)
                .fermentationMethod(Recipe.FermentationMethod.COLD_FERMENTATION)
                .totalFermentationHours(24)
                .roomTemperature(22.0)
                .fridgeTemperature(4.0)
                .ovenType(com.pizzamaestro.model.OvenType.HOME_OVEN_WITH_STEEL)
                .ovenTemperature(280)
                .usePreferment(false)
                .calculatedRecipe(Recipe.CalculatedRecipe.builder()
                        .totalDoughWeight(1000.0)
                        .flourGrams(590.0)
                        .waterGrams(384.0)
                        .saltGrams(16.5)
                        .yeastGrams(0.9)
                        .oilGrams(0.0)
                        .sugarGrams(0.0)
                        .bakerPercentages(Recipe.BakerPercentages.builder()
                                .flour(100.0)
                                .water(65.0)
                                .salt(2.8)
                                .yeast(0.15)
                                .oil(0.0)
                                .sugar(0.0)
                                .build())
                        .build())
                .notes("Ciasto wyszło świetnie! Następnym razem spróbuję 68% hydratacji.")
                .rating(5)
                .feedback("Cornicione puszysty, spód chrupiący. Idealna pizza!")
                .tags(List.of("neapolitańska", "pierwsza", "udana"))
                .version(1)
                .build();
        recipeRepository.save(recipe1);
        
        // Receptura 2 - premium user
        Recipe recipe2 = Recipe.builder()
                .userId(premiumUser.getId())
                .name("Pizza Romana - eksperyment")
                .description("Test pizzy rzymskiej z wysoką hydratacją")
                .favorite(false)
                .isPublic(true)
                .pizzaStyle(PizzaStyle.ROMAN)
                .numberOfPizzas(2)
                .ballWeight(220)
                .hydration(75.0)
                .saltPercentage(2.5)
                .oilPercentage(3.0)
                .sugarPercentage(0.0)
                .yeastType(Recipe.YeastType.INSTANT_DRY)
                .fermentationMethod(Recipe.FermentationMethod.COLD_FERMENTATION)
                .totalFermentationHours(48)
                .roomTemperature(21.0)
                .fridgeTemperature(4.0)
                .ovenType(com.pizzamaestro.model.OvenType.ELECTRIC_PIZZA_OVEN)
                .ovenTemperature(380)
                .usePreferment(true)
                .prefermentType(Recipe.PrefermentType.POOLISH)
                .prefermentPercentage(30.0)
                .prefermentFermentationHours(12)
                .calculatedRecipe(Recipe.CalculatedRecipe.builder()
                        .totalDoughWeight(440.0)
                        .flourGrams(245.0)
                        .waterGrams(184.0)
                        .saltGrams(6.1)
                        .yeastGrams(0.2)
                        .oilGrams(7.4)
                        .sugarGrams(0.0)
                        .build())
                .notes("Poolish dał niesamowity smak! Ciasto bardzo lekkie.")
                .rating(4)
                .tags(List.of("rzymska", "poolish", "eksperyment"))
                .version(1)
                .build();
        recipeRepository.save(recipe2);
        
        // Receptura 3 - admin
        Recipe recipe3 = Recipe.builder()
                .userId(admin.getId())
                .name("Focaccia na imprezę")
                .description("Focaccia na 20 osób z rozmarynem i oliwą")
                .favorite(true)
                .isPublic(true)
                .pizzaStyle(PizzaStyle.FOCACCIA)
                .numberOfPizzas(6)
                .ballWeight(350)
                .hydration(78.0)
                .saltPercentage(2.5)
                .oilPercentage(6.0)
                .sugarPercentage(0.0)
                .yeastType(Recipe.YeastType.FRESH)
                .fermentationMethod(Recipe.FermentationMethod.MIXED)
                .totalFermentationHours(18)
                .roomTemperature(23.0)
                .fridgeTemperature(4.0)
                .ovenType(com.pizzamaestro.model.OvenType.HOME_OVEN)
                .ovenTemperature(230)
                .usePreferment(false)
                .calculatedRecipe(Recipe.CalculatedRecipe.builder()
                        .totalDoughWeight(2100.0)
                        .flourGrams(1120.0)
                        .waterGrams(874.0)
                        .saltGrams(28.0)
                        .yeastGrams(5.6)
                        .oilGrams(67.0)
                        .sugarGrams(0.0)
                        .build())
                .notes("Sprawdzony przepis na imprezy. Wszyscy pytają o recepturę!")
                .rating(5)
                .tags(List.of("focaccia", "impreza", "ulubiona"))
                .version(1)
                .build();
        recipeRepository.save(recipe3);
        
        log.info("Utworzono 3 przykładowe receptury");
    }
}
