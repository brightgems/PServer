package de.tuberlin.pserver.core.net;

import com.google.common.base.Preconditions;
import de.tuberlin.pserver.core.events.Event;
import io.netty.channel.Channel;

import java.util.UUID;

public final class NetEvents {

    private NetEvents() {}

    // -----------------------------------------------------------------------------------------

    public static final class NetEventTypes {

        private NetEventTypes() {}

        public static final String IO_EVENT_CHANNEL_CONNECTED = "con";

        public static final String IO_EVENT_RPC_CALLER_REQUEST = "req";

        public static final String IO_EVENT_RPC_CALLER_RESPONSE = "res";
    }

    // -----------------------------------------------------------------------------------------

    public static class NetEvent extends Event {

        private static final long serialVersionUID = -1L;

        transient private Channel channel;

        public UUID srcMachineID;

        public UUID dstMachineID;

        public NetEvent(final String type) { this(type, null, null); }
        public NetEvent(final String type, final UUID srcMachineID, final UUID dstMachineID) {
            super(type);
            this.srcMachineID = srcMachineID;
            this.dstMachineID = dstMachineID;
        }

        public void setSrcAndDst(final UUID srcMachineID, final UUID dstMachineID) {
            Preconditions.checkNotNull(srcMachineID);
            Preconditions.checkNotNull(dstMachineID);
            this.srcMachineID = srcMachineID;
            this.dstMachineID = dstMachineID;
        }

        public void setChannel(final Channel channel) {
            Preconditions.checkNotNull(channel);
            this.channel = channel;
        }

        public Channel getChannel() {
            return channel;
        }
    }

    // -----------------------------------------------------------------------------------------

    public static final class RPCCallerRequestEvent extends NetEvent {

        private static final long serialVersionUID = -1L;

        public final UUID callUID;

        public final RPCManager.MethodSignature methodSignature;

        public RPCCallerRequestEvent(final UUID callUID,
                                     final RPCManager.MethodSignature methodSignature) {

            super(NetEventTypes.IO_EVENT_RPC_CALLER_REQUEST);
            Preconditions.checkNotNull(callUID);
            Preconditions.checkNotNull(methodSignature);

            this.callUID = callUID;

            this.methodSignature = methodSignature;
        }
    }

    // -----------------------------------------------------------------------------------------

    public static final class RPCCalleeResponseEvent extends NetEvent {

        private static final long serialVersionUID = -1L;

        public final UUID callUID;

        public final Object result;

        public RPCCalleeResponseEvent(final UUID callUID,
                                      final Object result) {

            super(NetEventTypes.IO_EVENT_RPC_CALLER_RESPONSE);
            Preconditions.checkNotNull(callUID);

            this.callUID = callUID;

            this.result = result;
        }
    }
}