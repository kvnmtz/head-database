/**
 * @type {import('semantic-release').GlobalConfig}
 */
module.exports = {
  branches: ['1.20.1'],
  plugins: [
    '@semantic-release/commit-analyzer',
    '@semantic-release/release-notes-generator',
    './update-version.js',
    [
      '@semantic-release/git',
      {
        assets: ['gradle.properties'],
        message: 'chore(release): update version for ${nextRelease.version} [skip ci]',
      },
    ],
    [
      '@semantic-release/exec',
      {
        prepareCmd: './gradlew build',
      },
    ],
    [
      '@semantic-release/github',
      {
        'assets': [
          'forge/build/libs/head_database-forge-!(*-dev-shadow|*-sources).jar',
          'fabric/build/libs/head_database-fabric-!(*-dev-shadow|*-sources).jar',
        ],
      },
    ],
    './upload-modrinth.js',
    './upload-curseforge.js',
  ],
};
