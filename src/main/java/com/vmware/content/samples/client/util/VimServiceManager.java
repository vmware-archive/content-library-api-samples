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

import com.vmware.vcloud.suite.samples.common.PlatformServiceController;
import com.vmware.vcloud.suite.samples.common.ServiceManager;
import com.vmware.vcloud.suite.samples.common.ServiceManagerFactory;

/**
 * Helper class to manage the VMware API services.
 * The {@link com.vmware.vcloud.suite.samples.common.ServiceManager} connects and
 * manages VIM, VIM PBM APIs in addition to vAPI.
 */
public final class VimServiceManager {

    static ServiceManager serviceManager;

    private VimServiceManager() {
    }

    public static ServiceManager getServiceManager(PlatformServiceController psc, String managementNodeId) {
        try {
            serviceManager = ServiceManagerFactory.getServiceManager(psc, managementNodeId);
            return serviceManager;
        } catch (Exception e) {
            throw new RuntimeException("Cannot login to VMware API on the provided vCenter Server instance", e);
        }
    }

    public static void disconnectIfRequired() {
        if (serviceManager != null) {
            serviceManager.disconnect();
        }
    }
}
