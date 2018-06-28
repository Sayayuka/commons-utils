/*
package com.dev.commons.tools.xi.util;

import javax.faces.event.PhaseId;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.Seam;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.ContextAdaptor;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Manager;

import com.successfactors.logging.api.LogManager;
import com.successfactors.logging.api.Logger;
import com.successfactors.platform.HibernateUtils;
import com.successfactors.platform.di.SFContextUtils;
import com.successfactors.platform.di.SFScopeHolder;
import com.successfactors.platform.di.SFScopeType;

*/
/**
 * Central place to access seam specific apis. Since seam is changing often and
 * public APIs are not documented yet, it focuses all changes here incase an
 * upgrade modifies the code we are using.
 *
 *
 * @author ddiodati
 *
 *//*

public class SeamEnvUtils {

  */
/** Logger. *//*

  private static final Logger log = LogManager.getLogger();

  */
/**
   * private constructor.
   *//*

  private SeamEnvUtils() {
    super();
  }

  */
/**
   * Invalidates session.
   *
   * @param session
   *          http session object
   *//*

  public static final void invalidSession(final HttpSession session) {
    if (Contexts.isSessionContextActive()) {
      log.info("invalidate seam session");
      Seam.invalidateSession();
    } else if (SFContextUtils.isSpringEnabled() && session != null) {
      log.info("invalidate session in spring context");
      session.invalidate();
    } else if (session != null) {
      log.info("invalidate session without seam context");
      session.invalidate();
    }
  }

  */
/**
   * Obtains an object from seam enviroment.
   *
   * @param name
   *          The name of the seam object.
   * @param create
   *          If the object does not exist then create it.
   * @param type
   *          The expected class type to be returned.
   * @return The object requested.
   * @throws InstantiationException
   *           if the returned object is not of the correct type.
   *//*

  public static final Object getInstance(final String name, final boolean create, final Class<?> type) throws InstantiationException {
    if (SFContextUtils.isSpringEnabled()) {
      final Object result = SFContextUtils.getInstance(name, create);
      return result;
    }
    final Object result = Component.getInstance(name, create);

    if (result != null && !type.isAssignableFrom(result.getClass())) {
      throw new InstantiationException("Seam Object is not of type " + type.toString());
    }

    return result;
  }

  */
/**
   * Obtains an object from seam enviroment.
   *
   * @param name
   *          The name of the seam object.
   * @param create
   *          If the object does not exist then create it.
   * @return The object requested.
   *//*

  public static final Object getInstance(final String name, final boolean create) {
    if (SFContextUtils.isSpringEnabled()) {
      return SFContextUtils.getInstance(name, create);
    }
    return Component.getInstance(name, create);
  }

  */
/**
   * Obtains an object from seam environment. This method is only use for a
   * special case and please talk to Platform team first before using this
   * method.
   *
   * @param name
   *          The name of the seam object.
   * @param scope
   *          scope of component.
   * @param create
   *          If the object does not exist then create it.
   * @return The object requested.
   *//*

  public static final Object getInstance(final String name, final ScopeType scope, final boolean create) {
    if (SFContextUtils.isSpringEnabled()) {
      SFScopeType sfScope = null;
      if (ScopeType.SESSION.equals(scope)) {
        sfScope = SFScopeType.SESSION;
      } else {
        sfScope = SFScopeType.EVENT;
      }
      return SFContextUtils.getInstance(name, sfScope, create);
    }
    return Component.getInstance(name, scope, create);
  }

  */
/**
   * Obtains an object from seam environment. This method is only use for field
   * injection
   *
   * @param name
   *          The name of the seam object.
   * @param scope
   *          scope of component.
   * @param <T>
   *          Class<T>
   * @return The object requested.
   *//*

  public static final <T> T getInstance(final String name, final ScopeType scope, final Class<T> clz) {
    return getInstance(name, scope, false, clz);
  }

  */
