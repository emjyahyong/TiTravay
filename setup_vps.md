# Procédure DevOps complète — TiTravay

> Guide d'exécution pas-à-pas des 15 tâches du roadmap, pour un VPS Ubuntu (22.04/24.04), avec une app **Java 21 / Spring Boot 3.4.5 / PostgreSQL**.
> Convention : `$USER` = ton utilisateur non-root, `example.com` = ton nom de domaine, `titravay` = nom du projet/conteneur.

---

## Phase 1 — Socle VPS

### 1. Hostname, timezone, NTP

```bash
# Connexion initiale en root
ssh root@VPS_IP

# Hostname
hostnamectl set-hostname titravay-vps
echo "127.0.0.1 titravay-vps" >> /etc/hosts

# Timezone (Réunion = Indian/Reunion, sinon Europe/Paris selon l'hébergeur)
timedatectl set-timezone Europe/Paris

# Vérifier la synchro NTP
timedatectl status
# doit afficher : "NTP service: active" / "System clock synchronized: yes"

# Si absent, installer et activer chrony
apt install -y chrony
systemctl enable --now chrony
```

### 2. Mise à jour système

```bash
apt update && apt upgrade -y
apt install -y curl wget git ufw vim htop net-tools unzip
apt autoremove -y
```

### 3. Créer un utilisateur non-root

```bash
adduser emjy
usermod -aG sudo emjy

# Copier la config SSH du root vers le nouvel utilisateur (si clé déjà en place)
rsync --archive --chown=emjy:emjy ~/.ssh /home/emjy

# Tester la connexion dans un NOUVEAU terminal AVANT de fermer la session root
ssh emjy@VPS_IP
sudo whoami   # doit renvoyer "root"
```

⚠️ Ne jamais fermer la session root tant que la connexion `emjy` + `sudo` n'est pas validée.

### 4. Installer Docker + Docker Compose

```bash
# Dépôt officiel Docker
sudo apt install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Ajouter l'utilisateur au groupe docker (éviter sudo à chaque commande)
sudo usermod -aG docker emjy
newgrp docker

# Vérification
docker run hello-world
docker compose version
```

### 5. Installer Nginx (reverse proxy)

```bash
sudo apt install -y nginx
sudo systemctl enable --now nginx

sudo tee /etc/nginx/sites-available/titravay <<'EOF'
server {
    listen 80;
    server_name 164.132.103.89;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # nécessaire pour le WebSocket/SockJS de TiTravay
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
EOF

sudo ln -s /etc/nginx/sites-available/titravay /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx
```

✅ **Checkpoint Phase 1** : `ssh emjy@VPS_IP`, `docker run hello-world` OK, `curl localhost` répond via Nginx.

---

## Phase 2 — Sécurité VPS

> Ordre impératif : **clés SSH → UFW → fail2ban → Certbot**

### 6. Clés SSH (avant toute chose)

```bash
# Depuis ta machine locale, générer une paire si besoin
ssh-keygen -t ed25519 -C "emjy@titravay"
ssh-copy-id emjy@VPS_IP

# Vérifier que la connexion par clé fonctionne AVANT de couper le mot de passe
ssh emjy@VPS_IP
```

Sur le VPS :

```bash
sudo vim /etc/ssh/sshd_config
```

Modifier :
```
PermitRootLogin no
PasswordAuthentication no
PubkeyAuthentication yes
```

```bash
sudo systemctl restart sshd
# Tester dans un NOUVEAU terminal avant de fermer la session actuelle
ssh emjy@VPS_IP
```

### 7. UFW — autoriser les ports AVANT d'activer

```bash
sudo ufw allow OpenSSH        # ou : sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

sudo ufw enable                # confirmer "y"
sudo ufw status verbose
```

⚠️ Ne jamais faire `ufw enable` avant d'avoir autorisé le port SSH — risque de se couper l'accès au VPS.

### 8. fail2ban

```bash
sudo apt install -y fail2ban

sudo tee /etc/fail2ban/jail.local <<'EOF'
[sshd]
enabled = true
port = ssh
filter = sshd
logpath = /var/log/auth.log
maxretry = 5
bantime = 3600
findtime = 600
ignoreip = 127.0.0.1/8 TON_IP_PERSONNELLE
EOF

sudo systemctl enable --now fail2ban
sudo fail2ban-client status sshd
```

### 9. Certbot / Let's Encrypt

```bash
sudo apt install -y certbot python3-certbot-nginx

sudo certbot --nginx -d example.com -d www.example.com
# Certbot modifie automatiquement le bloc Nginx et ajoute la redirection HTTP -> HTTPS

# Vérifier le renouvellement automatique
sudo systemctl status certbot.timer
sudo certbot renew --dry-run
```

✅ **Checkpoint Phase 2** : mot de passe SSH désactivé, `ufw status` = actif avec 22/80/443, `fail2ban-client status sshd` actif, site accessible en HTTPS.

---

## Phase 3 — Conteneurisation

### 10. Dockerfile multi-stage (TiTravay — Spring Boot / Maven)

```dockerfile
# ---- Stage 1 : build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Stage 2 : runtime ----
FROM eclipse-temurin:21-jre-jammy
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build local pour tester :
```bash
docker build -t titravay:latest .
docker run --rm -p 8080:8080 titravay:latest
```

### 11. `docker-compose.yml` (production)

```yaml
version: "3.9"

