<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page isELIgnored="false"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="css/gxt-all.css"/>
    <link rel="stylesheet" type="text/css" href="css/pub-array-style.css"/>

    <title><c:out value="${experimentMetadata.experimentName}" escapeXml="false"/></title>
    
    <!--                                           -->
    <!-- This script loads your compiled module.   -->
    <!-- If you add any GWT meta tags, they must   -->
    <!-- be added before this line.                -->
    <!--                                           -->
    <script type="text/javascript" language="javascript" src="queryapplication/queryapplication.nocache.js"></script>
  </head>

  <!--                                           -->
  <!-- The body can have arbitrary html, or      -->
  <!-- you can leave the body empty if you want  -->
  <!-- to create a completely dynamic UI.        -->
  <!--                                           -->
  <body class="page">

    <!-- OPTIONAL: include this if you want history support -->
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>

    <h1 class="page"><c:out value="${experimentMetadata.experimentName}" escapeXml="false"/></h1>

    <div class="pageSection">
        <c:out value="${experimentMetadata.experimentDescription}" escapeXml="false"/>
    </div>

    <h2 class="page">Step 1: Select Output Columns (Optional)</h2>
    <div class="pageSection">
        Select any columns that you would like to appear
        in your search results in Step 2. You can use the
        &quot;Filter&quot; and &quot;Toggle Select All&quot;
        controls to save time in making your selections.
        <div id="termsOfInterestTableContainer"></div>
    </div>

    <h2 class="page">Step 2: Gene Search</h2>

    <div class="pageSection">
        In this section you can search for genes by clicking the
        &quot;Add Filter&quot; button and selecting the criteria that you want
        to search on.
        <div id="queryFilterContainer"></div>
        <div id="queryResultsTableContainer"></div>
    </div>

    <h2 class="page">Step 3: View Gene Expression Details</h2>
    <div class="pageSection">
        Any values that you select in the results table from Step 2 will be
        plotted in this section. You can sort the arrays according to
        design criteria and then group arrays based on those same criteria.
        <div id="probeDetailsContainer"></div>
        <div id="perGeneImageContainer"></div>
    </div>
  </body>
</html>
