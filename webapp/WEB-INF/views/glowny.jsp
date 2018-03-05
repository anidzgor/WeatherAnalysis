<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="pl.spring.FusionCharts" %>
<!DOCTYPE html>
<html>
<head>
    <title>Aplikacja Spring</title>
    <script src="../../js/fusioncharts.js"></script>
    <script src="../../js/fusioncharts.charts.js"></script>
    <script src="../../js/fusioncharts.theme.fint.js"></script>

    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
</head>
<body>

<div class="row">
    <div class="text-center" id="chart"></div>
</div>
<%
    FusionCharts lineChart = new FusionCharts(
            "msline",// chartType
            "chart1",// chartId
            "900","350",// chartWidth, chartHeight
            "chart",// chartContainer
            "jsonurl",// dataFormat
            "../../datas/diff.json"
    );

%>
<%=lineChart.render()%>

</body>
</html>