package com.channelize.sample;

class Config {

    //This will be the default API calling url. ("Subdomain" is present in Channelize.io Plugin)
    static final String API_DEFAULT_URL = "https://YOUR_SUBDOMAIN.primemessenger.com/api/";

    //This will be the mqtt client server url.  ("Subdomain" is present in Channelize.io Plugin)
    static final String MQTT_SERVER_URL = "wss://YOUR_SUBDOMAIN.primemessenger.com";

    // This will be the api key.  ("API key" is present in Channelize.io Plugin)
    static final String API_KEY = "CHANNELIZE_API_KEY";

    //This will be the private api key.  ("PRIVATE API key" is present in Channelize.io Plugin)
    static final String API_PRIVATE_KEY = "CHANNELIZE_API_KEY";

    // this will be project_number in google-services.json file.
    // Leave this field to empty if you don't want the push notifications.
    public static final String FIREBASE_SENDER_ID = "CHANNELIZE_FIREBASE_SENDER_ID";

    // This will be the mobilesdk_app_id in google-services.json file'
    // Leave this field to empty if you don't want the push notifications.
    public static final String FIREBASE_APPLICATION_ID = "CHANNELIZE_FIREBASE_APPLICATION_ID";

    // This will be the giphy api key for the sticker and gif module.
    // To use this feature you've to purchase the sticker-gif module
    // Leave this field to empty if you don't want the sticker/gif features.
    static final String GIPHY_API_KEY = "CHANNELIZE_GIPHY_API_KEY";

    // This will be the google places api key for the location module.
    // Leave this field to empty if you don't want the location features.
    static final String GOOGLE_PLACES_API_KEY = "CHANNELIZE_GOOGLE_PLACES_API_KEY";

    // This will be the agora app id for voice/video call.
    // This will only works when you've the video voice call module.
    // So add this line only when you've the video voice call module.
    static final String AGORA_APP_ID = "";

}
