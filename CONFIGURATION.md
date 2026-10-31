# Configuration locale

L'application utilise les variables d'environnement suivantes :

- `DB_USERNAME` et `DB_PASSWORD` : identifiants PostgreSQL obligatoires.
- `JWT_SECRET` : clé HS512 obligatoire, d'au moins 64 octets (512 bits). Une clé
  Base64 doit décoder vers au moins 64 octets.
- `DB_URL` : URL PostgreSQL, facultative en local; valeur par défaut :
  `jdbc:postgresql://localhost:5432/sindik`.
- `CORS_ALLOWED_ORIGINS` : liste séparée par des virgules des origines frontend
  autorisées. La valeur locale par défaut est `http://localhost:4200`.

Aucune valeur par défaut n'est fournie pour les identifiants ou la clé JWT. Spring
échoue au démarrage si l'une de ces variables manque; `JWT_SECRET` doit aussi
respecter la longueur minimale HS512.

## Lancement local

1. Copier `.env.example` vers `.env` et renseigner les variables avec des valeurs
   locales privées. Le fichier `.env` est ignoré par Git.
2. Démarrer PostgreSQL avec `docker compose up -d postgres` depuis ce dossier.
3. Avant de lancer l'application depuis un IDE ou Maven, exporter les mêmes
   variables dans l'environnement du processus. PowerShell, par exemple :

   ```powershell
   $env:DB_USERNAME = 'valeur locale'
   $env:DB_PASSWORD = 'valeur locale'
   $env:JWT_SECRET = (openssl rand -base64 64)
   ./mvnw.cmd spring-boot:run
   ```

   Docker Compose charge automatiquement `.env`; Spring Boot lancé directement
   ne charge pas ce fichier. Configurez donc les variables dans la configuration
   d'exécution de l'IDE ou dans l'environnement du shell.

Pour un déploiement, fournir ces variables via le gestionnaire de secrets de
l'environnement. Ne pas réutiliser les identifiants ou la clé locale.

Le build Angular de production utilise une URL API relative. Le serveur web doit
servir le frontend et transférer les routes backend (`/auth`, `/users`,
`/residences`, `/buildings`, `/units`, `/residents`, `/contracts`, `/payments` et
`/syndic-buildings`) vers l'API, sur la même origine. Si le frontend et l'API sont
hébergés sur des domaines séparés, adapter `baseUrl` dans
`src/environements/environement.production.ts` et définir `CORS_ALLOWED_ORIGINS`
avec l'origine exacte du frontend.
