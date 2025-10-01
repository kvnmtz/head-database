const axios = require('axios').default;
const FormData = require('form-data');
const fs = require("node:fs");

const MINECRAFT_VERSION = '1.20.1';

function escapeControlCharacters(str) {
    return str.replace(/[\0-\x1F\x7F]/g, (char) => {
        switch (char) {
            case '\n':
                return '\\n';
            case '\r':
                return '\\r';
            case '\t':
                return '\\t';
            case '\b':
                return '\\b';
            case '\f':
                return '\\f';
            case '\v':
                return '\\v';
            case '\0':
                return '\\0';
            default:
                return '\\x' + char.charCodeAt(0).toString(16).padStart(2, '0');
        }
    });
}

async function uploadToCurseForge(curseforgeToken, version, changelog, gameVersions, buildPath) {
    const formData = new FormData();
    formData.append('metadata', `{
      "changelog": "${changelog}",
      "changelogType": "markdown",
      "displayName": "Head Database ${MINECRAFT_VERSION}-${version}",
      "gameVersions": [${gameVersions.join(', ')}],
      "releaseType": "release",
      "relations": {
          "projects": [
              {
                  slug: "architectury-api",
                  projectID: 419699,
                  type: "requiredDependency"
              }
          ]
      }
    }`);
    formData.append('file', fs.createReadStream(buildPath));

    const headers = formData.getHeaders();
    headers['X-Api-Token'] = curseforgeToken;

    await axios.post('https://minecraft.curseforge.com/api/projects/1356590/upload-file', formData, {
        headers: headers,
    });
}

// noinspection JSUnusedGlobalSymbols
module.exports = {
    verifyConditions: async (pluginConfig, context) => {
        const {env} = context;
        if (!env.CURSEFORGE_PAT.length) {
            throw AggregateError('No CurseForge personal access token provided');
        }
    },
    success: async (pluginConfig, context) => {
        const {nextRelease} = context;
        const version = nextRelease.version;
        const changelog = escapeControlCharacters(nextRelease.notes);

        const {env} = context;
        const curseforgeToken = env.CURSEFORGE_PAT;

        // Forge Release
        await uploadToCurseForge(
            curseforgeToken,
            version,
            changelog,
            [9639, 7498, 10150, 9990],
            `./forge/build/libs/head_database-forge-${MINECRAFT_VERSION}-${version}.jar`
        );

        // Fabric Release
        await uploadToCurseForge(
            curseforgeToken,
            version,
            changelog,
            [9639, 7499, 9990],
            `./fabric/build/libs/head_database-fabric-${MINECRAFT_VERSION}-${version}.jar`
        );
    }
}