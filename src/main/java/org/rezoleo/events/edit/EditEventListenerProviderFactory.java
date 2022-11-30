package org.rezoleo.events.edit;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import java.net.http.HttpClient;

public class EditEventListenerProviderFactory implements EventListenerProviderFactory {

    private String url;
    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {

        HttpClient client = HttpClient.newHttpClient();
        return new EditEventListenerProvider(client, this.url);
    }

    @Override
    public void init(Config.Scope scope) {
        this.url = System.getenv("USER_UPDATE_EVENT_NOTIFICATION_URL");
        if (this.url == null) {
            throw new Error("Environment variable USER_UPDATE_EVENT_NOTIFICATION_URL is not set");
        }
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "edit-user";
    }
}
