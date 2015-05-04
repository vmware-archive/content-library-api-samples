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
package com.vmware.content.samples;

import com.vmware.content.samples.client.ClsApiClient;
import com.vmware.content.samples.client.util.IOUtil;
import com.vmware.content.samples.client.util.LoginInfo;
import com.vmware.content.samples.client.util.VimServiceManager;

/**
 * Abstract class to do the setup and teardown tasks which every sample needs to do.
 * The business logic is implemented in {@link #runSample(String[])} method.
 */
public abstract class AbstractSample {

    /**
     * Content Library API client.
     */
    protected ClsApiClient client;

    /**
     * Business logic demonstrated by this sample.
     *
     * @param args parameters this sample might need.
     */
    protected abstract void runSample(String[] args) throws Exception;

    /**
     * Glue code required to run the sample.
     *
     * @param args parameters this sample might need.
     */
    public void run(String[] args) {
        setUp();
        try {
            runSample(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            tearDown();
        }
    }

    /**
     * Login to the Content Library API.
     */
    private void setUp() {
        LoginInfo loginInfo = LoginInfo.getLoginInfo();
        client = new ClsApiClient(loginInfo.getPlatformServiceControllerIp(),
                loginInfo.getSsoUsername(), loginInfo.getSsoPassword());
        client.login();
        IOUtil.print("Logged in to Content Library API successfully.");
    }

    /**
     * Logout from the clients.
     */
    private void tearDown() {
        VimServiceManager.disconnectIfRequired();
        client.logout();
    }
}
