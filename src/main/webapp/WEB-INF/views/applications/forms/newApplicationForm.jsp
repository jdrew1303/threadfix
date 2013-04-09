<%@ include file="/common/taglibs.jsp"%>

<div class="modal-header">
	<button type="button" class="close" data-dismiss="modal"
		aria-hidden="true">X</button>
	<h4 id="myModalLabel">New Application</h4>
</div>
<spring:url value="/organizations/{orgId}/modalAddApp" var="saveUrl">
	<spring:param name="orgId" value="${ organization.id }"/>
</spring:url>
	<form:form style="margin-bottom:0px;" id="myAppForm${ organization.id }" modelAttribute="application" method="post" autocomplete="off" action="${fn:escapeXml(saveUrl)}">
	<div class="modal-body">
		<table>
			<tr class="left-align">
				<td style="padding:5px;">Name</td> 
				<td style="padding:5px;">
					<form:input style="margin-bottom:0px;" id="nameInput" path="name" cssClass="focus" size="50" maxlength="60" />
				  	<form:errors path="name" cssClass="errors" />
				</td>
			</tr>
			<tr class="left-align">
				<td style="padding:5px;">URL</td>
				<td style="padding:5px;">
					<form:input style="margin-bottom:0px;" id="urlInput" path="url" size="50" maxlength="255" />
				  	<form:errors path="url" cssClass="errors" />
			  	</td>
			</tr>
			<tr class="left-align">
				<td style="padding:5px;">Team</td>
				<td style="padding:5px;"><c:out value="${ organization.name }"/></td>
			</tr>
			<tr class="left-align">
				<td style="padding:5px;">Criticality</td>
				<td style="padding:5px;">
					<form:select style="margin-bottom:0px;" id="criticalityId" path="applicationCriticality.id">
						<form:options items="${applicationCriticalityList}" itemValue="id" itemLabel="name"/>
					</form:select>
					<form:errors path="applicationCriticality.id" cssClass="errors" />
				</td>
			</tr>
		</table>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
		<a id="submitAppModal${ organization.id }" class="btn btn-primary" onclick="javascript:submitAjaxModal('<c:out value="${saveUrl }"/>','#myAppForm${ organization.id }', '#formDiv${ organization.id }', '#teamTable', '#myAppModal${ organization.id }', '#teamInfoDiv${ organization.id }');return false;">Add Application</a>
	</div>
</form:form>
<script>
$("#myAppForm<c:out value='organization.id'/>").keypress(function(e){
    if (e.which == 13){
        $("#submitAppModal<c:out value='organization.id'/>").click();
    }
});
</script>
