 package org.rezoleo.events.edit;

 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.jboss.logging.Logger;
 import org.keycloak.events.Event;
 import org.keycloak.events.EventListenerProvider;
 import org.keycloak.events.EventType;
 import org.keycloak.events.admin.AdminEvent;
 import org.keycloak.events.admin.OperationType;
 import org.keycloak.events.admin.ResourceType;

 import java.io.IOException;
 import java.net.URI;
 import java.net.http.HttpClient;
 import java.net.http.HttpRequest;
 import java.net.http.HttpResponse;
 import java.time.Duration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;

 public class EditEventListenerProvider implements EventListenerProvider {
     private static final Logger log = Logger.getLogger(EditEventListenerProvider.class);
     private static ObjectMapper mapper = new ObjectMapper();
     private static final int REQUEST_TIMEOUT = 5;
     private final HttpClient client;
     private final String editUserEventNotificationUrl;

     public EditEventListenerProvider(HttpClient client, String editUserEventNotificationUrl) {
         this.client = client;
         this.editUserEventNotificationUrl = editUserEventNotificationUrl;
     }

     @Override
     public void onEvent(Event event) {
         // TODO: filter specific client if update originated from him?
         // TODO: test with room
         if (isUserUpdate(event)) {
             log.info("User update on " + getUserId(event));
             System.out.println("DETAILS: " + event.getDetails().toString());
             String userJson = null;
             try {
                 userJson = userToJson(event);
             } catch (JsonProcessingException e) {
                 throw new RuntimeException(e);
             }
             HttpRequest request = HttpRequest.newBuilder()
                     .uri(URI.create(this.editUserEventNotificationUrl))
                     .method("PATCH", HttpRequest.BodyPublishers.ofString(userJson))
                     .header("Content-Type", "application/json")
                     .timeout(Duration.ofSeconds(REQUEST_TIMEOUT))
                     .build();
             try {
                 log.info("Send user update notifcation");
                 HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                 log.info("User update notification sent: " + response);
             } catch (IOException e) {
                 log.error(e.toString());
             } catch (InterruptedException e) {
                 log.error(e.toString());
             }
         }
     }

     @Override
     public void onEvent(AdminEvent adminEvent, boolean b) {
         if (isUserUpdate(adminEvent)) {
             log.info("User update on " + getUserId(adminEvent));
             String userJson = null;
             try {
                 userJson = userToJson(adminEvent);
             } catch (JsonProcessingException e) {
                 throw new RuntimeException(e);
             }
             HttpRequest request = HttpRequest.newBuilder()
                     .uri(URI.create(this.editUserEventNotificationUrl))
                     .method("PATCH", HttpRequest.BodyPublishers.ofString(userJson))
                     .header("Content-Type", "application/json")
                     .timeout(Duration.ofSeconds(REQUEST_TIMEOUT))
                     .build();
             try {
                 log.info("Send user update notifcation");
                 HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                 log.info("User update notification sent: " + response);
             } catch (IOException e) {
                 log.error(e.toString());
             } catch (InterruptedException e) {
                 log.error(e.toString());
             }
         }
     }

     @Override
     public void close() {

     }

     private boolean isUserUpdate(Event event) {
         return event.getType() == EventType.UPDATE_PROFILE;
     }

     private boolean isUserUpdate(AdminEvent adminEvent) {
         boolean isUpdate = adminEvent.getOperationType() == OperationType.UPDATE;
         boolean isUserResource = adminEvent.getResourceType() == ResourceType.USER;
         return isUpdate && isUserResource;
     }

     private String userToJson(Event event) throws JsonProcessingException {
         Map<String, String> changes = event.getDetails();
         Map userJson = new HashMap();
         userJson.put("sso_id", getUserId(event));
         addUpdatedValueToJson(changes, userJson, "updated_first_name", "firstname");
         addUpdatedValueToJson(changes, userJson, "updated_last_name", "lastname");
         addUpdatedValueToJson(changes, userJson, "updated_email", "email");
 //        if (json.has("attributes") && json.getJSONObject("attributes").has("room")) {
 //            userJson.put("room", json.getJSONObject("attributes").getJSONArray("room").getString(0));
 //        }
         log.debug(mapper.writeValueAsString(userJson));
         return mapper.writeValueAsString(userJson);
     }

     private void addUpdatedValueToJson(Map<String, String> changes, Map userJson, String updatedValue, String jsonKey) {
         if (changes.containsKey(updatedValue)) {
             userJson.put(jsonKey, changes.get(updatedValue));
         }
     }

     private String userToJson(AdminEvent adminEvent) throws JsonProcessingException {
         String user = adminEvent.getRepresentation();
         Map<String, Object> userJson = new HashMap<>();
         Map<String, Object> json = mapper.readValue(user, Map.class);
         userJson.put("sso_id", getUserId(adminEvent));
         addValueToJson(userJson, json, "firstName", "firstname");
         addValueToJson(userJson, json, "lastName", "lastname");
         addValueToJson(userJson, json, "email", "email");
         if (json.containsKey("attributes") && ((Map) json.get("attributes")).containsKey("room")) {
             userJson.put("room", ((List)((Map)json.get("attributes")).get("room")).get(0));
         }
         log.debug(mapper.writeValueAsString(userJson));
         return mapper.writeValueAsString(userJson);
     }

     private void addValueToJson(Map userJson, Map json, String updatedValue, String jsonKey) {
         if (json.containsKey(updatedValue)) {
             userJson.put(jsonKey, json.get(updatedValue));
         }
     }

     private String getUserId(Event event) {
         return event.getUserId();
     }

     private String getUserId(AdminEvent adminEvent) {
         String[] resourcePath = adminEvent.getResourcePath().split("/");
         return resourcePath[resourcePath.length - 1];
     }
 }
