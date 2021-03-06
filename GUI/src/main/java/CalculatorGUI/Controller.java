package CalculatorGUI;

import expressionEvaluation.Function;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextField;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller of the Calculator UI, responsible for graphing the function and completing the associated logic with
 * extrema/POI.
 * Calculator has capability to graph
 *
 * - Polynomial Expressions (Removable Discontinuities supported)
 * - Trigonometric Expressions (Asymptotes are not currently supported)
 * - Exponential Expressions
 * - Natural Logarithmic Expressions (Base A is currently not graphable due to issues with the Differentiation)
 */

public class Controller implements Initializable {

    @FXML
    public TextField functionField;

    @FXML
    public LineChart graph;

    @FXML
    public TextField lowerBoundField;

    @FXML
    public TextField upperBoundField;

    @FXML
    public TextField incrementField;

    @FXML
    public NumberAxis xAxis;

    @FXML
    public NumberAxis yAxis;

    private double boundA, boundB;

    private double previousX, currentX, nextX;

    private Function func;

    private XYChart.Series<Double, Double> series, extrema, inflection, discontinuities;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        graph.setAnimated(false);
        lowerBoundField.setText("-10");
        upperBoundField.setText("10");
    }

    @FXML
    public void zoomInAction(final ActionEvent event) {
        xAxis.setUpperBound(xAxis.getUpperBound() - 5);
        yAxis.setUpperBound(yAxis.getUpperBound() - 5);
        xAxis.setLowerBound(xAxis.getLowerBound() + 5);
        yAxis.setLowerBound(yAxis.getLowerBound() + 5);
    }

    @FXML
    public void zoomOutAction(final ActionEvent event) {
        xAxis.setUpperBound(xAxis.getUpperBound() + 5);
        yAxis.setUpperBound(yAxis.getUpperBound() + 5);
        xAxis.setLowerBound(xAxis.getLowerBound() - 5);
        yAxis.setLowerBound(yAxis.getLowerBound() - 5);
    }

    @FXML
    public void moveUpAction(final ActionEvent event) {
        yAxis.setUpperBound(yAxis.getUpperBound() + 5);
        yAxis.setLowerBound(yAxis.getLowerBound() + 5);
    }

    @FXML
    public void moveDownAction(final ActionEvent event) {
        yAxis.setUpperBound(yAxis.getUpperBound() - 5);
        yAxis.setLowerBound(yAxis.getLowerBound() - 5);
    }

    @FXML
    @SuppressWarnings("Duplicates")//Suppress duplicate warning given by intializing the variables in F(x)/F'(x)/F''(x)
