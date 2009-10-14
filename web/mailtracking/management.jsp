<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/fenix-renderers.tld" prefix="fr"%>

<%@ page import="module.mailtracking.domain.CorrespondenceType" %>

<bean:define id="correspondenceType" name="correspondenceType" />


<script type="text/javascript" src="<%= request.getContextPath() + "/javaScript/dataTables/media/js/jquery.dataTables.js"%>"></script>

<style type="text/css" title="currentStyle">
	@import "<%= request.getContextPath() + "/javaScript/dataTables/media/css/demo_table.css" %>";
</style>


<bean:define id="mailTrackingId" name="mailTracking" property="externalId" />


<h2><bean:message key="title.mail.tracking,application" bundle="MAIL_TRACKING_RESOURCES" /></h2>

<h3><bean:message key="label.last.correspondence.entries" bundle="MAIL_TRACKING_RESOURCES" /> </h3>

<fr:form id="search.parameters.simple.form" action="/mailtracking.do?method=prepare">
	<fr:edit id="search.parameters.simple.bean" name="searchParametersBean" visible="false" />
</fr:form>

<p>
<logic:equal name="correspondenceType" value="<%= CorrespondenceType.SENT.name() %>">
	<bean:message key="label.mail.tracking.sent.correspondence" bundle="MAIL_TRACKING_RESOURCES" />
	<html:link page="<%= "/mailtracking.do?method=prepare&amp;correspondenceType=" + CorrespondenceType.RECEIVED.name() + "&amp;mailTrackingId=" + mailTrackingId %>"><bean:message key="label.mail.tracking.received.correspondence" bundle="MAIL_TRACKING_RESOURCES" /></html:link>
</logic:equal>
<logic:equal name="correspondenceType" value="<%=  CorrespondenceType.RECEIVED.name() %>">
	<html:link page="<%= "/mailtracking.do?method=prepare&amp;correspondenceType=" + CorrespondenceType.SENT.name() + "&amp;mailTrackingId=" + mailTrackingId %>"><bean:message key="label.mail.tracking.sent.correspondence" bundle="MAIL_TRACKING_RESOURCES" /></html:link>
	<bean:message key="label.mail.tracking.received.correspondence" bundle="MAIL_TRACKING_RESOURCES" />
</logic:equal> 
</p>

<p>
	<html:link page="<%= "/mailtracking.do?method=prepareCreateNewEntry&amp;correspondenceType=" + correspondenceType + "&amp;mailTrackingId=" + mailTrackingId %>">
		<bean:message key="label.mail.tracking.create.new.entry" bundle="MAIL_TRACKING_RESOURCES"/>
	</html:link>
</p>
<logic:empty name="searchEntries">
	<bean:message key="message.searched.correspondence.entries.empty" bundle="MAIL_TRACKING_RESOURCES" /> 
</logic:empty>


<logic:notEmpty name="searchEntries">
<fr:view name="searchEntries" schema="<%= CorrespondenceType.SENT.name().equals(correspondenceType) ? "module.mailtracking.correspondence.sent.entries.view" : "module.mailtracking.correspondence.received.entries.view" %>" >
	<fr:layout name="ajax-tabular">
		<fr:property name="classes" value="table display-sent"/>
		<fr:property name="ajaxSourceUrl" value="<%= "/mailtracking.do?method=ajaxFilterCorrespondence&mailTrackingId=" + mailTrackingId + "&correspondenceType=" + correspondenceType %>" />

		<fr:property name="linkFormat(edit)" value="<%= "/mailtracking.do?method=prepareEditEntry&amp;correspondenceType=" + correspondenceType + "&amp;mailTrackingId=" + mailTrackingId + "&amp;entryId=${externalId}" %>"/>
		<fr:property name="bundle(edit)" value="MAIL_TRACKING_RESOURCES"/>
		<fr:property name="key(edit)" value="link.edit"/>
		<fr:property name="order(edit)" value="2" />
		<fr:property name="visibleIf(edit)" value="userAbleToEdit" />

		<fr:property name="linkFormat(delete)" value="<%= "/mailtracking.do?method=deleteEntry&amp;correspondenceType=" + correspondenceType + "&amp;mailTrackingId=" + mailTrackingId + "&amp;entryId=${externalId}" %>"/>
		<fr:property name="bundle(delete)" value="MAIL_TRACKING_RESOURCES"/>
		<fr:property name="key(delete)" value="link.delete"/>
		<fr:property name="confirmationKey(delete)" value="message.confirm.entry.delete" />
		<fr:property name="confirmationTitleKey(delete)" value="title.confirm.entry.delete" />
		<fr:property name="order(delete)" value="3" />
		<fr:property name="visibleIf(edit)" value="userAbleToDelete" />
		
		<fr:property name="extraParameter(correspondenceType)" value="<%=  (String) correspondenceType %>" />
	</fr:layout>
</fr:view>

</logic:notEmpty>

