package com.mgu.istio.oidclight;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgu.istio.oidclight.model.UserInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserConfigurationService implements InitializingBean {
    public static final String CONFIG_SIMUL_OPTION = "config.simul";

    private static final Logger LOGGER = LoggerFactory.getLogger(UserConfigurationService.class);

    private Map<String, UserInformation> userInformationMap = new HashMap<>();


    /**
     * Returns true if no user are defined
     *
     * @return
     */
    public boolean hasNoUser() {
        return userInformationMap.isEmpty();
    }

    /**
     * Get list of UserInformation
     *
     * @return list of UserInformation
     */
    public Collection<UserInformation> getUserInformationList() {
        return userInformationMap.values();
    }

    /**
     * Get User information
     *
     * @param userId user ID
     * @return null if user is not found
     */
    public UserInformation getUserInformation(String userId) {
        return userInformationMap.get(userId);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadConfiguration(new FileInputStream(new File("users.json")));
    }

    /**
     * Load configuration
     *
     * @param is
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    private void loadConfiguration(InputStream is) throws JsonParseException, JsonMappingException, IOException {
        userInformationMap.clear();
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.readValue(is, new TypeReference<List<UserInformation>>() {})
                .forEach(u -> userInformationMap.put(u.getUserId(), u));
        LOGGER.info("users: {}", userInformationMap);
    }
}
