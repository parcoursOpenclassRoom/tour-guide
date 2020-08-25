package tourGuide.jsonConfig;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import tourGuide.model.user.UserPreferences;

import java.io.IOException;
import java.lang.reflect.Field;
@JsonComponent
public class UserPreferenceSerialzer extends JsonSerializer<UserPreferences>  {
    @Override
    public void serialize(UserPreferences userPreferences, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException,
            JsonProcessingException {
        jsonGenerator.writeStartObject();
        Field[] fields = userPreferences.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if(field.getName() == "lowerPricePoint" || field.getName() == "highPricePoint" ){
                    jsonGenerator.writeObjectField(field.getName(), field.getName() == "lowerPricePoint" ? userPreferences.getLowerPricePoint().getNumber() : userPreferences.getHighPricePoint().getNumber());
                }else if(field.getName() == "currency"){
                   // jsonGenerator.writeObjectField(field.getName(), userPreferences.getCurrency().getCurrencyCode());
                }else{
                    jsonGenerator.writeObjectField(field.getName(), field.get(userPreferences));
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        jsonGenerator.writeEndObject();
    }
}