services:
  app:
    image: ghcr.io/emjy/titravay:latest
    restart: always
    depends_on:
      db:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/titravay
      SPRING_DATASOURCE_USERNAME: titravay_user
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    ports:
      - "8080:8080"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 5s
      retries: 3
    networks:
      - titravay-net

  db:
    image: postgres:16-alpine
    restart: always
    environment:
      POSTGRES_DB: titravay
      POSTGRES_USER: titravay_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - pg-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U titravay_user"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - titravay-net

volumes:
  pg-data:

networks:
  titravay-net:
```

Sur le VPS :
```bash
mkdir -p ~/titravay && cd ~/titravay
# déposer docker-compose.yml et un fichier .env (DB_PASSWORD=..., JWT_SECRET=...)
echo ".env" >> .gitignore
```

### 12. Secrets GitHub

Sur GitHub : `Repo → Settings → Secrets and variables → Actions → New repository secret`

| Nom | Valeur |
|---|---|
| `SSH_HOST` | IP du VPS |
| `SSH_USER` | `emjy` |
| `SSH_KEY` | Clé privée dédiée au déploiement (générée séparément, jamais ta clé perso) |
| `DB_PASSWORD` | Mot de passe PostgreSQL |
| `JWT_SECRET` | Secret JWT de l'app |
| `GHCR_TOKEN` | Personal Access Token avec scope `write:packages` |

```bash
# Générer une clé dédiée au déploiement CI/CD
ssh-keygen -t ed25519 -f ~/.ssh/deploy_titravay -C "github-actions-deploy" -N ""
ssh-copy-id -i ~/.ssh/deploy_titravay.pub emjy@VPS_IP
cat ~/.ssh/deploy_titravay   # copier tout, coller dans le secret SSH_KEY
```

✅ **Checkpoint Phase 3** : `docker compose up -d` fonctionne localement sur le VPS, 6 secrets créés sur GitHub.

---

## Phase 4 — CI/CD Pipeline

### 13. Workflow GitHub Actions

Fichier `.github/workflows/ci-cd.yml` :

```yaml
name: CI/CD TiTravay

on:
  push:
    branches: [main]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: emjy/titravay

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Run tests
        run: mvn -B test

      - name: Publish test report
        if: always()
        uses: dorny/test-reporter@v1
        with:
          name: JUnit Tests
          path: target/surefire-reports/*.xml
          reporter: java-junit

  build-and-push:
    needs: build-and-test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Log in to GHCR
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GHCR_TOKEN }}

      - name: Build and push image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:latest

  deploy:
    needs: build-and-push
    runs-on: ubuntu-latest
    steps:
      - name: Deploy over SSH
        uses: appleboy/ssh-action@v1.0.3
        with:
          host: ${{ secrets.SSH_HOST }}
          username: ${{ secrets.SSH_USER }}
          key: ${{ secrets.SSH_KEY }}
          script: |
            cd ~/titravay
            docker compose pull
            docker compose up -d
            docker image prune -f
```

✅ **Checkpoint Phase 4** : un `git push` sur `main` déclenche bien les 3 jobs dans l'onglet Actions de GitHub.

---

## Phase 5 — Validation

### 14. Test du pipeline end-to-end

```bash
git checkout -b feature/test-pipeline
echo "// test" >> src/main/java/.../SomeFile.java
git add . && git commit -m "test: valider le pipeline CI/CD"
git push origin feature/test-pipeline

# Ouvrir une PR vers main (ou push direct sur main selon config), puis vérifier :
# - Job "build-and-test" : vert, rapport JUnit visible
# - Job "build-and-push" : image visible sur ghcr.io/emjy/titravay
# - Job "deploy" : logs SSH OK, app accessible sur https://example.com
```

Vérification manuelle post-déploiement :
```bash
curl -I https://example.com
docker compose logs -f app
```

### 15. Test de redémarrage VPS

```bash
sudo reboot

# Après reconnexion (attendre 1-2 min)
ssh emjy@VPS_IP

# Vérifier que tout redémarre automatiquement
systemctl status docker
systemctl status nginx
systemctl status fail2ban
docker compose -f ~/titravay/docker-compose.yml ps
curl -I https://example.com
```

Si l'app ne redémarre pas seule, vérifier que Docker est bien activé au boot :
```bash
sudo systemctl enable docker
```
et que `restart: always` est bien présent dans `docker-compose.yml` pour chaque service.

✅ **Checkpoint Phase 5** : pipeline complet vert de bout en bout, VPS résilient à un redémarrage.

---

## Annexes utiles pour le dossier CDA (CP11)

Pour ton dossier, pense à capturer comme preuves :
- Capture d'écran du run GitHub Actions avec les 3 jobs verts
- Capture `ufw status verbose` et `fail2ban-client status sshd`
- Extrait du `Dockerfile` multi-stage commenté
- Extrait du `ci-cd.yml` avec explication du choix des 3 jobs
- Capture du certificat Let's Encrypt actif (`certbot certificates`)
- Schéma d'architecture : Internet → Nginx (443) → App (8080) → PostgreSQL

*Procédure DevOps — Projet TiTravay — Certification CDA TP-01281*