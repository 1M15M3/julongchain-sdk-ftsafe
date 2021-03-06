/**
 * Copyright Dingxuan. All Rights Reserved.
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
package org.bcia.javachain.common.groupconfig.value;

import org.bcia.javachain.common.groupconfig.GroupConfigConstant;
import org.bcia.julongchain.protos.common.Configuration;

/**
 * 对象
 *
 * @author zhouhui
 * @date 2018/5/10
 * @company Dingxuan
 */
public class BlockDataHashingStructureValue extends StandardConfigValue {

    public BlockDataHashingStructureValue() {
        this.key = GroupConfigConstant.BLOCK_DATA_HASHING_STRUCTURE;

        Configuration.BlockDataHashingStructure blockDataHashingStructure = Configuration.BlockDataHashingStructure
                .newBuilder().setWidth(GroupConfigConstant.DEFAULT_BLOCK_DATA_HASHING_WIDTH).build();
        this.value = blockDataHashingStructure;
    }
}
