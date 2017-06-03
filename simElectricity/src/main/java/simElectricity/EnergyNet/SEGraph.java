package simElectricity.EnergyNet;

import java.util.Iterator;
import java.util.LinkedList;

import simElectricity.EnergyNet.Components.Cable;
import simElectricity.EnergyNet.Components.GridNode;
import simElectricity.EnergyNet.Components.SEComponent;

/**
 * Unweighed graph, using Adjacency lists (SEComponent.neighbors)
 */
public class SEGraph {
	private LinkedList<SEComponent> components;
	private LinkedList<SEComponent> wires;	
	private LinkedList<SEComponent> terminalNodes;
	
    public SEGraph() {
    	components = new LinkedList<SEComponent>();
    	wires = new LinkedList<SEComponent>();
    	terminalNodes = new LinkedList<SEComponent>();
    }
	
    public int size(){
    	return components.size() + wires.size();
    }
   
	
	public boolean containsNode(SEComponent node){
		if (components.contains(node))
			return true;
		else if (wires.contains(node))
			return true;
		else
			return false;
	}
	
	
    /**
     * Add a vertex into the graph
     */
    public void addVertex(SEComponent node) {
        if (containsNode(node))
            return;

        if (isWire(node))
        	wires.addLast(node);
        else
        	components.addLast(node);
    }
       
    /**
     * Add a neighbor to a vertex
     */
    public void addEdge(SEComponent node, SEComponent neighbor) {
        if (!containsNode(node))
            return;
        
        if (!containsNode(neighbor))
            return;

        if (!node.neighbors.contains(neighbor))
        	node.neighbors.addLast(neighbor);

        if (!neighbor.neighbors.contains(node))
        	neighbor.neighbors.addLast(node);
    }
    
	public void addGridEdge(GridNode node1, GridNode node2, double resistance){
        if (!containsNode(node1))
            return;
        
        if (!containsNode(node2))
            return;

        if (!node1.neighbors.contains(node2)){
        	node1.neighbors.addLast(node2);
        	node1.neighborR.addLast(resistance);
        }
        

        if (!node2.neighbors.contains(node1)){
            node2.neighbors.addLast(node1);
    		node2.neighborR.addLast(resistance);        	
        }
	}
	
	public void interconnection(Cable cable, GridNode gridNode){
		gridNode.interConnection = cable;
		cable.connectedGridNode = gridNode;
	}
	
	public void breakInterconnection(Cable cable){
    	if (cable.connectedGridNode != null){
        	cable.connectedGridNode.interConnection = null;
        	cable.connectedGridNode = null;			
    	}
	}
	
	public void breakInterconnection(GridNode gridNode){
		if (gridNode.interConnection != null){
			gridNode.interConnection.connectedGridNode = null;
			gridNode.interConnection = null;
		}
	}
    
    /**
     * Remove a vertex, including its assosiated edges
     */
    public void removeVertex(SEComponent node) {
        if (!containsNode(node))
            return;
        
        //Cut possible interconnection
        if (node instanceof Cable)
        	breakInterconnection((Cable)node);
        
        //Remove this node from its neighbors' list
        for (SEComponent neighbor: node.neighbors){
        	LinkedList<SEComponent> list = neighbor.neighbors;
        	if (list.contains(node))
        		list.remove(node);
        }

        node.neighbors.clear();
        (isWire(node) ? wires : components).remove(node);
    }
    
    public LinkedList<GridNode> removeGridVertex(GridNode gridNode){
        if (!containsNode(gridNode))
            return null;
        
        //Cut possible interconnection
        breakInterconnection(gridNode);
        
        LinkedList<GridNode> ret = new LinkedList<GridNode>();
        
		//Delete resistance properties of GridNodes
		for (SEComponent neighbor : gridNode.neighbors){
			if (neighbor instanceof GridNode){
				Iterator<SEComponent> iterator1 = neighbor.neighbors.iterator();
				Iterator<Double> iterator2 = ((GridNode)neighbor).neighborR.iterator();
								
				deleteInfoFromNeighbor: while(iterator1.hasNext()){
					SEComponent seComponent = iterator1.next();
					if (seComponent instanceof GridNode){
						iterator2.next();
						if (seComponent == gridNode){
							iterator2.remove();
							iterator1.remove();
							break deleteInfoFromNeighbor;
						}
					}
				}
				
				ret.add((GridNode) neighbor);
			}
		}
		
		gridNode.neighbors.clear();
		gridNode.neighborR.clear();
        (isWire(gridNode) ? wires : components).remove(gridNode);
        
        return ret;
    }
    
    /**
     * Remove an edge
     */
    public void removeEdge(SEComponent node, SEComponent neighbor) {
        if (!containsNode(node))
            return;
        
        if (!containsNode(neighbor))
            return;

        if (neighbor.neighbors.contains(neighbor))
        	neighbor.neighbors.remove(neighbor);

        if (neighbor.neighbors.contains(node))
        	neighbor.neighbors.remove(node);
    }
    
