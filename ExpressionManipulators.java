package calculator.ast;

import calculator.interpreter.Environment;


import calculator.errors.EvaluationError;
import datastructures.interfaces.IDictionary;
import datastructures.interfaces.IList;
//import misc.exceptions.NotYetImplementedException;

/**
 * All of the static methods in this class are given the exact same parameters for
 * consistency. You can often ignore some of these parameters when implementing your
 * methods.
 *
 * Some of these methods should be recursive. You may want to consider using public-private
 * pairs in some cases.
 */
public class ExpressionManipulators {
    /**
     * Accepts an 'toDouble(inner)' AstNode and returns a new node containing the simplified version
     * of the 'inner' AstNode.
     *
     * Preconditions:
     *
     * - The 'node' parameter is an operation AstNode with the name 'toDouble'.
     * - The 'node' parameter has exactly one child: the AstNode to convert into a double.
     *
     * Postconditions:
     *
     * - Returns a number AstNode containing the computed double.
     *
     * For example, if this method receives the AstNode corresponding to
     * 'toDouble(3 + 4)', this method should return the AstNode corresponding
     * to '7'.
     *
     * @throws EvaluationError  if any of the expressions contains an undefined variable.
     * @throws EvaluationError  if any of the expressions uses an unknown operation.
     */
    public static AstNode handleToDouble(Environment env, AstNode node) {
        // To help you get started, we've implemented this method for you.
        // You should fill in the TODOs in the 'toDoubleHelper' method.
        return new AstNode(toDoubleHelper(env.getVariables(), node.getChildren().get(0)));
    }

    private static double toDoubleHelper(IDictionary<String, AstNode> variables, AstNode node) {
        // There are three types of nodes, so we have three cases.
        IList<AstNode> list = node.getChildren();
        if(!ifAllDefined(variables, node)) {
            throw new EvaluationError("input not defined");
        }
        
        if (node.isNumber()) {
            // TODO: your code here
            return node.getNumericValue();
        } else if (node.isVariable()) { 
            // TODO: your code here
//            if(!variables.containsKey(node.getName())) {
//                throw new EvaluationError("variable not defined");
//            }
            AstNode definition = variables.get(node.getName()); 
            return toDoubleHelper(variables, definition);
        } else {
            String name = node.getName();            
            // TODO: your code here
            // create an operation list, check if the operation is in the list
//            if(!variables.containsKey(name)) {
//                throw new EvaluationError("operation not defined");
//            }
            if(name == "+") {
                AstNode child1 = node.getChildren().get(0);
                AstNode child2 = node.getChildren().get(1);
                double num1 = toDoubleHelper(variables, child1);
                double num2 = toDoubleHelper(variables, child2);
                return num1+num2;
            }else if(name == "-") {
                AstNode child1 = node.getChildren().get(0);
                AstNode child2 = node.getChildren().get(1);
                double num1 = toDoubleHelper(variables, child1);
                double num2 = toDoubleHelper(variables, child2);
                return num1-num2;
            }else if(name == "*") {
                AstNode child1 = node.getChildren().get(0);
                AstNode child2 = node.getChildren().get(1);
                double num1 = toDoubleHelper(variables, child1);
                double num2 = toDoubleHelper(variables, child2);
                return num1*num2;
            }else if(name == "/") {
                AstNode child1 = node.getChildren().get(0);
                AstNode child2 = node.getChildren().get(1);
                double num1 = toDoubleHelper(variables, child1);
                double num2 = toDoubleHelper(variables, child2);
                return num1/num2;               
            }else if(name == "negate") {                
                    AstNode child1 = node.getChildren().get(0);
                    double num1 = toDoubleHelper(variables, child1);
                    return num1 * -1;
            }else if(name == "^") {
                AstNode base = node.getChildren().get(0);
                AstNode exp = node.getChildren().get(1);
                double base_num = toDoubleHelper(variables, base);
                double exp_num = toDoubleHelper(variables, exp);
                return Math.pow(base_num, exp_num);
            }else if(name == "sin") {
                AstNode child1 = node.getChildren().get(0);
                double num1 = toDoubleHelper(variables, child1);
                return Math.sin(Math.toRadians(num1));
            }else { // cosine
                AstNode child1 = node.getChildren().get(0);
                double num1 = toDoubleHelper(variables, child1);
                return Math.cos(Math.toRadians(num1));
            }
           
        }
    }

