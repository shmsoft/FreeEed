package org.freeeed.blockchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.freeeed.main.ActionStaging;
import org.freeeed.services.Project;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

public class BlockChainUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOGGER = Logger.getLogger(BlockChainUtil.class);

    public static void stageBlockRange(int from, int to, ActionStaging actionStaging) throws IOException {
        Web3j client = EthConfig.getIpcClient();
        File blockChainData = new File("blockChainData" + Project.getCurrentProject().getProjectCode());
        Project.getCurrentProject().setProjectFilePath(blockChainData.getAbsolutePath());

        try (BufferedWriter blockWriter = new BufferedWriter(new FileWriter(blockChainData))) {
            for (int i = from; i <= to; i++) {
                actionStaging.setProgressUIMessage("Getting block number " + i);
                LOGGER.info("working with block number " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                blockWriter.newLine();
                try {
                    Request<?, EthBlock> ethBlockRequest = client.ethGetBlockByNumber(new DefaultBlockParameterNumber(i), true);
                    EthBlock.Block block = ethBlockRequest.send().getBlock();
                    Map<String, String> keyValuePair = MAPPER.convertValue(block, Map.class);
                    keyValuePair.put("readableTimestamp", timestampFrom(block.getTimestamp().longValue()));
                    blockWriter.write(MAPPER.writeValueAsString(keyValuePair));
                } catch (Exception ex) {
                    LOGGER.error("Check GETH is running locally and synchronizing...");
                }
                actionStaging.updateUIProgress(1);
            }
        }
    }

    private static String timestampFrom(Long epochSecond) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond),
                ZoneId.systemDefault()).toOffsetDateTime().toString();
    }
}
