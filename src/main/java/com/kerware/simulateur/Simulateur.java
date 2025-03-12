package com.kerware.simulateur;

/**
 *  Cette classe permet de simuler le calcul de l'impôt sur le revenu
 *  en France pour l'année 2024 sur les revenus de l'année 2023 pour
 *  des cas simples de contribuables célibataires, mariés, divorcés, veufs
 *  ou pacsés avec ou sans enfants à charge ou enfants en situation de handicap
 *  et parent isolé.
 *
 *  EXEMPLE DE CODE DE TRES MAUVAISE QUALITE FAIT PAR UN DEBUTANT
 *
 *  Pas de lisibilité, pas de commentaires, pas de tests
 *  Pas de documentation, pas de gestion des erreurs
 *  Pas de logique métier, pas de modularité
 *  Pas de gestion des exceptions, pas de gestion des logs
 *  Principe "Single Responsability" non respecté
 *  Pas de traçabilité vers les exigences métier
 *
 *  Pourtant ce code fonctionne correctement
 *  Il s'agit d'un "legacy" code qui est difficile à maintenir
 *  L'auteur n'a pas fourni de tests unitaires
 **/

public class Simulateur {


    // Les limites des tranches de revenus imposables
    private int l00 = 0 ;
    private int l01 = 11294;
    private int l02 = 28797;
    private int l03 = 82341;
    private int l04 = 177106;
    private int l05 = Integer.MAX_VALUE;

    private int[] limites = new int[6];

    // Les taux d'imposition par tranche
    private double t00 = 0.0;
    private double t01 = 0.11;
    private double t02 = 0.3;
    private double t03 = 0.41;
    private double t04 = 0.45;

    private double[] taux = new double[5];

    // Les limites des tranches pour la contribution exceptionnelle sur les hauts revenus
    private int lce00 = 0;
    private int lce01 = 250000;
    private int lce02 = 500000;
    private int lce03 = 1000000;
    private int lce04 = Integer.MAX_VALUE;

    private int[] limitesCEHR = new int[5];

    // Les taux de la contribution exceptionnelle sur les hauts revenus pour les celibataires
    private double tce00 = 0.0;
    private double tce01 = 0.03;
    private double tce02 = 0.04;
    private double tce03 = 0.04;

    private double[] tauxCEHRCelibataire = new double[4];

    // Les taux de la contribution exceptionnelle sur les hauts revenus pour les couples
    private double tce00C = 0.0;
    private double tce01C = 0.0;
    private double tce02C = 0.03;
    private double tce03C = 0.04;

    private double[] tauxCEHRCouple = new double[4];

    // Abattement
    private  int lAbtMax = 14171;
    private  int lAbtMin = 495;
    private double tAbt = 0.1;

    // Plafond de baisse maximal par demi part
    private double plafDemiPart = 1759;

    private double seuilDecoteDeclarantSeul = 1929;
    private double seuilDecoteDeclarantCouple    = 3191;

    private double decoteMaxDeclarantSeul = 873;
    private double decoteMaxDeclarantCouple = 1444;
    private double tauxDecote = 0.4525;

    // revenu net
    private int rNetDecl1 = 0;
    private int rNetDecl2 = 0;
    // nb enfants
    private int nbEnf = 0;
    // nb enfants handicapés
    private int nbEnfH = 0;

    // revenu fiscal de référence
    private double rFRef = 0;

    // revenu imposable
    private double rImposable = 0;

    // abattement
    private double abt = 0;

    // nombre de parts des  déclarants
    private double nbPtsDecl = 0;
    // nombre de parts du foyer fiscal
    private double nbPts = 0;

    // decote
    private double decote = 0;
    // impôt des déclarants
    private double mImpDecl = 0;
    // impôt du foyer fiscal
    private double mImp = 0;
    private double mImpAvantDecote = 0;
    // parent isolé
    private boolean parIso = false;
    // Contribution exceptionnelle sur les hauts revenus
    private double contribExceptionnelle = 0;

    // Getters pour adapter le code legacy pour les tests unitaires

    public double getRevenuReference() {
        return rFRef;
    }

    public double getDecote() {
        return decote;
    }


    public double getAbattement() {
        return abt;
    }

    public double getNbParts() {
        return nbPts;
    }

    public double getImpotAvantDecote() {
        return mImpAvantDecote;
    }

    public double getImpotNet() {
        return mImp;
    }

    public int getRevenuNetDeclatant1() {
        return rNetDecl1;
    }

    public int getRevenuNetDeclatant2() {
        return rNetDecl2;
    }

    public double getContribExceptionnelle() {
        return contribExceptionnelle;
    }


    // Fonction de calcul de l'impôt sur le revenu net en France en 2024 sur les revenu 2023

