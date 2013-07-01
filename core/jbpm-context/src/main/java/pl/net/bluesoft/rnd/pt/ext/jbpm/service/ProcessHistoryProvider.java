package pl.net.bluesoft.rnd.pt.ext.jbpm.service;

import org.aperteworkflow.bpm.graph.GraphElement;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import java.util.List;

/**
 * User: POlszewski
 * Date: 2013-06-21
 * Time: 11:00
 */
public class ProcessHistoryProvider {
	private static final String JOIN_NODE_NAME = "join";
	private static final String FORK_NODE_NAME = "fork";

	public List<GraphElement> getProcessHistory(ProcessInstance pi) {
		throw new RuntimeException("TODO");
//		HistoryService service = getProcessEngine().getHistoryService();
//
//		HistoryActivityInstanceQuery createHistoryActivityInstanceQuery = service.createHistoryActivityInstanceQuery();
//		HistoryActivityInstanceQuery activityInstanceQuery = createHistoryActivityInstanceQuery.processInstanceId(pi.getInternalId());
//		List<HistoryActivityInstance> list = activityInstanceQuery.list();
//
//		Map<String, GraphElement> processGraphElements = parseProcessDefinition(pi);
//
//		ArrayList<GraphElement> res = new ArrayList<GraphElement>();
//		for (HistoryActivityInstance hpi : list) {
//			log.fine("Handling: " + hpi.getActivityName());
//			if (hpi instanceof HistoryActivityInstanceImpl) {
//				HistoryActivityInstanceImpl activity = (HistoryActivityInstanceImpl)hpi;
//				String activityName = activity.getActivityName();
//				if (res.isEmpty()) { //initialize start node and its transition
//					GraphElement startNode = processGraphElements.get("__AWF__start_node");
//					if (startNode != null) {
//						res.add(startNode);
//					}
//					GraphElement firstTransition = processGraphElements.get("__AWF__start_transition_to_" + activityName);
//					if (firstTransition != null) {
//						res.add(firstTransition);
//					}
//				}
//
//				StateNode sn = (StateNode)processGraphElements.get(activityName);
//
//				if (sn == null) {
//					continue;
//				}
//				sn = sn.cloneNode();
//				sn.setUnfinished(activity.getEndTime() == null);
//				sn.setLabel(activityName + ": " + hpi.getDuration() + "ms");
//				res.add(sn);
//				//look for transition
//				TransitionArc ta = (TransitionArc)processGraphElements.get(activityName + '_' + activity.getTransitionName());
//				if (ta == null) { //look for default!
//					ta = (TransitionArc)processGraphElements.get("__AWF__default_transition_" + activityName);
//				}
//				if (ta == null) {
//					continue;
//				}
//
//				res.add(ta.cloneNode());
//			}
//			else {
//				log.severe("Unsupported entry: " + hpi);
//			}
//		}
//
//		addJoinAndForkElements(res, processGraphElements);
//
//		HistoryProcessInstanceQuery historyProcessInstanceQuery = processEngine.getHistoryService()
//				.createHistoryProcessInstanceQuery().processInstanceId(pi.getInternalId());
//		HistoryProcessInstance historyProcessInstance = historyProcessInstanceQuery.uniqueResult();
//		if (historyProcessInstance != null && historyProcessInstance.getEndActivityName() != null) {
//			StateNode sn = (StateNode)processGraphElements.get(historyProcessInstance.getEndActivityName());
//			if (sn != null) {
//				StateNode e = sn.cloneNode();
//				e.setUnfinished(true);
//				res.add(e);
//			}
//		}
//
//		return res;
	}


//	private Map<String, GraphElement> parseProcessDefinition(ProcessInstance pi) {
//		Map<String, GraphElement> res = new HashMap<String, GraphElement>();
//		byte[] processDefinition = getProcessDefinition(pi);
//		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//
//		try {
//			//Using factory get an instance of document builder
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			//parse using builder to get DOM representation of the XML file
//			Document dom = db.parse(new ByteArrayInputStream(processDefinition));
//			Element documentElement = dom.getDocumentElement();
//			String[] nodeTypes = { "start", "end", "java", "task", "decision", "join", "fork" };
//			for (String nodeType : nodeTypes) {
//				NodeList nodes = documentElement.getElementsByTagName(nodeType);
//				for (int i = 0; i < nodes.getLength(); i++) {
//					Element node = (Element)nodes.item(i);
//					try {
//						StateNode sn = new StateNode();
//						String gval = node.getAttribute("g");
//						String[] vals = gval.split(",", 4);
//						int x = Integer.parseInt(vals[0]);
//						int y = Integer.parseInt(vals[1]);
//						int w = Integer.parseInt(vals[2]);
//						int h = Integer.parseInt(vals[3]);
//						sn.setX(x);
//						sn.setY(y);
//						sn.setWidth(w);
//						sn.setHeight(h);
//						sn.setNodeType(nodeType);
//						String name = node.getAttribute("name");
//						sn.setLabel(name);
//						res.put(name, sn);
//						if ("start".equals(nodeType)) {
//							res.put("__AWF__start_node", sn);
//						}
//						log.fine("Found node" + name + ": " + x + ',' + y + ',' + w + ',' + h);
//					}
//					catch (Exception e) {
//						log.log(Level.SEVERE, e.getMessage(), e);
//					}
//				}
//			}
//			//once again - for transitions
//			for (String nodeType : nodeTypes) {
//				NodeList nodes = documentElement.getElementsByTagName(nodeType);
//				for (int i = 0; i < nodes.getLength(); i++) {
//					Element node = (Element)nodes.item(i);
//					try {
//						String startNodeName = node.getAttribute("name");
//						StateNode startNode = (StateNode)res.get(startNodeName);
//						if (startNode == null) {
//							log.severe("Start node " + startNodeName +
//									" has not been localized, skipping transition drawing too.");
//							continue;
//						}
//						NodeList transitions = node.getElementsByTagName("transition");
//						for (int j = 0; j < transitions.getLength(); j++) {
//							Element transitionEl = (Element)transitions.item(j);
//							String name = transitionEl.getAttribute("name");
//							String to = transitionEl.getAttribute("to");
//							StateNode endNode = (StateNode)res.get(to);
//							if (endNode == null) {
//								log.severe("End node " + to + " has not been localized for transition " + name +
//										" of node " + startNodeName + ", skipping transition drawing.");
//								continue;
//							}
//							String g = transitionEl.getAttribute("g");
//							if (g != null) {
//								String[] dockersAndDistances = g.split(":");
//								String[] dockers = { };
//								if (dockersAndDistances.length == 2) {
//									dockers = dockersAndDistances[0].split(";");//what the other numbers mean - I have no idea...
//								}
//								//calculate line start node which is a center of the start node
//								int startX = startNode.getX() + startNode.getWidth() / 2;
//								int startY = startNode.getY() + startNode.getHeight() / 2;
//								//and the same for end node
//								int endX = endNode.getX() + endNode.getWidth() / 2;
//								int endY = endNode.getY() + endNode.getHeight() / 2;
//
//								TransitionArc arc = new TransitionArc();
//								arc.setName(name);
//								arc.addPoint(startX, startY);
//								for (String docker : dockers) {
//									String[] split = docker.split(",", 2);
//									arc.addPoint(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
//								}
//								arc.addPoint(endX, endY);
//
//								double a;//remember about vertical line
//								double b;
//
//								endX = arc.getPath().get(1).getX();
//								endY = arc.getPath().get(1).getY();
//								if (startX - endX == 0) { //whoa - vertical line - simple case, but requires special approach
//									if (endY > startNode.getY() + startNode.getHeight()) { //below
//										startY = startNode.getY() + startNode.getHeight();
//									}
//									else {
//										startY = startNode.getY();
//									}
//								}
//								else {
//									a = (double)(startY - endY) / (double)(startX - endX);
//									b = (double)startY - (double)startX * a;
//									for (int x = startX; x <= endX; x++) {
//										int y = (int)Math.round(a * x + b);
//										boolean inside = false;
//										if (x >= startNode.getX() && x <= startNode.getX() + startNode.getWidth()) {
//											if (y >= startNode.getY() && y <= startNode.getY() + startNode.getHeight()) {
//												inside = true;
//											}
//										}
//										if (!inside) {
//											startX = x;
//											startY = y;
//											break;
//										}
//									}
//									for (int x = startX; x > endX; x--) {
//										int y = (int)Math.round(a * x + b);
//										boolean inside = false;
//										if (x >= startNode.getX() && x <= startNode.getX() + startNode.getWidth()) {
//											if (y >= startNode.getY() && y <= startNode.getY() + startNode.getHeight()) {
//												inside = true;
//											}
//										}
//										if (!inside) {
//											startX = x;
//											startY = y;
//											break;
//										}
//									}
//								}
//								arc.getPath().get(0).setX(startX);
//								arc.getPath().get(0).setY(startY);
//
//								endX = arc.getPath().get(arc.getPath().size() - 1).getX();
//								endY = arc.getPath().get(arc.getPath().size() - 1).getY();
//								startX = arc.getPath().get(arc.getPath().size() - 2).getX();
//								startY = arc.getPath().get(arc.getPath().size() - 2).getY();
//								if (startX - endX == 0) { //whoa - vertical line - simple case, but requires special approach
//									if (startY > endNode.getY() + endNode.getHeight()) { //below
//										endY = endNode.getY() + endNode.getHeight();
//									}
//									else {
//										endY = endNode.getY();
//									}
//								}
//								else {
//									a = (double)(startY - endY) / (double)(startX - endX);//remember about vertical line
//									//startY = startX*a+b
//									b = (double)startY - (double)startX * a;
//									for (int x = endX; x <= startX; x++) {
//										int y = (int)Math.round(a * x + b);
//										boolean inside = false;
//										if (x >= endNode.getX() && x <= endNode.getX() + endNode.getWidth()) {
//											if (y >= endNode.getY() && y <= endNode.getY() + endNode.getHeight()) {
//												inside = true;
//											}
//										}
//										if (!inside) {
//											endX = x;
//											endY = y;
//											break;
//										}
//									}
//									for (int x = endX; x > startX; x--) {
//										int y = (int)Math.round(a * x + b);
//										boolean inside = false;
//										if (x >= endNode.getX() && x <= endNode.getX() + endNode.getWidth()) {
//											if (y >= endNode.getY() && y <= endNode.getY() + endNode.getHeight()) {
//												inside = true;
//											}
//										}
//										if (!inside) {
//											endX = x;
//											endY = y;
//											break;
//										}
//									}
//								}
//								arc.getPath().get(arc.getPath().size() - 1).setX(endX);
//								arc.getPath().get(arc.getPath().size() - 1).setY(endY);
//								arc.setDestination(to);
//								arc.setSource(startNodeName);
//
//								res.put(startNodeName + '_' + name, arc);
//								if ("start".equals(nodeType)) {
//									res.put("__AWF__start_transition_to_" + to, arc);
//								}
//								if (transitions.getLength() == 1) {
//									res.put("__AWF__default_transition_" + startNodeName, arc);
//								}
//							}
//							else {
//								log.severe("No 'g' attribute for transition " + name +
//										" of node " + startNodeName + ", skipping transition drawing.");
//							}
//						}
//					}
//					catch (Exception e) {
//						log.log(Level.SEVERE, e.getMessage(), e);
//					}
//				}
//			}
//		}
//		catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		return res;
//	}
//
//
//	private void addJoinAndForkElements(List<GraphElement> historyGraph,
//										Map<String, GraphElement> originalGraphMap) {
//		Map<String, StateNode> joinList = new HashMap<String, StateNode>();
//		Map<String, StateNode> forkList = new HashMap<String, StateNode>();
//
//		Map<GraphElement, List<TransitionArc>> joinWithTransitions = new HashMap<GraphElement, List<TransitionArc>>();
//		Map<GraphElement, List<TransitionArc>> forkWithTransitions = new HashMap<GraphElement, List<TransitionArc>>();
//
//		List<TransitionArc> transitionArcList = new ArrayList<TransitionArc>();
//
//		Collection<GraphElement> originalGraph = originalGraphMap.values();
//
//		for (GraphElement originalGraphElement : originalGraph) {
//
//			fillListWithAllPosibleTransitionArc(originalGraphElement,
//					transitionArcList);
//			fillListsWithAllPosibleJoinAndForkNodes(originalGraphElement,
//					joinList, forkList);
//		}
//
//		if (!joinList.isEmpty() || !forkList.isEmpty()) {
//			for (TransitionArc transitionArc : transitionArcList) {
//				relateNodesWithTransitionArcs(forkList, transitionArc,
//						forkWithTransitions);
//				relateNodesWithTransitionArcs(joinList, transitionArc,
//						joinWithTransitions);
//			}
//
//			Set<GraphElement> forks = forkWithTransitions.keySet();
//			List<String> tasksNamesFromHistoryAsArray = getTasksNamesFromHistoryAsArray(historyGraph);
//
//			for (GraphElement fork : forks) {
//				List<TransitionArc> forkTransitions = forkWithTransitions.get(fork);
//				if (isForkOutgoingsExistsInHistoryGraph(forkTransitions, fork, tasksNamesFromHistoryAsArray)) {
//					historyGraph.addAll(forkTransitions);
//					historyGraph.add(fork);
//				}
//			}
//
//			Set<GraphElement> joins = joinWithTransitions.keySet();
//			for (GraphElement join : joins) {
//				List<TransitionArc> list = joinWithTransitions.get(join);
//				if (isJoinOutgoingWasMissingInHistoryGraph(list, tasksNamesFromHistoryAsArray, historyGraph)) {
//					historyGraph.add(join);
//				}
//			}
//		}
//	}
//
//	private void fillListWithAllPosibleTransitionArc(GraphElement originalGraphElement, List<TransitionArc> transitionArcList) {
//		if (originalGraphElement instanceof TransitionArc) {
//			TransitionArc transitionArc = (TransitionArc)originalGraphElement;
//			transitionArcList.add(transitionArc);
//		}
//	}
//
//	private void fillListsWithAllPosibleJoinAndForkNodes(
//			GraphElement originalGraphElement, Map<String, StateNode> joinList,
//			Map<String, StateNode> forkList) {
//		if (originalGraphElement instanceof StateNode) {
//			updateNodeList(joinList, JOIN_NODE_NAME, originalGraphElement);
//			updateNodeList(forkList, FORK_NODE_NAME, originalGraphElement);
//		}
//	}
//
//	private void updateNodeList(Map<String, StateNode> nodeList,
//								String nodeType, GraphElement originalGraphElement) {
//		StateNode stateNode = (StateNode)originalGraphElement;
//
//		if (stateNode.getNodeType() != null && stateNode.getNodeType().equals(nodeType)) {
//			nodeList.put(stateNode.getLabel(), stateNode);
//		}
//	}
//
//	private void relateNodesWithTransitionArcs(
//			Map<String, StateNode> joinForkList, TransitionArc transitionArc,
//			Map<GraphElement, List<TransitionArc>> joinOrForkWithTransitions) {
//		if (joinForkList.containsKey(transitionArc.getSource())) {
//			StateNode stateNode = joinForkList.get(transitionArc.getSource());
//			List<TransitionArc> listOfTransition;
//
//			if (joinOrForkWithTransitions.containsKey(stateNode)) {
//				listOfTransition = joinOrForkWithTransitions.get(stateNode);
//			}
//			else {
//				listOfTransition = new ArrayList<TransitionArc>();
//			}
//
//			listOfTransition.add(transitionArc);
//			joinOrForkWithTransitions.put(stateNode, listOfTransition);
//		}
//	}
//
//	private List<String> getTasksNamesFromHistoryAsArray(List<GraphElement> historyGraph) {
//		List<String> newHistoryElement = new ArrayList<String>();
//
//		for (GraphElement historyElement : historyGraph) {
//			if (historyElement instanceof StateNode) {
//				StateNode historyNode = (StateNode)historyElement;
//				String histloryNodeLabel = historyNode.getLabel();
//				String[] split = histloryNodeLabel.split(":");
//				String taskNameFromHistory = split[0];
//
//				newHistoryElement.add(taskNameFromHistory);
//			}
//		}
//		return newHistoryElement;
//	}
//
//	private boolean isForkOutgoingsExistsInHistoryGraph(List<TransitionArc> list, GraphElement fork, List<String> tasksNamesFromHistoryAsArray) {
//		int apperenceCounter = list.size();
//		for (TransitionArc transitionArc : list) {
//			if (tasksNamesFromHistoryAsArray.contains(transitionArc
//					.getDestination())) {
//				apperenceCounter--;
//			}
//		}
//		return apperenceCounter == 0;
//	}
//
//	private boolean isJoinOutgoingWasMissingInHistoryGraph(
//			List<TransitionArc> list,
//			List<String> tasksNamesFromHistoryAsArray,
//			List<GraphElement> historyGraph) {
//		boolean addedTransition = false;
//
//		for (TransitionArc transitionArc : list) {
//			if (tasksNamesFromHistoryAsArray.contains(transitionArc.getDestination())) {
//				historyGraph.add(transitionArc);
//				addedTransition = true;
//			}
//		}
//		return addedTransition;
//	}
}
