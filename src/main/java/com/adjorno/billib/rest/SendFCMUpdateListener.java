package com.adjorno.billib.rest;

import com.adjorno.billib.fcm.FcmClientSettings;
import com.m14n.billib.data.BB;
import com.m14n.ex.Ex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.bytefish.fcmjava.client.FcmClient;
import de.bytefish.fcmjava.client.settings.PropertiesBasedSettings;
import de.bytefish.fcmjava.http.options.IFcmClientSettings;
import de.bytefish.fcmjava.model.options.FcmMessageOptions;
import de.bytefish.fcmjava.model.topics.Topic;
import de.bytefish.fcmjava.requests.topic.TopicUnicastMessage;
import de.bytefish.fcmjava.responses.TopicMessageResponse;

@Component
public class SendFCMUpdateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendFCMUpdateListener.class);

    private FcmClient mFcmClient;

    @EventListener
    public void handleUpdateEvent(UpdateResult updateResult) {
        LOGGER.info("Update Event received");
        Date theUpdateWeekDate = updateResult.getUpdateWeek();
        if (theUpdateWeekDate != null) {
            String theUpdateWeek = BB.CHART_DATE_FORMAT.format(theUpdateWeekDate);
            FcmClient theFcmClient = getFcmClient();
            FcmMessageOptions options = FcmMessageOptions.builder().build();
            TopicMessageResponse response =
                    theFcmClient.send(new TopicUnicastMessage(options, new Topic("update"),
                            updateResult.getChartUpdates().stream().map(p -> p.getFirst())
                                    .collect(Collectors.toMap(Function.identity(), u -> theUpdateWeek))
                            //                        , NotificationPayload.builder().setBadge("Badge").setBody("Body").setTitle("Title")
                            //                                .setColor("RED").build()
                    ));
            LOGGER.info(response.toString());
        }
    }

    private FcmClient getFcmClient() {
        if (mFcmClient == null) {
            mFcmClient = new FcmClient(readFcmSettings());
        }
        return mFcmClient;
    }

    private IFcmClientSettings readFcmSettings() {
        IFcmClientSettings theFcmSettings = null;
        InputStream theFcmPropertiesStream = null;
        try {
            theFcmPropertiesStream = getClass().getResourceAsStream("/fcm.properties");
            if (theFcmPropertiesStream != null) {
                Properties theFcmProperties = new Properties();
                theFcmProperties.load(theFcmPropertiesStream);
                theFcmSettings = PropertiesBasedSettings.createFromProperties(theFcmProperties);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Ex.closeSilently(theFcmPropertiesStream);
        }
        if (theFcmSettings == null) {
            theFcmSettings = FcmClientSettings.createFromSysEnv();
        }
        return theFcmSettings;
    }
}
