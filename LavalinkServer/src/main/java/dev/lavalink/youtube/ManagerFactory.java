package dev.lavalink.youtube;

import dev.lavalink.youtube.clients.*;
import dev.lavalink.youtube.clients.skeleton.Client;
import dev.lavalink.youtube.plugin.ClientProvider;
import lavalink.server.logging.ConsoleLogging;

import java.util.ArrayList;
import java.util.List;

import static dev.lavalink.youtube.plugin.ClientProvider.getClientByName;

public final class ManagerFactory {

    private static final String[] Clients = { "MUSIC", "WEB", "ANDROID", "TVHTML5EMBEDDED" };

    public static YoutubeAudioSourceManager Create() {
        return new YoutubeAudioSourceManager(true, true, true, getClients(Clients, new ClientProvider.OptionsProvider() {
            @Override
            public ClientOptions getOptionsForClient(String clientName) {
                return ClientOptions.DEFAULT;
            }
        }));
    }

    private enum ClientMapping implements ClientProvider.ClientReference {
        ANDROID(AndroidWithThumbnail::new),
        ANDROID_TESTSUITE(AndroidTestsuiteWithThumbnail::new),
        ANDROID_LITE(AndroidLiteWithThumbnail::new),
        IOS(IosWithThumbnail::new),
        MUSIC(MusicWithThumbnail::new),
        TVHTML5EMBEDDED(TvHtml5EmbeddedWithThumbnail::new),
        WEB(WebWithThumbnail::new),
        MEDIA_CONNECT(MediaConnectWithThumbnail::new);

        private final ClientWithOptions<Client> clientFactory;

        ClientMapping(ClientWithOptions<Client> clientFactory) {
            this.clientFactory = clientFactory;
        }

        @Override
        public String getName() {
            return name();
        }

        @Override
        public Client getClient(ClientOptions options) {
            return clientFactory.create(options);
        }
    }

    private static Client[] getClients(String[] clients, ClientProvider.OptionsProvider optionsProvider) {
        return getClients(ClientMapping.values(), clients, optionsProvider);
    }

    private static Client[] getClients(ClientProvider.ClientReference[] clientValues,
                                       String[] clients,
                                       ClientProvider.OptionsProvider optionsProvider) {
        List<Client> resolved = new ArrayList<>();

        for (String clientName : clients) {
            Client client = getClientByName(clientValues, clientName, optionsProvider);

            if (client == null) {
                ConsoleLogging.LogError("Failed to resolve {} into a Client " + clientName);
                continue;
            }

            resolved.add(client);
        }

        return resolved.toArray(new Client[0]);
    }
}
