<%@ page import="java.util.*,org.eclipse.help.servlet.*,org.w3c.dom.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>

<%
	 String  ContentStr = WebappResources.getString("Content", request);
	 String  SearchStr = WebappResources.getString("SearchResults", request);
	 String  LinksStr = WebappResources.getString("Links", request);
	
	// Load the preferences
	String banner = null;
	String banner_height = "45";
	String help_home = null;
	
	ContentUtil content = new ContentUtil(application, request);
	Element prefsElement = content.loadPreferences();

	if (prefsElement != null){
		NodeList prefs = prefsElement.getElementsByTagName("pref");
		for (int i=0; i<prefs.getLength(); i++)
		{
			Element pref = (Element)prefs.item(i);
			String name = pref.getAttribute("name");
			if (name.equals("banner"))
				banner = pref.getAttribute("value");
			else if (name.equals("banner_height"))
				banner_height = pref.getAttribute("value");
			else if (name.equals("help_home"))
				help_home = pref.getAttribute("value");
		}
	}
	if (banner != null){
		if (banner.trim().length() == 0)
			banner = null;
		else
			banner = "content/help:" + banner;
	}
	
	if (help_home != null)
		help_home = "content/help:" + help_home;
	else
		help_home = "home.jsp";
		
		
			 
	// Paramters allowed:
	// tab = toc | search | links
	// toc
	// topic
	// searchWord
	// contextId
	// lang
	
	String query = "";
	if (request.getQueryString() != null && request.getQueryString().length() > 0)
		query = "?" + request.getQueryString();
	
	// url of MainFrame
	String srcMainFrame = help_home;
	if(request.getParameter("topic")!=null)
	{
		String topic = request.getParameter("topic");
		if (topic.startsWith("/"))
		{	
			topic = request.getContextPath() + "/content/help:" + topic;
		}
		srcMainFrame=topic;
	}
%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
-->
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Help</title>
	
	<script language="Javascript">
		// Global for the nav frame script
		var titleArray = new Array();
		titleArray["toc"] = "<%=ContentStr%>";
		titleArray["search"] = "<%=SearchStr%>";
		titleArray["links"] = "<%=LinksStr%>";
		</script>
		
	<script language="JavaScript" src="help.js"></script>
	
</head>

<%
	if (banner != null)
	{
%>
<frameset onload="onloadFrameset()" rows="<%=banner_height%>,35,*"  frameborder="0" framespacing="0" border="0" spacing="0">
	<frame name="BannerFrame" src='<%=banner%>'  marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=no>
	<frame name="SearchFrame" src='<%="search.jsp"+query%>'  marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=no>
	<frameset id="helpFrameset" cols="28%,*"  framespacing="0" border="0"  frameborder="0" spacing="0" resize=no scrolling=no>
		<frameset name="navFrameset" rows="27,*,26" marginwidth="0" marginheight="0" scrolling="no" frameborder="0">
			<frame name="NavToolbarFrame" src='<%="navToolbar.jsp"+query%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=yes>
			<frame name="NavFrame" src='blank.html' marginwidth="0" marginheight="0" scrolling="yes" frameborder="0" resize=yes>
			<frame name="TabsFrame" src='<%="tabs.jsp"+query%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=yes>
		</frameset>
		<frameset id="contentFrameset" rows="27,*", frameborder=0 framespacing=0 border=0>
			<frame name="ToolbarFrame" src='<%="toolbar.jsp"+query%>' marginwidth="0" marginheight="0"  frameborder="0" resize=yes scrolling=no>
			<frame name="MainFrame" src="<%=srcMainFrame%>" marginwidth="10" marginheight="10" scrolling="auto"  frameborder="0" resize="yes">
		</frameset>
	</frameset>
</frameset>
<%
	} else {
%>
<frameset onload="onloadFrameset()" rows="<%=banner_height%>,35,*"  frameborder="0" framespacing="0" border="0" spacing="0">
	<frame name="SearchFrame" src='<%="search.jsp"+query%>'  marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=no>
	<frameset id="helpFrameset" cols="28%,*"  framespacing="0" border="0"  frameborder="0" spacing="0" resize=no scrolling=no>
		<frameset name="navFrameset" rows="27,*,26" marginwidth="0" marginheight="0" scrolling="no" frameborder="0">
			<frame name="NavToolbarFrame" src='<%="navToolbar.jsp"+query%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=yes>
			<frame name="NavFrame" src='blank.html' marginwidth="0" marginheight="0" scrolling="yes" frameborder="0" resize=yes>
			<frame name="TabsFrame" src='<%="tabs.jsp"+query%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=yes>
		</frameset>
		<frameset id="contentFrameset" rows="27,*", frameborder=0 framespacing=0 border=0>
			<frame name="ToolbarFrame" src='<%="toolbar.jsp"+query%>' marginwidth="0" marginheight="0"  frameborder="0" resize=yes scrolling=no>
			<frame name="MainFrame" src="<%=srcMainFrame%>" marginwidth="10" marginheight="10" scrolling="auto"  frameborder="0" resize="yes">
		</frameset>
	</frameset>
</frameset>
<%
	}
%>

</html>

