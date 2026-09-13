# Steal a Striker Index

Application Android Java pour suivre l’Index du jeu Roblox **Steal a Striker / Voler un attaquant**.

## Structure cible
- 70 personnages au total.
- 8 familles uniquement : Commun, Peu commun, Rare, Épique, Légendaire, Mythique, World Class, The World's Best.
- Une carte par personnage : image de profil au-dessus, nom en dessous, petite case à cocher en bas à droite.
- Les cases sont sauvegardées localement avec `SharedPreferences` et restent présentes après une mise à jour installée par-dessus.
- Recherche et filtre par famille.

## Images de profil
Aucune image de personnage n’est générée par le projet. Les images fournies manuellement doivent être ajoutées dans :

`app/src/main/res/drawable/`

avec le format :

`profile_<id_du_personnage>.png`

Exemples : `profile_isagi.png`, `profile_loki_godspeed.png`.

Si une image n’existe pas encore, l’application affiche `profile_placeholder`.

## Données de l’Index
`CharacterRepository.java` contient la liste et impose les 8 familles autorisées, l’absence de doublons et une limite de 70 personnages. Le total cible est `EXPECTED_TOTAL = 70`.

Les entrées actuellement codées sont uniquement celles dont la famille a pu être confirmée sans supposition à partir des sources accessibles. Les noms/rangements manquants doivent être ajoutés depuis l’Index réel du jeu plutôt qu’inventés.

## Mise à jour APK
- Vérification manuelle ou automatique via GitHub `releases/latest`.
- Téléchargement de la nouvelle APK puis lancement de l’installation Android.
- Même `applicationId` et même certificat de signature pour conserver les données lors des mises à jour.
- GitHub Actions compile l’APK Debug et Release, signe la Release, vérifie le certificat, publie l’Artifact et crée une Release GitHub.

## Compilation
```bash
./gradlew assembleDebug
```

Configuration : Java 17, minSdk 21, compileSdk 34, targetSdk 34.
