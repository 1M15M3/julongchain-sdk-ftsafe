/**
 * Copyright DingXuan. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bcia.javachain.sdk.security.msp;

import org.bcia.javachain.sdk.security.msp.entity.IdentityIdentifier;
import org.bcia.javachain.sdk.security.msp.entity.OUIdentifier;
import org.bcia.javachain.common.exception.MspException;
import org.bcia.javachain.common.exception.VerifyException;
import org.bcia.julongchain.protos.common.MspPrincipal;

import java.io.IOException;
import java.util.Date;

/**
 * 身份接口
 * @author zhangmingyang
 * @Date: 2018/3/8
 * @company Dingxuan
 */
public interface IIdentity {

    Date expireAt() throws MspException;

    IdentityIdentifier getIdentifier();

    String getMSPIdentifier();

    void validate() throws MspException;

    OUIdentifier[] getOrganizationalUnits() throws MspException;

    void verify(byte[] msg, byte[] sig) throws VerifyException;

    byte[] serialize();

    void satisfiesPrincipal(MspPrincipal.MSPPrincipal principal) throws MspException, IOException;
}
