/*******************************************************************************
 * Copyright (c) 2015 VMware, Inc.  All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, without warranties or
 * conditions of any kind, EITHER EXPRESS OR IMPLIED.  See the License for the
 * specific language governing permissions and limitations under the License.
 ******************************************************************************/
package com.vmware.content.samples.client.util;

/**
 * Helper class to act as a container for login info,
 * with a helper method to get login info.
 */
public class LoginInfo {
    // platform service controller runs the sso service for
    // typical vCenter Server installations
    private final String platformServiceControllerIp;
    private final String ssoUsername;
    private final String ssoPassword;

    public LoginInfo(String platformServiceControllerIp,
            String ssoUsername, String ssoPassword) {
        this.platformServiceControllerIp = platformServiceControllerIp;
        this.ssoUsername = ssoUsername;
        this.ssoPassword = ssoPassword;
    }

    public String getPlatformServiceControllerIp() {
        return platformServiceControllerIp;
    }

    public String getSsoUsername() {
        return ssoUsername;
    }

    public String getSsoPassword() {
        return ssoPassword;
    }

    public static LoginInfo getLoginInfo() {
        String platformServiceController =
                IOUtil.read("Enter the hostname/IP for the vCenter SSO Server: ");
        String username = IOUtil.read("Enter SSO username: ");
        String password = IOUtil.readPassword("Enter SSO password: ");
        return new LoginInfo(platformServiceController,
                username, password);
    }
}