    /**
     * Accepts a 'simplify(inner)' AstNode and returns a new node containing the simplified version
     * of the 'inner' AstNode.
     *
     * Preconditions:
     *
     * - The 'node' parameter is an operation AstNode with the name 'simplify'.
     * - The 'node' parameter has exactly one child: the AstNode to simplify
     *
     * Postconditions:
     *
     * - Returns an AstNode containing the simplified inner parameter.
     *
     * For example, if we received the AstNode corresponding to the expression
     * "simplify(3 + 4)", you would return the AstNode corresponding to the
     * number "7".
     *
     * Note: there are many possible simplifications we could implement here,
     * but you are only required to implement a single one: constant folding.
     *
     * That is, whenever you see expressions of the form "NUM + NUM", or
     * "NUM - NUM", or "NUM * NUM", simplify them.
     */
    public static AstNode handleSimplify(Environment env, AstNode node) {
        // Try writing this one on your own!
        // Hint 1: Your code will likely be structured roughly similarly
        //         to your "handleToDouble" method
        // Hint 2: When you're implementing constant folding, you may want
        //         to call your "handleToDouble" method in some way

        // TODO: Your code here
        
        IDictionary<String, AstNode> variable = env.getVariables();
        String result = "";       
        IList<AstNode> list = node.getChildren();       
        if(node.isNumber()) {
            return handleToDouble(env, node);
        }
        else if(node.isVariable()) { // if the variable is defined
            if(variable.containsKey(node.getName())) {
                return handleToDouble(env,node);
            }else {// if the variable is not defined, leave it as it is
                result += node.getName();
                return new AstNode(result);
            }
        }else { // is an operation
            if(!variable.containsKey(node.getName())) {
                throw new EvaluationError("operation not defined");
            }
            if(list.size() == 2) {// two leaves
                if(variable.containsKey(list.get(0).getName()) && variable.containsKey(list.get(1).getName())) {
                    // if 2 leaves both defined
                    result += handleToDouble(env,node);
                }else { // not both defined, simplify left, simplify right, put together
                    result += handleSimplify(env, list.get(0)) + node.getName() + handleSimplify(env, list.get(1));
                }
                
            }else {//only one leaf, sin, cos, negate
                if(variable.containsKey(list.get(0).getName())) {
                    result += handleToDouble(env, node);
                }else {
                    result += node.getName() + list.get(0).getName();
                }               
            }
            return new AstNode(result);
            
        }
            
        
        
            
        
    }

    /**
     * Accepts a 'plot(exprToPlot, var, varMin, varMax, step)' AstNode and
     * generates the corresponding plot. Returns some arbitrary AstNode.
     *
     * Example 1:
     *
     * >>> plot(3 * x, x, 2, 5, 0.5)
     *
     * This method will receive the AstNode corresponding to 'plot(3 * x, x, 2, 5, 0.5)'.
     * Your 'handlePlot' method is then responsible for plotting the equation
     * "3 * x", varying "x" from 2 to 5 in increments of 0.5.
     *
     * In this case, this means you'll be plotting the following points:
     *
     * [(2, 6), (2.5, 7.5), (3, 9), (3.5, 10.5), (4, 12), (4.5, 13.5), (5, 15)]
     *
     * ---
     *
     * Another example: now, we're plotting the quadratic equation "a^2 + 4a + 4"
     * from -10 to 10 in 0.01 increments. In this case, "a" is our "x" variable.
     *
     * >>> c := 4
     * 4
     * >>> step := 0.01
     * 0.01
     * >>> plot(a^2 + c*a + a, a, -10, 10, step)
     *
     * ---
     *
     * @throws EvaluationError  if any of the expressions contains an undefined variable.
     * @throws EvaluationError  if varMin > varMax
     * @throws EvaluationError  if 'var' was already defined
     * @throws EvaluationError  if 'step' is zero or negative
     */
    
    
    private static boolean ifAllDefined(IDictionary<String, AstNode> variables, AstNode node) {
        IList<AstNode> list = node.getChildren();
        while(list.iterator().hasNext()) {// 
            if(!variables.containsKey(node.getName()) || !variables.containsKey(list.iterator().next().getName())) {
                return false;
            }
        }
        return true;
    }
    
    public static AstNode plot(Environment env, AstNode node) {
        // TODO: Your code here
        IDictionary<String, AstNode> variables = env.getVariables();
        IList<AstNode> child = node.getChildren(); //0:expr, 1: var, 2:min, 3:max,4:gap
        AstNode function = child.get(0);
        AstNode var = child.get(1);
        AstNode min = child.get(2);
        AstNode max = child.get(3);
        AstNode step = child.get(4);
        if(min.getNumericValue() > max.getNumericValue()) {
            throw new EvaluationError("varmin > varmax");
        }
        if(variables.containsKey(var.getName())) {
            throw new EvaluationError("variable was already defined");
        }
        if(step.getNumericValue() <= 0) {
            throw new EvaluationError("step must be positive");
        }
        variables.put(var.getName(), new AstNode(1));
        
        if(!ifAllDefined(variables, function)) { 
            throw new EvaluationError("expression contains an undefined variable");
        }
        variables.remove(var.getName());
        
        
        IList<Double> xValues = null; // don't how to initiate an empty list
        IList<Double> yValues = null;       
        for(double i = min.getNumericValue(); i < max.getNumericValue(); i += step.getNumericValue()) {
            xValues.add(i);
            variables.put(var.getName(), new AstNode(i));
            yValues.add(toDoubleHelper(variables, function));             
        }
        
        env.getImageDrawer().drawScatterPlot("plot", var.getName(), "output", xValues, yValues);
        
        // Note: every single function we add MUST return an
        // AST node that your "simplify" function is capable of handling.
        // However, your "simplify" function doesn't really know what to do
        // with "plot" functions (and what is the "plot" function supposed to
        // evaluate to anyways?) so we'll settle for just returning an
        // arbitrary number.
        //
        // When working on this method, you should uncomment the following line:
        //
        return new AstNode(1);
    }
}
