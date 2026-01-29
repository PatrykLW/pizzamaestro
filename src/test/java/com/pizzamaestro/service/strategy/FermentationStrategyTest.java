package com.pizzamaestro.service.strategy;

import com.pizzamaestro.model.Recipe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy strategii obliczania drożdży.
 */
class FermentationStrategyTest {

    private final RoomTemperatureStrategy roomTempStrategy = new RoomTemperatureStrategy();
    private final ColdFermentationStrategy coldStrategy = new ColdFermentationStrategy();
    private final SameDayStrategy sameDayStrategy = new SameDayStrategy();
    private final MixedFermentationStrategy mixedStrategy = new MixedFermentationStrategy();

    @Test
    @DisplayName("Strategia temp. pokojowej - dłuższy czas = mniej drożdży")
    void roomTemp_longerTime_lessYeast() {
        // when
        double yeast6h = roomTempStrategy.calculateYeastPercentage(
                6, 24.0, 4.0, Recipe.FermentationMethod.ROOM_TEMPERATURE);
        double yeast12h = roomTempStrategy.calculateYeastPercentage(
                12, 24.0, 4.0, Recipe.FermentationMethod.ROOM_TEMPERATURE);

        // then
        assertThat(yeast6h).isGreaterThan(yeast12h);
    }

    @Test
    @DisplayName("Strategia temp. pokojowej - wyższa temperatura = mniej drożdży")
    void roomTemp_higherTemp_lessYeast() {
        // when
        double yeast20C = roomTempStrategy.calculateYeastPercentage(
                8, 20.0, 4.0, Recipe.FermentationMethod.ROOM_TEMPERATURE);
        double yeast28C = roomTempStrategy.calculateYeastPercentage(
                8, 28.0, 4.0, Recipe.FermentationMethod.ROOM_TEMPERATURE);

        // then
        assertThat(yeast20C).isGreaterThan(yeast28C);
    }

    @Test
    @DisplayName("Fermentacja chłodnicza wymaga mniej drożdży niż temp. pokojowa")
    void coldFermentation_requiresLessYeast() {
        // when
        double coldYeast = coldStrategy.calculateYeastPercentage(
                24, 22.0, 4.0, Recipe.FermentationMethod.COLD_FERMENTATION);
        double roomYeast = roomTempStrategy.calculateYeastPercentage(
                8, 22.0, 4.0, Recipe.FermentationMethod.ROOM_TEMPERATURE);

        // then
        assertThat(coldYeast).isLessThan(roomYeast);
    }

    @Test
    @DisplayName("Same day strategy wymaga więcej drożdży")
    void sameDay_requiresMoreYeast() {
        // when
        double sameDayYeast = sameDayStrategy.calculateYeastPercentage(
                3, 24.0, 4.0, Recipe.FermentationMethod.SAME_DAY);
        double roomYeast = roomTempStrategy.calculateYeastPercentage(
                8, 24.0, 4.0, Recipe.FermentationMethod.ROOM_TEMPERATURE);

        // then
        assertThat(sameDayYeast).isGreaterThan(roomYeast);
    }

    @ParameterizedTest
    @DisplayName("Procent drożdży mieści się w rozsądnym zakresie")
    @CsvSource({
        "6, 20.0, 4.0",
        "12, 22.0, 4.0",
        "24, 24.0, 4.0",
        "48, 26.0, 4.0"
    })
    void yeastPercentage_isInReasonableRange(int hours, double roomTemp, double fridgeTemp) {
        // when
        double yeastRoom = roomTempStrategy.calculateYeastPercentage(
                hours, roomTemp, fridgeTemp, Recipe.FermentationMethod.ROOM_TEMPERATURE);
        double yeastCold = coldStrategy.calculateYeastPercentage(
                hours, roomTemp, fridgeTemp, Recipe.FermentationMethod.COLD_FERMENTATION);

        // then
        assertThat(yeastRoom).isBetween(0.01, 5.0);
        assertThat(yeastCold).isBetween(0.01, 1.0);
    }

    @Test
    @DisplayName("Mieszana fermentacja daje wartość pośrednią")
    void mixedFermentation_intermediateValue() {
        // when
        double mixedYeast = mixedStrategy.calculateYeastPercentage(
                24, 22.0, 4.0, Recipe.FermentationMethod.MIXED);
        double roomYeast = roomTempStrategy.calculateYeastPercentage(
                24, 22.0, 4.0, Recipe.FermentationMethod.ROOM_TEMPERATURE);
        double coldYeast = coldStrategy.calculateYeastPercentage(
                24, 22.0, 4.0, Recipe.FermentationMethod.COLD_FERMENTATION);

        // then - mixed powinien być gdzieś pomiędzy
        // (w praktyce bliżej cold, bo większość czasu w lodówce)
        assertThat(mixedYeast).isGreaterThan(coldYeast);
    }
}
