# Projet de Simulation de Circulation de Trains

## Description du Projet

Ce projet implémente une simulation de circulation de trains sur une ligne ferroviaire avec gares et sections. Il utilise la programmation concurrente en Java avec des mécanismes de synchronisation pour éviter les collisions et les interblocages.

## Architecture du Projet

### Structure des Fichiers

```
trains_project/
├── bin/
│   └── train/          # Fichiers compilés (.class)
├── src/
│   └── train/          # Code source Java
│       ├── BadPositionForTrainException.java   # Exception pour position invalide
│       ├── Direction.java                      # Énumération des directions (LR/RL)
│       ├── Element.java                        # Classe abstraite pour éléments de la ligne
│       ├── Station.java                        # Gare terminale
│       ├── IntermediateStation.java           # Gare intermédiaire (permet croisements)
│       ├── Section.java                        # Section de voie ferrée
│       ├── Position.java                       # Position d'un train (élément + direction)
│       ├── Train.java                          # Représentation d'un train
│       ├── Railway.java                        # Gestion de la ligne et synchronisation
│       ├── RailwayView.java                    # Interface de visualisation
│       ├── RailwayFrame.java                   # Fenêtre graphique
│       └── Main.java                           # Point d'entrée du programme
└── README.md           # Ce fichier
```

### Hiérarchie des Classes

```
Element (abstract)
├── Station                    # Gare terminale (aux extrémités)
│   └── IntermediateStation   # Gare intermédiaire (au milieu de la ligne)
└── Section                    # Section de voie ferrée

Train (Runnable)              # Train autonome (thread)
Position                       # Position = Élément + Direction
Direction (enum)               # LR (Left-Right) ou RL (Right-Left)
Railway                        # Gestionnaire de la ligne ferroviaire
```

## Concepts Clés

### 1. Éléments de la Ligne

#### Station (Gare Terminale)
- **Capacité** : n quais (peut accueillir n trains simultanément)
- **Position** : Aux extrémités de la ligne (début ou fin)
- **Fonction** : Point de départ/arrivée des trains
- **Règle** : Les trains changent de direction automatiquement en arrivant

#### IntermediateStation (Gare Intermédiaire)
- **Capacité** : n quais
- **Position** : Au milieu de la ligne, entre des sections
- **Fonction** : Permet aux trains de se croiser
- **Particularité** : Pas de restriction de sens unique (contrairement aux gares terminales)

#### Section
- **Capacité** : 1 train maximum
- **Fonction** : Tronçon de voie entre deux gares
- **Règle** : Un seul train à la fois pour éviter les collisions

### 2. Mécanismes de Synchronisation

#### Système de Réservation
Chaque train réserve une place à la gare de destination **avant** de quitter sa gare actuelle. Cela garantit qu'il aura une place en arrivant.

```
État de la gare :
- Places totales : size
- Places occupées : trainCount
- Places réservées : reservedSpots
- Places disponibles : size - trainCount - reservedSpots
```

#### Prévention de l'Interblocage

**Gestion par Segment :**

La ligne est divisée en **segments** (portions entre deux gares). Chaque segment a son propre contrôle de direction.

```
Configuration avec gare intermédiaire :

   Segment 0                    Segment 1
┌────────────────────┐    ┌────────────────────┐
│  GareA ── AB ── BC │────│ CD ── DE ── GareD  │
└────────────────────┘    └────────────────────┘
                    GareC
                (croisement)
```

**Règle par segment :**
- Un train ne peut entrer sur un segment que si **aucun train ne circule en sens inverse SUR CE SEGMENT**
- Deux trains peuvent circuler en sens opposés sur des **segments différents** simultanément

**Exemple :**
- Train T1 (→) peut circuler sur le segment 0 (GareA → GareC)
- Train T2 (←) peut circuler sur le segment 1 (GareD → GareC) **en même temps**
- Ils se croisent dans GareC (gare intermédiaire)
- Pas de blocage car ils sont sur des segments différents !

### 3. Invariants de Sûreté

#### Invariant 1 : Section unique
> Une section ne peut contenir qu'**un seul train à la fois**

**Implémentation** : 
- `Section.canAccept()` retourne `true` seulement si `trainCount == 0`
- Utilisation de `synchronized` dans `Railway.move()`

#### Invariant 2 : Sens unique par segment
> Sur un segment donné, si un train circule dans une direction, aucun train ne peut circuler en sens inverse **sur ce même segment**

**Implémentation** :
- Maps `trainsPerSegmentLR` et `trainsPerSegmentRL` : compteurs par segment et direction
- Méthode `getSegmentIndex()` : identifie le segment entre deux gares
- Méthode `noOppositeTrainsOnSegment()` : vérifie le segment spécifique
- Vérification dans `canLeaveStation()` avant chaque départ

#### Invariant 3 : Limite de trains (gares intermédiaires) ⚠️ **NOUVEAU**
> Pour une gare intermédiaire avec **n places**, le nombre total de trains ne doit **pas dépasser n+1**

**Pourquoi ?**
Avec n places et n+2 trains :
- n trains peuvent occuper la gare intermédiaire
- 1 train attend de chaque côté
- **Interblocage** : personne ne peut avancer !

**Solution :**
Avec n+1 trains maximum, il reste toujours de la place pour qu'au moins un train puisse entrer et libérer l'espace.

**Exemple dans Main.java :**
```java
IntermediateStation C = new IntermediateStation("GareC", 2);  // 2 places
// On crée 3 trains (2+1) ✓
// PAS 4 trains (2+2) ✗ → risque d'interblocage
```

## Workflow de Déplacement d'un Train

