package com.pizzamaestro.config;

import com.pizzamaestro.model.PizzaStyle;
import com.pizzamaestro.model.TechniqueGuide;
import com.pizzamaestro.repository.TechniqueGuideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Inicjalizator bazy wiedzy - przewodniki po technikach pizzy.
 */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class TechniqueDataInitializer implements CommandLineRunner {
    
    private final TechniqueGuideRepository repository;
    
    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("📚 Przewodniki już istnieją w bazie");
            return;
        }
        
        log.info("📚 Inicjalizacja bazy wiedzy - przewodniki po technikach");
        
        // Prefermenty
        createPoolishGuide();
        createBigaGuide();
        createSourdoughGuide();
        
        // Techniki składania
        createStretchAndFoldGuide();
        createCoilFoldGuide();
        createSlapAndFoldGuide();
        
        // Kulkowanie
        createBallShapingGuide();
        createPreshapeGuide();
        
        // Rozciąganie
        createHandStretchingGuide();
        
        // Fermentacja
        createColdFermentationGuide();
        
        log.info("✅ Zainicjalizowano {} przewodników", repository.count());
    }
    
    // ========================================
    // PREFERMENTY
    // ========================================
    
    private void createPoolishGuide() {
        repository.save(TechniqueGuide.builder()
                .category(TechniqueGuide.TechniqueCategory.PREFERMENT)
                .slug("poolish")
                .title("Poolish - polski prefrement")
                .titleEn("Poolish Pre-ferment")
                .shortDescription("Płynny preferment o 100% hydratacji. Dodaje złożony smak i poprawia strukturę ciasta.")
                .fullDescription("""
                        Poolish to tradycyjny preferment pochodzący z Polski (stąd nazwa), szeroko stosowany we włoskim piekarnictwie.
                        
                        Charakteryzuje się 100% hydratacją (równe ilości mąki i wody wagowo), co daje płynną konsystencję.
                        
                        Poolish dodaje:
                        • Złożony, lekko kwasowy smak
                        • Lepszą strukturę miękiszu z większymi bąblami
                        • Dłuższą świeżość wypieków
                        • Lepszą strawność dzięki przedłużonej fermentacji
                        
                        Idealny dla:
                        • Pizzy neapolitańskiej
                        • Bagietki
                        • Ciabatty
                        """)
                .difficulty(TechniqueGuide.DifficultyLevel.INTERMEDIATE)
                .estimatedTimeMinutes(720) // 12h
                .requiredEquipment(List.of("Miska", "Folia spożywcza", "Waga kuchenna"))
                .steps(List.of(
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(1)
                                .title("Przygotowanie składników")
                                .description("Odmierz równe ilości mąki i wody (np. 200g + 200g)")
                                .detailedExplanation("Użyj wody o temperaturze pokojowej (20-22°C). Mąka powinna mieć W220-280.")
                                .durationSeconds(60)
                                .tips(List.of("Używaj wody filtrowanej", "Mąka może być ta sama co do głównego ciasta"))
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(2)
                                .title("Dodanie drożdży")
                                .description("Dodaj minimalną ilość drożdży (0.1-0.5% świeżych lub 0.05-0.2% suchych)")
                                .detailedExplanation("Mała ilość drożdży = wolna fermentacja = więcej smaku. Dla 200g mąki użyj 0.2-1g świeżych drożdży.")
                                .durationSeconds(30)
                                .tips(List.of("Mniej drożdży = dłuższa fermentacja, ale lepszy smak", "Przy 24°C użyj mniej, przy 18°C więcej"))
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(3)
                                .title("Mieszanie")
                                .description("Wymieszaj dokładnie aż nie będzie grudek mąki")
                                .detailedExplanation("Mieszaj łyżką lub widelcem przez 1-2 minuty. Konsystencja powinna być jak gęsta zupa.")
                                .durationSeconds(120)
                                .tips(List.of("Nie musisz wyrabiać - to tylko mieszanie"))
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(4)
                                .title("Fermentacja")
                                .description("Przykryj folią i pozostaw w temperaturze pokojowej")
                                .detailedExplanation("Fermentacja trwa 8-18h w zależności od temperatury i ilości drożdży. Poolish jest gotowy gdy powierzchnia jest pełna bąbelków i zaczyna lekko opadać.")
                                .durationSeconds(43200) // 12h
                                .tips(List.of(
                                        "Przy 22°C: 12-16h",
                                        "Przy 18°C: 16-20h",
                                        "Gotowy poolish ma delikatny, kwasowy zapach",
                                        "Jeśli opadł za bardzo - nadal można użyć, ale smak będzie bardziej kwasowy"
                                ))
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(5)
                                .title("Użycie")
                                .description("Dodaj poolish do reszty składników głównego ciasta")
                                .detailedExplanation("Poolish stanowi zazwyczaj 20-40% całkowitej mąki w przepisie. Pamiętaj o odjęciu mąki i wody poolish od głównego ciasta.")
                                .tips(List.of(
                                        "20% poolish = subtelny smak",
                                        "30% poolish = wyraźny smak (zalecane)",
                                        "40%+ poolish = intensywny smak, ale ciasto może być słabsze"
                                ))
                                .build()
                ))
                .proTips(List.of(
                        TechniqueGuide.ProTip.builder()
                                .title("Test gotowości")
                                .content("Gotowy poolish powinien mieć kopulastą powierzchnię pokrytą bąbelkami i lekko zaczynać opadać w środku.")
                                .category(TechniqueGuide.ProTip.TipCategory.QUALITY_BOOST)
                                .build(),
                        TechniqueGuide.ProTip.builder()
                                .title("Lodówka dla kontroli")
                                .content("Możesz spowolnić fermentację umieszczając poolish w lodówce po 4-6h. Wyjmij 2h przed użyciem.")
                                .category(TechniqueGuide.ProTip.TipCategory.TIME_SAVER)
                                .premiumOnly(false)
                                .build()
                ))
                .commonMistakes(List.of(
                        TechniqueGuide.CommonMistake.builder()
                                .mistake("Za dużo drożdży")
                                .consequence("Poolish fermentuje za szybko, mniej smaku, może opaść przed użyciem")
                                .solution("Użyj mniej drożdży lub obniż temperaturę")
                                .prevention("Mierz drożdże precyzyjnie, najlepiej wagą 0.1g")
                                .build(),
                        TechniqueGuide.CommonMistake.builder()
                                .mistake("Przefermentowanie")
                                .consequence("Poolish zapadł się i ma silny, octowy zapach")
                                .solution("Można użyć, ale zmniejsz ilość do 20% mąki")
                                .prevention("Ustaw timer, obserwuj stan poolish")
                                .build()
                ))
                .science(TechniqueGuide.ScienceExplanation.builder()
                        .mainPrinciple("Przedłużona fermentacja pozwala drożdżom i enzymom rozłożyć złożone cukry i białka na prostsze związki smakowe.")
                        .chemicalProcesses(List.of(
                                "Hydroliza skrobi przez amylazy",
                                "Proteoliza przez enzymy proteolityczne",
                                "Produkcja kwasu mlekowego i octowego"
                        ))
                        .physicalProcesses(List.of(
                                "Częściowy rozwój glutenu w środowisku o wysokiej hydratacji",
                                "Tworzenie się bąbli CO2"
                        ))
                        .whyItWorks("Wysoka hydratacja aktywuje enzymy, a niska ilość drożdży wymusza wolną fermentację, która generuje więcej związków smakowych.")
                        .build())
                .relatedTechniques(List.of("biga", "cold-fermentation"))
                .recommendedForStyles(List.of(PizzaStyle.NEAPOLITAN, PizzaStyle.NEW_YORK, PizzaStyle.FOCACCIA))
                .premium(false)
                .active(true)
                .build());
        
        log.info("  ✓ Utworzono przewodnik: Poolish");
    }
    
    private void createBigaGuide() {
        repository.save(TechniqueGuide.builder()
                .category(TechniqueGuide.TechniqueCategory.PREFERMENT)
                .slug("biga")
                .title("Biga - włoski preferment")
                .titleEn("Biga Pre-ferment")
                .shortDescription("Suchy preferment o 50-60% hydratacji. Dodaje siłę ciasta i orzechowy smak.")
                .fullDescription("""
                        Biga to tradycyjny włoski preferment o niskiej hydratacji (50-60%), co daje twardą, suchą konsystencję.
                        
                        W porównaniu do poolish, biga:
                        • Ma bardziej orzechowy, mniej kwasowy smak
                        • Dodaje więcej siły ciasta (silniejszy gluten)
                        • Wymaga dłuższej fermentacji (16-24h)
                        • Jest trudniejsza w mieszaniu z głównym ciastem
                        
                        Idealna dla:
                        • Pizzy neapolitańskiej (szczególnie dla długich fermentacji)
                        • Ciabatty
                        • Chleba włoskiego
                        """)
                .difficulty(TechniqueGuide.DifficultyLevel.INTERMEDIATE)
                .estimatedTimeMinutes(1080) // 18h
                .requiredEquipment(List.of("Miska", "Folia spożywcza", "Waga kuchenna", "Skrobka"))
                .steps(List.of(
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(1)
                                .title("Przygotowanie składników")
                                .description("Odmierz mąkę i wodę w proporcji 100:50-60 (np. 200g mąki + 100-120g wody)")
                                .detailedExplanation("Niska hydratacja daje suchą konsystencję. Użyj mąki W260-320.")
                                .durationSeconds(60)
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(2)
                                .title("Dodanie drożdży")
                                .description("Dodaj 0.1% świeżych drożdży (lub 0.04% suchych)")
                                .detailedExplanation("Bardzo mała ilość drożdży ze względu na długą fermentację. Dla 200g mąki = 0.2g świeżych.")
                                .durationSeconds(30)
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(3)
                                .title("Mieszanie")
                                .description("Wymieszaj aż powstanie suche, grubiaste ciasto")
                                .detailedExplanation("Biga nie będzie gładka - to normalne. Mieszaj aż nie będzie suchej mąki. Konsystencja jak kruche ciasto.")
                                .durationSeconds(180)
                                .tips(List.of("Nie dodawaj więcej wody - ma być sucha", "Użyj rąk do końcowego wymieszania"))
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(4)
                                .title("Fermentacja")
                                .description("Przykryj i pozostaw 16-24h w temperaturze 16-18°C")
                                .detailedExplanation("Biga fermentuje wolniej niż poolish. Idealna temperatura to 16-18°C. Przy 22°C skróć do 12-16h.")
                                .durationSeconds(64800) // 18h
                                .tips(List.of(
                                        "Gotowa biga zwiększa objętość 2-3x",
                                        "Powierzchnia będzie pęknięta i dziurkowana",
                                        "Zapach powinien być orzechowy, nie kwaśny"
                                ))
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(5)
                                .title("Rozbijanie")
                                .description("Przed dodaniem do ciasta głównego, rozbij bigę na małe kawałki")
                                .detailedExplanation("Biga jest twarda - pokrój ją na małe kawałki lub wyrwij palcami przed dodaniem do miksera/miski.")
                                .tips(List.of("Możesz dodać bigę do wody i rozetrzeć przed dodaniem mąki"))
                                .build()
                ))
                .proTips(List.of(
                        TechniqueGuide.ProTip.builder()
                                .title("Siła ciasta")
                                .content("Biga dodaje więcej siły ciasta niż poolish - idealna jeśli twoja mąka jest słaba lub planujesz wysoką hydratację głównego ciasta.")
                                .category(TechniqueGuide.ProTip.TipCategory.QUALITY_BOOST)
                                .build(),
                        TechniqueGuide.ProTip.builder()
                                .title("Kombinacja biga + poolish")
                                .content("Niektórzy pizzaioli używają zarówno bigi jak i poolish w jednym cieście dla złożoności smaku i siły.")
                                .category(TechniqueGuide.ProTip.TipCategory.ADVANCED)
                                .premiumOnly(true)
                                .build()
                ))
                .commonMistakes(List.of(
                        TechniqueGuide.CommonMistake.builder()
                                .mistake("Za wysoka temperatura fermentacji")
                                .consequence("Biga fermentuje za szybko, traci orzechowy smak")
                                .solution("Przenieś do chłodniejszego miejsca lub lodówki")
                                .prevention("Monitoruj temperaturę pomieszczenia")
                                .build()
                ))
                .science(TechniqueGuide.ScienceExplanation.builder()
                        .mainPrinciple("Niska hydratacja ogranicza aktywność enzymów, co skutkuje wolniejszą fermentacją i orzechowymi nutami smakowymi.")
                        .chemicalProcesses(List.of(
                                "Reakcja Maillarda podczas fermentacji",
                                "Ograniczona hydroliza (mniej wody)"
                        ))
                        .whyItWorks("Mniej wody = wolniejsza aktywność enzymatyczna = dłuższa fermentacja = więcej orzechowych nut z reakcji Maillarda.")
                        .build())
                .relatedTechniques(List.of("poolish", "cold-fermentation"))
                .recommendedForStyles(List.of(PizzaStyle.NEAPOLITAN, PizzaStyle.FOCACCIA))
                .premium(false)
                .active(true)
                .build());
        
        log.info("  ✓ Utworzono przewodnik: Biga");
    }
    
    private void createSourdoughGuide() {
        repository.save(TechniqueGuide.builder()
                .category(TechniqueGuide.TechniqueCategory.PREFERMENT)
                .slug("sourdough-starter")
                .title("Zakwas - lievito madre")
                .titleEn("Sourdough Starter")
                .shortDescription("Naturalny zakwas bez komercyjnych drożdży. Najgłębszy smak i najlepsza strawność.")
                .fullDescription("""
                        Lievito madre (zakwas) to naturalny starter zawierający dzikie drożdże i bakterie kwasu mlekowego.
                        
                        Zakwas daje:
                        • Najgłębszy, najbardziej złożony smak
                        • Doskonałą strawność (długa fermentacja rozkłada gluten)
                        • Naturalną konserwację (kwasy hamują pleśnie)
                        • Unikalne aromat i teksturę
                        
                        Wymaga:
                        • Regularnego karmienia (co 4-8h przy temp. pokojowej)
                        • Cierpliwości (3-7 dni na stworzenie od zera)
                        • Dłuższego czasu fermentacji (12-48h)
                        """)
                .difficulty(TechniqueGuide.DifficultyLevel.ADVANCED)
                .estimatedTimeMinutes(4320) // 72h na stworzenie
                .requiredEquipment(List.of("Słoik", "Waga", "Mąka razowa/pełnoziarnista do startu"))
                .steps(List.of(
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(1)
                                .title("Dzień 1: Start")
                                .description("Wymieszaj 50g mąki razowej + 50g wody")
                                .detailedExplanation("Mąka razowa/pełnoziarnista zawiera więcej dzikich drożdży i bakterii. Woda bez chloru!")
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(2)
                                .title("Dzień 2-3: Obserwacja")
                                .description("Mieszaj raz dziennie, obserwuj bąbelki")
                                .detailedExplanation("Możesz zauważyć aktywność i nieprzyjemny zapach - to normalne. Bakterie się mnożą.")
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(3)
                                .title("Dzień 4-7: Karmienie")
                                .description("Odrzuć połowę, dodaj 50g mąki + 50g wody co 12-24h")
                                .detailedExplanation("Zakwas powinien zacząć rosnąć regularnie. Gdy podwaja objętość w 4-6h, jest gotowy.")
                                .critical(true)
                                .build()
                ))
                .proTips(List.of(
                        TechniqueGuide.ProTip.builder()
                                .title("Test pływania")
                                .content("Łyżeczka zakwasu powinna unosić się na wodzie gdy jest na szczycie aktywności.")
                                .category(TechniqueGuide.ProTip.TipCategory.QUALITY_BOOST)
                                .build()
                ))
                .relatedTechniques(List.of("poolish", "biga", "cold-fermentation"))
                .recommendedForStyles(List.of(PizzaStyle.NEAPOLITAN, PizzaStyle.ROMAN, PizzaStyle.FOCACCIA))
                .premium(true)
                .active(true)
                .build());
        
        log.info("  ✓ Utworzono przewodnik: Zakwas");
    }
    
    // ========================================
    // TECHNIKI SKŁADANIA
    // ========================================
    
    private void createStretchAndFoldGuide() {
        repository.save(TechniqueGuide.builder()
                .category(TechniqueGuide.TechniqueCategory.FOLDING)
                .slug("stretch-and-fold")
                .title("Stretch and Fold")
                .titleEn("Stretch and Fold Technique")
                .shortDescription("Podstawowa technika składania ciasta. Buduje siłę glutenu bez intensywnego wyrabiania.")
                .fullDescription("""
                        Stretch and Fold to delikatna technika budowania siły glutenu poprzez rozciąganie i składanie ciasta.
                        
                        Zalety:
                        • Buduje silną sieć glutenową bez przegrzewania ciasta
                        • Zachowuje strukturę bąbli (alweoli)
                        • Idealna dla ciast o wysokiej hydratacji
                        • Łatwa do opanowania
                        
                        Używana w:
                        • Pizza o wysokiej hydratacji (>65%)
                        • Ciabatta
                        • Focaccia
                        • Chleby rzemieślnicze
                        """)
                .difficulty(TechniqueGuide.DifficultyLevel.BEGINNER)
                .estimatedTimeMinutes(5)
                .steps(List.of(
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(1)
                                .title("Zwilż ręce")
                                .description("Zanurz ręce w wodzie aby ciasto nie przywierało")
                                .durationSeconds(10)
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(2)
                                .title("Chwyć brzeg")
                                .description("Chwyć ciasto z jednej strony miski")
                                .durationSeconds(5)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(3)
                                .title("Rozciągnij")
                                .description("Delikatnie podnieś i rozciągnij ciasto do góry")
                                .detailedExplanation("Rozciągaj aż poczujesz opór, ale nie rwij ciasta. Ciasto powinno rozciągnąć się 2-3 razy.")
                                .durationSeconds(10)
                                .tips(List.of("Nie rwij ciasta - rozciągaj delikatnie", "Poczuj napięcie glutenu"))
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(4)
                                .title("Złóż")
                                .description("Złóż rozciągnięte ciasto na środek")
                                .durationSeconds(5)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(5)
                                .title("Obróć i powtórz")
                                .description("Obróć miskę o 90° i powtórz 3 razy (4 strony)")
                                .detailedExplanation("Po 4 składaniach (N, E, S, W) ciasto będzie miało kulisty kształt.")
                                .durationSeconds(60)
                                .build()
                ))
                .proTips(List.of(
                        TechniqueGuide.ProTip.builder()
                                .title("Harmonogram składań")
                                .content("Wykonuj 3-4 serie składań co 30-45 minut w pierwszych 2h fermentacji. Każda seria = 4 składania.")
                                .category(TechniqueGuide.ProTip.TipCategory.QUALITY_BOOST)
                                .build(),
                        TechniqueGuide.ProTip.builder()
                                .title("Mokre vs suche ręce")
                                .content("Dla niższej hydratacji (<65%) możesz użyć lekko naoliwione ręce zamiast mokrych.")
                                .category(TechniqueGuide.ProTip.TipCategory.COMMON_FIX)
                                .build()
                ))
                .relatedTechniques(List.of("coil-fold", "ball-shaping"))
                .recommendedForStyles(List.of(PizzaStyle.NEAPOLITAN, PizzaStyle.ROMAN, PizzaStyle.FOCACCIA))
                .premium(false)
                .active(true)
                .build());
        
        log.info("  ✓ Utworzono przewodnik: Stretch and Fold");
    }
    
    private void createCoilFoldGuide() {
        repository.save(TechniqueGuide.builder()
                .category(TechniqueGuide.TechniqueCategory.FOLDING)
                .slug("coil-fold")
                .title("Coil Fold")
                .titleEn("Coil Fold Technique")
                .shortDescription("Zaawansowana technika dla bardzo mokrych ciast. Minimalne napięcie, maksymalna delikatność.")
                .fullDescription("""
                        Coil Fold to najdelikatniejsza technika składania, idealna dla ekstremalnie wilgotnych ciast (75%+).
                        
                        Różnica od Stretch and Fold:
                        • Nie dotykasz ciasta z góry - tylko z boków
                        • Ciasto zwija się samo pod własnym ciężarem
                        • Mniejsze ryzyko uszkodzenia struktury
                        
                        Kiedy używać:
                        • Hydratacja 75%+
                        • Delikatne ciasta zakwasowe
                        • Gdy ciasto jest zbyt mokre na stretch and fold
                        """)
                .difficulty(TechniqueGuide.DifficultyLevel.INTERMEDIATE)
                .estimatedTimeMinutes(3)
                .steps(List.of(
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(1)
                                .title("Zwilż ręce")
                                .description("Ręce powinny być mokre")
                                .durationSeconds(10)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(2)
                                .title("Podłóż ręce")
                                .description("Wsuń ręce pod ciasto z dwóch przeciwnych stron")
                                .detailedExplanation("Palce pod spód ciasta, kciuki na wierzchu - jak podnoszenie niemowlęcia.")
                                .durationSeconds(10)
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(3)
                                .title("Podnieś środek")
                                .description("Delikatnie podnieś środek ciasta")
                                .detailedExplanation("Brzegi ciasta same zwisają i zwijają się pod spód.")
                                .durationSeconds(10)
                                .tips(List.of("Ciasto powinno samo się zwijać", "Nie ciągnij - tylko podnoś"))
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(4)
                                .title("Opuść")
                                .description("Opuść ciasto na drugi koniec miski")
                                .durationSeconds(5)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(5)
                                .title("Obróć i powtórz")
                                .description("Obróć miskę o 90° i powtórz raz")
                                .detailedExplanation("Wystarczą 2 zwinięcia (wzdłuż i wszerz) na jedną serię.")
                                .durationSeconds(30)
                                .build()
                ))
                .proTips(List.of(
                        TechniqueGuide.ProTip.builder()
                                .title("Kiedy przerwać składania")
                                .content("Gdy ciasto zaczyna trzymać kształt przez 30+ minut bez rozpływania się, składania są kompletne.")
                                .category(TechniqueGuide.ProTip.TipCategory.QUALITY_BOOST)
                                .build()
                ))
                .relatedTechniques(List.of("stretch-and-fold", "slap-and-fold"))
                .recommendedForStyles(List.of(PizzaStyle.ROMAN, PizzaStyle.FOCACCIA))
                .premium(false)
                .active(true)
                .build());
        
        log.info("  ✓ Utworzono przewodnik: Coil Fold");
    }
    
    private void createSlapAndFoldGuide() {
        repository.save(TechniqueGuide.builder()
                .category(TechniqueGuide.TechniqueCategory.FOLDING)
                .slug("slap-and-fold")
                .title("Slap and Fold (French Fold)")
                .titleEn("Slap and Fold / French Fold")
                .shortDescription("Intensywna technika wyrabiania mokrych ciast. Szybka budowa glutenu.")
                .fullDescription("""
                        Slap and Fold (French Fold) to dynamiczna technika wyrabiania mokrych ciast na blacie.
                        
                        Charakterystyka:
                        • Intensywne wyrabianie przez 5-10 minut
                        • Ciasto jest rzucane/klepane o blat
                        • Bardzo skuteczna budowa glutenu
                        • Efektowna do oglądania
                        
                        Kiedy używać:
                        • Ciasta o hydratacji 65-75%
                        • Gdy potrzebujesz szybko zbudować gluten
                        • Alternatywa dla miksera spiralnego
                        """)
                .difficulty(TechniqueGuide.DifficultyLevel.ADVANCED)
                .estimatedTimeMinutes(10)
                .steps(List.of(
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(1)
                                .title("Wyłóż ciasto")
                                .description("Wyłóż ciasto na czysty, nienaoliwiony blat")
                                .detailedExplanation("Blat NIE powinien być posypany mąką - ciasto musi się przyczepiać.")
                                .durationSeconds(10)
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(2)
                                .title("Chwyć")
                                .description("Chwyć ciasto obiema rękami od spodu")
                                .durationSeconds(5)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(3)
                                .title("Podnieś i klep")
                                .description("Podnieś ciasto i klepnij o blat górną częścią")
                                .detailedExplanation("Ruch jest jak rzucanie mokrego ręcznika. Ciasto powinno się rozciągnąć.")
                                .durationSeconds(5)
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(4)
                                .title("Złóż")
                                .description("Złóż ciasto na siebie ruchem do góry")
                                .durationSeconds(5)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(5)
                                .title("Powtarzaj")
                                .description("Powtarzaj przez 5-10 minut")
                                .detailedExplanation("Na początku ciasto będzie się rwać i przyklejać. Po kilku minutach stanie się gładkie.")
                                .durationSeconds(600)
                                .tips(List.of(
                                        "Pierwsza minuta będzie bałagan - to normalne",
                                        "Ciasto zmieni się dramatycznie po 5 minutach",
                                        "Gotowe ciasto będzie gładkie i elastyczne"
                                ))
                                .build()
                ))
                .commonMistakes(List.of(
                        TechniqueGuide.CommonMistake.builder()
                                .mistake("Dodawanie mąki na blat")
                                .consequence("Ciasto nie buduje glutenu przez tarcie")
                                .solution("Kontynuuj na czystym blacie - ciasto przestanie się przyklejać")
                                .prevention("Nie panikuj gdy ciasto się klei na początku")
                                .build()
                ))
                .relatedTechniques(List.of("stretch-and-fold", "coil-fold"))
                .recommendedForStyles(List.of(PizzaStyle.NEAPOLITAN, PizzaStyle.NEW_YORK))
                .premium(false)
                .active(true)
                .build());
        
        log.info("  ✓ Utworzono przewodnik: Slap and Fold");
    }
    
    // ========================================
    // KULKOWANIE
    // ========================================
    
    private void createBallShapingGuide() {
        repository.save(TechniqueGuide.builder()
                .category(TechniqueGuide.TechniqueCategory.SHAPING)
                .slug("ball-shaping")
                .title("Kulkowanie - technika włoska")
                .titleEn("Ball Shaping - Italian Method")
                .shortDescription("Klasyczna technika formowania kulek ciasta (panetti). Klucz do pięknej pizzy.")
                .fullDescription("""
                        Kulkowanie (piegatura) to kluczowa technika formowania porcji ciasta w kulki.
                        
                        Cel kulkowania:
                        • Napięcie powierzchni dla lepszego kształtu
                        • Zamknięcie gazów wewnątrz
                        • Przygotowanie do finalnego rozciągania
                        
                        Ważne:
                        • Kulki powinny mieć gładką, napiętą powierzchnię
                        • Szew (zamknięcie) zawsze od spodu
                        • Nie dodawaj za dużo mąki
                        """)
                .difficulty(TechniqueGuide.DifficultyLevel.INTERMEDIATE)
                .estimatedTimeMinutes(3)
                .steps(List.of(
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(1)
                                .title("Podziel ciasto")
                                .description("Podziel ciasto na porcje o równej wadze")
                                .detailedExplanation("Użyj wagi! Dla neapolitańskiej: 220-280g. Dla NY: 280-350g.")
                                .durationSeconds(30)
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(2)
                                .title("Pre-shape")
                                .description("Wstępnie uformuj w luźną kulkę")
                                .detailedExplanation("Zgarnij brzegi do środka, odwróć.")
                                .durationSeconds(10)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(3)
                                .title("Odpoczynek")
                                .description("Pozwól odpocząć 10-15 minut")
                                .detailedExplanation("Gluten się relaksuje, kulkowanie finalne będzie łatwiejsze.")
                                .durationSeconds(900)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(4)
                                .title("Finalne kulkowanie")
                                .description("Użyj techniki 'piega' - składaj brzegi do środka i obracaj")
                                .detailedExplanation("""
                                        Połóż ciasto przed sobą. Chwyć dalszy brzeg i złóż do środka.
                                        Obróć o 90°, powtórz. Kontynuuj aż powstanie kula z napiętą powierzchnią.
                                        Odwróć - szew na dole.
                                        """)
                                .durationSeconds(30)
                                .tips(List.of(
                                        "Ruch jest jak zamykanie koperty",
                                        "Napinaj powierzchnię przy każdym złożeniu",
                                        "Szew MUSI być na dole"
                                ))
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(5)
                                .title("Napinanie na blacie")
                                .description("Opcjonalnie: naciągnij kulkę na blacie ruchem okrężnym")
                                .detailedExplanation("Połóż kulkę na czystym blacie (bez mąki). Złożonymi dłońmi zataczaj kółka, napinając powierzchnię.")
                                .durationSeconds(15)
                                .build()
                ))
                .proTips(List.of(
                        TechniqueGuide.ProTip.builder()
                                .title("Test napięcia")
                                .content("Gotowa kulka powinna być gładka, bez widocznych szwów na wierzchu, i utrzymywać kształt.")
                                .category(TechniqueGuide.ProTip.TipCategory.QUALITY_BOOST)
                                .build(),
                        TechniqueGuide.ProTip.builder()
                                .title("Przechowywanie")
                                .content("Kulki układaj w pojemniku z pokrywką lub tackach z semolina, z odstępem 3-5cm między nimi.")
                                .category(TechniqueGuide.ProTip.TipCategory.TIME_SAVER)
                                .build()
                ))
                .relatedTechniques(List.of("preshape", "hand-stretching"))
                .recommendedForStyles(List.of(PizzaStyle.NEAPOLITAN, PizzaStyle.NEW_YORK))
                .premium(false)
                .active(true)
                .build());
        
        log.info("  ✓ Utworzono przewodnik: Kulkowanie");
    }
    
    private void createPreshapeGuide() {
        repository.save(TechniqueGuide.builder()
                .category(TechniqueGuide.TechniqueCategory.SHAPING)
                .slug("preshape")
                .title("Pre-shape (wstępne formowanie)")
                .titleEn("Pre-shaping Technique")
                .shortDescription("Wstępne formowanie przed finalnym kulkowaniem. Ułatwia pracę z ciastem.")
                .fullDescription("""
                        Pre-shape to wstępny etap formowania, który:
                        
                        • Organizuje masę ciasta
                        • Daje odpoczynek przed finalnym kulkowaniem
                        • Ułatwia finalne formowanie
                        
                        Używany gdy:
                        • Ciasto jest bardzo rozleźle
                        • Planujesz precyzyjne kulki
                        • Pracujesz z dużą ilością ciasta
                        """)
                .difficulty(TechniqueGuide.DifficultyLevel.BEGINNER)
                .estimatedTimeMinutes(2)
                .steps(List.of(
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(1)
                                .title("Wyłóż ciasto")
                                .description("Wyłóż ciasto na lekko omączoną powierzchnię")
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(2)
                                .title("Podziel")
                                .description("Podziel na porcje skrobką")
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(3)
                                .title("Zgarnij")
                                .description("Skrobką zgarnij brzegi każdej porcji do środka")
                                .detailedExplanation("Nie musisz być precyzyjny - to tylko wstępne formowanie.")
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(4)
                                .title("Odwróć")
                                .description("Odwróć porcję szewem do dołu")
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(5)
                                .title("Odpoczynek")
                                .description("Pozwól odpocząć 10-20 minut przed finalnym kulkowaniem")
                                .critical(true)
                                .build()
                ))
                .relatedTechniques(List.of("ball-shaping"))
                .recommendedForStyles(List.of(PizzaStyle.NEAPOLITAN, PizzaStyle.NEW_YORK, PizzaStyle.ROMAN))
                .premium(false)
                .active(true)
                .build());
        
        log.info("  ✓ Utworzono przewodnik: Pre-shape");
    }
    
    // ========================================
    // ROZCIĄGANIE
    // ========================================
    
    private void createHandStretchingGuide() {
        repository.save(TechniqueGuide.builder()
                .category(TechniqueGuide.TechniqueCategory.STRETCHING)
                .slug("hand-stretching")
                .title("Rozciąganie ręczne")
                .titleEn("Hand Stretching Technique")
                .shortDescription("Klasyczna technika rozciągania pizzy neapolitańskiej. Zachowuje bąble i cornicione.")
                .fullDescription("""
                        Rozciąganie ręczne to tradycyjna technika tworzenia podstawy pizzy:
                        
                        Zalety:
                        • Zachowanie struktury bąbli (alweoli)
                        • Tworzenie puszystego cornicione (brzegu)
                        • Równomierna grubość środka
                        
                        Ważne:
                        • NIE używaj wałka (niszczy bąble)
                        • Nie rozciągaj brzegu (będzie płaski)
                        • Pracuj szybko (ciasto się kurczy)
                        """)
                .difficulty(TechniqueGuide.DifficultyLevel.INTERMEDIATE)
                .estimatedTimeMinutes(2)
                .steps(List.of(
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(1)
                                .title("Przygotuj kulkę")
                                .description("Kulka powinna być w temperaturze pokojowej, odpocznięta")
                                .detailedExplanation("Wyjmij z lodówki 1-2h przed rozciąganiem. Ciasto w temp. pokojowej jest bardziej elastyczne.")
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(2)
                                .title("Omącz")
                                .description("Delikatnie omącz kulkę w mące (lub semolina)")
                                .tips(List.of("Nie za dużo mąki", "Semolina daje lepszą chrupkość"))
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(3)
                                .title("Spłaszcz środek")
                                .description("Palcami spłaszcz środek, zostawiając 2cm brzeg")
                                .detailedExplanation("Delikatnie naciskaj od środka na zewnątrz. NIE dotykaj zewnętrznych 2cm.")
                                .durationSeconds(15)
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(4)
                                .title("Rozciągnij na dłoniach")
                                .description("Podnieś ciasto i pozwól grawitacji rozciągnąć")
                                .detailedExplanation("Połóż ciasto na grzbietach dłoni. Delikatnie rozciągaj obracając ciasto ruchem nadgarstków.")
                                .durationSeconds(30)
                                .tips(List.of(
                                        "Grawitacja robi większość pracy",
                                        "Obracaj ciasto ciągle",
                                        "Nie ciągnij za mocno - ciasto się rwie"
                                ))
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(5)
                                .title("Sprawdź grubość")
                                .description("Środek powinien być cienki (2-3mm), brzeg gruby")
                                .detailedExplanation("Gdy podnosisz ciasto do światła, środek powinien być półprzezroczysty.")
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(6)
                                .title("Przenieś na łopatę")
                                .description("Ułóż na omączonej łopacie i szybko dodaj składniki")
                                .tips(List.of("Pracuj szybko!", "Potrząśnij łopatą aby upewnić się że pizza się rusza"))
                                .critical(true)
                                .build()
                ))
                .proTips(List.of(
                        TechniqueGuide.ProTip.builder()
                                .title("Technika neapolitańska")
                                .content("Prawdziwi pizzaioli rozciągają tylko przez obrót na grzbietach dłoni - bez naciągania na pięści.")
                                .category(TechniqueGuide.ProTip.TipCategory.ADVANCED)
                                .premiumOnly(true)
                                .build()
                ))
                .commonMistakes(List.of(
                        TechniqueGuide.CommonMistake.builder()
                                .mistake("Używanie wałka")
                                .consequence("Zniszczone bąble, płaski cornicione, gumowata tekstura")
                                .solution("Zawsze rozciągaj ręcznie")
                                .prevention("Cierpliwość i praktyka")
                                .build(),
                        TechniqueGuide.CommonMistake.builder()
                                .mistake("Ciasto się kurczy")
                                .consequence("Nie można rozciągnąć do pożądanego rozmiaru")
                                .solution("Pozwól ciasta odpocząć 10-15 minut i spróbuj ponownie")
                                .prevention("Upewnij się że ciasto jest dobrze odpocznięte i w temp. pokojowej")
                                .build()
                ))
                .relatedTechniques(List.of("ball-shaping", "preshape"))
                .recommendedForStyles(List.of(PizzaStyle.NEAPOLITAN, PizzaStyle.NEW_YORK))
                .premium(false)
                .active(true)
                .build());
        
        log.info("  ✓ Utworzono przewodnik: Rozciąganie ręczne");
    }
    
    // ========================================
    // FERMENTACJA
    // ========================================
    
    private void createColdFermentationGuide() {
        repository.save(TechniqueGuide.builder()
                .category(TechniqueGuide.TechniqueCategory.FERMENTATION)
                .slug("cold-fermentation")
                .title("Fermentacja zimna (retardacja)")
                .titleEn("Cold Fermentation / Retardation")
                .shortDescription("Wolna fermentacja w lodówce. Głęboki smak, lepsza strawność, elastyczny harmonogram.")
                .fullDescription("""
                        Fermentacja zimna to technika dojrzewania ciasta w temperaturze 2-6°C.
                        
                        Zalety:
                        • Głęboko rozwinięty smak (kwasowość, orzechowość)
                        • Lepsza strawność (więcej rozłożonego glutenu)
                        • Elastyczny harmonogram (można użyć ciasta przez 1-5 dni)
                        • Łatwiejsze planowanie
                        
                        Jak to działa:
                        • Zimno spowalnia drożdże, ale nie enzymy
                        • Enzymy rozkładają cukry i białka
                        • Tworzą się związki smakowe
                        """)
                .difficulty(TechniqueGuide.DifficultyLevel.BEGINNER)
                .estimatedTimeMinutes(2880) // 48h
                .steps(List.of(
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(1)
                                .title("Bulk fermentation")
                                .description("Pozostaw ciasto 1-4h w temp. pokojowej")
                                .detailedExplanation("Drożdże muszą się aktywować przed przeniesieniem do lodówki.")
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(2)
                                .title("Ukulkuj")
                                .description("Podziel ciasto na kulki przed chłodzeniem")
                                .detailedExplanation("Możesz też schłodzić całą masę i kulkować przed użyciem.")
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(3)
                                .title("Przenieś do lodówki")
                                .description("Umieść w szczelnym pojemniku lub pod folią")
                                .tips(List.of(
                                        "Pojemnik musi być szczelny (ciasto wysycha)",
                                        "Zostaw miejsce na wzrost",
                                        "Posyp semolina aby nie przywierały"
                                ))
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(4)
                                .title("Fermentuj")
                                .description("Pozostaw 24-72h (do 5 dni dla niskich drożdży)")
                                .detailedExplanation("""
                                        24h: Podstawowy rozwój smaku
                                        48h: Wyraźny smak (zalecane)
                                        72h: Głęboki, złożony smak
                                        96h+: Intensywny, lekko kwaśny (tylko dla doświadczonych)
                                        """)
                                .critical(true)
                                .build(),
                        TechniqueGuide.InstructionStep.builder()
                                .stepNumber(5)
                                .title("Temperowanie")
                                .description("Wyjmij 1-3h przed użyciem")
                                .detailedExplanation("Zimne ciasto jest sztywne i trudne do rozciągnięcia. Musi wrócić do temp. pokojowej.")
                                .tips(List.of(
                                        "Małe kulki (220g): 1-1.5h",
                                        "Duże kulki (350g): 2-3h",
                                        "W gorący dzień: krócej"
                                ))
                                .critical(true)
                                .build()
                ))
                .proTips(List.of(
                        TechniqueGuide.ProTip.builder()
                                .title("Okno użycia")
                                .content("Ciasto ma 'okno' optymalnego użycia. Zbyt wcześnie - smak nie rozwinięty. Zbyt późno - przefermentowane.")
                                .category(TechniqueGuide.ProTip.TipCategory.QUALITY_BOOST)
                                .build()
                ))
                .science(TechniqueGuide.ScienceExplanation.builder()
                        .mainPrinciple("Zimno spowalnia metabolizm drożdży, ale enzymy (amylazy, proteazy) nadal pracują, rozkładając cukry i białka na związki smakowe.")
                        .chemicalProcesses(List.of(
                                "Powolna produkcja CO2 przez drożdże",
                                "Aktywność amylaz - rozkład skrobi na cukry",
                                "Aktywność proteaz - rozkład glutenu na aminokwasy",
                                "Produkcja kwasów organicznych"
                        ))
                        .whyItWorks("Optymalna temperatura dla enzymów (4-10°C) ale suboptymalna dla drożdży, co daje długi czas na rozwój smaku bez przefermentowania.")
                        .build())
                .relatedTechniques(List.of("poolish", "biga", "ball-shaping"))
                .recommendedForStyles(List.of(PizzaStyle.NEAPOLITAN, PizzaStyle.NEW_YORK, PizzaStyle.ROMAN))
                .premium(false)
                .active(true)
                .build());
        
        log.info("  ✓ Utworzono przewodnik: Fermentacja zimna");
    }
}
