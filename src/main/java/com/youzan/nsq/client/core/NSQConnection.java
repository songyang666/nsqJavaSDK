package com.youzan.nsq.client.core;

import com.youzan.nsq.client.core.command.NSQCommand;
import com.youzan.nsq.client.entity.Address;
import com.youzan.nsq.client.entity.NSQConfig;
import com.youzan.nsq.client.entity.NSQMessage;
import com.youzan.nsq.client.entity.Topic;
import com.youzan.nsq.client.network.frame.ErrorFrame;
import com.youzan.nsq.client.network.frame.NSQFrame;
import com.youzan.nsq.client.network.frame.ResponseFrame;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;

import java.io.Closeable;
import java.util.concurrent.TimeoutException;

/**
 * <pre>
 * NSQ Connection Definition.
 * This is underlying Netty Pipeline with decoder and encoder.
 * </pre>
 *
 * @author <a href="mailto:my_email@email.exmaple.com">zhaoxi (linzuxiong)</a>
 */
public interface NSQConnection extends Closeable {

    AttributeKey<NSQConnection> STATE = AttributeKey.valueOf("Connection.State");

    Address getAddress();

    NSQConfig getConfig();

    boolean isConnected();

    int getId();

    int getCurrentRdyCount();

    void setCurrentRdyCount(int newRdyCnt);

    /**
     * If any client wants to use my connection, then the client need to pass
     * itself into me before calling init(this method) because of the usage of
     * Netty.
     *
     * @throws TimeoutException a timeout error
     */
    void init() throws TimeoutException;

    /**
     * initialization of NSQConnection for consumer, with topic name passin.
     * @param topic topic this NSQConenction subscribe to
     * @throws TimeoutException raised when timeout in initialization
     */
    void init(final Topic topic) throws TimeoutException;

    /**
     * Check internalID and disk queue offset of message received in current connection, config of current connection
     * must be in order mode.
     * @param internalID internal ID of message for check
     * @param diskQueueOffset diskQueueOffset of message for check
     * @param msg message to check order
     * @return true if connection is not in order mode or internalID and diskQueueOffset are newest(largest), otherwise
     * return false.
     */
    boolean checkOrder(long internalID, long diskQueueOffset, final NSQMessage msg);

    /**
     * Synchronize the protocol packet
     *
     * @param command a {@link NSQCommand}
     * @return a {@link NSQFrame}  after send a request
     * @throws TimeoutException a timed out error
     */
    NSQFrame commandAndGetResponse(final NSQCommand command) throws TimeoutException;

    ChannelFuture command(final NSQCommand command);

    void addResponseFrame(ResponseFrame frame);

    void addErrorFrame(ErrorFrame frame);

    /**
     * Tell if connection to nsqd receive message with extendable content.
     * @return {@link Boolean#TRUE} if message from current connection does not meant to be extendable, otherwise {@link Boolean#FALSE}
     */
    boolean isExtend();

    Topic getTopic();

    /**
     * Perform the action quietly. No exceptions.
     */
    @Override
    void close();
}
