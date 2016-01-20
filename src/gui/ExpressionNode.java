package gui;

import java.util.Collection;
import java.util.Stack;

public class ExpressionNode {

	private ExpressionNode parent;
	private Stack<ExpressionNode> children;
	
	private String expression;
	private int subExpressionCount;
	
	public ExpressionNode(String expression) {
		this(expression, null);
	}
	
	public ExpressionNode(String expression, ExpressionNode parent) {
		this.expression = expression;
		
		this.parent = parent;
		this.children = new Stack<ExpressionNode>();
		
		this.subExpressionCount = 0;
	}
	
	public ExpressionNode getParent() {
		return parent;
	}
	
	public Collection<ExpressionNode> getChildren() {
		return children;
	}
	
	public void pushSubExpression(ExpressionNode node) {
		children.push(node);
		subExpressionCount++;
	}
	
	public ExpressionNode peekSubExpression() {
		return children.peek();
	}
	
	public ExpressionNode popSubExpression() {
		return children.pop();
	}
	
	public int getNbChildren() {
		return subExpressionCount;
	}
	
	public String getExpression() {
		return expression;
	}
	
	public boolean isLeafNode() {
		return (subExpressionCount == 0);
	}

}