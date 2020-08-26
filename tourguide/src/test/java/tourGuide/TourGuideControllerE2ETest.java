package tourGuide;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.javamoney.moneta.Money;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import tourGuide.jsonConfig.UserPreferenceSerialzer;
import tourGuide.model.user.UserPreferences;

import javax.money.Monetary;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class TourGuideControllerE2ETest {
    @Autowired
    private MockMvc mockMvc;

    @LocalServerPort
    private int port;

    @Test
    public void addPreferencesTest() throws Exception {
        String uri = "add-user-preferences?userName=internalUser1";
        UserPreferences userPreferences = new UserPreferences();
        userPreferences.setAttractionProximity(2147483647);
        userPreferences.setLowerPricePoint(Money.of(100, Monetary.getCurrency("USD")));
        userPreferences.setTripDuration(1);
        userPreferences.setTicketQuantity(1);
        userPreferences.setNumberOfAdults(1);
        userPreferences.setNumberOfChildren(1);
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module =
                new SimpleModule("UserPreferenceSerialzer", new Version(1, 0, 0, null, null, null));
        module.addSerializer(UserPreferences.class, new UserPreferenceSerialzer());
        mapper.registerModule(module);
        String requestJson=mapper.writeValueAsString(userPreferences);
        MvcResult result = mockMvc.perform(post(createURLWithPort(uri)).contentType(APPLICATION_JSON)
                .content(requestJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(content);
        assertNotNull(jsonObject);
        assertEquals(userPreferences.getAttractionProximity(), jsonObject.get("attractionProximity"));
    }

    @Test
    public void getPreferencesTest() throws Exception {
        String uri = "get-user-preferences?userName=internalUser1";
        mockMvc.perform(get(createURLWithPort(uri)).contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    public void getNearbyAttractionsTest() throws Exception {
        String uri = "getNearbyAttractions?userName=internalUser1";
        mockMvc.perform(get(createURLWithPort(uri)).contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    public void getRewardsTest() throws Exception {
        String uri = "getRewards?userName=internalUser1";
        mockMvc.perform(get(createURLWithPort(uri)).contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    public void getLocationTest() throws Exception {
        String uri = "getLocation?userName=internalUser1";
        mockMvc.perform(get(createURLWithPort(uri)).contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    public void getTripDealsTest() throws Exception {
        String uri = "getTripDeals?userName=internalUser1";
        mockMvc.perform(get(createURLWithPort(uri)).contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    public void getAllCurrentLocationsTest() throws Exception {
        String uri = "getAllCurrentLocations";
        mockMvc.perform(get(createURLWithPort(uri)).contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
}
