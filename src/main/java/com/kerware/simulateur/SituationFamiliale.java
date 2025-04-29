package com.kerware.simulateur;

public enum SituationFamiliale {
    CELIBATAIRE,
    PACSE,
    MARIE,
    DIVORCE,
    VEUF;

    public boolean isSingle() {
        return this == CELIBATAIRE || this == DIVORCE || this == VEUF;
    }

    public boolean isMarried() {
        return this == MARIE || this == PACSE;
    }

    public boolean isVeuf() {
        return this == VEUF;
    }
}

