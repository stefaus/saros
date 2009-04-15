package de.fu_berlin.inf.dpp.activities;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import de.fu_berlin.inf.dpp.net.internal.ActivitiesPacketExtension;
import de.fu_berlin.inf.dpp.util.xstream.UrlEncodingStringConverter;

// TODO [MR] Add some information what needs to be done to add a new activity.
public abstract class AbstractActivity implements IActivity {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(AbstractActivity.class
        .getName());

    @XStreamAsAttribute
    @XStreamConverter(UrlEncodingStringConverter.class)
    protected final String source;

    public AbstractActivity(String source) {
        if (source == null)
            throw new IllegalArgumentException("Source cannot be null");
        this.source = source;
    }

    public String getSource() {
        return this.source;
    }

    @Override
    public int hashCode() {
        return (source == null) ? 0 : source.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof AbstractActivity))
            return false;

        AbstractActivity other = (AbstractActivity) obj;
        return ObjectUtils.equals(this.source, other.source);
    }

    public String toXML() {
        return ActivitiesPacketExtension.getXStream().toXML(this);
    }
}
