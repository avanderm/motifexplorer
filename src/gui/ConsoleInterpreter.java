package gui;

import gui.mvc.Controller;
import gui.mvc.DatabaseCall;
import gui.mvc.FeatureMap;
import gui.mvc.MatchCount;
import gui.mvc.MatrixScan;
import gui.mvc.ParseGenes;
import gui.mvc.ParseMotifs;
import gui.mvc.ParsePromoters;
import gui.mvc.PrintHelp;
import gui.mvc.RetrieveGenes;
import gui.mvc.RetrieveMotifs;
import gui.mvc.RetrievePromoters;
import gui.mvc.RetrieveSets;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.Interpreter;

public class ConsoleInterpreter implements Interpreter {
	
	private static final String INDEX_PATTERN =
			"(?:sets|([0-9]+(?:,[0-9]+(?::[0-9]+)?+)*+))";
	
	private Controller controller;
	
	private HashMap<String, String> variableMap;
	private String variablePattern;
	
	private HashMap<String, String> commandMap;
	private String commandPattern;
	
	private Pattern pv;
	private Pattern pi;
	private Pattern pc;
	
	public ConsoleInterpreter(Controller controller) {
		this.controller = controller;
		
		// Prepare commands
		commandMap = new HashMap<String, String>();
		commandMap.put("matrix_scan", "matrix scan");
		commandMap.put("feature_map", "feature map");
		commandMap.put("match_count", "match count");
		commandMap.put("parse_g", "parse genes");
		commandMap.put("parse_p", "parse promoters");
		commandMap.put("parse_m", "parse motifs");
		commandMap.put("db", "database call");
		commandMap.put("exit", "exit");
		commandMap.put("help", "help");
		
		commandPattern = "^(";
		int nbCommands = commandMap.size();
		for(String command : commandMap.keySet())
			if (nbCommands-- > 1)
				commandPattern += (command + "|");
			else
				commandPattern += (command + ")$");
		
		pc = Pattern.compile(commandPattern);
		
		// Prepare variables
		variableMap = new HashMap<String, String>();
		variableMap.put("g", "genes");
		variableMap.put("s", "sets");
		variableMap.put("p", "promoters");
		variableMap.put("m", "motifs");
		
		variablePattern = "^(";
		int nbVariables = variableMap.size();
		for(String variable : variableMap.keySet())
			if (nbVariables-- > 1)
				variablePattern += (variable + "|");
			else
				variablePattern += (variable + ")$");
		
		pv = Pattern.compile(variablePattern);
		pi = Pattern.compile(INDEX_PATTERN);
	}
	
	@Override
	public void interpret(String command) throws InvalidCommand, InvalidExpression {
		if ((command = command.replaceAll("(?<!db|help)\\s*", "")).length() == 0)
			return;
		
		if (!command.endsWith(";"))
			throw new InvalidExpression("Insert ; to complete statement");
		
		ExpressionNode root = null;
		ExpressionNode currentNode = null;
		
		String token = "";
		for(int i = 0; i < command.length(); i++) {
			char c = command.charAt(i);
			
			if (c == '(') {
				if (root == null) {
					root = new ExpressionNode(token);
					currentNode = root;
				} else {
					ExpressionNode node = new ExpressionNode(token, currentNode);
					currentNode.pushSubExpression(node);
					currentNode = node;
				}
				
				token = "";
			} else if (c == ';') {
				if (root == null) {
					root = new ExpressionNode(token);
					currentNode = root;
				} else if (token.length() != 0) {
					ExpressionNode node = new ExpressionNode(token, currentNode);
					currentNode.pushSubExpression(node);
					//currentNode = node;
				}
				
				token = "";
			} else if (c == ')') {
				if (token.length() != 0 && currentNode != null) {
					ExpressionNode node = new ExpressionNode(token, currentNode);
					currentNode.pushSubExpression(node);
					
					token = "";
				}
				
				if (command.charAt(i-1) == ')')
					currentNode = currentNode.getParent();
			} else if (c == ',') {
				//TODO: mi, pi
				if (command.charAt(i-1) == ')')
					currentNode = currentNode.getParent();
				else {
					if (pi.matcher(token).find())
						token += ',';
					else {
						ExpressionNode node = new ExpressionNode(token, currentNode);
						if (currentNode != null)
							currentNode.pushSubExpression(node);
						
						token = "";
					}
				}
			} else if (c == ' ') {
				root = new ExpressionNode(token);
				currentNode = root;
				
				token = "";
			} else
				token += c;
		}
		
		if (currentNode != root)
			throw new InvalidExpression("Unmatched parentheses for expression: " +
					currentNode.getExpression());
		
		Stack<String> dataStack = new Stack<String>();
		traverse(root, dataStack);
	}
	
