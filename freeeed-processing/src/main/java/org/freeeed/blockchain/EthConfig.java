package org.freeeed.blockchain;

import org.freeeed.util.OsUtil;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.protocol.ipc.WindowsIpcService;

public class EthConfig {

    private static OsUtil.OS os = OsUtil.getOs();
    private static Web3j client;

    private static void initializeClient() {
        System.out.println("Subscribing to ipc.. on " + os);
        if (os == OsUtil.OS.WINDOWS) {
            client = Web3j.build(new WindowsIpcService("\\\\.\\pipe\\geth.ipc"));
        } else if (os == OsUtil.OS.MACOSX) {
            client = Web3j.build(new UnixIpcService(System.getProperty("user.home") + "/Library/Ethereum/geth.ipc"));
        } else {
            client = Web3j.build(new UnixIpcService(System.getProperty("user.home") + "/.ethereum/geth.ipc"));
        }
        System.out.println("Successfully connected to ipc, waiting for blocks..");
        client.ethSyncing().observable().subscribe(is -> {
            System.out.println("synchronizing " + is.isSyncing());
        });
    }

    public static Web3j getIpcClient() {
        if (client == null) {
            initializeClient();
        }
        return client;
    }
}