//    Graphs parent function

    public void handleGraphButtonAction(final ActionEvent event) {

        graph.getData().clear();
        String funcField = functionField.getText();
        String boundAField = lowerBoundField.getText();
        String boundBField = upperBoundField.getText();
        String incrementNum = incrementField.getText();
        double increment;

        if (!incrementNum.isBlank())
            increment = Double.parseDouble(incrementNum);
        else
            increment = .01;

        if (!boundAField.isBlank())
            boundA = Double.parseDouble(boundAField);
        else
            boundA = -10;

        if (!boundBField.isBlank())
            boundB = Double.parseDouble(boundBField);
        else
            boundB = 10;

        if (!funcField.isBlank()) {

            series = new XYChart.Series<>();
            extrema = new XYChart.Series<>();
            inflection = new XYChart.Series<>();
            func = new Function(funcField);

            //Adds points on graph
            for (double i = boundA + increment; i < boundB; i += increment) {

                previousX = i - increment;
                currentX = i;
                nextX = i + increment;

//                checks to see if each plotted point is an extrema via parent function extrema; identifies if currentX is either
////                greater than prev/nextX or less than prev/nextX. Then uses the bisection method from prevX to nextX to
////                more accurately identify where an extrema could occur

                if (func.evaluate(currentX) > func.evaluate(previousX) && func.evaluate(currentX) > func.evaluate(nextX)
                        || func.evaluate(currentX) < func.evaluate(previousX) && func.evaluate(currentX) < func.evaluate(nextX)) {
                    Double bisectionZero = func.bisectionMethodDerivative(previousX, nextX);
                    if (bisectionZero != null) {
                        extrema.getData().add(new XYChart.Data<>(bisectionZero, func.evaluate(bisectionZero)));
                        System.out.println("Extrema at f(" + i + ") = " + func.evaluate(bisectionZero));
                    }
                }

//                checks to see if each plotted point is a POI via first derivative extrema; identifies if currentX is either
//                greater than prev/nextX or less than prev/nextX. Then uses the bisection method from prevX to nextX to
//                more accurately identify where a POI could occur.

                if (func.derivative(currentX) > func.derivative(previousX) && func.derivative(currentX) > func.derivative(nextX)
                        || func.derivative(currentX) < func.derivative(previousX) && func.derivative(currentX) < func.derivative(nextX)) {
//                    System.out.println(" previousX " + previousX + "," + func.derivative(previousX) + " currentX " + currentX + "," + func.evaluate(currentX) + " nextX " + nextX + "," + func.derivative(nextX));
                    Double bisectionZero = func.bisectionMethodSecondDerivative(previousX, nextX);
                    if (bisectionZero != null) {
                        inflection.getData().add(new XYChart.Data<>(bisectionZero, func.evaluate(bisectionZero)));
                        System.out.println("POI at f(" + i + ") = " + func.evaluate(bisectionZero));

                    }
                }

//                charts points of graph (NaN required to graph functions with naturally bounded areas ex: log10x, ln(x)

                if (!Double.isNaN(func.evaluate(i)))
                    series.getData().add(new XYChart.Data<>(i, func.evaluate(i)));

            }

            series.setName(func.toString());
            extrema.setName("extrema");
            inflection.setName("inflectionPoints");

            graph.getData().add(series);
            graph.getData().add(extrema);
            graph.getData().add(inflection);

            //If function has denominator, check to see if removable discontinuities exist.

            if (funcField.contains("/")) {
                String denominator = funcField.substring(funcField.indexOf("/") + 1);
                discontinuities = new XYChart.Series<>();

                Function temp = new Function(denominator);

                for (double i = boundA + increment; i < boundB; i += increment) {
                    previousX = i - increment;
                    currentX = i;
                    nextX = i + increment;

                    if ((temp.evaluate(previousX) > 0 && temp.evaluate(nextX) < 0)
                            || (temp.evaluate(previousX) < 0 && temp.evaluate(nextX) > 0)) {
                        double zero = temp.newtonsMethod(currentX);
                        if (func.isRemovableDiscontinuity(zero))
                            discontinuities.getData().add(new XYChart.Data<>(zero, func.evaluate(zero + Math.pow(10, -7))));
                        //Add 10e-7 in order to actually graph the point
                    }
                }
                discontinuities.setName("discontinuities");
                graph.getData().add(discontinuities);
            }


        }
    }

    @FXML
    @SuppressWarnings("Duplicates")//Suppress duplicate warning given by intializing the variables in F(x)/F'(x)/F''(x)
