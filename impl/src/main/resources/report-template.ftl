<!DOCTYPE html>
<html>
<head><title>${coverageTitle} TCK Coverage Report</title>
    <style type="text/css">
        body {
        font-family: verdana, arial, sans-serif;
        font-size: 11px; }
        .code {
        float: left;
        font-weight: bold;
        width: 50px;
        margin-top: 0px;
        height: 100%; }
        a.external, a.external:visited, a.external:hover {
        color: #0000ff;
        font-size: 9px;
        font-style: normal;
        padding-left: 2px;
        margin-left: 6px;
        margin-right: 6px;
        padding-right: 2px; }
        .results {
        margin-left: 50px; }
        .description {
        margin-top: 2px;
        margin-bottom: 2px; }
        .sectionHeader {
        border-bottom: 1px solid #cccccc;
        margin-top: 8px;
        font-weight: bold; }
        .packageName {
        color: #999999;
        font-size: 9px;
        font-weight: bold; }
        .groupName {
        color: #0000FF;
        font-size: 12px;
        font-weight: bold; }
        .embeddedImage {
        margin: 6px;
        border: 1px solid black;
        float: right; }
        .coverage {
        clear: both; }
        .noCoverage {
        margin-top: 2px;
        margin-bottom: 2px;
        font-weight: bold;
        font-style: italic;
        color: #ff0000; }
        .coverageHeader {
        font-weight: bold;
        text-decoration: underline;
        margin-top: 2px;
        margin-bottom: 2px; }
        .coverageMethod {
        font-style: italic; }
        .highlight {
        background-color: #ffff00; }
        .literal {
        font-family: courier new; }
        .implied {
        color: #fff;
        font-weight: bold;
        background-color: #000; }
        .group {
        border-top: 1px solid #000000;
        border-bottom: 1px solid #000000;
        padding-bottom: 1px;
        margin-bottom: 2px;
        min-height: 36px;
        background-color: #eeeeee; }
        .groupAssertions {
        padding-bottom: 1px;
        margin-top: 8px;
        margin-left: 50px;
        margin-bottom: 2px;
        min-height: 36px;
        background-color: ffffff; }
        .COVERED {
        border-top: 1px solid #488c41;
        border-bottom: 1px solid #488c41;
        padding-bottom: 1px;
        margin-bottom: 2px;
        min-height: 36px;
        background-color: #ddffdd; }
        .UNCOVERED {
        border-top: 1px solid #ab2020;
        border-bottom: 1px solid #ab2020;
        padding-bottom: 1px;
        margin-bottom: 2px;
        min-height: 36px;
        background-color: #ffdddd; }
        .UNIMPLEMENTED {
        border-top: 1px solid #ff9900;
        border-bottom: 1px solid #ff9900;
        padding-bottom: 1px;
        margin-bottom: 2px;
        min-height: 36px;
        background-color: #ffcc33; }
        .UNTESTABLE {
        padding-bottom: 16px;
        margin-bottom: 2px;
        border-top: 1px solid #317ba6;
        border-bottom: 1px solid #317ba6;
        min-height: 36px;
        background-color: #80d1ff; }
        .stickynote {
        position: absolute;
        left: 16px;
        margin-top: 2em; }
        tr:nth-child(even) {background-color: #f7f7f7}
    </style>
    <script src="Chart.js"></script>
</head>
<body><h1>${coverageTitle} TCK Coverage</h1>

<h2>${version}</h2>

<h3>Contents</h3>

<div><a href="#chapterSummary">Chapter Summary</a></div>
<div><a href="#sectionSummary">Section Summary</a></div>
<div><a href="#coverageDetail">Coverage Detail</a></div>
<div><a href="#unmatched">Unmatched Tests</a></div>
<div><a href="#unversioned">Unversioned Tests</a></div>
<div><a href="#groupsummary">Test Group Summary</a></div>
<h3 id="coverageDistribution">Coverage Distribution</h3>

<canvas id="coverageChart" width="500" height="400"></canvas>

<h3 id="chapterSummary">Chapter Summary</h3>
<table width="100%">
    <tr style="background-color:#dddddd">
        <th align="left">Chapter</th>
        <th>Assertions</th>
        <th>Testable</th>
        <th>Total Tested</th>
        <th>Total Tests</th>
        <th>Tested<br/> (problematic)</th>
        <th>Tested<br/> (working)</th>
        <th>Coverage %</th>
    </tr>
    <#list chapterItems as it>
    <tr>
        <td>
            <a href="#${it.section.id}">${it.section.id} ${it.section.title}</a>
        </td>
        
        <td align="center">${it.assertions}</td>
        <td align="center">${it.testable}</td>
        <td align="center">${it.tested}</td>
        <td align="center">${it.testCount}</td>
        <td align="center">${it.unimplemented}</td>
        <td align="center">${it.implemented}</td>

        <#if (it.assertions > 0)>
            <#if (it.coverage < failThreshold) >
                <td align="center" style="background-color:#ffaaaa;">
            <#elseif (it.coverage < passThreshold) >
                <td align="center" style="background-color:#ffffaa;">
            <#else>
                <td align="center" style="background-color:#aaffaa;">
            </#if>
            ${it.displayCoverage()} </td>
        <#else>
            <td align="center">${it.displayCoverage()} </td>
        </#if>
                    
    </tr>
    </#list>
    <tr style="font-weight: bold;background-color:#dddddd">
        <td>Total</td>
        <td align="center">${totalAssertions}</td>
        <td align="center">${totalTestable}</td>
        <td align="center">${totalTested}</td>
        <td align="center">${totalTests}</td>
        <td align="center">${totalUnimplemented}</td>
        <td align="center">${totalImplemented}</td>
        <td align="center">${totalCoveragePercent}</td>
    </tr>
</table>
<h3 id="sectionSummary">Section Summary</h3>
<table width="100%">
    <tr style="background-color:#dddddd">
        <th align="left">Section</th>
        <th>Assertions</th>
        <th>Testable</th>
        <th>Total Tested</th>
        <th>Tested<br/> (problematic)</th>
        <th>Tested<br/> (working)</th>
        <th>Coverage %</th>
    </tr>

    <#list sectionItems as it>
    <tr>
        <#if it.section.level == 3>
            <td style="padding-left:32px">
                <a href="#${it.section.id}">${it.section.id} ${it.section.title}</a>
            </td>
        <#elseif it.section.level == 2>
            <td style="padding-left:16px">
                <a href="#${it.section.id}">${it.section.id} ${it.section.title}</a>
            </td>
        <#else>
            <td style="padding-left:0px">
                <a href="#${it.section.id}">${it.section.id} ${it.section.title}</a>
            </td>
        </#if>
        <td align="center">${it.assertions}</td>
        <td align="center">${it.testable}</td>
        <td align="center">${it.tested}</td>
        <td align="center">${it.unimplemented}</td>
        <td align="center">${it.implemented}</td>
        
        <#if (it.assertions > 0)>
            <#if (it.coverage < failThreshold) >
                <td align="center" style="background-color:#ffaaaa;">
            <#elseif (it.coverage < passThreshold) >
                <td align="center" style="background-color:#ffffaa;">
            <#else>
                <td align="center" style="background-color:#aaffaa;">
            </#if>        
            ${it.displayCoverage()} </td>
        <#else>
          <td align="center">${it.displayCoverage()} </td>
        </#if>
                    
    </tr>
    </#list>
</table>

<h3 id="coverageDetail">Coverage Detail</h3>
<table>
    <tr>
        <th style="background-color:#dddddd">Colour Key</th>
    </tr>
    <tr>
        <td style="background-color:#ddffdd;text-align:center">Assertion is covered</td>
    </tr>
    <tr>
        <td style="background-color:#ffdddd;text-align:center">Assertion is not covered</td>
    </tr>
    <tr>
        <td style="background-color:#ffcc33;text-align:center">Assertion test is unimplemented</td>
    </tr>
    <tr>
        <td style="background-color:#80d1ff;text-align:center">Assertion is untestable</td>
    </tr>
</table>

<#list items?keys as it>
<div style="visibility:hidden" id="${it.id}"></div>

<div id="${it.title}">
    <h4 class="sectionHeader" id="${it.id}">Section ${it.id} - ${it.title} <sup>[${it.originalId}]</sup></h4>

    <#list items?values[it_index] as val>
    
    <#if val.assertions?has_content>
    <div class="group">
        <p class="description">${val.text}</p>

        <div class="groupAssertions">
            <#list val.assertions as assert>
            <div class="${assert.status}">

                <#if assert.implied>
                <span class="implied">The following assertion is not made explicitly by the spec, however it is implied</span>
                </#if>

                <span class="code">${assert.id})</span>

                <div class="results">
                    <p class="description">${assert.text}</p>

                    <div class="coverage">

                        <#if assert.status == "COVERED">
                        <p class="coverageHeader">Coverage</p>
                        </#if>

                        <#if assert.status == "UNCOVERED">
                        <p class="noCoverage">No tests exist for this assertion</p>
                        </#if>

                        <#list assert.tests as test>
                        <div class="packageName">${test.packageName}</div>
                        <div class="coverageMethod">
                            <strong>${test.testName}</strong>
                            <#list test.links as link>
                                <a class="external" target="_blank" href="${link.url}">${link.provider?lower_case}</a>
                            </#list>
                        </div> 
                        <div class="description">Test archive name: ${test.archiveName}</div>
                        </#list>
                    </div>
                </div>
            </div>
            </#list>
        </div>
    </div>
   </#if>
   <#if val.id??>

    <div class="${val.status}">

    <#if val.note??>
    <img title="${val.note}" alt="${val.note}" src="images/stickynote.png" class="stickynote" width="20" height="20"/>
    </#if>

    <#if val.implied>
    <span class="implied">The following assertion is not made explicitly by the spec, however it is implied</span>
    </#if>

    <span class="code">${val.id})</span>

    <div class="results">
        <p class="description">${val.text}</p>

        <div class="coverage">

            <#if val.status == "COVERED">
            <p class="coverageHeader">Coverage</p>
            </#if>

            <#if val.status == "UNCOVERED">
            <p class="noCoverage">No tests exist for this assertion</p>
            </#if>

            <#list val.tests as test>
            <div class="packageName">${test.packageName}</div>
            <div class="coverageMethod">
                <strong>${test.testName}</strong>
                <#list test.links as link>
                <a class="external" target="_blank" href="${link.url}">${link.provider?lower_case}</a>
                </#list>
            </div>
            <div class="description">Test archive name: ${test.archiveName}</div>
        </#list>
        </div>
    </div>
    </div>
    </#if>
    </#list>

</div>
</#list>

<#if unversioned?has_content>
<h3 id="unversioned">Unversioned tests</h3>

<p>The following ${unversioned?size} test classes either do not have a version specified, or the version is unrecognized:</p>
<table border="1" cellspacing="0" cellpadding="0">
    <tr>
        <th>Test Class</th>
        <th>Version</th>
    </tr>
    <#list unversioned?keys as it>
    <tr>
    <td>${it}</td>
    <td>${unversioned[it]}</td>
    </tr>
    </#list>
</table>
</#if>

<#if unmatched?has_content>
<h3 id="unmatched">Unmatched tests</h3>

<p>The following ${unmatched?size} tests do not match any known assertions:</p>
<table border="1" cellspacing="0" cellpadding="0">
    <tr>
        <th>Section</th>
        <th>Assertion</th>
        <th>Test Class</th>
        <th>Test Method</th>
    </tr>
    <#list unmatched as it>
    <tr>
        <td>${it.section}</td>
        <td>${it.assertion}</td>

        <td>
            <div class="packageName">${it.packageName}</div>
            ${it.className}
        </td>

        <td>${it.methodName}()</td>
    </tr>
    </#list>

</table>
</#if>

<#if sumTestGroups?has_content>
<h3 id="groupsummary">Highlighted test groups</h3>
<table border="1" cellspacing="0" cellpadding="0">
    <tr>
        <th>Test Class</th>
        <th>Test method</th>
    </tr>

    <#list sumTestGroups?keys as it>
        <tr>
            <td colspan="2">
                <div class="groupName">${it} (${sumTestGroups[it]?size})</div>
            </td>
        </tr>
    <#list sumTestGroups[it] as val>
        <tr>
            <td>
                <div class="packageName">${val.packageName}</div>
                ${val.className}
            </td>
            <td>${val.methodName}</td>
        </tr>
    </#list>
        </#list>

</table>
        </#if>
</table>
<script>
    var data = {
                labels:
                [
                <#list chapterItems as item>"${item.section.id}. ${item.section.title}",</#list>
                ],
                datasets: [
                {
                    label: "Chapters",
                    fillColor: "rgba(0,180,0,0.5)",
                    strokeColor: "rgba(220,220,220,0.8)",
                    highlightFill: "rgba(0,220,0,0.75)",
                    highlightStroke: "rgba(220,220,220,0.8)",

                    data:
                    [
                     <#list chapterItems as item> ${item.coverageForGraph()},</#list>
                    ]
                },
               ]
    };

    var options = {
                barValueSpacing : 0,
                scaleFontSize : 10
    };

    window.onload = function(){
		var ctx = document.getElementById("coverageChart").getContext("2d");
		window.myBar = new Chart(ctx).Bar(data, options);
	}

</script>
</body>
</html>
