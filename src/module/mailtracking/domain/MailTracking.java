package module.mailtracking.domain;

import module.mailtracking.domain.CorrespondenceEntry.CorrespondenceEntryBean;
import module.organization.domain.Unit;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.MyOrg;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;
import myorg.domain.groups.NamedGroup;
import myorg.domain.groups.People;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import pt.ist.fenixWebFramework.services.Service;
import pt.utl.ist.fenix.tools.util.StringNormalizer;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

public class MailTracking extends MailTracking_Base {

    private MailTracking() {
	super();
	setMyOrg(MyOrg.getInstance());
    }

    private MailTracking(Unit unit) {
	this();
	init(unit);
    }

    private void init(Unit unit) {
	checkParameters(unit);

	this.setUnit(unit);
	this.setName(unit.getPartyName());
	this.setActive(Boolean.TRUE);
    }

    private void checkParameters(Unit unit) {
	if (unit == null)
	    throw new DomainException("error.mail.tracking.unit.cannot.be.empty");
    }

    @Service
    public static MailTracking createMailTracking(Unit unit) {
	if (unit.hasMailTracking())
	    throw new DomainException("error.mail.tracking.exists.for.unit");

	MailTracking mailTracking = new MailTracking(unit);

	People people = new NamedGroup("operators");
	people.addUsers(UserView.getCurrentUser());

	mailTracking.setOperatorsGroup(people);
	return mailTracking;
    }

    @Service
    public void removeOperator(User user) {
	((People) this.getOperatorsGroup()).removeUsers(user);
    }

    @Service
    public void addOperator(User user) {
	((People) this.getOperatorsGroup()).addUsers(user);
    }

    public boolean isUserOperator(User user) {
	return this.getOperatorsGroup().isMember(user);
    }

    @Service
    public void edit(MailTrackingBean bean) {
	this.setName(bean.getName());
	this.setActive(bean.getActive());
    }

    public java.util.List<CorrespondenceEntry> getActiveEntries(final CorrespondenceType type) {
	java.util.List<CorrespondenceEntry> activeEntries = new java.util.ArrayList<CorrespondenceEntry>();

	CollectionUtils.select(this.getEntries(), new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		CorrespondenceEntry entry = (CorrespondenceEntry) arg0;
		return CorrespondenceEntryState.ACTIVE.equals(entry.getState()) && (type == null || type.equals(entry.getType()));
	    }

	}, activeEntries);

	return activeEntries;
    }

    public java.util.List<CorrespondenceEntry> find(CorrespondenceType type, final String sender, final String recipient,
	    final String subject, final DateTime whenReceivedBegin, final DateTime whenReceivedEnd) {
	java.util.List<CorrespondenceEntry> entries = new java.util.ArrayList<CorrespondenceEntry>();

	final String normalizedSender = StringNormalizer.normalize(sender);
	final String normalizedRecipient = StringNormalizer.normalize(recipient);
	final String normalizedSubject = StringNormalizer.normalize(subject);

	if (StringUtils.isEmpty(sender) && StringUtils.isEmpty(recipient) && whenReceivedBegin == null && whenReceivedEnd == null)
	    return entries;

	CollectionUtils.select(this.getActiveEntries(type), new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		CorrespondenceEntry entry = (CorrespondenceEntry) arg0;
		String normalizedEntrySender = StringNormalizer.normalize(entry.getSender());
		String normalizedEntryRecipient = StringNormalizer.normalize(entry.getRecipient());
		String normalizedEntrySubject = StringNormalizer.normalize(entry.getSubject());

		DateTime whenReceivedEntry = entry.getWhenReceived();

		return (StringUtils.isEmpty(sender) || normalizedEntrySender.indexOf(normalizedSender) > -1)
			&& (StringUtils.isEmpty(normalizedRecipient) || normalizedEntryRecipient.indexOf(normalizedRecipient) > -1)
			&& (StringUtils.isEmpty(normalizedSubject) || normalizedEntrySubject.indexOf(normalizedSubject) > -1)
			&& (whenReceivedBegin == null || !whenReceivedEntry.isBefore(whenReceivedBegin))
			&& (whenReceivedEnd == null || !whenReceivedEntry.isAfter(whenReceivedEnd));
	    }

	}, entries);

	return entries;
    }

    public java.util.List<CorrespondenceEntry> simpleSearch(CorrespondenceType type, final String key) {
	java.util.List<CorrespondenceEntry> entries = new java.util.ArrayList<CorrespondenceEntry>();

	if (StringUtils.isEmpty(key)) {
	    return entries;
	}

	final String normalizedKey = StringNormalizer.normalize(key);

	CollectionUtils.select(this.getActiveEntries(type), new Predicate() {

	    @Override
	    public boolean evaluate(Object arg0) {
		CorrespondenceEntry entry = (CorrespondenceEntry) arg0;
		String normalizedEntrySender = StringNormalizer.normalize(entry.getSender());
		String normalizedEntryRecipient = StringNormalizer.normalize(entry.getRecipient());
		String normalizedSubject = StringNormalizer.normalize(entry.getSubject());

		return (normalizedEntrySender.indexOf(normalizedKey) > -1 || normalizedEntryRecipient.indexOf(normalizedKey) > -1 || normalizedSubject
			.indexOf(normalizedKey) > -1);
	    }

	}, entries);

	return entries;
    }

    @Service
    public CorrespondenceEntry createNewEntry(CorrespondenceEntryBean bean, CorrespondenceType type, Document mainDocument)
	    throws Exception {
	CorrespondenceEntry entry = createNewEntry(bean.getSender(), bean.getRecipient(), bean.getSubject(), bean
		.getWhenReceived(), type);

	if (mainDocument != null) {
	    entry.addDocuments(mainDocument);
	}

	return entry;
    }

    protected CorrespondenceEntry createNewEntry(String sender, String recipient, String subject, DateTime whenReceived,
	    CorrespondenceType type) {
	return new CorrespondenceEntry(this, sender, recipient, subject, whenReceived, type);
    }

    public static class MailTrackingBean implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private MultiLanguageString name;
	private Boolean active;
	private MailTracking mailTracking;

	public MailTrackingBean(MailTracking mailTracking) {
	    this.mailTracking = mailTracking;
	    this.name = mailTracking.getName();
	    this.active = mailTracking.getActive();
	}

	public MultiLanguageString getName() {
	    return name;
	}

	public void setName(MultiLanguageString name) {
	    this.name = name;
	}

	public Boolean getActive() {
	    return active;
	}

	public void setActive(Boolean active) {
	    this.active = active;
	}

	public MailTracking getMailTracking() {
	    return mailTracking;
	}

	public void setMailTracking(MailTracking mailTracking) {
	    this.mailTracking = mailTracking;
	}

    }

    public MailTrackingBean createBean() {
	return new MailTrackingBean(this);
    }
}
