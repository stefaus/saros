package de.fu_berlin.inf.dpp.stf.test.stf.menubar;

import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.ALICE;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.BOB;
import static de.fu_berlin.inf.dpp.stf.client.tester.SarosTester.CARL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.junit.BeforeClass;
import org.junit.Test;

import de.fu_berlin.inf.dpp.stf.client.StfTestCase;
import de.fu_berlin.inf.dpp.stf.client.util.Constants;
import de.fu_berlin.inf.dpp.stf.client.util.Util;
import de.fu_berlin.inf.dpp.stf.shared.Constants.TypeOfCreateProject;

public class MenuSarosByAliceBobCarlTest extends StfTestCase {

    @BeforeClass
    public static void runBeforeClass() throws RemoteException,
        InterruptedException {
        initTesters(ALICE, BOB, CARL);
        setUpWorkbench();
        setUpSaros();
        Util.setUpSessionWithAJavaProjectAndAClass(ALICE, BOB);
    }

    @Test
    public void inviteUsersInSession() throws RemoteException,
        InterruptedException {
        assertFalse(CARL.superBot().views().sarosView().isInSession());
        Util.inviteBuddies(Constants.PROJECT1, TypeOfCreateProject.NEW_PROJECT,
            ALICE, CARL);
        assertTrue(CARL.superBot().views().sarosView().isInSession());
    }
}