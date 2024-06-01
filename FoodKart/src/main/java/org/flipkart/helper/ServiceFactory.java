package org.flipkart.helper;


import org.flipkart.service.PlatformService;
import org.flipkart.service.UserService;

public class ServiceFactory {

    private static PlatformService platformService;

    private static UserService userService;


    public static PlatformService getPlatformService() {
        if(platformService == null)
            return new PlatformService();
        return platformService;
    }
    public static UserService getUserService() {
        if(userService == null)
            return new UserService();
        return userService;
    }

}
