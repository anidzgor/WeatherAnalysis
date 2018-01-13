<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="pl.spring.FusionCharts" %>
<!DOCTYPE html>
<html>
<head>
    <title>Aplikacja Spring</title>
    <script src="../../js/fusioncharts.js"></script>
    <script src="../../js/fusioncharts.charts.js"></script>
    <script src="../../js/fusioncharts.theme.fint.js"></script>
</head>
<body>
<div id="chart"></div>
<%
    FusionCharts lineChart = new FusionCharts(
            "msline",// chartType
            "chart1",// chartId
            "900","350",// chartWidth, chartHeight
            "chart",// chartContainer
            "jsonurl",// dataFormat
            "../../datas/data.json"
    );

%>
<%=lineChart.render()%>

</body>
</html>