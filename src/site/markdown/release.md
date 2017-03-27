Releasing Morimekta Utilities
=============================

The stages done to make a release.

### Making a SNAPSHOT release

Snapshot releases can be made directly from the `master` branch.

* Run `# mvn clean deploy` from master at the desired commit.

### Making a version release

Proper releases are done with a branch cut.

#### Making the release cut.

* Run `# mvn clean verify install site` to build and verify the snapshot build
  you want to release.
* Run `# mvn release:prepare`, which will create two new commits, one with the
  actual release, and one with the "next development cycle".
* Run `# mvn release:perform` to generate the artifacts and push to sonatype
  for staging.
* Run `# git checkout HEAD~1 -b release`.
  This will check out the actual release commit.
* Run `# mvn clean verify site site:stage`, which will build the website for the
  release.
* Run `# git checkout gh-pages && cp -R target/staging/* .`, which will
  prepare the page site for the release.
* Run `# git commit -a -m "Site release for ..."` to commit.

* Check sonatype staging repository found at the
  [Nexus Repository Manager](https://oss.sonatype.org/#stagingRepositories). And if
  correct, do the release. If not a git hard rollback is needed (to remove release
  version, tag and commits).

Now the release is complete.
