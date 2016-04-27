package com.youzan.nsq.client.remoting.connector;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youzan.nsq.client.bean.NSQNode;
import com.youzan.nsq.client.exceptions.NSQException;
import com.youzan.nsq.client.remoting.NSQConnector;
import com.youzan.nsq.client.remoting.listener.ConnectorListener;
import com.youzan.util.IOUtil;
import com.youzan.util.NamedThreadFactory;

/**
 * Created by pepper on 14/12/22.
 */
public class CustomerConnector {
    private static final Logger log = LoggerFactory.getLogger(CustomerConnector.class);
    private static final int readyCount = 10;

    private final String host;// lookup host
    private final int port;// lookup port
    private final String topic;
    private final String channel;
    private ConnectorListener subListener;
    private ConcurrentHashMap</* ip:port */String, NSQConnector> connectorMap;

    public static final int DEFAULT_MONITORING_PERIOD_IN_SECOND = 45;
    private final ScheduledExecutorService monitoringBoss = Executors
            .newSingleThreadScheduledExecutor(new NamedThreadFactory("ConsumerMonitoring", Thread.MIN_PRIORITY));
    private final ConnectorMonitor monitor;

    public CustomerConnector(String host, int port, String topic, String channel) {
        this.host = host;
        this.port = port;
        this.topic = topic;
        this.channel = channel;
        this.connectorMap = new ConcurrentHashMap<String, NSQConnector>();
        this.monitor = new ConnectorMonitor(host, port);
    }

    public ConcurrentHashMap<String, NSQConnector> getConnectorMap() {
        return connectorMap;
    }

    public void connect() {
        if (subListener == null) {
            log.warn("ConnectorListener must be seted.");
            return;
        }

        List<NSQNode> nsqNodes = ConnectorUtils.lookupTopic(host, port, topic);
        if (null == nsqNodes || nsqNodes.isEmpty()) {
            log.error("customer start fail !! no nsqd addr found at lookupd {}:{} with topic: {}", host, port, topic);
            return;
        }

        for (NSQNode node : nsqNodes) {
            NSQConnector connector = null;
            try {
                connector = new NSQConnector(node.getHost(), node.getPort(), subListener, readyCount);
                connector.sub(topic, channel);
                connector.rdy(readyCount);
                connectorMap.put(ConnectorUtils.getConnectorKey(node), connector);
            } catch (NSQException e) {
                final StringBuffer sb = new StringBuffer(100);
                sb.append("CustomerConnector can not connect ").append(ConnectorUtils.getConnectorKey(node));
                log.error(sb.toString(), e);
                IOUtil.closeQuietly(connector);
            }
        }

        // Post
        monitor.registerConsumer(this);
        monitoringBoss.scheduleAtFixedRate(monitor, 10, DEFAULT_MONITORING_PERIOD_IN_SECOND, TimeUnit.SECONDS);
    }

    public void setSubListener(ConnectorListener listener) {
        this.subListener = listener;
    }

    public ConnectorListener getSubListener() {
        return subListener;
    }

    public String getTopic() {
        return topic;
    }

    public String getChannel() {
        return channel;
    }

    public int getReadyCount() {
        return readyCount;
    }

    public boolean removeConnector(NSQConnector connector) {
        if (connector == null) {
            return true;
        }
        IOUtil.closeQuietly(connector);
        return connectorMap.remove(ConnectorUtils.getConnectorKey(connector), connector);
    }

    public void addConnector(NSQConnector connector) {
        connectorMap.put(ConnectorUtils.getConnectorKey(connector), connector);
    }

    public void close() {
        for (NSQConnector connector : connectorMap.values()) {
            IOUtil.closeQuietly(connector);
        }
        monitoringBoss.shutdownNow();
    }
}