/**
   * Obtains an object from seam environment. This method is only use for field
   * injection
   *
   * @param name
   *          The name of the seam object.
   * @param scope
   *          scope of component.
   * @param create
   *          If the object does not exist then create it.
   * @param <T>
   *          Class<T>
   * @return The object requested.
   *//*

  public static final <T> T getInstance(final String name, final ScopeType scope, final boolean create, final Class<T> clz) {
    Object obj = null;
    if (SFContextUtils.isSpringEnabled()) {
      SFScopeType sfScope = null;
      if (ScopeType.SESSION.equals(scope)) {
        sfScope = SFScopeType.SESSION;
      } else {
        sfScope = SFScopeType.EVENT;
      }
      obj = SFContextUtils.getInstance(name, sfScope, create);
    } else {
      if (!Contexts.isApplicationContextActive()) {
        log.warn("No active application scope");
        return null;
      }
      obj = Component.getInstance(name, scope, create);
    }
    if (obj == null) {
      if (int.class.equals(clz)) {
        return (T) new Integer(0);
      } else if (long.class.equals(clz)) {
        return (T) new Long(0);
      } else if (byte.class.equals(clz)) {
        return (T) new Byte((byte) 0);
      } else if (double.class.equals(clz)) {
        return (T) new Double(0.0);
      } else if (short.class.equals(clz)) {
        return (T) new Short((short) 0);
      } else if (float.class.equals(clz)) {
        return (T) new Float(0f);
      } else if (boolean.class.equals(clz)) {
        return (T) Boolean.FALSE;
      } else if (char.class.equals(clz)) {
        return (T) new Character('\u0000');
      } else {
        return null;
      }
    }
    return (T) obj;
  }

  */
/**
   * Obtains an object from seam enviroment. <strong>NOTE: the desired class
   * must be annotated with "@Name"!</strong>
   *
   * @param clazz
   *          The class to create an instance of.
   * @param create
   *          If the object does not exist then create it.
   * @return The object requested.
   *//*

  public static final Object getInstance(final Class<?> clazz, final boolean create) {
    if (SFContextUtils.isSpringEnabled()) {
      return SFContextUtils.getInstance(clazz);
    }
    return Component.getInstance(clazz, create);
  }

  */
/**
   * Obtains the name of a seam component.
   *
   * @param seamComponent
   *          The seam component instance.
   * @return The name of the component.
   *//*

  public static final String getComponentName(final Object seamComponent) {
    if (SFContextUtils.isSpringEnabled()) {
      log.error("spring context doesn't support to get ComponentName of " + seamComponent);
      final Named named = seamComponent.getClass().getAnnotation(Named.class);
      if (named != null) {
        return named.value();
      } else {
        return null;
      }
    }
    return Component.getComponentName(seamComponent.getClass());
  }

  */
/**
   * Removes this object from any context by name.
   *
   * @param name
   *          The name of a seam component.
   *//*

  public static final void removeInstance(final String name) {
    if (SFContextUtils.isSpringEnabled()) {
      SFContextUtils.removeInstance(name);
      return;
    }
    Contexts.removeFromAllContexts(name);
  }

  */
/**
   * Removes this object from the given context by name.
   *
   * @param scope
   *          scope of component.
   * @param name
   *          component name.
   *//*

  public static final void removeInstance(final ScopeType scope, final String name) {
    if (SFContextUtils.isSpringEnabled()) {
      SFScopeType scopeType = null;
      if (scope == ScopeType.SESSION) {
        scopeType = SFScopeType.SESSION;
      } else if (scope == ScopeType.EVENT) {
        scopeType = SFScopeType.EVENT;
      } else {
        scopeType = null;
      }
      SFContextUtils.removeInstance(scopeType, name);
      return;
    }
    Context context = null;

    switch (scope) {
    case APPLICATION:
      if (Contexts.isApplicationContextActive()) {
        context = Contexts.getApplicationContext();
      }
      break;
    case BUSINESS_PROCESS:
      if (Contexts.isBusinessProcessContextActive()) {
        context = Contexts.getBusinessProcessContext();
      }
      break;
    case CONVERSATION:
      if (Contexts.isConversationContextActive()) {
        context = Contexts.getConversationContext();
      }
      break;
    case EVENT:
      if (Contexts.isEventContextActive()) {
        context = Contexts.getEventContext();
      }
      break;
    case METHOD:
      if (Contexts.isMethodContextActive()) {
        context = Contexts.getMethodContext();
      }
      break;
    case PAGE:
      if (Contexts.isPageContextActive()) {
        context = Contexts.getPageContext();
      }
      break;
    case SESSION:
      if (Contexts.isSessionContextActive()) {
        context = Contexts.getSessionContext();
      }
      break;
    case STATELESS:
      break;
    case UNSPECIFIED:
      break;
    }

    if (context != null) {
      context.remove(name);
    }
  }

  */
/**
   * returns the conversation id parameter name.
   *
   * @return parameter name.
   *//*

  public static final String getConversationIdParam() {
    return Manager.instance().getConversationIdParameter();
  }

  */
