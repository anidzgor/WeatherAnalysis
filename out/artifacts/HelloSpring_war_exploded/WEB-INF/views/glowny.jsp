<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="pl.spring.FusionCharts" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
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

<%@include file="header.html" %>

<div class="container-fluid">
    <div class="row">
        <label for="dessert">Wybierz datę</label>
        <select id="dessert">
            <option>16 marzec 2018</option>
            <option>16 marzec 2018</option>
            <option>16 marzec 2018</option>
        </select>
        <div class="text-center"><img src="../../2018-03-09_05.png" alt="Map" height="510" width="650" /></div>
    </div>
</div>

<select name="database1">
    <c:forEach items="${list}" var="listValue">
        <option value="${listValue}">
                ${listValue}
        </option>
    </c:forEach>
</select>



<%--<div class="row">--%>
    <%--<div class="text-center" id="chart"></div>--%>
<%--</div>--%>
<%--<%--%>
    <%--FusionCharts lineChart = new FusionCharts(--%>
            <%--"msline",// chartType--%>
            <%--"chart1",// chartId--%>
            <%--"900","350",// chartWidth, chartHeight--%>
            <%--"chart",// chartContainer--%>
            <%--"jsonurl",// dataFormat--%>
            <%--"../../datas/diff.json"--%>
    <%--);--%>

<%--%>--%>
<%--<%=lineChart.render()%>--%>

</body>
</html>