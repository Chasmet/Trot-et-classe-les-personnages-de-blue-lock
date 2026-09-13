# Blue Lock Tier List

Application Android Java pour trier et classer les styles/personnages de **Blue Lock Rivals**.

## Fonctionnalités
- 47 styles/personnages actifs, limités ou archivés.
- Rangs personnels S+, S, A, B, C, D et NC.
- Recherche et filtre par rareté.
- Sauvegarde locale des rangs via SharedPreferences.
- Menu Réglages avec vérification manuelle/automatique des mises à jour.
- Mise à jour par GitHub `releases/latest`, téléchargement de l'APK et installation par-dessus sans effacer les rangs.
- GitHub Actions compile, signe toujours avec la même clé de test Android et publie une Release à chaque push sur `main`.

## Compilation
```bash
./gradlew assembleDebug
```

Configuration : Java 17, minSdk 21, compileSdk 34, targetSdk 34.

> Le classement initial est indicatif et sert de point de départ. Il est entièrement modifiable dans l'application.
