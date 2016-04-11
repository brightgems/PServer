package de.tuberlin.pserver.runtime.core.remoteobj;

import de.tuberlin.pserver.runtime.core.events.EventDispatcher;
import de.tuberlin.pserver.runtime.core.network.NetManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalObject<T> extends EventDispatcher {

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    private final Map<Integer, Method> globalMethods;

    // ---------------------------------------------------
    // Constructors.
    // ---------------------------------------------------

    public GlobalObject(NetManager netManager, T instance, String globalObjectName) {
        super(true);

        this.globalMethods = new HashMap<>();
        List<Method> methods = Arrays.asList(Object.class.getMethods());
        //methods.addAll(Arrays.asList(EventDispatcher.class.getMethods()));
        for (Method method : instance.getClass().getMethods()) {
            if (!methods.contains(method) && !Modifier.isStatic(method.getModifiers())) {
                globalMethods.put(MethodInvocationMsg.getMethodID(method), method);
            }
        }

        netManager.addEventListener(MethodInvocationMsg.METHOD_INVOCATION_EVENT + "_" + globalObjectName , (event) -> {
            MethodInvocationMsg mim = (MethodInvocationMsg) event;
            Method calledMethod = globalMethods.get(mim.methodID);
            Object res = null;

            if (calledMethod == null)
                throw new UnsupportedOperationException("msg.methodID = " + mim.methodID);

            try {
                res = calledMethod.invoke(instance, mim.arguments);
            } catch (IllegalAccessException | InvocationTargetException e) {
                res = e;
            }

            mim.netChannel.sendMsg(
                    new MethodInvocationMsg(
                            globalObjectName,
                            mim.callID,
                            mim.classID,
                            mim.methodID,
                            null,
                            res
                    )
            );
        });
    }
}