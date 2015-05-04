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
package com.vmware.content.samples.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.LogManager;

import com.vmware.content.Configuration;
import com.vmware.content.Library;
import com.vmware.content.LocalLibrary;
import com.vmware.content.SubscribedLibrary;
import com.vmware.content.Type;
import com.vmware.content.library.Item;
import com.vmware.content.library.item.DownloadSession;
import com.vmware.content.library.item.Storage;
import com.vmware.content.library.item.UpdateSession;
import com.vmware.content.library.item.downloadsession.File;
import com.vmware.content.samples.client.util.IOUtil;
import com.vmware.vapi.bindings.Service;
import com.vmware.vcenter.ovf.LibraryItem;
import com.vmware.vcloud.suite.samples.common.LookupServiceHelper;
import com.vmware.vcloud.suite.samples.common.PlatformServiceController;
import com.vmware.vcloud.suite.samples.common.SSOConnection;
import com.vmware.vcloud.suite.samples.vapi.endpoint.VapiServiceEndpoint;

/**
 * This is a simplified wrapper around the Content Library APIs.
 * It takes care of most of the boilerplate code required for
 * establishing connections and getting authentication.
 */
public class ClsApiClient {
    private String username;
    private String password;
    private String lookupServiceUrl;
    private VapiServiceEndpoint vApiServiceEndpoint;
    private PlatformServiceController platformServiceController;
    private String managementNodeId;

    private boolean loggedIn = false;

    static {
        // avoid unnecessary log spew on stdout
        LogManager.getLogManager().getLogger("").setLevel(Level.SEVERE);
    }

    public ClsApiClient(String hostname, String username, String password) {
        // assuming default look up service SDK URL
        this.lookupServiceUrl = "https://" + getHostName(hostname) + "/lookupservice/sdk";
        this.password = password;
        this.username = username;
    }

    public PlatformServiceController getPlatformServiceController() {
        return platformServiceController;
    }

    public String getManagementNodeId() {
        return managementNodeId;
    }

    public synchronized void login() {
        try {
            // login to platform service controller
            // (in typical vCenter Server installations, this runs sso service)
            platformServiceController = new PlatformServiceController(lookupServiceUrl);
            try {
                platformServiceController.login(username, password);
            } catch (Exception e) {
                throw new RuntimeException("Unable to login to vCenter SSO Server", e);
            }

            // look up management node (assuming this system has only one management node)
            LookupServiceHelper lookupService = platformServiceController.getLsServiceHelper();
            try {
                managementNodeId = lookupService.getDefaultMgmtNode();
                assert lookupService.getMgmtNodeInstanceName(managementNodeId) != null;
            } catch (Exception e) {
                throw new RuntimeException("Cannot get default management node instance", e);
            }

            String vApiUrl = lookupService.findVapiUrl(managementNodeId);
            IOUtil.print("vAPI URL :" + vApiUrl);

            // login to vAPI
            SSOConnection ssoConn = platformServiceController.getSsoConnection();
            vApiServiceEndpoint = new VapiServiceEndpoint(vApiUrl);
            vApiServiceEndpoint.login(ssoConn.getSamlBearerToken());
            loggedIn = true;
        } catch (Exception e) {
            throw new RuntimeException("Login failed", e);
        }
    }

    public void logout() {
        vApiServiceEndpoint.logout();
        loggedIn = false;
    }

    private void checkLoggedIn() {
        if (!loggedIn) {
            throw new IllegalStateException(
                    "login() must be called before accessing services");
        }
    }

    private <T extends Service> T getService(Class<T> serviceClass) {
        checkLoggedIn();
        return (T) vApiServiceEndpoint.getService(serviceClass);
    }

    public Library library() {
        return getService(Library.class);
    }

    public LocalLibrary localLibrary() {
        return getService(LocalLibrary.class);
    }

    public SubscribedLibrary subscribedLibrary() {
        return getService(SubscribedLibrary.class);
    }

    public Item item() {
        return getService(Item.class);
    }

    public LibraryItem ovfLibraryItem() {
        return getService(LibraryItem.class);
    }

    public Storage storage() {
        return getService(Storage.class);
    }

    public DownloadSession downloadSession() {
        return getService(DownloadSession.class);
    }

    public File downloadSessionFile() {
        return getService(File.class);
    }

    public UpdateSession updateSession() {
        return getService(UpdateSession.class);
    }

    public com.vmware.content.library.item.updatesession.File updateSessionFile() {
        return getService(com.vmware.content.library.item.updatesession.File.class);
    }

    public Configuration configuration() {
        return getService(Configuration.class);
    }

    public Type type() {
        return getService(Type.class);
    }

    private static String getHostName(String ip) {
        InetAddress address;
        try {
            address = InetAddress.getByName(ip);
            return address.getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Unable to do a reverse DNS lookup on " + ip, e);
        }
    }
}
