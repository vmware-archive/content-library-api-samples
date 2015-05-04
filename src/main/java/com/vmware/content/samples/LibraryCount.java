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

/**
 * A very simple API client which,
 * <ul>
 *     <li> logs in to the Content Library API </li>
 *     <li> makes a Content Library API call to list the libraries
 *          in the provided vCenter Server instance </li>
 *     <li> prints the number of libraries from the result of the list call </li>
 *     <li> logs out from the Content Library API </li>
 * </ul>
 */
public class LibraryCount {
    public static void main(String[] args) throws Exception {
        String vCenterSsoServer =
                IOUtil.read("Enter the hostname/IP for the vCenter SSO Server: ");
        String username = IOUtil.read("Enter SSO username: ");
        String password = IOUtil.readPassword("Enter SSO password: ");

        ClsApiClient client =
                new ClsApiClient(vCenterSsoServer, username, password);
        client.login();
        try {
            System.out.println("The number of libraries in this system is: " +
                    client.library().list().size());
        } finally {
            client.logout();
        }
    }
}
