package calculator.ast;

import calculator.interpreter.Environment;

import calculator.errors.EvaluationError;
import datastructures.concrete.DoubleLinkedList;
import datastructures.interfaces.IDictionary;
import datastructures.interfaces.IList;

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
        if (node.isNumber()) {
            return node.getNumericValue();
        } else if (node.isVariable()) {
            if (!variables.containsKey(node.getName())) {
                throw new EvaluationError("Attempted to call an undefined variable");
            }
            return toDoubleHelper(variables, variables.get(node.getName()));
        } else if (node.isOperation()) {
            String name = node.getName();
            if (!operationsDefined(name)) {
                throw new EvaluationError("Attempted to call an unknown operation");
            }
            IList<AstNode> list = node.getChildren();
            double number = 0.0;
            if (name.equals("+")) {
                number = toDoubleHelper(variables, list.get(0)) + toDoubleHelper(variables, list.get(1));
            } else if (name.equals("-")) {
                number = toDoubleHelper(variables, list.get(0)) - toDoubleHelper(variables, list.get(1));
            } else if (name.equals("*")) {
                number = toDoubleHelper(variables, list.get(0)) * toDoubleHelper(variables, list.get(1));
            } else if (name.equals("/")) {
                number = toDoubleHelper(variables, list.get(0)) / toDoubleHelper(variables, list.get(1));    
            } else if (name.equals("^")) {
                number = Math.pow(toDoubleHelper(variables, list.get(0)), toDoubleHelper(variables, list.get(1)));
            } else if (name.equals("negate")) {
                number = -1 * toDoubleHelper(variables, list.get(0));
            } else if (name.equals("sin")) {
                number = Math.sin(toDoubleHelper(variables, list.get(0)));
            } else if (name.equals("cos")) {
                number = Math.cos(toDoubleHelper(variables, list.get(0)));
            }
            return number;
        } else {
            throw new EvaluationError("Attempted to call a node of unknown type");
        }
    }
    
    private static boolean operationsDefined(String name) {
        return (name.equals("+") || name.equals("-") || name.equals("*") || name.equals("/") || name.equals("^") ||
                /*name.equals(":=") ||*/ name.equals("negate") || name.equals("sin") || name.equals("cos") ||
                name.equals("simplify") || name.equals("toDouble") || name.equals("plot"));
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
        return toSimplifyHelper(env.getVariables(), node.getChildren().get(0));
    }
    
    private static AstNode toSimplifyHelper(IDictionary<String, AstNode> variables, AstNode node) {
        IList<AstNode> list = node.getChildren();     
        if (node.isNumber()) {
            return node;
        } else if (node.isVariable()) { // if the variable is defined
            String name = node.getName();
            if (variables.containsKey(name)) {
                AstNode newNode = variables.get(name);
                if (calculatable(variables, newNode)) { 
                    return new AstNode(toDoubleHelper(variables, newNode));
                }
                return toSimplifyHelper(variables, newNode);
            }
        } else { // is an operation
            String name = node.getName();
            if (!operationsDefined(name)) {
                throw new EvaluationError("operation not defined");
            }
            if (list.size() == 2) {
                AstNode child1 = toSimplifyHelper(variables, list.get(0));
                AstNode child2 = toSimplifyHelper(variables, list.get(1));
                if (calculatable(variables, child1) && calculatable(variables, child2)
                        && simplifyOperations(name)) {
                    return new AstNode(toDoubleHelper(variables, node));
                }
            } else {
                AstNode child1 = toSimplifyHelper(variables, list.get(0));
                if (calculatable(variables, child1) && simplifyOperations(name)) {
                    return new AstNode(toDoubleHelper(variables, node));
                }
            }
        }
        return node;
    }
    
    private static boolean simplifyOperations(String name) {
        return (name.equals("+") || name.equals("*") || name.equals("negate"));
    }
    
    private static boolean calculatable(IDictionary<String, AstNode> variables, AstNode node) {
        return (node.isNumber() || (node.isVariable() && variables.containsKey(node.getName())));
    }
    
   /* private static boolean calculable(Environment env, AstNode node) {
        return (node.isNumber() || (node.isVariable() && env.getVariables().containsKey(node.getName())));
    }*/
 
    
    /*private static int doubleToInt(double param) {
        return (int) Math.floor(param);
    }*/
    
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
    public static AstNode plot(Environment env, AstNode node) {
        // TODO: Your code here
        
        // Note: every single function we add MUST return an
        // AST node that your "simplify" function is capable of handling.
        // However, your "simplify" function doesn't really know what to do
        // with "plot" functions (and what is the "plot" function supposed to
        // evaluate to anyways?) so we'll settle for just returning an
        // arbitrary number.
        //
        // When working on this method, you should uncomment the following line:
        //
        // return new AstNode(1);
        IDictionary<String, AstNode> variables = env.getVariables();
        IList<AstNode> child = node.getChildren(); //0:expr, 1: var, 2:min, 3:max,4:gap
        AstNode function = child.get(0);
        AstNode var = child.get(1);
        AstNode min = child.get(2);
        AstNode max = child.get(3);
        AstNode step = child.get(4);
        Double min_num = getNum(variables, min);
        Double max_num = getNum(variables, max);
        Double step_num = getNum(variables, step);
        
        if (min_num > max_num) {
            throw new EvaluationError("varmin > varmax");
        }
        if (variables.containsKey(var.getName())) {
            throw new EvaluationError("variable was already defined");
        }
        
        if (step_num <= 0) {
            throw new EvaluationError("step must be positive");
        }
        variables.put(var.getName(), new AstNode(1));
        
        if (!ifAllDefined(variables, function)) { 
            throw new EvaluationError("expression contains an undefined variable");
        }
        variables.remove(var.getName());
        
        
        IList<Double> xValues = new DoubleLinkedList<>();
        IList<Double> yValues = new DoubleLinkedList<>();
        

        for(double i = min_num; i <= max_num; i += step_num) {
            xValues.add(i);
            variables.put(var.getName(), new AstNode(i));
            yValues.add(toDoubleHelper(variables, function));             
        }
        
        env.getImageDrawer().drawScatterPlot("plot", var.getName(), "output", xValues, yValues);
        variables.remove(var.getName());
        return new AstNode(1);
       }
    
    
    private static boolean ifAllDefined(IDictionary<String, AstNode> variables, AstNode node) {
        Boolean bool = true;
        if (node.getChildren() == null || node.getChildren().size() == 0) {
            if ((node.isOperation() && !operationsDefined(node.getName())) ||
                (node.isVariable() && !variables.containsKey(node.getName()))) {
                bool = false;
            }
        } else {
            IList<AstNode> list = node.getChildren();
            int size = list.size();
            int count = 0;
            while (bool && count < size) {
                bool = ifAllDefined(variables, list.get(count));
                count++;
            }
        }
        return bool;
    }
    
    private static double getNum(IDictionary<String, AstNode> variables, AstNode node) {
        double num = 0;
        if (node.isNumber()) {
            num = node.getNumericValue();
        } else {
            num = toDoubleHelper(variables, node);
        }
        return num;
    }
}
