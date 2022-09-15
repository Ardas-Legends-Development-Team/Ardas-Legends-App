const {MessageEmbed} = require("discord.js");
const {UPDATE_IGN} = require("../../../../configs/embed_thumbnails.json");
const {serverIP, serverPort} = require("../../../../configs/config.json");
const axios = require("axios");
const {isMemberStaff} = require("../../../../utils/utilities");

module.exports = {
    async execute(interaction) {
        if (!isMemberStaff(interaction)) {
            await interaction.reply({content: "You don't have permission to use this command.", ephemeral: false});
            return;
        }
        const ign = interaction.options.getString('ign');
        // send to server and edit reply
        const data = {
            discordId: interaction.options.getString('discord-id'),
            ign: ign
        }

        axios.patch('http://'+serverIP+':'+serverPort+'/api/player/update/ign', data)
            .then(async function (response) {
                // The request and data is successful.
                const replyEmbed = new MessageEmbed()
                    .setTitle(`Update IGN`)
                    .setColor('GREEN')
                    .setDescription(`You successfully updated your ign to ${ign}.`)
                    .setThumbnail(UPDATE_IGN)
                    .setTimestamp()
                await interaction.editReply({embeds: [replyEmbed], ephemeral: false});
            })
            .catch(async function (error) {
            const replyEmbed = new MessageEmbed()
                    .setTitle("Error while updating ign")
                    .setColor("RED")
                    .setDescription(error.response.data.message)
                    .setTimestamp()
                await interaction.editReply({embeds: [replyEmbed]})
            })
    },
};