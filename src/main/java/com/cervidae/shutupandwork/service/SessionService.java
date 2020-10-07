package com.cervidae.shutupandwork.service;

import com.cervidae.shutupandwork.pojo.Session;
import com.cervidae.shutupandwork.pojo.User;
import com.cervidae.shutupandwork.util.Constants;
import com.cervidae.shutupandwork.util.ICache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SessionService implements IService {

    final ICache<String, Session> iCache;

    final UserService userService;

    @Autowired
    public SessionService(ICache<String, Session> iCache, UserService userService) {
        this.iCache = iCache;
        this.userService = userService;
    }

    private void validateID(String sessionID) {
        // validate sessionID
        if (!sessionID.matches(Constants.sessionIDRegex)) {
            throw new IllegalArgumentException("sessionID must be a 6 digit number");
        }
    }
    /**
     * Join the specified session. If not exist, create a session
     * @param user the user
     * @param sessionID ID of the session
     * @return the session
     */
    public Session join(User user, String sessionID) {
        validateID(sessionID);
        Session session;
        if (iCache.contains(sessionID)) {
            session = iCache.select(sessionID);
            if (session.getStatus() == Session.Status.SUCCESS || session.getStatus() == Session.Status.FAIL) {
                session.reset();
            } else if (session.getStatus() != Session.Status.WAITING) {
                throw new IllegalArgumentException("session is not in WAITING state");
            }
            session.addUser(user);
        } else {
            session = new Session(user, sessionID);
            iCache.insert(sessionID, session);
        }
        return session;
    }

    /**
     * Reset a session to WAITING state.
     * It must be in SUCCESS or FAIL state, but WAITING is tolerated (no change inflicted)
     * @param sessionID ID of the session
     * @return the session
     */
    public Session reset(String sessionID) {
        Session session = getSessionNotNull(sessionID);
        if (session.getStatus() == Session.Status.SUCCESS || session.getStatus() == Session.Status.FAIL) {
            session.reset();
        } else if (session.getStatus() != Session.Status.WAITING) {
            throw new IllegalArgumentException("session is not in SUCCESS/FAIL state");
        }
        return session;
    }

    public void leave(String username, String sessionID) {
        validateID(sessionID);
        Session session = getSession(sessionID);
        if (session.getUserList().containsKey(username)) {
            User user = userService.getByUsername(username);
            if (session.getStatus()== Session.Status.ACTIVE) {
                session.fail(user);
            } else {
                session.removeUser(user);
            }
        }
    }

    /**
     * Get a specific session with sessionID, nullable
     * @param sessionID ID of the session
     * @return the session
     */
    public Session getSession(String sessionID) {
        return iCache.select(sessionID);
    }

    /**
     * Get a specific session with sessionID, not nullable (will throw exception)
     * @param sessionID ID of the session
     * @return the session
     */
    public Session getSessionNotNull(String sessionID) {
        if (!iCache.contains(sessionID)) {
            throw new IllegalArgumentException("cannot find specified session");
        }
        return iCache.select(sessionID);
    }

    /**
     * Start the session (Pessimistic lock, see Session)
     * @param sessionID ID of the session
     * @param target the target of the session
     */
    public Session start(String sessionID, long target) {
        validateID(sessionID);
        return getSessionNotNull(sessionID).start(target);
    }

    /**
     * Mark the session as succeed (Pessimistic lock, see Session)
     * Pessimistic lock: since this function need only be called EXACTLY ONCE
     * @param sessionID ID of the session
     */
    public Session success(String sessionID) {
        validateID(sessionID);
        return getSessionNotNull(sessionID).success();
    }

    /**
     * Mark the session as failed (Pessimistic lock, see Session)
     * Pessimistic lock: since this function need only be called EXACTLY ONCE
     * @param sessionID ID of the session
     * @param username user to blame
     */
    public Session fail(String sessionID, String username) {
        validateID(sessionID);
        User user = userService.getByUsername(username);
        return getSessionNotNull(sessionID).fail(user);
    }
}
