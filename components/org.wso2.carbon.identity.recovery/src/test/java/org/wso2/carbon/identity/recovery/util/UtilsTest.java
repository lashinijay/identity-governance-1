/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.recovery.util;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.recovery.IdentityRecoveryClientException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.internal.IdentityRecoveryServiceDataHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class UtilsTest {

    private static final String TENANT_DOMAIN = "test.com";
    private static final int TENANT_ID = 123;
    private static final String USER_NAME = "testUser";
    private static final String USER_STORE_DOMAIN = "TEST";
    @Mock
    private UserStoreManager userStoreManager;
    @Mock
    private UserRealm userRealm;
    @Mock
    private RealmService realmService;
    @Mock
    private IdentityRecoveryServiceDataHolder identityRecoveryServiceDataHolder;

    private MockedStatic<IdentityTenantUtil> mockedStaticIdentityTenantUtil;
    private MockedStatic<UserStoreManager> mockedStaticUserStoreManager;
    private MockedStatic<IdentityRecoveryServiceDataHolder> mockedIdentityRecoveryServiceDataHolder;
    private MockedStatic<IdentityUtil> mockedIdentityUtil;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
    }

    @BeforeClass
    public void beforeTest() {

        mockedStaticIdentityTenantUtil = mockStatic(IdentityTenantUtil.class);
        mockedStaticUserStoreManager = mockStatic(UserStoreManager.class);
        mockedIdentityRecoveryServiceDataHolder = Mockito.mockStatic(IdentityRecoveryServiceDataHolder.class);
        mockedIdentityUtil = mockStatic(IdentityUtil.class);
    }

    @AfterClass
    public void afterTest() {

        mockedStaticIdentityTenantUtil.close();
        mockedStaticUserStoreManager.close();
        mockedIdentityRecoveryServiceDataHolder.close();
        mockedIdentityUtil.close();
    }

    @Test(expectedExceptions = IdentityRecoveryClientException.class)
    public void testCheckPasswordPatternViolationForInvalidDomain() throws Exception {

        User user = new User();
        user.setUserName(USER_NAME);
        user.setTenantDomain(TENANT_DOMAIN);
        user.setUserStoreDomain(USER_STORE_DOMAIN);

        when(IdentityTenantUtil.getTenantId(user.getTenantDomain())).thenReturn(TENANT_ID);
        mockedIdentityRecoveryServiceDataHolder.when(IdentityRecoveryServiceDataHolder::getInstance).thenReturn(
                identityRecoveryServiceDataHolder);
        when(identityRecoveryServiceDataHolder.getRealmService()).thenReturn(realmService);
        when(realmService.getTenantUserRealm(TENANT_ID)).thenReturn(userRealm);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        mockedIdentityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn("PRIMARY");
        when(userStoreManager.getSecondaryUserStoreManager(USER_STORE_DOMAIN)).thenReturn(null);

        try {
            Utils.checkPasswordPatternViolation(new UserStoreException("Invalid Domain Name"), user);
        } catch (IdentityRecoveryClientException e) {
            assertEquals(e.getErrorCode(),
                    IdentityRecoveryConstants.ErrorMessages.ERROR_CODE_DOMAIN_VIOLATED.getCode());
            assertEquals(e.getMessage(), "Invalid domain " + user.getUserStoreDomain() + " provided.");
            throw e;
        }
    }
}
