package com.kerware.simulateur;

public record TrancheCEHR(int borneInferieure, int borneSuperieure, double taux) implements Tranche{}
