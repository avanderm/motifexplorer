package gui;

public class ConsoleHistory {
	Node<String> currentNode;
	Node<String> standardCommand;
	
	public ConsoleHistory() {
		standardCommand = new Node<String>(null, "", null);
		currentNode = standardCommand;
	}
	
	public String olderCommand() {
		if (currentNode.prev != null)
			currentNode = currentNode.prev;
		
		return currentNode.item;
	}
	
	public String newerCommand() {
		if (currentNode.next != null)
			currentNode = currentNode.next;
		
		return currentNode.item;
	}
	
	public String reset() {
		currentNode = standardCommand;
		return currentNode.item;
	}
	
	public void addCommand(String item) {
		Node<String> insertNode = new Node<String>(standardCommand.prev, item, standardCommand);
		standardCommand.prev = insertNode;
		if (insertNode.prev != null)
			insertNode.prev.next = insertNode;
		
		reset();
	}
	
	private static class Node<E> {
		E item;
		Node<E> next;
		Node<E> prev;
		
		Node(Node<E> prev, E element, Node<E> next) {
			this.item = element;
			this.next = next;
			this.prev = prev;
		}
	}
}