    public void removeGridEdge(GridNode node1, GridNode node2){
		Iterator<SEComponent> iterator1 = node1.neighbors.iterator();
		Iterator<Double> iterator2 = node1.neighborR.iterator();
		deleteInfoFromNeighbor: while(iterator1.hasNext()){
			iterator2.next();
			if (iterator1.next() == node2){
				iterator2.remove();
				iterator1.remove();
				break deleteInfoFromNeighbor;
			}
		}
		
		iterator1 = node2.neighbors.iterator();
		iterator2 = node2.neighborR.iterator();
		deleteInfoFromNeighbor: while(iterator1.hasNext()){
			iterator2.next();
			if (iterator1.next() == node1){
				iterator2.remove();
				iterator1.remove();
				break deleteInfoFromNeighbor;
			}
		}		
    }
           
    
    public void clearVoltageCache(){
    	for (SEComponent node : terminalNodes)
    		node.voltageCache = 0;
    	for (SEComponent wire : wires)
    		wire.voltageCache = 0;
    }
    ////////////////////////////////////////////////
    ///Optimizer
    ////////////////////////////////////////////////    
	private static boolean isWire(SEComponent node){
		if (node instanceof Cable)
			return true;
		if (node instanceof GridNode){
			if(((GridNode)node).getType() == GridNode.ISEGridNode_Wire)
				return true;
		}
		return false;
	}
	
	private static boolean isInterconnectionTerminal(SEComponent node){
		if ((node instanceof Cable) && ((Cable)node).connectedGridNode != null)
			return true;
		else if ((node instanceof GridNode) && ((GridNode)node).interConnection != null)
			return true;
		else 
			return false;
	}
    
    public static double calcR(SEComponent cur, SEComponent neighbor){
    	if (cur instanceof Cable){
    		Cable curConductor = (Cable) cur;
    		if (neighbor instanceof Cable){
    			return curConductor.resistance + ((Cable) neighbor).resistance;
    		}else{
    			return curConductor.resistance;
    		}
    	} else if (cur instanceof GridNode){
    		GridNode curGridNode = (GridNode)cur;
    		if (neighbor instanceof GridNode){
    			return curGridNode.getResistance((GridNode)neighbor);
    		}else {
    			throw new RuntimeException("Unaccptable conntection");
    		}
    	} else {
    		if (neighbor instanceof Cable){
    			return ((Cable)neighbor).resistance;
    		}
    	}
    	
    	throw new RuntimeException("Unaccptable conntection");
    }
       
    public void optimizGraph(){
    	terminalNodes.clear();
    	terminalNodes.addAll(components);

    	for (SEComponent node : terminalNodes){
    		node.visited = false;
    		node.optimizedNeighbors.clear();
			node.optimizedResistance.clear();
    	}
    	
    	for (SEComponent wire : wires){
    		wire.visited = false;
			wire.optimizedNeighbors.clear();
			wire.optimizedResistance.clear();
			
			
			wire.eliminated = true;
    		
			if (wire.neighbors.size()>2				//A node has more than 2 connections
    				||								//OR
    			isInterconnectionTerminal(wire))	//A interconnection terminal
    			terminalNodes.add(wire);
    	}
    	
    	//SubComponents, Cable/TransmissionLine with more than 2 connections, Interconnection terminals
    	for (SEComponent node : terminalNodes){
    		node.eliminated = false;
    		
    		if (!node.visited){
    			node.visited = true;
    			
    			rraverseNeighbors: for (SEComponent neighbor: node.neighbors){
    				if (neighbor.visited)
    					continue rraverseNeighbors;
    				
        			SEComponent reach = neighbor;	//Far reach
        			SEComponent prev = node;
        			double resistance = 0.0D;
        			
        			search: do{
        				if (reach == node){
        					reach = null;	//Can not form an edge
        					break search;	//Avoid circulation
        				}
        				
      				
        				resistance += calcR(prev, reach);
        				
        				if (reach.neighbors.size() > 2
        						|| 
        					isInterconnectionTerminal(reach))	//We have to calculate the voltage of the interconnection point
        					break search;
        				
        		    	if (reach.neighbors.size() == 1){
        		    		if (isWire(reach))
        		    			reach = null;	//A single ended wire can not form an edge
        		    		break search;	//Dead end!
        		    	}
        		    	else if (reach.neighbors.size() == 2){	//Always moveforward!
        		    		
        		    		
    		    			reach.visited = true;
    		    			prev = reach;
        		    		if (reach.neighbors.getFirst() == prev){
        		    			reach = reach.neighbors.getLast();
        		    		}else if (reach.neighbors.getLast() == prev){
        		    			reach = reach.neighbors.getFirst();
        		    		}else{
        		    			throw new RuntimeException("WTF mate! This can not happen!");
        		    		}
        		    	}
        			}while (true);	
        			
        			if (reach != null){	//node-node connection
        				Iterator<SEComponent> iteratorON = node.optimizedNeighbors.iterator();
        				Iterator<Double> iteratorR = node.optimizedResistance.iterator();
        				
        				/*
        				 * Combine parallel path
        				 * Example:
        				 * 		x
        				 * 		x
        				 * 		xxxx
        				 * 		x  x
        				 * 		x  x
        				 * 		xxxxxxxxxx
        				 */
        				checkParallel: while (iteratorON.hasNext()){
        					double prevR = iteratorR.next();
        					if (iteratorON.next() == reach){
        						//Delete previous edge
                				Iterator<SEComponent> iteratorON2 = reach.optimizedNeighbors.iterator();
                				Iterator<Double> iteratorR2 = reach.optimizedResistance.iterator();
                				delete: while (iteratorON2.hasNext()){
                					iteratorR2.next();
                					if (iteratorON2.next() == node){
                						iteratorR2.remove();
                						iteratorON2.remove();
                						break delete;
                					}
                				}

        						iteratorR.remove();
        						iteratorON.remove();
        						
        						resistance = resistance * prevR / (resistance + prevR);
        						break checkParallel;
        					}
        				}
        				

                		node.optimizedNeighbors.addLast(reach);
                		node.optimizedResistance.addLast(resistance);      

                		reach.optimizedNeighbors.addLast(node);
                		reach.optimizedResistance.addLast(resistance);        					
        			}
        		}
    		}

    	}
    		
    }
    
