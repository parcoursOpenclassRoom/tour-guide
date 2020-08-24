package tourGuide.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.IntNode;
import org.javamoney.moneta.Money;
import org.springframework.boot.jackson.JsonComponent;
import tourGuide.user.UserPreferences;

import javax.money.Monetary;
import java.io.IOException;
import java.lang.reflect.Field;

@JsonComponent
class MoneyJsonCombinedSerializer {

    public static class MoneyJsonSeserializer
            extends JsonSerializer<UserPreferences> {

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
                        jsonGenerator.writeObjectField(field.getName(), userPreferences.getCurrency().getCurrencyCode());
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

    public static class MoneyJsonDeserializer
            extends JsonDeserializer<UserPreferences> {
        @Override
        public UserPreferences deserialize(JsonParser jsonParser,
                                DeserializationContext deserializationContext)
                throws IOException, JsonProcessingException {

            TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);
            UserPreferences userPreferences = new UserPreferences();
            Field[] fields = userPreferences.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                IntNode optionValue = (IntNode) treeNode.get(field.getName());
                try {
                    if(optionValue != null && optionValue.numberValue() != null){
                        if(field.getName() == "lowerPricePoint" || field.getName() == "highPricePoint" ){
                            field.set(userPreferences, convert(optionValue.intValue()));
                        }else{
                            field.set(userPreferences, optionValue.numberValue());
                        }
                    }

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return userPreferences;
        }

        private Money convert(int money){
            return Money.of(money, Monetary.getCurrency("USD"));
        }
    }
}