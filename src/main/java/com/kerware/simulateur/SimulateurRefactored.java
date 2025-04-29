package com.kerware.simulateur;


import java.util.List;

/**
 * Simulateur d'impôt sur le revenu en France pour 2024 (revenus 2023).
 * Cas simples de contribuables avec différents statuts familiaux.
 */
public class SimulateurRefactored {

    // --- Constantes fiscales ---

    // Tranches d'imposition
    private static final List<TrancheImpot> TRANCHES_IMPOT = List.of(
            new TrancheImpot(0, 11_294, 0.0),
            new TrancheImpot(11_294, 28_797, 0.11),
            new TrancheImpot(28_797, 82_341, 0.30),
            new TrancheImpot(82_341, 177_106, 0.41),
            new TrancheImpot(177_106, Integer.MAX_VALUE, 0.45)
    );


    // Contribution exceptionnelle sur les hauts revenus (CEHR)
    private final int[] limitesCEHR = {0, 250000, 500000, 1000000, Integer.MAX_VALUE};
    private final double[] tauxCEHRCelibataire = {0.0, 0.03, 0.04, 0.04};
    private final double[] tauxCEHRCouple = {0.0, 0.0, 0.03, 0.04};

    // Abattement
    private final int lAbtMax = 14171;
    private final int lAbtMin = 495;
    private final double tAbt = 0.1;

    // Plafonnement des effets du quotient familial
    private final double plafDemiPart = 1759;

    // Décote
    private final double seuilDecoteDeclarantSeul = 1929;
    private final double seuilDecoteDeclarantCouple = 3191;
    private final double decoteMaxDeclarantSeul = 873;
    private final double decoteMaxDeclarantCouple = 1444;
    private final double tauxDecote = 0.4525;

    // --- Données de calcul (pour consultation) ---
    private int rNetDecl1 = 0, rNetDecl2 = 0;
    private int nbEnf = 0, nbEnfH = 0;
    private double rFRef = 0, rImposable = 0, abt = 0;
    private double nbPtsDecl = 0, nbPts = 0, decote = 0;
    private double mImpDecl = 0, mImp = 0, mImpAvantDecote = 0;
    private boolean parIso = false;
    private double contribExceptionnelle = 0;

    // --- Getters utiles pour les tests ---
    public double getRevenuReference() { return rFRef; }
    public double getDecote() { return decote; }
    public double getAbattement() { return abt; }
    public double getNbParts() { return nbPts; }
    public double getImpotAvantDecote() { return mImpAvantDecote; }
    public double getImpotNet() { return mImp; }
    public int getRevenuNetDeclatant1() { return rNetDecl1; }
    public int getRevenuNetDeclatant2() { return rNetDecl2; }
    public double getContribExceptionnelle() { return contribExceptionnelle; }

    // --- Méthode principale ---
    public int calculImpot(int revNetDecl1, int revNetDecl2, SituationFamiliale sitFam,
                           int nbEnfants, int nbEnfantsHandicapes, boolean parentIsol) {

        verifierParametres(revNetDecl1, revNetDecl2, sitFam, nbEnfants, nbEnfantsHandicapes, parentIsol);

        this.rNetDecl1 = revNetDecl1;
        this.rNetDecl2 = revNetDecl2;
        this.nbEnf = nbEnfants;
        this.nbEnfH = nbEnfantsHandicapes;
        this.parIso = parentIsol;

        calculAbattement(sitFam);
        calculPartsFiscales(sitFam);
        calculRevenuFiscal();
        calculContributionExceptionnelle();

        double impotBrutDecl = calculImpotParTranche(rFRef / nbPtsDecl) * nbPtsDecl;
        this.mImpDecl = Math.round(impotBrutDecl);

        double impotBrutFoyer = calculImpotParTranche(rFRef / nbPts) * nbPts;
        this.mImp = Math.round(impotBrutFoyer);

        appliquerPlafondQF();
        this.mImpAvantDecote = mImp;
        appliquerDecote();

        this.mImp += contribExceptionnelle;
        this.mImp = Math.round(mImp);

        return (int) mImp;
    }

