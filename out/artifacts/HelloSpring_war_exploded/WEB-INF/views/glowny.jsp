<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="pl.spring.FusionCharts" %>
<!DOCTYPE html>
<html>
<head>
    <title>Aplikacja Spring</title>
    <script src="../../js/fusioncharts.js"></script>
    <script src="../../js/fusioncharts.charts.js"></script>
    <script src="../../js/fusioncharts.theme.fint.js"></script>
    <link href="<c:url value="/resources/style.css" />" rel="stylesheet">

    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
</head>
<body>

<%@include file="header.html" %>

<div class="container-fluid">
    <div class="row">

        <div class="col-sm-4">
            <div class="box text-center">
                <p class="title">Wybierz datę</p>
                <p>
                <form:select path="lists">
                    <form:options items="${lists}" />
                </form:select>
                </p>
                <button type="button" class="btn btn-default">Generuj</button>
            </div>
        </div>

        <div class="col-sm-4">
            <div class="text-center"><img src="../../2018-03-09_05.png" alt="Map" height="510" width="650" /></div>
        </div>

        <div class="col-sm-4"></div>

    </div>
</div>



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