    public int calculImpot( int revNetDecl1, int revNetDecl2, SituationFamiliale sitFam, int nbEnfants, int nbEnfantsHandicapes, boolean parentIsol) {

        // Préconditions
        if ( revNetDecl1  < 0 || revNetDecl2 < 0 ) {
            throw new IllegalArgumentException("Le revenu net ne peut pas être négatif");
        }

        if ( nbEnfants < 0 ) {
            throw new IllegalArgumentException("Le nombre d'enfants ne peut pas être négatif");
        }

        if ( nbEnfantsHandicapes < 0 ) {
            throw new IllegalArgumentException("Le nombre d'enfants handicapés ne peut pas être négatif");
        }

        if ( sitFam == null ) {
            throw new IllegalArgumentException("La situation familiale ne peut pas être null");
        }

        if ( nbEnfantsHandicapes > nbEnfants ) {
            throw new IllegalArgumentException("Le nombre d'enfants handicapés ne peut pas être supérieur au nombre d'enfants");
        }

        if ( nbEnfants > 7 ) {
            throw new IllegalArgumentException("Le nombre d'enfants ne peut pas être supérieur à 7");
        }

        if ( parentIsol && ( sitFam == SituationFamiliale.MARIE || sitFam == SituationFamiliale.PACSE ) ) {
            throw new IllegalArgumentException("Un parent isolé ne peut pas être marié ou pacsé");
        }

        boolean seul = sitFam == SituationFamiliale.CELIBATAIRE || sitFam == SituationFamiliale.DIVORCE || sitFam == SituationFamiliale.VEUF;
        if (  seul && revNetDecl2 > 0 ) {
            throw new IllegalArgumentException("Un célibataire, un divorcé ou un veuf ne peut pas avoir de revenu pour le déclarant 2");
        }

        // Initialisation des variables

        rNetDecl1 = revNetDecl1;
        rNetDecl2 = revNetDecl2;

        nbEnf = nbEnfants;
        nbEnfH = nbEnfantsHandicapes;
        parIso = parentIsol;

        limites[0] = l00;
        limites[1] = l01;
        limites[2] = l02;
        limites[3] = l03;
        limites[4] = l04;
        limites[5] = l05;

        taux[0] = t00;
        taux[1] = t01;
        taux[2] = t02;
        taux[3] = t03;
        taux[4] = t04;

        limitesCEHR[0] = lce00;
        limitesCEHR[1] = lce01;
        limitesCEHR[2] = lce02;
        limitesCEHR[3] = lce03;
        limitesCEHR[4] = lce04;

        tauxCEHRCelibataire[0] = tce00;
        tauxCEHRCelibataire[1] = tce01;
        tauxCEHRCelibataire[2] = tce02;
        tauxCEHRCelibataire[3] = tce03;

        tauxCEHRCouple[0] = tce00C;
        tauxCEHRCouple[1] = tce01C;
        tauxCEHRCouple[2] = tce02C;
        tauxCEHRCouple[3] = tce03C;

        System.out.println("--------------------------------------------------");
        System.out.println( "Revenu net declarant1 : " + rNetDecl1 );
        System.out.println( "Revenu net declarant2 : " + rNetDecl2 );
        System.out.println( "Situation familiale : " + sitFam.name() );

        // Abattement
        // EXIGENCE : EXG_IMPOT_02
        long abt1 = Math.round(rNetDecl1 * tAbt);
        long abt2 = Math.round(rNetDecl2 * tAbt);

        if (abt1 > lAbtMax) {
            abt1 = lAbtMax;
        }
        if ( sitFam == SituationFamiliale.MARIE || sitFam == SituationFamiliale.PACSE ) {
            if (abt2 > lAbtMax) {
                abt2 = lAbtMax;
            }
        }

        if (abt1 < lAbtMin) {
            abt1 = lAbtMin;
        }

        if ( sitFam == SituationFamiliale.MARIE || sitFam == SituationFamiliale.PACSE ) {
            if (abt2 < lAbtMin) {
                abt2 = lAbtMin;
            }
        }

        abt = abt1 + abt2;
        System.out.println( "Abattement : " + abt );

        rFRef = rNetDecl1 + revNetDecl2 - abt;
        if ( rFRef < 0 ) {
            rFRef = 0;
        }

        System.out.println( "Revenu fiscal de référence : " + rFRef );


        // parts déclarants
        // EXIG  : EXG_IMPOT_03
        switch ( sitFam ) {
            case CELIBATAIRE:
                nbPtsDecl = 1;
                break;
            case MARIE:
                nbPtsDecl = 2;
                break;
            case DIVORCE:
                nbPtsDecl = 1;
                break;
            case VEUF:
                nbPtsDecl = 1;
                break;
            case PACSE:
                nbPtsDecl = 2;
                break;
        }

        System.out.println( "Nombre d'enfants  : " + nbEnf );
        System.out.println( "Nombre d'enfants handicapés : " + nbEnfH );

        // parts enfants à charge
        if ( nbEnf <= 2 ) {
            nbPts = nbPtsDecl + nbEnf * 0.5;
        } else if ( nbEnf > 2 ) {
            nbPts = nbPtsDecl+  1.0 + ( nbEnf - 2 );
        }

        // parent isolé

        System.out.println( "Parent isolé : " + parIso );

        if ( parIso ) {
            if ( nbEnf > 0 ){
                nbPts = nbPts + 0.5;
            }
        }

        // Veuf avec enfant
        if ( sitFam == SituationFamiliale.VEUF && nbEnf > 0 ) {
            nbPts = nbPts + 1;
        }

        // enfant handicapé
        nbPts = nbPts + nbEnfH * 0.5;

        System.out.println( "Nombre de parts : " + nbPts );

        // EXIGENCE : EXG_IMPOT_07:
        // Contribution exceptionnelle sur les hauts revenus
        contribExceptionnelle = 0;
        int i = 0;
        do {
            if ( rFRef >= limitesCEHR[i] && rFRef < limitesCEHR[i+1] ) {
                if ( nbPtsDecl == 1 ) {
                    contribExceptionnelle += ( rFRef - limitesCEHR[i] ) * tauxCEHRCelibataire[i];
                } else {
                    contribExceptionnelle += ( rFRef - limitesCEHR[i] ) * tauxCEHRCouple[i];
                }
                break;
            } else {
                if ( nbPtsDecl == 1 ) {
                    contribExceptionnelle += ( limitesCEHR[i+1] - limitesCEHR[i] ) * tauxCEHRCelibataire[i];
                } else {
                    contribExceptionnelle += ( limitesCEHR[i+1] - limitesCEHR[i] ) * tauxCEHRCouple[i];
                }
            }
            i++;
        } while( i < 5);

        contribExceptionnelle = Math.round( contribExceptionnelle );
        System.out.println( "Contribution exceptionnelle sur les hauts revenus : " + contribExceptionnelle );

        // Calcul impôt des declarants
        // EXIGENCE : EXG_IMPOT_04
        rImposable = rFRef / nbPtsDecl ;

        mImpDecl = 0;

        i = 0;
        do {
            if ( rImposable >= limites[i] && rImposable < limites[i+1] ) {
                mImpDecl += ( rImposable - limites[i] ) * taux[i];
                break;
            } else {
                mImpDecl += ( limites[i+1] - limites[i] ) * taux[i];
            }
            i++;
        } while( i < 5);

        mImpDecl = mImpDecl * nbPtsDecl;
        mImpDecl = Math.round( mImpDecl );

        System.out.println( "Impôt brut des déclarants : " + mImpDecl );

        // Calcul impôt foyer fiscal complet
        // EXIGENCE : EXG_IMPOT_04
        rImposable =  rFRef / nbPts;
        mImp = 0;
        i = 0;

        do {
            if ( rImposable >= limites[i] && rImposable < limites[i+1] ) {
                mImp += ( rImposable - limites[i] ) * taux[i];
                break;
            } else {
                mImp += ( limites[i+1] - limites[i] ) * taux[i];
            }
            i++;
        } while( i < 5);

        mImp = mImp * nbPts;
        mImp = Math.round( mImp );

        System.out.println( "Impôt brut du foyer fiscal complet : " + mImp );

        // Vérification de la baisse d'impôt autorisée
        // EXIGENCE : EXG_IMPOT_05
        // baisse impot

        double baisseImpot = mImpDecl - mImp;

        System.out.println( "Baisse d'impôt : " + baisseImpot );

        // dépassement plafond
        double ecartPts = nbPts - nbPtsDecl;

        double plafond = (ecartPts / 0.5) * plafDemiPart;

        System.out.println( "Plafond de baisse autorisée " + plafond );

        if ( baisseImpot >= plafond ) {
            mImp = mImpDecl - plafond;
        }

        System.out.println( "Impôt brut après plafonnement avant decote : " + mImp );
        mImpAvantDecote = mImp;

        // Calcul de la decote
        // EXIGENCE : EXG_IMPOT_06

        decote = 0;
        // decote
        if ( nbPtsDecl == 1 ) {
            if ( mImp < seuilDecoteDeclarantSeul ) {
                 decote = decoteMaxDeclarantSeul - ( mImp  * tauxDecote );
            }
        }
        if (  nbPtsDecl == 2 ) {
            if ( mImp < seuilDecoteDeclarantCouple ) {
                 decote =  decoteMaxDeclarantCouple - ( mImp  * tauxDecote  );
            }
        }
        decote = Math.round( decote );

        if ( mImp <= decote ) {
            decote = mImp;
        }

        System.out.println( "Decote : " + decote );

        mImp = mImp - decote;

        mImp += contribExceptionnelle;

        mImp = Math.round( mImp );

        System.out.println( "Impôt sur le revenu net final : " + mImp );
        return  (int)mImp;
    }





}
