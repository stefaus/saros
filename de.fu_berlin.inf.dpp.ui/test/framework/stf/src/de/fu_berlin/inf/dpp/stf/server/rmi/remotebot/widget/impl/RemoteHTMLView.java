package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.ag_se.browser.html.ISelector;
import de.fu_berlin.inf.ag_se.browser.html.ISelector.IdSelector;
import de.fu_berlin.inf.dpp.stf.server.HTMLSTFRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.bot.BotPreferences;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLButton;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLView;
import de.fu_berlin.inf.dpp.ui.pages.AbstractBrowserPage;
import de.fu_berlin.inf.dpp.ui.pages.MainPage;

public class RemoteHTMLView extends HTMLSTFRemoteObject implements
    IRemoteHTMLView {

    /**
     * Defines how to identify the HTML representation of a given
     * {@link de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLView.View
     * View}. As multiple conceptual views may be displayed in the same browser
     * widget, this "key" has two parts: The pageClass to find the correct
     * browser widget, and the id of the corresponding DOM entry.
     */
    private static class Key {
        private Class<? extends AbstractBrowserPage> pageClass;
        private String id;

        Key(Class<? extends AbstractBrowserPage> pageClass, String id) {
            this.pageClass = pageClass;
            this.id = id;
        }
    }

    /**
     * This map make the connection between conceptual
     * {@linkplain de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLView.View
     * views} and their technical realization. It allows UI designers, for
     * example, to move a simple form (such as
     * {@link de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteHTMLView.View#ADD_CONTACT
     * ADD_CONTACT} to a separate browser widget without breaking the tests by
     * just changing the pageClass part of the corresponding Key.
     */
    private static final Map<View, Key> map = new HashMap<View, Key>();
    static {
        map.put(View.MAIN_VIEW, new Key(MainPage.class, "root"));
        map.put(View.ADD_CONTACT, new Key(MainPage.class, "add-contact-form"));
    }

    private static final RemoteHTMLView INSTANCE = new RemoteHTMLView();
    private static final Logger log = Logger.getLogger(RemoteHTMLView.class);

    public static RemoteHTMLView getInstance() {
        return INSTANCE;
    }

    private View view;
    private RemoteHTMLButton button;

    public RemoteHTMLView() {
        button = RemoteHTMLButton.getInstance();
    }

    @Override
    public boolean hasButton(String id) throws RemoteException {
        return exists(new IdSelector(id));
    }

    @Override
    public IRemoteHTMLButton button(String id) throws RemoteException {
        IdSelector selector = new IdSelector(id);
        button.setSelector(selector);
        ensureExistence(selector);
        return button;
    }

    @Override
    public boolean isOpen() {
        String id = map.get(view).id;
        return exists(new IdSelector(id));
    }

    public void selectView(View view) {
        this.view = view;
        this.button.setBrowser(getBrowser());
    }

    private IJQueryBrowser getBrowser() {
        return getBrowserManager().getBrowser(map.get(view).pageClass);
    }

    private void ensureExistence(ISelector selector) throws RemoteException {
        if (!exists(selector))
            throw new RemoteException("did not find HTML element "
                + selector.getStatement());
    }

    private boolean exists(ISelector selector) {
        boolean foundIt = false;
        try {
            foundIt = getBrowser().containsElement(selector).get(
                BotPreferences.SHORT_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error("could not determine whether element exists", e);
        } catch (TimeoutException e) {
            log.error("could not determine whether element exists", e);
        }
        return foundIt;
    }

}
