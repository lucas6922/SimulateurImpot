package com.kerware.simulateur;

public interface Tranche {
    int borneInferieure();
    int borneSuperieure();
    double taux();

    default double baseTaxable(double revenu) {
        if (revenu < borneInferieure()) {
            return 0;
        }
        return (Math.min(revenu, borneSuperieure()) - borneInferieure()) * taux();
    }
}
