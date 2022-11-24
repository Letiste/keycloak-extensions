package org.rezoleo.events.edit;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import twitter4j.JSONObject;

import java.util.Map;

public class EditEventListenerProvider implements EventListenerProvider {
    private static final Logger log = Logger.getLogger(EditEventListenerProvider.class);

    @Override
    public void onEvent(Event event) {
        // TODO: filter specific client if update originated from him?
        if (isUserUpdate(event)) {
            log.info("User update on " + getUserId(event));
            System.out.println("DETAILS: " + event.getDetails().toString());
            userToJson(event);
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
        if (isUserUpdate(adminEvent)) {
            log.info("User update on " + getUserId(adminEvent));
            userToJson(adminEvent);
        }
    }

    @Override
    public void close() {

    }

    private boolean isUserUpdate(Event event) {
        boolean isUpdate = event.getType() == EventType.UPDATE_PROFILE;
        return isUpdate;
    }

    private boolean isUserUpdate(AdminEvent adminEvent) {
        boolean isUpdate = adminEvent.getOperationType() == OperationType.UPDATE;
        boolean isUserResource = adminEvent.getResourceType() == ResourceType.USER;
        return isUpdate && isUserResource;
    }

    private String userToJson(Event event) {
        Map<String, String> changes = event.getDetails();
        JSONObject userJson = new JSONObject();
        addUpdatedValueToJson(changes, userJson, "updated_first_name", "firstname");
        addUpdatedValueToJson(changes, userJson, "updated_last_name", "lastname");
        addUpdatedValueToJson(changes, userJson, "updated_email", "email");
//        if (json.has("attributes") && json.getJSONObject("attributes").has("room")) {
//            userJson.put("room", json.getJSONObject("attributes").getJSONArray("room").getString(0));
//        }
        log.debug(userJson.toString());
        return userJson.toString();
    }

    private void addUpdatedValueToJson(Map<String, String> changes, JSONObject userJson, String updatedValue, String jsonKey) {
        if (changes.containsKey(updatedValue)) {
            userJson.put(jsonKey, changes.get(updatedValue));
        }
    }

    private String userToJson(AdminEvent adminEvent) {
        String user = adminEvent.getRepresentation();
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

    private String getUserId(Event event) {
        return event.getUserId();
    }

    private String getUserId(AdminEvent adminEvent) {
        String[] resourcePath = adminEvent.getResourcePath().split("/");
        return resourcePath[resourcePath.length - 1];
    }
}
