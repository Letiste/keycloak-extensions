package org.rezoleo.userprofile.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ConfiguredProvider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.userprofile.UserProfileAttributeValidationContext;
import org.keycloak.validate.AbstractStringValidator;
import org.keycloak.validate.ValidationContext;
import org.keycloak.validate.ValidationError;
import org.keycloak.validate.ValidatorConfig;

public class RoomValidator extends AbstractStringValidator implements ConfiguredProvider {

    public static final String ID = "room";
    private static final Logger log = Logger.getLogger(RoomValidator.class);
    // TODO: Find a way to format the room (f123 should be authorized and formatted to F123)
    protected static final Pattern PATTERN = Pattern.compile("([A-F][0-3][0-9]{2}[a-b]?|DF[1-4])");

    public static final RoomValidator INSTANCE = new RoomValidator();

    public static final String MESSAGE_NO_MATCH = "room-invalid";

    public static final String CFG_ERROR_MESSAGE = "error-message";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(CFG_ERROR_MESSAGE);
        property.setLabel("Error message key");
        property.setHelpText("Key of the error message in i18n bundle. Dafault message key is " + MESSAGE_NO_MATCH);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    protected void doValidate(String value, String inputHint, ValidationContext context, ValidatorConfig config) {
        log.info("VALIDATING ROOM");
        if (!PATTERN.matcher(value).matches()) {
            context.addError(new ValidationError(ID, inputHint, "The room does not have the correct format. Examples of correct format are F123a or DF2"));
        }

        UserModel currentUser = ((UserProfileAttributeValidationContext) context).getAttributeContext().getUser();
        List<UserModel> usersInCurrentRoom = context.getSession().users().searchForUserByUserAttributeStream(context.getSession().getContext().getRealm(), "room", value).collect(Collectors.toList());

        boolean isRoomTaken;
        if (currentUser == null) {
            isRoomTaken = !usersInCurrentRoom.isEmpty();
        } else {
            isRoomTaken = !usersInCurrentRoom.isEmpty() && !currentUser.getId().equals(usersInCurrentRoom.get(0).getId());
        }
        if (isRoomTaken) {
            context.addError(new ValidationError(ID, inputHint, "this room already belongs to another user"));
        }
    }


    @Override
    public String getHelpText() {
        return "Ensure room is unique.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

}
