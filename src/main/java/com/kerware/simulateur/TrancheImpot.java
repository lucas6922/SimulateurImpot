package com.kerware.simulateur;

/**
 * Représente une tranche d'imposition : bornes et taux.
 */
public record TrancheImpot(int borneInferieure, int borneSuperieure, double taux) {}
