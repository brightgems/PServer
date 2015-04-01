package de.tuberlin.pserver.core.net;

import com.google.common.base.Preconditions;
import de.tuberlin.pserver.core.events.EventDispatcher;
import de.tuberlin.pserver.core.infra.InfrastructureManager;
import de.tuberlin.pserver.core.infra.MachineDescriptor;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class NetManager extends EventDispatcher {

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    private static final Logger LOG = LoggerFactory.getLogger(NetManager.class);

    private final MachineDescriptor machine;

    /** Storing the currently connected peers. Peers are uniquely identified with UIDs.
     * The communication to a remote peer takes places via a (netty) channel. Number of
     * peers is not fixed and may change during runtime. */
    private final Map<UUID,Channel> peers;

    /** Event loops for handling outgoing and incoming messages.
     * There is no strict separation between inbound and outbound processing.
     * It is important that enough threads are assigned to guarantee
     * high and stable message throughput. */
    private final NioEventLoopGroup elg;

    private final InfrastructureManager infraManager;

    // ---------------------------------------------------
    // Constructor.
    // ---------------------------------------------------

    public NetManager(final MachineDescriptor machine, final InfrastructureManager infraManager, final int eventLoopThreads) {
        super(true, "IOManager");
        Preconditions.checkNotNull(machine);
        Preconditions.checkArgument(eventLoopThreads > 0 && eventLoopThreads < 256);
        this.machine = machine;
        this.infraManager = Preconditions.checkNotNull(infraManager);
        this.peers = new ConcurrentHashMap<>();
        this.elg = new NioEventLoopGroup(eventLoopThreads);
        start();
    }

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    public MachineDescriptor getMachineDescriptor() { return machine; }

    public void connectTo(final MachineDescriptor dstMachine) {
        Preconditions.checkNotNull(dstMachine);

        final Lock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();
        final InetSocketAddress socketAddress = new InetSocketAddress(dstMachine.address, dstMachine.port);
        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(elg).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                //ch.pipeline().addFirst(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4));
                ch.pipeline().addFirst(new ObjectEncoder());
                ch.pipeline().addFirst(new IOEventChannelHandler());
                ch.pipeline().addFirst(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(getClass().getClassLoader())));
            }
        });

        final ChannelFuture cf = bootstrap.connect(socketAddress);
        cf.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture cf) throws Exception {
                if (cf.isSuccess()) {
                    peers.put(dstMachine.machineID, cf.channel());
                    final NetEvents.NetEvent event = new NetEvents.NetEvent(
                            NetEvents.NetEventTypes.IO_EVENT_CHANNEL_CONNECTED,
                            machine.machineID,
                            dstMachine.machineID
                    );
                    sendEvent(dstMachine, event);
                    lock.lock();
                    condition.signal();
                    lock.unlock();
                } else {
                    throw new IllegalStateException(cf.cause());
                }
            }
        });

        lock.lock();
        try {
            condition.await();
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage());
        } finally {
            lock.unlock();
        }
    }

    public void sendEvent(final MachineDescriptor machine, final NetEvents.NetEvent event) {
        sendEvent(machine.machineID, event);
    }

    public void sendEvent(final UUID machineID, final NetEvents.NetEvent event) {
        Preconditions.checkNotNull(machineID);
        Preconditions.checkNotNull(event);
        final Channel channel = peers.get(machineID);
        Preconditions.checkState(channel != null);
        event.setSrcAndDst(machine.machineID, machineID);
        try {
            channel.writeAndFlush(event).sync();
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage());
        }
    }

    public void broadcastEvent(final NetEvents.NetEvent event) {
        for (final MachineDescriptor md : infraManager.getMachines()) {
            if (!machine.machineID.equals(md.machineID))
                sendEvent(md, event);
        }
    }

    public void disconnect(final UUID machineID) {
        Preconditions.checkNotNull(machineID);
        final Channel channel = peers.get(machineID);
        Preconditions.checkState(channel != null);
        try {
            channel.disconnect().sync();
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage());
        }
    }

    public void shutdown() {
        for (final UUID machineID : peers.keySet())
            disconnect(machineID);
        peers.clear();
        try {
            elg.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage());
        }
        shutdownEventDispatcher();
    }

    // ---------------------------------------------------
    // Private Methods.
    // ---------------------------------------------------

    private void start() {
        final ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(elg).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                //ch.pipeline().addFirst(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4));
                ch.pipeline().addFirst(new ObjectEncoder());
                ch.pipeline().addFirst(new IOEventChannelHandler());
                ch.pipeline().addFirst(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(getClass().getClassLoader())));
            }
        });

        final ChannelFuture cf = bootstrap.bind(machine.port);
        cf.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (cf.isSuccess()) {
                    System.out.println("Server bound to port: " + machine.port);
                } else {
                    throw new IllegalStateException(cf.cause());
                }
            }
        });

        try {
            // Wait until the netty-server is bound.
            cf.sync();
        } catch (InterruptedException e) {
            LOG.error(e.getLocalizedMessage());
        }
    }

    // ---------------------------------------------------
    // Inner Classes.
    // ---------------------------------------------------

    private final class IOEventChannelHandler extends SimpleChannelInboundHandler<NetEvents.NetEvent> {

        @Override
        protected void channelRead0(final ChannelHandlerContext ctx, final NetEvents.NetEvent event) throws Exception {
            Preconditions.checkNotNull(event);
            event.setChannel(ctx.channel());
            if (NetEvents.NetEventTypes.IO_EVENT_CHANNEL_CONNECTED.equals(event.type)) {
                //final SocketAddress address = ctx.channel().remoteAddress();
                //if (address instanceof InetSocketAddress) {
                //    final MachineDescriptor machine =
                //        new MachineDescriptor(
                //            event.srcMachineID,
                //            ((InetSocketAddress) address).getAddress(),
                //            machine.port
                //        );
                    peers.put(event.srcMachineID, ctx.channel());
                    //descriptors.put(event.srcMachineID, machine);
                //} else
                //    throw new IllegalStateException();
            } else
                dispatchEvent(event);
        }
    }
}