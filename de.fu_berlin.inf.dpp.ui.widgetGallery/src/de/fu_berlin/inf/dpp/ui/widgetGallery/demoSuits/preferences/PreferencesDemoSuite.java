package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.preferences;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;

@DemoSuite({ GeneralPageDemo.class, AdvancedPageDemo.class,
    CommunicationPageDemo.class, FeedbackPageDemo.class, NetworkPageDemo.class,
    PersonalizationPageDemo.class })
@Demo
public class PreferencesDemoSuite extends AbstractDemo {

    @Override
    public void createDemo(Composite composite) {
        // nothing to do
    }

}
