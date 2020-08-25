package tourGuide.jsonConfig;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;
import org.javamoney.moneta.Money;
import org.springframework.boot.jackson.JsonComponent;
import tourGuide.model.user.UserPreferences;

import javax.money.Monetary;
import java.io.IOException;
import java.lang.reflect.Field;
@JsonComponent
public class UserPreferenceDeserialzer  extends JsonDeserializer<UserPreferences> {
    @Override
    public UserPreferences deserialize(JsonParser jsonParser,
                                       DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);
        UserPreferences userPreferences = new UserPreferences();
        Field[] fields = userPreferences.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName() != "$jacocoData") {
                IntNode optionValue = (IntNode) treeNode.get(field.getName());
                try {
                    if (optionValue != null) {
                        int val = optionValue.intValue();
                        if (field.getName() == "lowerPricePoint" || field.getName() == "highPricePoint") {
                            field.set(userPreferences, convert(val));
                        } else {
                            field.set(userPreferences, val);
                        }
                    }

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return userPreferences;
    }

    private Money convert(int money){
        return Money.of(money, Monetary.getCurrency("USD"));
    }
}