```
┌─────────────────────────────────────────┐
│ 1. Train dans une GARE                  │
│    Veut aller vers une SECTION          │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│ 2. Vérifications (attente si nécessaire)│
│    - Section suivante libre ?           │
│    - Trains opposés SUR CE SEGMENT ?    │
│    - Gare destination a des places ?    │
└──────────────┬──────────────────────────┘
               │ OUI à toutes
               ▼
┌─────────────────────────────────────────┐
│ 3. Réservation                          │
│    - Réserver place à gare destination  │
│    - Quitter gare actuelle              │
│    - Entrer dans section                │
│    - Enregistrer train sur le segment   │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│ 4. Train sur SECTION                    │
│    Veut aller vers SECTION ou GARE      │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│ 5. Si vers SECTION :                    │
│    - Attendre que section soit libre    │
│    - Quitter section actuelle           │
│    - Entrer dans nouvelle section       │
│                                         │
│    Si vers GARE :                       │
│    - Quitter section                    │
│    - Libérer le segment                 │
│    - Consommer réservation              │
│    - Entrer dans gare                   │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│ 6. Changement de direction si besoin    │
│    - À l'extrémité droite : LR → RL     │
│    - À l'extrémité gauche : RL → LR     │
└──────────────┬──────────────────────────┘
               │
               ▼
         Recommencer
```

## Comment Compiler et Lancer le Projet

### Prérequis
- **Java JDK** 8 ou supérieur
- Terminal PowerShell (Windows) ou terminal Unix

### Compilation

**Windows (PowerShell) :**
```powershell
# Se placer dans le répertoire du projet
cd "\trains_project"

# Compiler tous les fichiers Java
javac -d bin src/train/*.java
```



### Exécution

**Windows (PowerShell) :**
```powershell
# Exécuter le programme
java -cp bin train.Main
```



### Utilisation avec VS Code

Si vous utilisez VS Code :
1. Ouvrir le dossier du projet
2. Ouvrir [src/train/Main.java](src/train/Main.java)
3. Cliquer sur "Run" au-dessus de la méthode `main`

## Configuration de la Simulation

### Modifier le Nombre de Trains

Éditer [src/train/Main.java](src/train/Main.java) :

```java
// Configuration actuelle : 3 trains (respecte l'invariant pour GareC avec 2 places)
Train t1 = new Train("T1", new Position(A, Direction.LR), railway);
Train t2 = new Train("T2", new Position(A, Direction.LR), railway);
Train t3 = new Train("T3", new Position(D, Direction.RL), railway);

// Pour ajouter un 4ème train (ATTENTION : risque d'interblocage avec GareC à 2 places)
// Train t4 = new Train("T4", new Position(D, Direction.RL), railway);
```

 **Attention** : Avec une gare intermédiaire de 2 places, ne dépassez pas 3 trains !

### Modifier la Configuration de la Ligne

```java
// Exemple actuel
Station A = new Station("GareA", 3);              // Gare terminale gauche, 3 places
IntermediateStation C = new IntermediateStation("GareC", 2);  // Gare intermédiaire, 2 places
Station D = new Station("GareD", 3);              // Gare terminale droite, 3 places

Section AB = new Section("AB");
Section BC = new Section("BC");
Section CD = new Section("CD");
Section DE = new Section("DE");

Element[] elements = { A, AB, BC, C, CD, DE, D };
```

**Pour ajouter une section :**
```java
Section EF = new Section("EF");
Element[] elements = { A, AB, BC, C, CD, DE, EF, D };
```

**Pour modifier la capacité d'une gare :**
```java
IntermediateStation C = new IntermediateStation("GareC", 4);  // 4 places au lieu de 2
// Alors on peut avoir jusqu'à 5 trains (4+1)
```

## Visualisation

Le programme affiche :
1. **Fenêtre graphique** : Visualisation de la ligne avec les trains
2. **Console** : Journal des événements (départs, arrivées, attentes, réservations)

### Exemple de Sortie Console

```
Ligne de chemin de fer: GareA--AB--BC--GareC--CD--DE--GareD
Configuration: 2 gares terminales (3 places) + 1 gare intermédiaire (2 places)
Invariant respecté: GareC a 2 places, donc max 3 trains (2+1)

Train T1 créé à GareA, direction → droite
Train T2 créé à GareA, direction → droite
Train T3 créé à GareD, direction ← gauche

Démarrage de la simulation avec 3 trains...
Les trains peuvent se croiser à la GareC (gare intermédiaire)

Train[T1] démarre
Train[T2] démarre
Train[T3] démarre
Train[T1] réserve une place à GareC (disponibles: 1/2)
Train[T3] réserve une place à GareC (disponibles: 0/2)
Train[T1] is on AB going from left to right
Train[T3] is on DE going from right to left
...
```

## Tests et Validation

### Test 1 : Respect de l'invariant (3 trains, gare de 2 places)
 **Résultat attendu** : Les trains circulent sans blocage

### Test 2 : Violation de l'invariant (4 trains, gare de 2 places)
 **Résultat attendu** : Risque d'interblocage

Pour tester :
```java
// Dans Main.java, décommenter le 4ème train
Train t4 = new Train("T4", new Position(A, Direction.LR), railway);
Thread thread4 = new Thread(t4);
thread4.start();
```

### Test 3 : Croisement dans gare intermédiaire
 **Résultat attendu** : Les trains venant de directions opposées peuvent se croiser dans la gare intermédiaire

## Auteurs et Références

- **Auteurs originaux** : Fabien Dagnat, Philippe Tanguy, Mayte Segarra (IMT Atlantique)
- **Modifications** : Extension avec gares intermédiaires et nouvel invariant de sûreté

## Licence

Projet à des fins éducatives - IMT Atlantique

---

**Dernière mise à jour** : Février 2026
