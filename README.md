# TiTravay

## Description

TiTravay est une plateforme web de services entre particuliers développée avec Spring Boot. L'application permet aux utilisateurs de proposer, découvrir et échanger des services locaux tout en facilitant la communication via un système de messagerie intégré.

**Problème résolu :** Mettre en relation des personnes cherchant ou proposant des services locaux (bricolage, jardinage, cours particuliers, etc.) avec un système de communication temps réel.

## Fonctionnalités principales

- **Gestion des services** : Création, consultation et gestion d'annonces de services
- **Système d'authentification** : Inscription et connexion sécurisées des utilisateurs
- **Messagerie temps réel** : Communication instantanée entre utilisateurs via WebSocket
- **Catégorisation** : Organisation des services par catégories (Bricolage, Jardinage, Informatique, etc.)
- **Géolocalisation** : Localisation des services pour une recherche pertinente
- **Interface responsive** : Design adapté aux différents appareils

## Technologies utilisées

### Backend
- **Java 21** : Langage de programmation principal
- **Spring Boot 3.4.5** : Framework principal
- **Spring Security** : Gestion de l'authentification et des autorisations
- **Spring Data JPA** : Accès aux données et ORM
- **Spring WebSocket** : Communication temps réel
- **PostgreSQL** : Base de données relationnelle

### Frontend
- **Thymeleaf** : Moteur de templates pour le rendu HTML
- **HTML5/CSS3** : Structure et style des pages
- **JavaScript** : Interactivité côté client

### Outils de développement
- **Maven** : Gestion des dépendances et build
- **Spring Boot DevTools** : Développement en hot-reload

## Installation

### Prérequis

- Java 21 ou supérieur
- Maven 3.6+
- PostgreSQL 12+
- Git

### Étapes d'installation

1. **Cloner le repository**
   ```bash
   git clone https://github.com/emjyahyong/TiTravay
   cd TiTravay
   ```

2. **Configurer la base de données**
   ```sql
   CREATE DATABASE TiTravay;
   CREATE USER UserTest WITH PASSWORD 'aaa';
   GRANT ALL PRIVILEGES ON DATABASE TiTravay TO UserTest;
   ```

3. **Configurer l'application**
   - Éditer le fichier `src/main/resources/application.properties`
   - Adapter les paramètres de connexion à votre base de données si nécessaire

4. **Compiler et lancer l'application**
   ```bash
   # Avec Maven Wrapper
   ./mvnw clean install
   ./mvnw spring-boot:run
   
   # Ou avec Maven installé localement
   mvn clean install
   mvn spring-boot:run
   ```

5. **Accéder à l'application**
   - URL par défaut : http://localhost:8080
   - L'application démarrera sur le port 8080

## Utilisation

### Cas d'usage typiques

1. **Création d'un compte**
   - Accéder à la page d'inscription
   - Remplir le formulaire avec nom d'utilisateur et mot de passe

2. **Publication d'un service**
   - Se connecter au système
   - Cliquer sur "Nouveau service"
   - Remplir les informations (titre, description, prix, localisation, catégorie)
   - Soumettre le formulaire

3. **Communication avec d'autres utilisateurs**
   - Consulter les services disponibles
   - Initier une conversation avec le fournisseur
   - Utiliser la messagerie temps réel pour échanger

### Endpoints principaux

- `GET /` : Page d'accueil
- `GET /login` : Page de connexion
- `GET /register` : Page d'inscription
- `GET /services/new` : Formulaire de création de service
- `POST /services/add` : Soumission d'un nouveau service
- `GET /services/{id}` : Détail d'un service
- WebSocket : `/topic/conversations/{conversationId}` pour la messagerie

## Structure du projet

```
TiTravay/
├── src/
│   ├── main/
│   │   ├── java/com/titravay/
│   │   │   ├── TiTravayApplication.java     # Point d'entrée principal
│   │   │   ├── config/                      # Configuration Spring
│   │   │   │   ├── SecurityConfig.java       # Configuration sécurité
│   │   │   │   └── WebSocketConfig.java      # Configuration WebSocket
│   │   │   ├── controller/                   # Contrôleurs MVC
│   │   │   │   ├── AuthController.java       # Authentification
│   │   │   │   ├── ServiceController.java    # Gestion des services
│   │   │   │   ├── ConversationController.java # Gestion conversations
│   │   │   │   └── ChatWebSocketController.java # Messagerie temps réel
│   │   │   ├── model/                        # Entités JPA
│   │   │   │   ├── User.java                 # Utilisateur
│   │   │   │   ├── Services.java             # Service
│   │   │   │   ├── Conversation.java         # Conversation
│   │   │   │   └── Message.java              # Message
│   │   │   ├── repository/                   # Repository Spring Data
│   │   │   ├── service/                      # Services métier
│   │   │   └── dto/                          # Objets de transfert
│   │   └── resources/
│   │       ├── application.properties         # Configuration application
│   │       ├── templates/                    # Templates Thymeleaf
│   │       │   ├── home.html                 # Page d'accueil
│   │       │   ├── login.html                # Connexion
│   │       │   ├── register.html             # Inscription
│   │       │   ├── service/                  # Pages services
│   │       │   └── conversation.html         # Messagerie
│   │       └── static/                       # Ressources statiques
│   └── test/                                 # Tests unitaires
├── pom.xml                                   # Configuration Maven
├── mvnw / mvnw.cmd                          # Maven Wrapper
└── README.md                                # Documentation
```

## Configuration

### Variables d'environnement

Les configurations principales se trouvent dans `application.properties` :

```properties
# Base de données
spring.datasource.url=jdbc:postgresql://localhost:5432/TiTravay
spring.datasource.username=[Votre Username]
spring.datasource.password=[Votre Password]

# Configuration JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.show_sql=true

# Serveur
server.port=8080
spring.application.name=TiTravay
```

### Personnalisation possible

- **Base de données** : Adapter les credentials PostgreSQL
- **Port serveur** : Modifier `server.port` si nécessaire
- **SSL** : Configurer HTTPS pour la production

## Tests

### Tests disponibles

- Tests unitaires Spring Boot dans `src/test/java/`
- Tests d'intégration Spring Security

### Lancer les tests

```bash
./mvnw test
```

## Améliorations futures

### Fonctionnalités prévues

- **Système de notation** : Évaluation des services et des utilisateurs
- **Recherche avancée** : Filtres par localisation, prix, disponibilité
- **Notifications** : Alertes par email pour nouveaux messages
- **Gestion des images** : Upload et gestion des photos de services
- **API REST** : Exposition d'une API pour applications mobiles
- **Système de paiement** : Intégration de solutions de paiement en ligne
- **Dashboard administrateur** : Interface de modération des contenus

### Améliorations techniques

- **Tests automatisés** : Augmentation de la couverture de tests
- **Dockerisation** : Conteneurisation pour le déploiement
- **Monitoring** : Intégration d'outils de supervision
- **Internationalisation** : Support multilingue

---

**Note** : Ce projet utilise Spring Boot et suit les meilleures pratiques de développement Java. La configuration actuelle est optimisée pour un environnement de développement et nécessite des ajustements pour un déploiement en production.
