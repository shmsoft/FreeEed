package org.freeeed.blockchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

public class BlockchainUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void stageBlockRange(int from, int to) {
        Web3j client = EthConfig.getIpcClient();
        for (int i = from; i < to; i++) {
            System.out.println("working with block number " + i);
            try {
                DefaultBlockParameterNumber param = new DefaultBlockParameterNumber(i);
                Request<?, EthBlock> ethBlockRequest = client.ethGetBlockByNumber(param, true);
                EthBlock.Block block = ethBlockRequest.send().getBlock();
                Map<String, String> keyValuePair = MAPPER.convertValue(block, Map.class);
                keyValuePair.put("readableTimestamp", timestampFrom(block.getTimestamp().longValue()));
                System.out.println(MAPPER.writeValueAsString(keyValuePair));
                //TODO - create a temp file, write json data to it
                //TODO - read the same file while processing and upload each json to elastic search
            } catch (Exception ex) {
                System.err.println("Check GETH is running locally and synchronizing...");
            }
        }
    }

    private static String timestampFrom(Long epochSecond) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond),
                ZoneId.systemDefault()).toOffsetDateTime().toString();
    }
}
