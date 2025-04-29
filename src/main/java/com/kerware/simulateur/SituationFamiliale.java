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
}