	private void traverse(ExpressionNode node, Stack<String> dataStack) throws InvalidExpression, InvalidCommand {
		for(int i = 0; i < node.getNbChildren(); i++)
			traverse(node.popSubExpression(), dataStack);
		
		String expression = node.getExpression();
		if (node.getParent() != null && node.getParent().getExpression().equals("help")) {
			dataStack.push(expression);
			return;
		}
		
		Matcher mv = pv.matcher(expression);
		if (mv.find()) {
			Collection<Integer> indices = null;
			Collection<String> temp = null;
			
			if (node.getNbChildren() == 0)
				indices = null;
			else if (node.getNbChildren() == 1) {
				String selector = dataStack.peek();
				Matcher mi = pi.matcher(selector);
				if (mi.find()) {
					if (selector.equals("sets"))
						temp = ((RetrieveSets) controller.collect()).getResult();
					else
						indices = parseSelector(selector);
					
					dataStack.pop();
				} else
					throw new InvalidExpression("Invalid selection for: " + expression);
			} else
				throw new InvalidExpression("Too many arguments for: " + expression);
			
			switch(variableMap.get(expression)) {
			case "genes":
				if (indices != null && temp == null) {
					temp = new ArrayList<String>(indices.size());
					for(Integer i : indices)
						temp.add("SIP" + i);
				}
				
				controller.process(new RetrieveGenes(temp));
				break;
			case "sets":
				controller.process(new RetrieveSets(indices));
				dataStack.push("sets");
				break;
			case "promoters":
				if (indices != null && temp == null) {
					temp = new ArrayList<String>(indices.size());
					for(Integer i : indices)
						temp.add("SIP" + i);
				}
				
				controller.process(new RetrievePromoters(temp));
				break;
			case "motifs":
				controller.process(new RetrieveMotifs(indices));
				break;
			}
			
			// Output result if root node
			if (node.getParent() == null)
				controller.print(controller.collect());
			
			return;
		}
		
		Matcher mc = pc.matcher(expression);
		if (mc.find()) {
			try {
				switch(commandMap.get(expression)) {
				case "exit":
					controller.shutDown();
				case "parse genes":
					if (node.getNbChildren() != 1)
						throw new InvalidExpression("Invalid arguments for: " + expression);
					controller.process(new ParseGenes(dataStack.pop(), true));
					break;
				case "parse promoters":
					if (node.getNbChildren() != 1)
						throw new InvalidExpression("Invalid arguments for: " + expression);
					controller.process(new ParsePromoters(dataStack.pop(), true));
					break;
				case "parse motifs":
					if (node.getNbChildren() != 1)
						throw new InvalidExpression("Invalid arguments for: " + expression);
					controller.process(new ParseMotifs(dataStack.pop(), true));
					break;
				case "matrix scan":
					if (node.getNbChildren() != 2)
						throw new InvalidExpression("Invalid arguments for: " + expression);
					
					controller.process(new MatrixScan(((RetrievePromoters) controller.collect()).getResult(),
							((RetrieveMotifs) controller.collect()).getResult()));
					
					break;
				case "feature map": {
					if (node.getNbChildren() != 1)
						throw new InvalidExpression("Invalid arguments for: " + expression);
					
					File file = new File(dataStack.pop());
					controller.process(new FeatureMap(file));
					break;
				}
				case "match count": {
					if (node.getNbChildren() != 1)
						throw new InvalidExpression("Invalid arguments for: " + expression);
					
					File file = new File(dataStack.pop());
					controller.process(new MatchCount(file));
					break;
				}
				case "database call":
					if (dataStack.empty())
						throw new InvalidExpression("Invalid arguments for: " + expression);
					else
						controller.process(new DatabaseCall(dataStack.pop()));
					break;
				case "help":
					if (dataStack.empty())
						controller.process(new PrintHelp("general"));
					else
						controller.process(new PrintHelp(dataStack.pop()));
					break;
				}
				
				return;
			} catch(ClassCastException e) {
				throw new InvalidExpression("Bad arguments for: " + expression);
			}
		}
		
		if (node.isLeafNode() && node.getParent() != null)
			dataStack.push(expression);
		else throw new InvalidCommand("Unknown command: " + expression);
	}
	
	private Collection<Integer> parseSelector(String selector) {
		if (selector.length() == 0)
			return null;
		
		TreeSet<Integer> indices = new TreeSet<Integer>();
		
		Pattern p = Pattern.compile("([0-9]+)(?::([0-9]+))?+");
		Matcher m = p.matcher(selector);
		
		while(m.find()) {
			int a = Integer.parseInt(m.group(1));
			int b = a;
			
			if (m.group(2) != null)
				b = Integer.parseInt(m.group(2));
			
			for(int i = a; i <= b; i++)
				indices.add(i);
		}
		
		return indices;
	}

}