//    Graphs First Derivative

    public void handleDerivative1ButtonAction(final ActionEvent event) {

        graph.getData().clear();
        String funcField = functionField.getText();
        String boundAField = lowerBoundField.getText();
        String boundBField = upperBoundField.getText();
        String incrementNum = incrementField.getText();
        double increment;

        if (!incrementNum.isBlank())
            increment = Double.parseDouble(incrementNum);
        else
            increment = .01;

        if (!boundAField.isBlank())
            boundA = Double.parseDouble(boundAField);
        else
            boundA = -10;

        if (!boundBField.isBlank())
            boundB = Double.parseDouble(boundBField);
        else
            boundB = 10;

        if (!funcField.isBlank()) {

            series = new XYChart.Series<>();
            extrema = new XYChart.Series<>();
            inflection = new XYChart.Series<>();
            func = new Function(funcField);

            //Adds points on graph
            for (double i = boundA + increment; i < boundB; i += increment) {

                previousX = i - increment;
                currentX = i;
                nextX = i + increment;

////                checks to see if each plotted point is an extrema.
//
//                if (func.derivative(currentX) > func.derivative(previousX) && func.derivative(currentX) > func.derivative(nextX)
//                        || func.derivative(currentX) < func.derivative(previousX) && func.derivative(currentX) < func.derivative(nextX)) {
//                    Double bisectionZero = func.bisectionMethodSecondDerivative(previousX, nextX);
//                    if (bisectionZero != null) {
//                        extrema.getData().add(new XYChart.Data<>(i, func.derivative(i)));
//                    }
//                }
//
////                checks to see if each plotted point is a POI via first derivative extrema; identifies if currentX is either
////                greater than prev/nextX or less than prev/nextX. Then uses the bisection method from prevX to nextX to
////                more accurately identify where a POI could occur.
//
//                if (func.secondDerivative(currentX) > func.secondDerivative(previousX) && func.secondDerivative(currentX) > func.secondDerivative(nextX)
//                        || func.secondDerivative(currentX) < func.secondDerivative(previousX) && func.secondDerivative(currentX) < func.secondDerivative(nextX)) {
//                    Double bisectionZero = func.bisectionMethodThirdDerivative(previousX, nextX);
//                    if(bisectionZero != null)
//                        inflection.getData().add(new XYChart.Data<>(i, func.derivative(i)));
//                }
//
                if (!Double.isNaN(func.evaluate(i)))
                    series.getData().add(new XYChart.Data<>(i, func.derivative(i)));

            }

            series.setName(func.toString());
            extrema.setName("extrema");
            inflection.setName("inflection points");
            graph.getData().add(series);
            graph.getData().add(extrema);
            graph.getData().add(inflection);

            //If function has denominator, check to see if removable discontinuities exist.
            if (funcField.contains("/")) {
                String denominator = funcField.substring(funcField.indexOf("/") + 1);
                discontinuities = new XYChart.Series<>();

                Function temp = new Function(denominator);

                for (double i = boundA + increment; i < boundB; i += increment) {
                    previousX = i - increment;
                    currentX = i;
                    nextX = i + increment;

                    if ((temp.evaluate(previousX) > 0 && temp.evaluate(nextX) < 0)
                            || (temp.evaluate(previousX) < 0 && temp.evaluate(nextX) > 0)) {
                        double zero = temp.newtonsMethod(currentX);
                        if (func.isRemovableDiscontinuity(zero))
                            discontinuities.getData().add(new XYChart.Data<>(zero, func.derivative(zero + Math.pow(10, -7))));
                    }
                }
                discontinuities.setName("discontinuities");
                graph.getData().add(discontinuities);
            }


        }
    }

    @FXML
    @SuppressWarnings("Duplicates")//Suppress duplicate warning given by intializing the variables in F(x)/F'(x)/F''(x)
