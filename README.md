# Pascani
Pascani is a Domain Specific Language for specifying, compiling and deploying dynamic performance monitors to introspect (FraSCAti) SCA applications at runtime.

### Eclipse Update Site

http://unicesi.github.io/pascani/releases

Pascani requires [Amelia](https://github.com/unicesi/amelia), so please make sure you add the Amelia update site (http://unicesi.github.io/amelia/releases) before you install Pascani features.

### Clone

Before cloning this repository, please notice two things: first, this repository does not contain generated sources, and second, the Eclipse update site is hosted in the `gh-pages` branch. That being said, my advice is to clone each branch separately; this avoids compiling the sources everytime you checkout the `gh-pages` branch. Additionally, this makes cloning the `master` branch lighter.

To clone the `master` branch:
```bash
git clone -b "master" --single-branch https://github.com/unicesi/pascani
```
To clone the `gh-pages` branch:
```bash
git clone -b "gh-pages" --single-branch https://github.com/unicesi/pascani p2-repository
```

### Compiling From Sources

If you want to build the Pascani sources locally, you need Maven.

First of all, make sure to increase memory

```bash
export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m"
```

And then run

```bash
mvn install -file maven/org.pascani.tycho.parent/pom.xml
```