/**
   * Returns the current conversation id.
   *
   * @return The conversationid.
   *//*

  public static final String getConversationId() {
    final Conversation conversation = Conversation.instance();
    if (!conversation.isNested() || conversation.isLongRunning()) {
      return conversation.getId();
    }
    return conversation.getParentId();
  }

  */
/**
   * Starts up a seam request.
   *
   * @param context
   *          The servlet context.
   * @param hreq
   *          The current http request.
   *//*

  public static final void beginRequest(final ServletContext context, final HttpServletRequest hreq) {
    Lifecycle.beginRequest(context, hreq.getSession(), hreq);
  }

  */
/**
   * Sets the jsf phase on to the seam lifecycle.
   *
   * @param id
   *          The phase that should be used.
   *//*

  public static final void setPhaseId(final PhaseId id) {
    if (SFContextUtils.isSpringEnabled()) {
      log.warn("spring context doesn't support setPhaseId");
      return;
    }
    Lifecycle.setPhaseId(id);
  }

  */
/**
   * Starts up a seam context.
   *//*

  public static final void beginCall() {
    if (SFContextUtils.isSpringEnabled()) {
      SFContextUtils.beginLifeCycleIfInactive();

      // PLT-46075
      if (!Contexts.isApplicationContextActive()) {
        Lifecycle.beginCall();
      }
    } else {
      Lifecycle.beginCall();
    }

    HibernateUtils.bindOpenSession4Seam();
  }

  */
/**
   * Ends a seam context.
   *//*

  public static final void endCall() {
    HibernateUtils.unbindOpenSession4Seam();

    if (SFContextUtils.isSpringEnabled()) {
      SFContextUtils.endLifeCycle();
      // PLT-46075
      if (Contexts.isApplicationContextActive()) {
        Lifecycle.endCall();
      }
    } else {
      Lifecycle.endCall();
    }
  }

  */
/**
   * Set variable into the specified context.
   *
   * @param scope
   *          the context scope to set the var.
   * @param name
   *          the name of the var.
   * @param value
   *          the value of the var.
   *//*

  public static final void setContextVariable(final ScopeType scope, final String name, final Object value) {
    if (SFContextUtils.isSpringEnabled()) {
      final SFScopeType sfScope = getSFScopeType(scope);
      SFContextUtils.setContextVariable(sfScope, name, value);
      return;
    }
    switch (scope) {
    case EVENT:
      Contexts.getEventContext().set(name, value);
      break;
    case METHOD:
      Contexts.getMethodContext().set(name, value);
      break;
    case PAGE:
      Contexts.getPageContext().set(name, value);
      break;
    case BUSINESS_PROCESS:
      Contexts.getBusinessProcessContext().set(name, value);
      break;
    case CONVERSATION:
      Contexts.getConversationContext().set(name, value);
      break;
    case SESSION:
      Contexts.getSessionContext().set(name, value);
      break;
    case APPLICATION:
      break;
    case STATELESS:
      break;
    case UNSPECIFIED:
      break;

    }

  }

  */
/**
   * Returns whether the seam context is alive.
   *
   * @return a boolean value that indicates whether the seam context is
   *         available.
   *//*

  public static boolean isApplicationContextActive() {
    if (SFContextUtils.isSpringEnabled()) {
      return SFContextUtils.isLifeCycleActive();
    }
    return Contexts.isApplicationContextActive();
  }

  */
/**
   * This method will raise a seam event with a String parameter.
   *
   * @param eventName
   *          the name of the event to be observed.
   * @param param
   *          the parameter to be passed to the observer.
   *//*

  public static final void raiseEvent(final String eventName, final String param) {
    if (Events.exists()) {
      Events.instance().raiseEvent(eventName, param);
    }
  }

  */
/**
   * This method will raise a seam event with an Object parameter.
   *
   * @param eventName
   *          the name of the event to be observed.
   * @param param
   *          the variable list of parameter to be passed to the observer.
   *
   *//*

  public static final void raiseEvent(final String eventName, final Object... param) {
    if (Events.exists()) {
      Events.instance().raiseEvent(eventName, param);
    }
  }

  */