//    Graphs Second Derivative

    public void handleDerivative2ButtonAction(final ActionEvent event) {

        graph.getData().clear();
        String funcField = functionField.getText();
        String boundAField = lowerBoundField.getText();
        String boundBField = upperBoundField.getText();
        String incrementNum = incrementField.getText();
        double increment;

        if (!incrementNum.isBlank())
            increment = Double.parseDouble(incrementNum);
        else
            increment = .01;

        if (!boundAField.isBlank())
            boundA = Double.parseDouble(boundAField);
        else
            boundA = -10;

        if (!boundBField.isBlank())
            boundB = Double.parseDouble(boundBField);
        else
            boundB = 10;

        if (!funcField.isBlank()) {
            series = new XYChart.Series<>();
            extrema = new XYChart.Series<>();
            inflection = new XYChart.Series<>();
            func = new Function(funcField);

            //Adds points on graph
            for (double i = boundA + increment; i < boundB; i += increment) {
                previousX = i - increment;
                currentX = i;
                nextX = i + increment;
                if (!Double.isNaN(func.evaluate(i))) {
                    series.getData().add(new XYChart.Data<>(i, func.secondDerivative(i)));
                }

//                Computes extrema/POI of second derivative, too much floating point inaccuracy to function across all
//                equations.

////                checks to see if each plotted point is an extrema.
//                if (func.secondDerivative(currentX) > func.secondDerivative(previousX) && func.secondDerivative(currentX) > func.secondDerivative(nextX)
//                        || func.secondDerivative(currentX) < func.secondDerivative(previousX) && func.secondDerivative(currentX) < func.secondDerivative(nextX)) {
//                    Double bisectionZero = func.bisectionMethodThirdDerivative(previousX, nextX);
//                    if(bisectionZero != null)
//                        extrema.getData().add(new XYChart.Data<>(i, func.secondDerivative(i)));
//                }
////
//////                checks to see if each plotted point is a POI via following derivative extrema
////
////                if ((func.thirdDerivative(currentX) - func.thirdDerivative(previousX) > tolerance
////                        && func.thirdDerivative(currentX) - func.thirdDerivative(nextX) > tolerance)
////                        || (func.thirdDerivative(currentX) - func.thirdDerivative(previousX) < -tolerance
////                        && func.thirdDerivative(currentX) - func.thirdDerivative(nextX) < -tolerance))
//
//                if (func.thirdDerivative(currentX) > func.thirdDerivative(previousX) && func.thirdDerivative(currentX) > func.thirdDerivative(nextX)
//                        || func.thirdDerivative(currentX) < func.thirdDerivative(previousX) && func.thirdDerivative(currentX) < func.thirdDerivative(nextX)) {
//                    Double bisectionZero = func.bisectionMethodFourthDerivative(previousX, nextX);
//                    if(bisectionZero != null)
//                        inflection.getData().add(new XYChart.Data<>(i, func.secondDerivative(i)));
//                }

////                charts points of graph (NaN required to graph functions with naturally bounded areas ex: log10x, ln(x)
            }

            series.setName(func.toString());
            extrema.setName("extrema");
            inflection.setName("inflection points");
            graph.getData().add(series);
            graph.getData().add(extrema);
            graph.getData().add(inflection);

            // If the function has a component of division, check for removable discontinuities
            if (funcField.contains("/")) {
                String denominator = funcField.substring(funcField.indexOf("/") + 1);
                discontinuities = new XYChart.Series<>();

                Function temp = new Function(denominator);

                for (double i = boundA + .01; i < boundB; i += .01) {
                    previousX = i - .01;
                    currentX = i;
                    nextX = i + .01;

                    if ((temp.evaluate(previousX) > 0 && temp.evaluate(nextX) < 0)
                            || (temp.evaluate(previousX) < 0 && temp.evaluate(nextX) > 0)) {
                        double zero = temp.newtonsMethod(currentX);
                        if (func.isRemovableDiscontinuity(zero)) {
                            discontinuities.getData().add(new XYChart.Data<>(zero, func.secondDerivative(zero + Math.pow(10, -7))));
                        }
                    }
                }
                discontinuities.setName("discontinuities");
                graph.getData().add(discontinuities);
            }


        }
    }

    @FXML
//    Clears Graph
    public void handleClearButtonAction(final ActionEvent event) {
        functionField.clear();
        graph.getData().clear();
        xAxis.setUpperBound(20);
        yAxis.setUpperBound(20);
        xAxis.setLowerBound(-20);
        yAxis.setLowerBound(-20);
    }

}