    public LinkedList<SEComponent> getTerminalNodes(){
    	return terminalNodes;
    }

    public double R0,R1;
    public SEComponent[] getTerminals(SEComponent wire){
    	if (!isWire(wire))
    		return null;	//Not applicable
    	
    	R0 = 0;
    	R1 = 0;
    	    	
    	if (wire.neighbors.size() > 2)
    		return null;	//Not an node
    	
    	if (wire.neighbors.size() == 0)
    		return new SEComponent[]{}; 	//No connection, 0V!
    	
    	SEComponent prev = wire;
    	SEComponent head = wire.neighbors.getFirst();
    	
    	search1: do{
    		if (head == wire)
    			return new SEComponent[]{};	//Avoid circulation, 0V
    		
    		R0 += calcR(prev, head);
    		
			if (head.neighbors.size() > 2
					|| 
				isInterconnectionTerminal(head))	//We have to calculate the voltage of the interconnection point
				break search1;
    		
	    	if (head.neighbors.size() == 1){	//Single end
	    		if (isWire(head))
	    			head = null;
	    		break search1;	//Dead end!
	    	}
	    	else if (head.neighbors.size() == 2){	//Always moveforward!
	    		if (head.neighbors.getFirst() == prev){
	    			prev = head;
	    			head = head.neighbors.getLast();
	    		}
	    		else if (head.neighbors.getLast() == prev){
	    			prev = head;
	    			head = head.neighbors.getFirst();
	    		}
	    	}
    	}while(true);
    	
    	//head == null -> single end
    	//head != null -> something
    	// A--^--A
    	// A--^--
    	// A--^
    	// ---^
    	if (wire.neighbors.size() == 1){
    		if (head == null)
    			return new SEComponent[]{};		//No connection to other nodes, 0V
    		else
    			return new SEComponent[]{head};	//Equal to the voltage of "head"
    	}
    		
    	//wire.neighbors.size() == 2
    	SEComponent terminal1 = head;
    	prev = wire;
    	head = wire.neighbors.getLast();

    	search1: do{
    		R1 += calcR(prev, head);
    		
			if (head.neighbors.size() > 2
					|| 
				isInterconnectionTerminal(head))	//We have to calculate the voltage of the interconnection point
				break search1;
    		
	    	if (head.neighbors.size() == 1){	//Single end
	    		if (isWire(head))
	    			head = null;
	    		break search1;	//Dead end!
	    	}
	    	else if (head.neighbors.size() == 2){	//Always moveforward!
	    		if (head.neighbors.getFirst() == prev){
	    			prev = head;
	    			head = head.neighbors.getLast();
	    		}
	    		else if (head.neighbors.getLast() == prev){
	    			prev = head;
	    			head = head.neighbors.getFirst();
	    		}
	    	}
    	}while(true);
    	
    	if (terminal1 == null){
    		if (head == null)
    			return new SEComponent[]{};		//No connection to other nodes, 0V
    		else
    			return new SEComponent[]{head};	//Equal to the voltage of "head"
    	}else{
    		if (head == null)
    			return new SEComponent[]{terminal1};	//Equal to the voltage of "head"
    		else
    			return new SEComponent[]{terminal1, head};
    	}
    }
}
