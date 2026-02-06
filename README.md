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
│       ├── Station.java                        # Gare
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
├── Station                    # Gare (aux extrémités)
└── Section                    # Section de voie ferrée

Train (Runnable)              # Train autonome (thread)
Position                       # Position = Élément + Direction
Direction (enum)               # LR (Left-Right) ou RL (Right-Left)
Railway                        # Gestionnaire de la ligne ferroviaire
```

## Concepts Clés

### 1. Éléments de la Ligne

#### Station (Gare)
- **Capacité** : n quais (peut accueillir n trains simultanément)
- **Position** : Aux extrémités de la ligne (début ou fin)
- **Fonction** : Point de départ/arrivée des trains
- **Règle** : Les trains changent de direction automatiquement en arrivant

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
Configuration :

   GareA ── AB ── BC ── CD ── GareD
```

**Règle :**
- Un train ne peut entrer sur la ligne que si **aucun train ne circule en sens inverse**
- Les trains dans le même sens peuvent se suivre (mais pas se doubler)

### 3. Invariants de Sûreté

#### Invariant 1 : Section unique
> Une section ne peut contenir qu'**un seul train à la fois**

**Implémentation** : 
- `Section.canAccept()` retourne `true` seulement si `trainCount == 0`
- Utilisation de `synchronized` dans `Railway.move()`

#### Invariant 2 : Sens unique sur la ligne
> Si un train circule dans une direction, aucun train ne peut circuler en sens inverse sur les sections

**Implémentation** :
- Compteurs `trainsOnSectionsLR` et `trainsOnSectionsRL` : nombre de trains par direction
- Méthode `canLeaveStation()` : vérifie qu'aucun train n'est en sens inverse
- Vérification avant chaque départ de gare

#### Invariant 3 : Réservation de place
> Un train doit réserver une place à la gare de destination **avant** de partir

**Pourquoi ?**
Sans réservation, plusieurs trains pourraient partir vers une gare qui n'a pas assez de places.

**Solution :**
Le train réserve une place dès qu'il décide de partir. La réservation est convertie en occupation réelle à l'arrivée.

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
// Configuration actuelle : 4 trains
Train t1 = new Train("T1", new Position(A, Direction.LR), railway);
Train t2 = new Train("T2", new Position(A, Direction.LR), railway);
Train t3 = new Train("T3", new Position(D, Direction.RL), railway);
Train t4 = new Train("T4", new Position(D, Direction.RL), railway);
```

### Modifier la Configuration de la Ligne

```java
// Exemple actuel
Station A = new Station("GareA", 3);              // Gare gauche, 3 places
Station D = new Station("GareD", 3);              // Gare droite, 3 places

Section AB = new Section("AB");
Section BC = new Section("BC");
Section CD = new Section("CD");

Element[] elements = { A, AB, BC, CD, D };
```

**Pour modifier la capacité d'une gare :**
```java
Station A = new Station("GareA", 5);  // 5 places au lieu de 3
```

## Visualisation

Le programme affiche :
1. **Fenêtre graphique** : Visualisation de la ligne avec les trains
2. **Console** : Journal des événements (départs, arrivées, attentes, réservations)

### Exemple de Sortie Console

```
Ligne de chemin de fer: GareA--AB--BC--CD--GareD
Configuration: 2 gares terminales (3 places chacune)

Train T1 créé à GareA, direction → droite
Train T2 créé à GareA, direction → droite
Train T3 créé à GareD, direction ← gauche
Train T4 créé à GareD, direction ← gauche

Démarrage de la simulation avec 4 trains...

Train[T1] démarre
Train[T2] démarre
Train[T3] démarre
Train[T1] réserve une place à GareD (disponibles: 2/3)
Train[T3] réserve une place à GareA (disponibles: 2/3)
Train[T1] is on AB going from left to right
Train[T3] is on CD going from right to left
...
```

## Tests et Validation

### Test 1 : Prévention de l'interblocage
 **Résultat attendu** : Les trains circulent sans blocage grâce à la règle de sens unique

### Test 2 : Système de réservation
 **Résultat attendu** : Les trains réservent leur place avant de partir, pas de surcharge des gares

### Test 3 : Visualisation
 **Résultat attendu** : L'interface graphique affiche les trains et leurs mouvements en temps réel

## Auteurs et Références

- **Auteurs originaux** : Fabien Dagnat, Philippe Tanguy, Mayte Segarra (IMT Atlantique)
- **Modifications** : Extension avec système de réservation et visualisation graphique

## Licence

Projet à des fins éducatives - IMT Atlantique

---

**Dernière mise à jour** : Février 2026
