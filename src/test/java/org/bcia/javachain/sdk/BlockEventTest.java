/*
 *  Copyright 2016,2017 DTCC, Fujitsu Australia Software Technology, IBM - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.bcia.javachain.sdk;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.bcia.julongchain.protos.common.Common.Block;
import org.bcia.julongchain.protos.common.Common.BlockData;
import org.bcia.julongchain.protos.common.Common.BlockHeader;
import org.bcia.julongchain.protos.common.Common.BlockMetadata;
import org.bcia.julongchain.protos.common.Common.Envelope;
import org.bcia.julongchain.protos.common.Common.GroupHeader;
import org.bcia.julongchain.protos.common.Common.Header;
import org.bcia.julongchain.protos.common.Common.HeaderType;
import org.bcia.julongchain.protos.common.Common.Payload;
import org.bcia.julongchain.protos.node.EventsPackage;
import org.bcia.julongchain.protos.node.TransactionPackage.TxValidationCode;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class BlockEventTest {
    private static Block block, badBlock;
    private static BlockHeader blockHeader;
    private static BlockData blockData;
    private static BlockMetadata blockMetadata;
    private static EventHub eventHub;
    private static EventsPackage.Event goodEventBlock;
    private static EventsPackage.Event badEventBlock;

    /**
     * @throws Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        eventHub = new EventHub("test", "grpc://lh:99", null, null);

        // build a block with 3 transactions, set transaction 1,3 as valid, transaction 2 as invalid
        BlockData.Builder blockDataBuilder = BlockData.newBuilder();
        Payload.Builder payloadBuilder = Payload.newBuilder();
        GroupHeader.Builder channelHeaderBuilder = GroupHeader.newBuilder();
        channelHeaderBuilder.setType(HeaderType.ENDORSER_TRANSACTION_VALUE);
        Header.Builder headerBuilder = Header.newBuilder();
        Envelope.Builder envelopeBuilder = Envelope.newBuilder();

        channelHeaderBuilder.setGroupId("TESTCHANNEL");

        // transaction 1
        channelHeaderBuilder.setTxId("TRANSACTION1");
        headerBuilder.setGroupHeader(channelHeaderBuilder.build().toByteString());
        payloadBuilder.setHeader(headerBuilder.build());
        payloadBuilder.setData(ByteString.copyFrom("test data".getBytes(UTF_8)));
        envelopeBuilder.setPayload(payloadBuilder.build().toByteString());
        blockDataBuilder.addData(envelopeBuilder.build().toByteString());

        // transaction 2
        channelHeaderBuilder.clearTxId();
        channelHeaderBuilder.setTxId("TRANSACTION2");
        headerBuilder.clearGroupHeader();
        headerBuilder.setGroupHeader(channelHeaderBuilder.build().toByteString());
        payloadBuilder.clearHeader();
        payloadBuilder.setHeader(headerBuilder.build());
        payloadBuilder.setData(ByteString.copyFrom("test data".getBytes(UTF_8)));
        envelopeBuilder.clearPayload();
        envelopeBuilder.setPayload(payloadBuilder.build().toByteString());
        blockDataBuilder.addData(envelopeBuilder.build().toByteString());

        // transaction 3
        channelHeaderBuilder.clearTxId();
        channelHeaderBuilder.setTxId("TRANSACTION3");
        headerBuilder.clearGroupHeader();
        headerBuilder.setGroupHeader(channelHeaderBuilder.build().toByteString());
        payloadBuilder.clearHeader();
        payloadBuilder.setHeader(headerBuilder.build());
        payloadBuilder.setData(ByteString.copyFrom("test data".getBytes(UTF_8)));
        envelopeBuilder.clearPayload();
        envelopeBuilder.setPayload(payloadBuilder.build().toByteString());
        blockDataBuilder.addData(envelopeBuilder.build().toByteString());
        // blockData with 3 envelopes
        blockData = blockDataBuilder.build();

        // block header
        BlockHeader.Builder blockHeaderBuilder = BlockHeader.newBuilder();
        blockHeaderBuilder.setNumber(1);
        blockHeaderBuilder.setPreviousHash(ByteString.copyFrom("previous_hash".getBytes(UTF_8)));
        blockHeaderBuilder.setDataHash(ByteString.copyFrom("data_hash".getBytes(UTF_8)));
        blockHeader = blockHeaderBuilder.build();

        // block metadata
        BlockMetadata.Builder blockMetadataBuilder = BlockMetadata.newBuilder();
        blockMetadataBuilder.addMetadata(ByteString.copyFrom("signatures".getBytes(UTF_8)));   //BlockMetadataIndex.SIGNATURES_VALUE
        blockMetadataBuilder.addMetadata(ByteString.copyFrom("last_config".getBytes(UTF_8)));  //BlockMetadataIndex.LAST_CONFIG_VALUE,
        // mark 2nd transaction in block as invalid
        byte[] txResultsMap = new byte[] {TxValidationCode.VALID_VALUE, (byte) TxValidationCode.INVALID_OTHER_REASON_VALUE, TxValidationCode.VALID_VALUE};
        blockMetadataBuilder.addMetadata(ByteString.copyFrom(txResultsMap));              //BlockMetadataIndex.TRANSACTIONS_FILTER_VALUE
        blockMetadataBuilder.addMetadata(ByteString.copyFrom("orderer".getBytes(UTF_8)));      //BlockMetadataIndex.ORDERER_VALUE
        blockMetadata = blockMetadataBuilder.build();

        Block.Builder blockBuilder = Block.newBuilder();
        blockBuilder.setData(blockData);
        blockBuilder.setHeader(blockHeader);
        blockBuilder.setMetadata(blockMetadata);
        block = blockBuilder.build();

        goodEventBlock = EventsPackage.Event.newBuilder().setBlock(blockBuilder).build();

        // block with bad header
        headerBuilder.clearGroupHeader();
        headerBuilder.setGroupHeader(ByteString.copyFrom("bad channel header".getBytes(UTF_8)));
        payloadBuilder.clearHeader();
        payloadBuilder.setHeader(headerBuilder.build());
        payloadBuilder.setData(ByteString.copyFrom("test data".getBytes(UTF_8)));
        envelopeBuilder.clearPayload();
        envelopeBuilder.setPayload(payloadBuilder.build().toByteString());
        blockDataBuilder.clearData();
        blockDataBuilder.addData(envelopeBuilder.build().toByteString());
        blockBuilder.setData(blockDataBuilder.build());
        badBlock = blockBuilder.build();
        badEventBlock = EventsPackage.Event.newBuilder().setBlock(badBlock).build();
    }

    @Test
    public void testBlockEvent() {
        try {
            BlockEvent be = new BlockEvent(eventHub, goodEventBlock);
            assertEquals(be.getGroupId(), "TESTCHANNEL");
            assertArrayEquals(be.getBlock().toByteArray(), block.toByteArray());
            List<BlockEvent.TransactionEvent> txList = be.getTransactionEventsList();
            assertEquals(txList.size(), 3);
            BlockEvent.TransactionEvent te = txList.get(1);
            assertFalse(te.isValid());
            assertEquals(te.getValidationCode(), (byte) TxValidationCode.INVALID_OTHER_REASON_VALUE);
            te = txList.get(2);
            assertTrue(te.isValid());
        } catch (InvalidProtocolBufferException e) {
            fail("did not parse Block correctly.Error: " + e.getMessage());
        }
    }

    // Bad block input causes constructor to throw exception
    @Test (expected = InvalidProtocolBufferException.class)
    public void testBlockEventBadBlock() throws InvalidProtocolBufferException {
        BlockEvent be = new BlockEvent(eventHub, badEventBlock);
        be.getGroupId();
    }

}
