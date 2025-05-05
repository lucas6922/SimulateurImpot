# Simulateur d'Impôt sur le Revenu – Refactorisation & Adaptateur

## Contexte
Ce projet simule le calcul de l'impôt sur le revenu en France pour l'année 2024 (revenus 2023). Nous étions confrontés à un code “legacy” de mauvaise qualité, sans modularité, documentation ni traçabilité des exigences métiers. L’objectif de cette refactorisation est de :

1. **Améliorer la lisibilité** et la maintenance du code.  
2. **Modulariser** la logique métier selon les principes SOLID.  
3. **Traçabilité** : insérer des commentaires liant chaque exigence (EXG_IMPOT_01 à EXG_IMPOT_07) aux parties du code.  
4. **Adapter** l’ancienne interface pour que les tests existants continuent de fonctionner.  

---

## 1. Problèmes identifiés dans le code Legacy
- **Nommage ambigu** : variables cryptiques et valeurs magiques (`l00`, `tce02C`, ...).  
- **Absence de modularité** : toute la logique est dans une unique méthode de plus de 200 lignes.  
- **Pas de documentation/exceptions standardisées** : messages d’erreur et `System.out.println` mélangés à la logique.  
- **Données embarquées dans des tableaux** : rend difficile l’évolution des barèmes et tranches.  
- **Manque de traçabilité** : aucun commentaire ne relie les blocs métier à leurs exigences fonctionnelles.  

---

## 2. Refactorisation Principale
1. **Séparation des constantes fiscales** dans des collections (`List<TrancheImpot>`, `List<TrancheCEHR>`).  
2. **Extraction des responsabilités** :  
   - Vérification des paramètres (`verifierParametres()`)  
   - Calcul de l’abattement (`calculAbattement()`)  
   - Calcul du nombre de parts (`calculPartsFiscales()`)  
   - Calcul du revenu fiscal (`calculRevenuFiscal()`)  
   - Contribution exceptionnelle (`calculContributionExceptionnelle()`)  
   - Calcul de l’impôt par tranche (`calculImpotParTranche()`)  
   - Plafonnement du quotient familial (`appliquerPlafondQF()`)  
   - Décote (`appliquerDecote()`)  
3. **Commentaires de traçabilité** : chaque bloc/méthode porte un tag `// EXG_IMPOT_0X` pour indiquer quelle exigence il implémente.  
4. **Arrondis** : usage systématique de `Math.round` pour satisfaire EXG_IMPOT_01.  

---

## 3. Patron Adaptateur (Adapter)
Pour préserver la compatibilité avec les tests unitaires et la logique d’injection de dépendances, nous avons défini une interface `ICalculateurImpot` et mis en place un adaptateur `AdaptateurSimulateur` qui traduit les appels anciens vers la nouvelle implémentation.
