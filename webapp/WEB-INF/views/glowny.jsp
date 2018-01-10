<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="pl.spring.FusionCharts" %>
<!DOCTYPE html>
<html>
<head>
    <title>Aplikacja Spring</title>
    <script src="../../js/fusioncharts.js"></script>
    <script src="../../js/fusioncharts.charts.js"></script>
    <script src="../../js/fusioncharts.theme.fint.js"></script>
    <%--<script type="text/javascript">--%>
        <%--FusionCharts.ready(function() {--%>
            <%--var fusioncharts = new FusionCharts({--%>
                <%--type: 'column2d',--%>
                <%--renderAt: 'chart-container',--%>
                <%--width: '500',--%>
                <%--height: '300',--%>
                <%--dataFormat: 'jsonurl',--%>
                <%--dataSource: '../../datas/data.json'--%>
            <%--});--%>
            <%--fusioncharts.render();--%>
        <%--});--%>
    <%--</script>--%>
</head>
<body>

<div id="chart-container"></div>

<div id="chart"></div>
<%
    FusionCharts lineChart = new FusionCharts(
            "line",// chartType
            "chart1",// chartId
            "600","350",// chartWidth, chartHeight
            "chart",// chartContainer
            "jsonurl",// dataFormat
            "../../datas/data2.json"
    );

%>
<%=lineChart.render()%>

</body>
</html>