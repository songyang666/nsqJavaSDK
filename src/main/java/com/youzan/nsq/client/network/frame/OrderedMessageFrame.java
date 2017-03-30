package com.youzan.nsq.client.network.frame;

/**
 * Created by lin on 16/9/26.
 */
public class OrderedMessageFrame extends MessageFrame{
    /**
     * 8-byte : disk queue offset
     */
    final private byte[] diskQueueOffset = new byte[8];
    /**
     * 4-byte : disk queue data size
     */
    final private byte[] diskQueueDataSize = new byte[4];

    @Override
    public void setData(byte[] bytes) {
        //capability of message array bytes.length - (8 + 2 + 8 + 4 + 16)
        final int messageBodySize = bytes.length - (38);
        messageBody = new byte[messageBodySize];
        System.arraycopy(bytes, 0, timestamp, 0, 8);
        System.arraycopy(bytes, 8, attempts, 0, 2);
        //Sub Ordered incoming extra info, disk queue offset & disk queue data size
        System.arraycopy(bytes, 10, messageID, 0, 16);
        System.arraycopy(bytes, 10, internalID, 0, 8);
        System.arraycopy(bytes, 18, traceID, 0, 8);

        System.arraycopy(bytes, 26, diskQueueOffset, 0, 8);
        System.arraycopy(bytes, 34, diskQueueDataSize, 0, 4);


        System.arraycopy(bytes, 38, messageBody, 0, messageBodySize);
    }

    /**
     * function to get diskQueueOffset of current msg, diskqueue has meaning only when message contains advanced info of
     * SUB
     * @return diskQueueOffSet (int 64) in byte[]
     */
    public byte[] getDiskQueueOffset(){
        return this.diskQueueOffset;
    }

    /**
     * function to get diskQueueDataSize of current msg, disk queue dat size has meaning only when message contains
     * advanced info of SUB
     * @return diskQueueDataSize (int 64) in byte[]
     */
    public byte[] getDiskQueueDataSize(){
        return this.diskQueueDataSize;
    }
}