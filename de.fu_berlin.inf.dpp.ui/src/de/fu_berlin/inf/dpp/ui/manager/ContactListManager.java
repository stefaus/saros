package de.fu_berlin.inf.dpp.ui.manager;

import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListRenderer;
import de.fu_berlin.inf.dpp.ui.model.Account;
import de.fu_berlin.inf.dpp.ui.model.Contact;
import de.fu_berlin.inf.dpp.ui.model.ContactList;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manages a contact list and delegates its rendering.
 * It monitors the XMPP connection and reflects changes in the
 * managed contact list.
 */
public class ContactListManager {

    private static final Logger LOG = Logger
        .getLogger(ContactListBrowserFunctions.class);

    private final XMPPConnectionService connectionService;

    private ContactListRenderer contactListRenderer;

    private ContactList contactList = ContactList.EMPTY_CONTACT_LIST;

    private boolean connected = false;

    /**
     * This class may be managed by pico container.
     * The parameter should then be injected automatically.
     *
     * @param connectionService
     */
    public ContactListManager(XMPPConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    //TODO this is just a very minimal implementation
    private final IConnectionListener connectionListener = new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection,
            ConnectionState state) {
            switch (state) {
            case CONNECTED:
                LOG.debug("StateListener: connected!!");
                synchronized (ContactListManager.this) {
                    JID jid = new JID(connection.getUser());
                    contactList = new ContactList(
                        new Account(jid.getName(), jid.getDomain()),
                        createListOfContacts(connection.getRoster()));
                    connected = true;
                    //TODO merge render functions, remove null check
                    if (contactListRenderer != null) {
                        contactListRenderer.renderIsConnected(true);
                        contactListRenderer.renderContactList(contactList);
                    }
                }
                break;
            case CONNECTING:
                LOG.debug("StateListener: connecting ");
                synchronized (ContactListManager.this) {
                    //TODO merge render functions, remove null check
                    if (contactListRenderer != null) {
                        contactListRenderer.renderIsConnecting();
                    }
                }
                break;
            case DISCONNECTING:
                LOG.debug("StateListener: disconnecting");
                synchronized (ContactListManager.this) {
                    //TODO merge render functions, remove null check
                    if (contactListRenderer != null) {
                        contactListRenderer.renderIsDisconnecting();
                    }
                }
                break;
            case ERROR:
                //TODO better error handling
                LOG.error("StateListener: error");
                break;
            case NOT_CONNECTED:
                LOG.debug("StateListener: not connected");
                synchronized (ContactListManager.this) {
                    connected = false;
                    contactList = ContactList.EMPTY_CONTACT_LIST;
                    //TODO merge render functions, remove null check
                    if (contactListRenderer != null) {
                        contactListRenderer.renderContactList(contactList);
                        contactListRenderer.renderIsConnected(false);
                    }
                }
                break;
            }
        }
    };

    /**
     * Adds the roster entries as contact object to a new list.
     *
     * @param roster the roster
     * @return the list containing the roster entries as contacts
     */
    private List<Contact> createListOfContacts(Roster roster) {
        List<Contact> res = new ArrayList<Contact>(roster.getEntries().size());
        for (RosterEntry contactEntry : roster.getEntries()) {
            Presence presence = roster.getPresence(contactEntry.getUser());
            //TODO consider all type, e.g. subscription pending
            boolean isOnline = presence.getType() == Presence.Type.available;
            res.add(new Contact(contactEntry.getUser(), isOnline));
        }
        return res;
    }

    /**
     * This methods gets called when the browser has changed. It re-renders the
     * current state.
     *
     * @param renderer the contact list renderer with the current browser
     */
    public synchronized void setContactListRenderer(
        ContactListRenderer renderer) {
        contactListRenderer = renderer;
        connectionService.addListener(connectionListener);
        //TODO merge render functions
        contactListRenderer.renderIsConnected(connected);
        contactListRenderer.renderContactList(contactList);
    }

    /**
     * Removes the contact list render and the connection listener.
     * This method should be called when the browser is disposed.
     */
    public synchronized void removeContactListRenderer() {
        //TODO no null assignment
        contactListRenderer = null;
        connectionService.removeListener(connectionListener);
    }
}