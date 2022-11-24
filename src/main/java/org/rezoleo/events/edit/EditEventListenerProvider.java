package org.rezoleo.events.edit;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import twitter4j.JSONObject;

public class EditEventListenerProvider implements EventListenerProvider {
    private static final Logger log = Logger.getLogger(EditEventListenerProvider.class);

    @Override
    public void onEvent(Event event) {
        // TODO: handle only update event
        // TODO: filter specific client if update originated from him?
        System.out.println("EVENT OCCURED: " + event.getType().toString());
        System.out.println("DETAILS: " + event.getDetails().toString());
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
        if (isUserUpdate(adminEvent)) {
            log.info("User update on " + getUserId(adminEvent));
            userToJson(adminEvent.getRepresentation());
        }
    }

    @Override
    public void close() {

    }

    private boolean isUserUpdate(AdminEvent adminEvent) {
        boolean isUpdate = adminEvent.getOperationType() == OperationType.UPDATE;
        boolean isUserResource = adminEvent.getResourceType() == ResourceType.USER;
        return isUpdate && isUserResource;
    }

    private String userToJson(String user) {
        JSONObject userJson = new JSONObject();
        JSONObject json = new JSONObject(user);
        userJson.put("firstname", json.getString("firstName"));
        userJson.put("lastname", json.getString("lastName"));
        userJson.put("email", json.getString("email"));
        if (json.has("attributes") && json.getJSONObject("attributes").has("room")) {
            userJson.put("room", json.getJSONObject("attributes").getJSONArray("room").getString(0));
        }
        log.debug(userJson.toString());
        return userJson.toString();
    }

    private String getUserId(AdminEvent adminEvent) {
        String[] resourcePath = adminEvent.getResourcePath().split("/");
        return resourcePath[resourcePath.length - 1];
    }
}
