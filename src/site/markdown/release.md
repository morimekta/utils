Releasing Morimekta Utilities
=============================

The stages done to make a release.

### Making a SNAPSHOT release

Snapshot releases can be made directly from the `master` branch.

* Run `# mvn clean install` from master at the desired commit.

Snapshot releases are not supposed to be deployed. Therefore the
`<snapshotRepository>` is **NOT** set in the master pom. Note that
it is only commented out, in case a release is really needed.

### Making a version release

Proper releases are done with a branch cut.

```bash
# Check for dependency and plugin updates:
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates

# Make the maven release:
mvn release:prepare
mvn release:perform
git fetch origin
```

```bash
# Make site release:
git checkout HEAD~1 -b release
# make the versions env variable:
export utils_version=$(cat pom.xml | grep '^    <version>' | sed 's: *[<][/]\?version[>]::g')

mvn clean verify site site:stage -Plib
git checkout gh-pages && git pull -p && cp -R target/staging/* .
git commit -a -m "Site release for ${utils_version}"
git push
```

* Check sonatype staging repository found at the
  [Nexus Repository Manager](https://oss.sonatype.org/#stagingRepositories). And if
  correct, do the release. If not a git hard rollback is needed (to remove release
  version, tag and commits).

Now the release is complete.
