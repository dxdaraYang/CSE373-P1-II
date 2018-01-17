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
                name.equals(":=") || name.equals("negate") || name.equals("sin") || name.equals("cos") ||
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
        if (!node.isOperation() || !node.getName().equals("simplify")) {
            throw new EvaluationError("Attempted to call 'handleSimplify()' on an AstNode whose name is not simplify");
        }
        AstNode child = node.getChildren().get(0);
        IDictionary<String, AstNode> variable = env.getVariables();
        IList<AstNode> list = child.getChildren();     
        if (child.isNumber()) {
            return child;
        } else if (child.isVariable()) { // if the variable is defined
            if (variable.containsKey(child.getName())) {
                return handleToDouble(env, node);
            } else {// if the variable is not defined, leave it as it is
                return new AstNode(child.getName());
            }
        } else { // is an operation
            String name = child.getName();
            if (!operationsDefined(name)) {
                throw new EvaluationError("operation not defined");
            }
            if (list.size() == 2) {
                AstNode child1 = list.get(0);
                AstNode child2 = list.get(1);
                if ((child1.isOperation() && !operationsDefined(child1.getName())) ||
                        (child2.isOperation() && !operationsDefined(child2.getName()))) {
                    throw new EvaluationError("operation not defined");
                }
                IList<AstNode> newChild1 = new DoubleLinkedList<>();
                newChild1.add(child1);
                AstNode node1 = new AstNode("simplify", newChild1);
                IList<AstNode> newChild2 = new DoubleLinkedList<>();
                newChild2.add(child2);
                AstNode node2 = new AstNode("simplify", newChild2);
                if ((child1.isVariable() && !variable.containsKey(child1.getName())) ||
                        (child2.isVariable() && !variable.containsKey(child2.getName()))
                        || !(name.equals("+") || name.equals("-") || name.equals("*") ||
                                name.equals("negate")) || (child1.isOperation() || child2.isOperation())) {
                        if (child1.isNumber() && child2.isNumber()) {
                            return new AstNode(doubleToInt(child1.getNumericValue()) +" "+name+" "+
                                    doubleToInt(child2.getNumericValue()));
                        } else if (child1.isNumber() && child2.isVariable()) {
                            return new AstNode(doubleToInt(child1.getNumericValue()) +" "+name+" "+
                                    child2.getName());
                        } else if (child1.isVariable() && child2.isVariable()) {
                            return new AstNode(child1.getName() +" "+name+" "+
                                    child2.getName());
                        } else if (child1.isVariable() && child2.isNumber()) {
                            return new AstNode(child1.getName()  +" "+name+" "+
                                    doubleToInt(child2.getNumericValue()));
                        } else if (child1.isVariable()) {
                            return new AstNode(child1.getName()  +" "+name+" "+
                                    handleSimplify(env, node2));
                        } else if (child1.isNumber()) {
                            return new AstNode(doubleToInt(child1.getNumericValue())  +" "+name+" "+
                                    handleSimplify(env, node2));
                        } else if (child2.isVariable()) {
                            return new AstNode(handleSimplify(env, node1)  +" "+name+" "+
                                    child2.getName());
                        } else if (child2.isNumber()) {
                            return new AstNode(handleSimplify(env, node1)  +" "+name+" "+
                                    doubleToInt(child2.getNumericValue()));
                        }
                        return new AstNode(handleSimplify(env, node1)  +" "+name+" "+
                                handleSimplify(env, node2));
                } else {
                    return handleToDouble(env, node);
                }
            } else {
                AstNode child1 = list.get(0);
                if (child1.isOperation() && !operationsDefined(child1.getName())) {
                    throw new EvaluationError("operation not defined");
                }
                IList<AstNode> newChild1 = new DoubleLinkedList<>();
                newChild1.add(child1);
                AstNode node1 = new AstNode("simplify", newChild1);
                if ((child1.isVariable() && !variable.containsKey(child1.getName()))
                        || !(name.equals("+") || name.equals("-") || name.equals("*") ||
                                name.equals("negate")) || child1.isOperation()) {
                    if (child1.isNumber()) {
                        if (name.equals("sin") || name.equals("cos") ) {
                            return new AstNode(child.getName() +"("+
                                    doubleToInt(child1.getNumericValue())+")");
                            
                        } else {
                            return new AstNode(child.getName() +
                                    doubleToInt(child1.getNumericValue()));
                        }
                    } else if (child1.isVariable()) {
                        if (name.equals("sin") || name.equals("cos") ) {
                            return new AstNode(child.getName() +"("+ child1.getName()+")");
                            
                        } else {
                            return new AstNode(child.getName() + child1.getName());
                        }
                    }
                    return new AstNode(child.getName() + handleSimplify(env, node1));
                } else {
                    return handleToDouble(env, node);
                }
            }
        }
    }
    
    private static int doubleToInt(double param) {
            return (int) Math.floor(param);
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
        if (min.getNumericValue() > max.getNumericValue()) {
            throw new EvaluationError("varmin > varmax");
        }
        if (variables.containsKey(var.getName())) {
            throw new EvaluationError("variable was already defined");
        }
        if (step.getNumericValue() <= 0) {
            throw new EvaluationError("step must be positive");
        }
        variables.put(var.getName(), new AstNode(1));
        
        if (!ifAllDefined(variables, function)) { 
            throw new EvaluationError("expression contains an undefined variable");
        }
        variables.remove(var.getName());
        
        
        IList<Double> xValues = new DoubleLinkedList<>();
        IList<Double> yValues = new DoubleLinkedList<>();
        for(double i = min.getNumericValue(); i <= max.getNumericValue(); i += step.getNumericValue()) {
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
}
