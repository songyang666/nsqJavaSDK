package com.youzan.nsq.client.network.frame;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NSQFrame {

    private static final Logger logger = LoggerFactory.getLogger(NSQFrame.class);

    public static final Charset ASCII = StandardCharsets.US_ASCII;
    public static final Charset UTF8 = StandardCharsets.UTF_8;
    public static final Charset DEFAULT_CHARSET = UTF8;

    public enum FrameType {
        RESPONSE_FRAME(0),//
        ERROR_FRAME(1),//
        MESSAGE_FRAME(2), //
        ;
        private int type;

        FrameType(int type) {
            this.type = type;
        }
    }

    private int size;
    private byte[] data;

    /**
     * @return FrameType
     */
    public abstract FrameType getType();

    /**
     * @return some readable content
     */
    public abstract String getMessage();

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public static NSQFrame newInstance(final int type) {
        switch (type) {
            case 0:
                return new ResponseFrame();
            case 1:
                return new ErrorFrame();
            case 2:
                return new MessageFrame();
            default: {
                logger.error("Un recognized NSQ Frame! Please check NSQ protocol!");
                return null;
            }
        }
    }
}