    // --- Méthodes internes ---

    private void verifierParametres(int r1, int r2, SituationFamiliale sf, int enf, int enfH, boolean iso) {
        if (r1 < 0 || r2 < 0) throw new IllegalArgumentException("Revenu net négatif");
        if (enf < 0 || enfH < 0) throw new IllegalArgumentException("Nombre d'enfants invalide");
        if (enfH > enf) throw new IllegalArgumentException("Plus d'enfants handicapés que d'enfants");
        if (enf > 7) throw new IllegalArgumentException("Nombre d'enfants > 7 non supporté");
        if (iso && (sf == SituationFamiliale.MARIE || sf == SituationFamiliale.PACSE))
            throw new IllegalArgumentException("Parent isolé ne peut être marié/pacsé");
        if ((sf == SituationFamiliale.CELIBATAIRE || sf == SituationFamiliale.DIVORCE || sf == SituationFamiliale.VEUF)
                && r2 > 0) throw new IllegalArgumentException("Déclarant seul avec 2e revenu");
    }

    private void calculAbattement(SituationFamiliale sf) {
        long abt1 = Math.max(lAbtMin, Math.min(lAbtMax, Math.round(rNetDecl1 * tAbt)));
        long abt2 = (sf == SituationFamiliale.MARIE || sf == SituationFamiliale.PACSE)
                ? Math.max(lAbtMin, Math.min(lAbtMax, Math.round(rNetDecl2 * tAbt))) : 0;
        this.abt = abt1 + abt2;
    }

    private void calculRevenuFiscal() {
        rFRef = rNetDecl1 + rNetDecl2 - abt;
        if (rFRef < 0) rFRef = 0;
    }

    private void calculPartsFiscales(SituationFamiliale sf) {
        nbPtsDecl = (sf == SituationFamiliale.MARIE || sf == SituationFamiliale.PACSE) ? 2 : 1;
        nbPts = nbPtsDecl + ((nbEnf <= 2) ? nbEnf * 0.5 : 1 + (nbEnf - 2));
        if (parIso && nbEnf > 0) nbPts += 0.5;
        if (sf == SituationFamiliale.VEUF && nbEnf > 0) nbPts += 1;
        nbPts += nbEnfH * 0.5;
    }

    private void calculContributionExceptionnelle() {
        contribExceptionnelle = 0;
        double[] tauxCEHR = (nbPtsDecl == 1) ? tauxCEHRCelibataire : tauxCEHRCouple;
        for (int i = 0; i < tauxCEHR.length; i++) {
            if (rFRef > limitesCEHR[i]) {
                double base = Math.min(rFRef, limitesCEHR[i + 1]) - limitesCEHR[i];
                contribExceptionnelle += base * tauxCEHR[i];
            }
        }
        contribExceptionnelle = Math.round(contribExceptionnelle);
    }

    private double calculImpotParTranche(double revenu) {
        double impot = 0;

        for (TrancheImpot tranche : TRANCHES_IMPOT){
            if( revenu > tranche.borneInferieure() ) {
                double base = Math.min(revenu, tranche.borneSuperieure()) - tranche.borneInferieure();
                impot += base * tranche.taux();
            } else {
                break;
            }
        }
        return impot;
    }

    private void appliquerPlafondQF() {
        double ecartPts = nbPts - nbPtsDecl;
        double plafond = (ecartPts / 0.5) * plafDemiPart;
        double baisse = mImpDecl - mImp;
        if (baisse > plafond) {
            mImp = mImpDecl - plafond;
        }
    }

    private void appliquerDecote() {
        if (nbPtsDecl == 1 && mImp < seuilDecoteDeclarantSeul) {
            decote = decoteMaxDeclarantSeul - (mImp * tauxDecote);
        } else if (nbPtsDecl == 2 && mImp < seuilDecoteDeclarantCouple) {
            decote = decoteMaxDeclarantCouple - (mImp * tauxDecote);
        } else {
            decote = 0;
        }
        decote = Math.round(Math.min(decote, mImp));
        mImp -= decote;
    }
}