/**
   * This is a helper method to start the 'seam' lifecycle. If first checks
   * whether seam lifecycle has already started, if so, then it does nothing and
   * returns false. Otherwise, it will start a new seam lifecycle and return
   * true, thus informing the caller that a new lifecycle has been initiated. If
   * a lifecycle is created it will set the named object into the session scope
   * once. The most common case is the set the parambean into the session.
   *
   * @param name
   *          Name of object to bind to session on env creation.
   * @param params
   *          object to be injected into the seam lifecycle(session scope) if
   *          the lifecycle needs to be started.
   * @return false : if a seam lifecycle is already active, thus, no lifecycle
   *         is initiated. true : if there is no active seam lifecycle AND thus,
   *         a new lifecycle is initiated.
   *//*

  public static boolean beginLifeCycleIfInactive(final String name, final Object params) {
    if (SFContextUtils.isSpringEnabled()) {
      SFContextUtils.beginLifeCycleIfInactive(name, params);

      // PLT-46075
      if (!Contexts.isApplicationContextActive()) {
        Lifecycle.beginCall();
      }
    }
    final boolean isApplicationContextAlive = SeamEnvUtils.isApplicationContextActive();
    if (!isApplicationContextAlive) {
      SeamEnvUtils.beginCall();
      Contexts.getSessionContext().set(name, params);
    }
    return !isApplicationContextAlive;
  }

  */
/**
   * This is a helper method to end the seam life cycle.
   *
   * @param isLocallyInitiated
   *          whether the caller was the entity that initated the seam life
   *          cycle.
   *//*

  public static void endLifeCycleIfInitiatedLocally(final boolean isLocallyInitiated) {
    if (SFContextUtils.isSpringEnabled()) {
      SFContextUtils.endLifeCycleIfInitiatedLocally(isLocallyInitiated);
      // PLT-46075
      if (Contexts.isApplicationContextActive() && isLocallyInitiated) {
        Lifecycle.endCall();
      }
      return;
    }
    if (isLocallyInitiated && SeamEnvUtils.isApplicationContextActive()) {
      SeamEnvUtils.endCall();
    }
  }

  */
/**
   * Starts a seam lifecyle if you have a httpservletrequest object.
   *
   * @see #beginLifeCycleIfInactive(String, Object) Must call the matching
   *      endLifeCycleIfInitiatedLocally with a HttpServletRequest
   * @param request
   *          The HttpServletRequest.
   * @return false : if a seam lifecycle is already active, thus, no lifecycle
   *         is initiated. true : if there is no active seam lifecycle AND thus,
   *         a new lifecycle is initiated.
   *//*

  public static boolean beginLifeCycleIfInactive(final HttpServletRequest request) {
    if (SFContextUtils.isSpringEnabled()) {
      final boolean alive = SFContextUtils.isLifeCycleActive();
      if (!alive) {
        SFScopeHolder.bindHttpContext(request);
        SFContextUtils.beginLifeCycleIfInactive();

        // PLT-46075
        if (!Contexts.isApplicationContextActive()) {
          Lifecycle.beginCall();
        }
      }
      return !alive;
    }

    final boolean isApplicationContextAlive = SeamEnvUtils.isApplicationContextActive();
    if (!isApplicationContextAlive) {
      Lifecycle.setServletRequest(request);
      Lifecycle.beginRequest(request.getSession().getServletContext(), request.getSession(), request);
      Manager.instance().restoreConversation(request.getParameterMap());
      Lifecycle.resumeConversation(request.getSession());
      Manager.instance().handleConversationPropagation(request.getParameterMap());
    }
    return !isApplicationContextAlive;
  }

  */
/**
   * This is a helper method to end the seam life cycle.
   *
   * @param isLocallyInitiated
   *          whether the caller was the entity that initiated the seam life
   *          cycle.
   * @param request
   *          The httpservlet request.
   *//*

  public static void endLifeCycleIfInitiatedLocally(final boolean isLocallyInitiated, //
      final HttpServletRequest request) {
    if (SFContextUtils.isSpringEnabled()) {
      SFContextUtils.endLifeCycleIfInitiatedLocally(isLocallyInitiated);
      // PLT-46075
      if (Contexts.isApplicationContextActive() && isLocallyInitiated) {
        Lifecycle.endCall();
      }
      return;
    }

    if (isLocallyInitiated && SeamEnvUtils.isApplicationContextActive()) {
      final HttpSession session = request.getSession(false);
      if (null != session) {
        Manager.instance().endRequest(ContextAdaptor.getSession(session));
        Lifecycle.endRequest(session);
      } else {
        Lifecycle.endRequest();
      }
    }
  }

  private static SFScopeType getSFScopeType(final ScopeType scope) {
    if (scope == ScopeType.SESSION) {
      return SFScopeType.SESSION;
    } else if (scope == ScopeType.EVENT) {
      return SFScopeType.EVENT;
    } else {
      return SFScopeType.EVENT;
    }
  }
}
*/
