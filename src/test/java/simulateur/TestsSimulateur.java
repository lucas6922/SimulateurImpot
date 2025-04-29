package simulateur;

import com.kerware.simulateur.AdaptateurSimulateur;
import com.kerware.simulateur.ICalculateurImpot;
import com.kerware.simulateur.SituationFamiliale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestsSimulateur {

    private static ICalculateurImpot simulateur;

    @BeforeAll
    public static void setUp() {
        simulateur = new AdaptateurSimulateur();
    }

    public static Stream<Arguments> donneesPartsFoyerFiscal() {
        return Stream.of(
                Arguments.of(24000, "CELIBATAIRE", 0, 0, false, 1),
                Arguments.of(24000, "CELIBATAIRE", 1, 0, false, 1.5),
                Arguments.of(24000, "CELIBATAIRE", 2, 0, false, 2),
                Arguments.of(24000, "CELIBATAIRE", 3, 0, false, 3),
                Arguments.of(24000, "MARIE", 0, 0, false, 2),
                Arguments.of(24000, "PACSE", 0, 0, false, 2),
                Arguments.of(24000, "MARIE", 3, 1, false, 4.5),
                Arguments.of(24000, "DIVORCE", 2, 0, true, 2.5),
                Arguments.of(24000, "VEUF", 3, 0, true, 4.5),
                Arguments.of(24000, "CELIBATAIRE", 0, 0, true, 1),
                Arguments.of(24000, "VEUF", 0, 0, false, 1)
                );

    }

    // COUVERTURE EXIGENCE : EXG_IMPOT_03
    @DisplayName("Tests du calcul des parts pour différents foyers fiscaux")
    @ParameterizedTest
    @MethodSource( "donneesPartsFoyerFiscal" )
    public void testNombreDeParts( int revenuNetDeclarant1, String situationFamiliale, int nbEnfantsACharge,
                                   int nbEnfantsSituationHandicap, boolean parentIsole, double nbPartsAttendu) {

        // Arrange
        simulateur.setRevenusNetDeclarant1( revenuNetDeclarant1 );
        simulateur.setRevenusNetDeclarant2( 0);
        simulateur.setSituationFamiliale( SituationFamiliale.valueOf(situationFamiliale) );
        simulateur.setNbEnfantsACharge( nbEnfantsACharge );
        simulateur.setNbEnfantsSituationHandicap( nbEnfantsSituationHandicap );
        simulateur.setParentIsole( parentIsole );

        // Act
        simulateur.calculImpotSurRevenuNet();

        // Assert
        assertEquals(   nbPartsAttendu, simulateur.getNbPartsFoyerFiscal());

    }


    public static Stream<Arguments> donneesAbattementFoyerFiscal() {
        return Stream.of(
                Arguments.of(4900, "CELIBATAIRE", 0, 0, false, 495), // < 495 => 495
                Arguments.of(12000, "CELIBATAIRE", 0, 0, false, 1200), // 10 %
                Arguments.of(200000, "CELIBATAIRE", 0, 0, false, 14171) // > 14171 => 14171
        );

    }

    // COUVERTURE EXIGENCE : EXG_IMPOT_03
    @DisplayName("Tests des abattements pour les foyers fiscaux")
    @ParameterizedTest
    @MethodSource( "donneesAbattementFoyerFiscal" )
    public void testAbattement( int revenuNetDeclarant1, String situationFamiliale, int nbEnfantsACharge,
                                   int nbEnfantsSituationHandicap, boolean parentIsole, int abattementAttendu) {

        // Arrange
        simulateur.setRevenusNetDeclarant1( revenuNetDeclarant1 );
        simulateur.setRevenusNetDeclarant2( 0);
        simulateur.setSituationFamiliale( SituationFamiliale.valueOf(situationFamiliale) );
        simulateur.setNbEnfantsACharge( nbEnfantsACharge );
        simulateur.setNbEnfantsSituationHandicap( nbEnfantsSituationHandicap );
        simulateur.setParentIsole( parentIsole );

        // Act
        simulateur.calculImpotSurRevenuNet();

        // Assert
        assertEquals(   abattementAttendu, simulateur.getAbattement());
    }


    public static Stream<Arguments> donneesRevenusFoyerFiscal() {
        return Stream.of(
                Arguments.of(12000, "CELIBATAIRE", 0, 0, false, 0), // 0%
                Arguments.of(20000, "CELIBATAIRE", 0, 0, false, 199), // 11%
                Arguments.of(35000, "CELIBATAIRE", 0, 0, false, 2736 ), // 30%
                Arguments.of(95000, "CELIBATAIRE", 0, 0, false, 19284), // 41%
                Arguments.of(200000, "CELIBATAIRE", 0, 0, false, 60768) // 45%
        );

    }

    // COUVERTURE EXIGENCE : EXG_IMPOT_04
    @DisplayName("Tests des différents taux marginaux d'imposition")
    @ParameterizedTest
    @MethodSource( "donneesRevenusFoyerFiscal" )
    public void testTrancheImposition( int revenuNet, String situationFamiliale, int nbEnfantsACharge,
                                int nbEnfantsSituationHandicap, boolean parentIsole, int impotAttendu) {

        // Arrange
        simulateur.setRevenusNetDeclarant1( revenuNet );
        simulateur.setRevenusNetDeclarant2( 0);
        simulateur.setSituationFamiliale( SituationFamiliale.valueOf(situationFamiliale) );
        simulateur.setNbEnfantsACharge( nbEnfantsACharge );
        simulateur.setNbEnfantsSituationHandicap( nbEnfantsSituationHandicap );
        simulateur.setParentIsole( parentIsole );

        // Act
        simulateur.calculImpotSurRevenuNet();

        // Assert
        assertEquals(   impotAttendu, simulateur.getImpotSurRevenuNet());
    }



    public static Stream<Arguments> donneesRobustesse() {
        return Stream.of(
                Arguments.of(-1, 0,"CELIBATAIRE", 0, 0, false), // 0%
                Arguments.of(20000,0, null , 0, 0, false), // 11%
                Arguments.of(35000,0, "CELIBATAIRE", -1, 0, false ), // 30%
                Arguments.of(95000,0, "CELIBATAIRE", 0, -1, false), // 41%
                Arguments.of(200000,0, "CELIBATAIRE", 3, 4, false, 60768),
                Arguments.of(200000,0, "MARIE", 3, 2, true),
                Arguments.of(200000,0, "PACSE", 3, 2, true),
                Arguments.of(200000,0, "MARIE", 8, 0, false),
                Arguments.of(200000,10000, "CELIBATAIRE", 8, 0, false),
                Arguments.of(200000,10000, "VEUF", 8, 0, false),
                Arguments.of(200000,10000, "DIVORCE", 8, 0, false)
        );
    }

    // COUVERTURE EXIGENCE : Robustesse
    @DisplayName("Tests de robustesse avec des valeurs interdites")

    @ParameterizedTest( name ="Test avec revenuNetDeclarant1={0}, revenuDeclarant2={1}, situationFamiliale={2}, nbEnfantsACharge={3}, nbEnfantsSituationHandicap={4}, parentIsole={5}")
    @MethodSource( "donneesRobustesse" )
    public void testRobustesse( int revenuNetDeclarant1, int revenuNetDeclarant2 , String situationFamiliale, int nbEnfantsACharge,
                                       int nbEnfantsSituationHandicap, boolean parentIsole) {

        // Arrange
        simulateur.setRevenusNetDeclarant1( revenuNetDeclarant1 );
        simulateur.setRevenusNetDeclarant2( revenuNetDeclarant2 );
        if ( situationFamiliale == null )
                simulateur.setSituationFamiliale( null  );
        else
                simulateur.setSituationFamiliale( SituationFamiliale.valueOf( situationFamiliale ));
        simulateur.setNbEnfantsACharge( nbEnfantsACharge );
        simulateur.setNbEnfantsSituationHandicap( nbEnfantsSituationHandicap );
        simulateur.setParentIsole( parentIsole );

        // Act & Assert
        assertThrows( IllegalArgumentException.class,  () -> { simulateur.calculImpotSurRevenuNet();} );


    }

    // AVEC D'AUTRES IDEES DE TESTS
    // AVEC @ParameterizedTest et @CsvFileSource
    @DisplayName("Tests supplémentaires de cas variés de foyers fiscaux - ")
    @ParameterizedTest( name = " avec revenuNetDeclarant1={0}, revenuNetDeclarant2={1}, situationFamiliale={2}, nbEnfantsACharge={3}, nbEnfantsSituationHandicap={4}, parentIsole={5} - IMPOT NET ATTENDU = {6}")
    @CsvFileSource( resources={"/datasImposition.csv"} , numLinesToSkip = 1 )
    public void testCasImposition( int revenuNetDeclarant1, int revenuNetDeclarant2,  String situationFamiliale, int nbEnfantsACharge,
                                       int nbEnfantsSituationHandicap, boolean parentIsole, int impotAttendu) {

       // Arrange
        simulateur.setRevenusNetDeclarant1( revenuNetDeclarant1 );
        simulateur.setRevenusNetDeclarant2( revenuNetDeclarant2 );
        simulateur.setSituationFamiliale( SituationFamiliale.valueOf( situationFamiliale) );
        simulateur.setNbEnfantsACharge( nbEnfantsACharge );
        simulateur.setNbEnfantsSituationHandicap( nbEnfantsSituationHandicap );
        simulateur.setParentIsole( parentIsole );

        // Act
        simulateur.calculImpotSurRevenuNet();

        // Assert
        assertEquals(   Integer.valueOf(impotAttendu), simulateur.getImpotSurRevenuNet());
    }

    @DisplayName("Test des parts avec uniquement des enfants en situation de handicap")
    @ParameterizedTest
    @MethodSource("donneesEnfantsHandicapesSeulement")
    public void testPartsAvecEnfantsHandicapesSeulement(int revenu, String situation, int nbEnfantsHandicap, double expectedParts, int nbEnfants) {
        simulateur.setRevenusNetDeclarant1(revenu);
        simulateur.setRevenusNetDeclarant2(0);
        simulateur.setSituationFamiliale(SituationFamiliale.valueOf(situation));
        simulateur.setNbEnfantsACharge(nbEnfants);
        simulateur.setNbEnfantsSituationHandicap(nbEnfantsHandicap);
        simulateur.setParentIsole(false);

        simulateur.calculImpotSurRevenuNet();
        assertEquals(expectedParts, simulateur.getNbPartsFoyerFiscal());
    }

    public static Stream<Arguments> donneesEnfantsHandicapesSeulement() {
        return Stream.of(
                Arguments.of(24000, "CELIBATAIRE", 1, 2, 1),
                Arguments.of(24000, "MARIE", 2, 4, 2)
        );
    }

    @DisplayName("Test des parts avec enfants à charge et handicapés")
    @ParameterizedTest
    @MethodSource("donneesEnfantsCombines")
    public void testPartsAvecEnfantsCombines(int revenu, String situation, int enfants, int enfantsHandicap, double expectedParts) {
        simulateur.setRevenusNetDeclarant1(revenu);
        simulateur.setRevenusNetDeclarant2(0);
        simulateur.setSituationFamiliale(SituationFamiliale.valueOf(situation));
        simulateur.setNbEnfantsACharge(enfants);
        simulateur.setNbEnfantsSituationHandicap(enfantsHandicap);
        simulateur.setParentIsole(false);

        simulateur.calculImpotSurRevenuNet();
        assertEquals(expectedParts, simulateur.getNbPartsFoyerFiscal());
    }

    public static Stream<Arguments> donneesEnfantsCombines() {
        return Stream.of(
                Arguments.of(24000, "CELIBATAIRE", 3, 1, 3.5),
                Arguments.of(24000, "MARIE", 2, 1, 3.5)
        );
    }

    @DisplayName("Test abattement à la limite haute de 10%")
    @ParameterizedTest
    @MethodSource("donneesAbattementBorne")
    public void testAbattementBorne(int revenu, int abattementAttendu) {
        simulateur.setRevenusNetDeclarant1(revenu);
        simulateur.setRevenusNetDeclarant2(0);
        simulateur.setSituationFamiliale(SituationFamiliale.CELIBATAIRE);
        simulateur.setNbEnfantsACharge(0);
        simulateur.setNbEnfantsSituationHandicap(0);
        simulateur.setParentIsole(false);

        simulateur.calculImpotSurRevenuNet();
        assertEquals(abattementAttendu, simulateur.getAbattement());
    }

    public static Stream<Arguments> donneesAbattementBorne() {
        return Stream.of(
                Arguments.of(141710, 14171),
                Arguments.of(141700, 14170),
                Arguments.of(141720, 14171)
        );
    }

    @DisplayName("Test du plafonnement du quotient familial")
    @ParameterizedTest
    @MethodSource("donneesQuotientPlafond")
    public void testPlafondQuotientFamilial(int revenuNet, String situation, int nbEnfants, int nbEnfantsHandicap, double expectedParts, int expectedImpotMax) {
        simulateur.setRevenusNetDeclarant1(revenuNet);
        simulateur.setRevenusNetDeclarant2(0);
        simulateur.setSituationFamiliale(SituationFamiliale.valueOf(situation));
        simulateur.setNbEnfantsACharge(nbEnfants);
        simulateur.setNbEnfantsSituationHandicap(nbEnfantsHandicap);
        simulateur.setParentIsole(false);

        simulateur.calculImpotSurRevenuNet();
        assertEquals(expectedParts, simulateur.getNbPartsFoyerFiscal());
        assertEquals(expectedImpotMax, simulateur.getImpotSurRevenuNet());
    }

    public static Stream<Arguments> donneesQuotientPlafond() {
        return Stream.of(
                Arguments.of(120000, "MARIE", 4, 0, 5, 8270),
                Arguments.of(120000, "CELIBATAIRE", 4, 0, 4, 17955)
        );
    }

    @DisplayName("Test du plafonnement du quotient familial")
    @ParameterizedTest
    @MethodSource("donneesParametres")
    public void testParametres(int r1, int r2, SituationFamiliale sf, int enf, int enfH, boolean iso) {
        simulateur.setRevenusNetDeclarant1(r1);
        simulateur.setRevenusNetDeclarant2(r2);
        simulateur.setSituationFamiliale(SituationFamiliale.valueOf(sf.toString()));
        simulateur.setNbEnfantsACharge(enf);
        simulateur.setNbEnfantsSituationHandicap(enfH);
        simulateur.setParentIsole(false);
        simulateur.setParentIsole(iso);

        assertThrows(IllegalArgumentException.class, () -> simulateur.calculImpotSurRevenuNet());

    }

    public static Stream<Arguments> donneesParametres() {
        return Stream.of(
                Arguments.of(-100, 0, "MARIE", 0, 0, false),
                Arguments.of(20000, 0, "CELIBATAIRE", -1, 0, false),
                Arguments.of(20000, 0, "CELIBATAIRE", 0, -2, false),
                Arguments.of(0, -100, "MARIE", 0, 0, false),
                Arguments.of(0, 0, "MARIE", 8, 0, false),
                Arguments.of(0, 0, "MARIE", 0, 0, true),
                Arguments.of(0, 100, "CELIBATAIRE", 0, 0, false)
        );
    }



}
