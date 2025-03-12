package com.kerware.simulateur;

public interface ICalculateurImpot {

    public void setRevenusNetDeclarant1( int rn );
    public void setRevenusNetDeclarant2( int rn );
    public void setSituationFamiliale( SituationFamiliale sf );
    public void setNbEnfantsACharge( int nbe );
    public void setNbEnfantsSituationHandicap( int nbesh );
    public void setParentIsole( boolean pi );

    public void calculImpotSurRevenuNet();

    public int getRevenuNetDeclatant1();
    public int getRevenuNetDeclatant2();
    public double getContribExceptionnelle();
    public int getRevenuFiscalReference();
    public int getAbattement();
    public double getNbPartsFoyerFiscal();
    public int getImpotAvantDecote();
    public int getDecote();
    public int getImpotSurRevenuNet();

}
