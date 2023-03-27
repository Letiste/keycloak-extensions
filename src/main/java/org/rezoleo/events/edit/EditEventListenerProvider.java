 package org.rezoleo.events.edit;

 import org.jboss.logging.Logger;
 import org.json.JSONObject;
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
 import java.util.Map;

 public class EditEventListenerProvider implements EventListenerProvider {
     private static final Logger log = Logger.getLogger(EditEventListenerProvider.class);
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
//         if (isUserUpdate(event)) {
//             log.info("User update on " + getUserId(event));
//             System.out.println("DETAILS: " + event.getDetails().toString());
//             String userJson = userToJson(event);
//             HttpRequest request = HttpRequest.newBuilder()
//                     .uri(URI.create(this.editUserEventNotificationUrl))
//                     .method("PATCH", HttpRequest.BodyPublishers.ofString(userJson))
//                     .header("Content-Type", "application/json")
//                     .timeout(Duration.ofSeconds(REQUEST_TIMEOUT))
//                     .build();
//             try {
//                 log.info("Send user update notifcation");
//                 HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//                 log.info("User update notification sent: " + response);
//             } catch (IOException e) {
//                 log.error(e.toString());
//             } catch (InterruptedException e) {
//                 log.error(e.toString());
//             }
//         }
     }

     @Override
     public void onEvent(AdminEvent adminEvent, boolean b) {
//         if (isUserUpdate(adminEvent)) {
//             log.info("User update on " + getUserId(adminEvent));
//             String userJson = userToJson(adminEvent);
//             HttpRequest request = HttpRequest.newBuilder()
//                     .uri(URI.create(this.editUserEventNotificationUrl))
//                     .method("PATCH", HttpRequest.BodyPublishers.ofString(userJson))
//                     .header("Content-Type", "application/json")
//                     .timeout(Duration.ofSeconds(REQUEST_TIMEOUT))
//                     .build();
//             try {
//                 log.info("Send user update notifcation");
//                 HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//                 log.info("User update notification sent: " + response);
//             } catch (IOException e) {
//                 log.error(e.toString());
//             } catch (InterruptedException e) {
//                 log.error(e.toString());
//             }
//         }
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

     private String userToJson(Event event) {
         Map<String, String> changes = event.getDetails();
         JSONObject userJson = new JSONObject();
         userJson.put("sso_id", getUserId(event));
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
         userJson.put("sso_id", getUserId(adminEvent));
         addValueToJson(userJson, json, "firstName", "firstname");
         addValueToJson(userJson, json, "lastName", "lastname");
         addValueToJson(userJson, json, "email", "email");
         if (json.has("attributes") && json.getJSONObject("attributes").has("room")) {
             userJson.put("room", json.getJSONObject("attributes").getJSONArray("room").getString(0));
         }
         log.debug(userJson.toString());
         return userJson.toString();
     }

     private void addValueToJson(JSONObject userJson, JSONObject json, String updatedValue, String jsonKey) {
         if (json.has(updatedValue)) {
             userJson.put(jsonKey, json.getString(updatedValue));
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
