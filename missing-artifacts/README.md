
# Repository for Missing Dependencies
This is a local maven repository for missing dependencies. Some of the artifacts deployed here are transitive dependencies whose repository has been permanently moved or deleted.

### Add missing artifacts
In order to add artifacts to this repository, you must have both the jar file and its corresponding metadata. To add new artifacts, simply execute:

```bash
mvn deploy:deploy-file \
    -Durl=file:///<path-to>/missing-artifacts \
    -Dfile=<path-to-jar-file> \
    -DgroupId=<groupId> \
    -DartifactId=<artifactId> \
    -Dpackaging=<packaging> \
    -Dversion=<version>
```
