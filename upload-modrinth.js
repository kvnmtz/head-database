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

async function uploadToModrinth(modrinthToken, version, changelog, loaders, buildPath) {
    const formData = new FormData();
    formData.append('data', `{
      "name": "Head Database ${MINECRAFT_VERSION}-${version}",
      "version_number": "${MINECRAFT_VERSION}-${version}",
      "changelog": "${changelog}",
      "dependencies": [
        {
          "version_id": null,
          "project_id": "lhGA9TYQ",
          "file_name": null,
          "dependency_type": "required"
        }
      ],
      "game_versions": [
        "${MINECRAFT_VERSION}"
      ],
      "loaders": [
        ${loaders.map(loader => `"${loader}"`).join(', ')}
      ],
      "version_type": "release",
      "featured": true,
      "status": "listed",
      "project_id": "xIoagfOv",
      "file_parts": [
        "file"
      ]
    }`);
    formData.append('file', fs.createReadStream(buildPath));

    const headers = formData.getHeaders();
    headers.authorization = modrinthToken;

    await axios.post('https://api.modrinth.com/v2/version', formData, {
        headers: headers,
    });
}

// noinspection JSUnusedGlobalSymbols
module.exports = {
    verifyConditions: async (pluginConfig, context) => {
        const {env} = context;
        if (!env.MODRINTH_PAT.length) {
            throw AggregateError('No Modrinth personal access token provided');
        }
    },
    success: async (pluginConfig, context) => {
        const {nextRelease} = context;
        const version = nextRelease.version;
        const changelog = escapeControlCharacters(nextRelease.notes);

        const {env} = context;
        const modrinthToken = env.MODRINTH_PAT;

        // Forge Release
        await uploadToModrinth(
            modrinthToken,
            version,
            changelog,
            ["forge", "neoforge"],
            `./forge/build/libs/head_database-forge-${MINECRAFT_VERSION}-${version}.jar`
        );

        // Fabric Release
        await uploadToModrinth(
            modrinthToken,
            version,
            changelog,
            ["fabric"],
            `./fabric/build/libs/head_database-fabric-${MINECRAFT_VERSION}-${version}.jar`
        );
    }
}