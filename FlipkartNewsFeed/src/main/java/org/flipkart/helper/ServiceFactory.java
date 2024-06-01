package org.flipkart.helper;


import org.flipkart.service.NewsFeed;
import org.flipkart.service.PlatformService;

public class ServiceFactory {

    private static PlatformService platformService;

    private static NewsFeed newsFeed;


    public static PlatformService getPlatformService() {
        if(platformService == null)
            return new PlatformService();
        return platformService;
    }

    public static NewsFeed getNewsFeed() {
        if(newsFeed == null)
            return new NewsFeed();
        return newsFeed;
    }
}
