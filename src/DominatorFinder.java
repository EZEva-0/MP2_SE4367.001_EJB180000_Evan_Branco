import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.toolkits.graph.DirectedGraph;


//Evan Branco, EJB180000
//I sincerely apologize, but I've had immense difficulty with this project
//The incredible length that the project was posted over surely deters all sympathy,
//but, if possible, I would like to express what understanding I do have, even if it 
//is not reflected in code, to try and get a few extra points where I would otherwise get a 0.
//My understanding of the purpose of the doAnalysis function. It takes a given
//directed graph, and traverses it, locating all dominations.
//The algorithm to do this is simple, first we locate all head nodes, a task that soot
//can perform for us. We then create 2 sets, and place them in the final dominators list,
//We create a "nodeToFlowSet", which is already completed, and an "indexToNode" set, which
//we add to the list as well, and let the internalTrasform print our results.
//The goal is to move through the entire graph and find all dominators.
//The algorithm to do this is given in the project directions.
//The dominator of the start node is the start itself (root node)
//We set all other nodes as true, and interactively eliminate nodes that are not dominators.
//and, for each change to any the denominator list, we add the node itself, and all the 
//predecessors of that node, to the dominator list.

//My ultimate confusion is... this task is already performed, seemingly correctly, with the
//given code! We set all nodes to true, we iterate over the full graph, we check for heads and 
//if there are we add only itself, and if the node isn't a head then every other node is is a 
//dominator to it. We then check for changes and remove all nodes that are invalid, only adding
//to our final set predecessor nodes and heads.
//So... what is incorrect here? What nuance am I missing? Surely something is missing!
//Did the professor want me to simply add more analysis functionality? Build a DominatorTree
//using DominatorTreeAdapter? Use more of the DirectedGraph methods? get the successors 
//as well as predecessors? I'm completely lost as to what is being asked of me, I don't see 
//what's wrong with the given code!


public class DominatorFinder<N> {
	protected DirectedGraph<N> graph;
    protected BitSet fullSet;
    protected List<N> heads;
    protected Map<N,BitSet> nodeToFlowSet;
    protected Map<N,Integer> nodeToIndex;
    protected Map<Integer,N> indexToNode;
    protected int lastIndex = 0;

    public DominatorFinder(DirectedGraph<N> graph)
    {
        this.graph = graph;
        doAnalysis();
    }

    protected void doAnalysis()
    {
        heads = graph.getHeads();
        nodeToFlowSet = new HashMap<N, BitSet>();
        nodeToIndex = new HashMap<N, Integer>();
        indexToNode = new HashMap<Integer,N>();

        //build full set
        fullSet = new BitSet(graph.size());
        fullSet.flip(0, graph.size());//set all to true

        //set up domain for intersection: head nodes are only dominated by themselves,
        //other nodes are dominated by everything else
        for(Iterator<N> i = graph.iterator(); i.hasNext();)
        {
            N o = i.next();
            if(heads.contains(o))
            {
                BitSet self = new BitSet();
                self.set(indexOf(o));
                nodeToFlowSet.put(o, self);
            }
            else
            {
                nodeToFlowSet.put(o, fullSet);
            }
        }

        boolean changed = true;
        do{
            changed = false;
            for(Iterator<N> i = graph.iterator(); i.hasNext();)
            {
                N o = i.next();
                if(heads.contains(o)) continue;

                //initialize to the "neutral element" for the intersection
                //this clone() is fast on BitSets (opposed to on HashSets)
				BitSet predsIntersect = (BitSet) fullSet.clone();

                //intersect over all predecessors
                for(Iterator<N> j = graph.getPredsOf(o).iterator(); j.hasNext();)
                {
                    BitSet predSet = nodeToFlowSet.get(j.next());
                    predsIntersect.and(predSet);
                }

                BitSet oldSet = nodeToFlowSet.get(o);
                //each node dominates itself
                predsIntersect.set(indexOf(o));
                if(!predsIntersect.equals(oldSet))
                {
                    nodeToFlowSet.put(o, predsIntersect);
                    changed = true;
                }
            }
        } while(changed);
    }

    protected int indexOf(N o) 
    {
        Integer index = nodeToIndex.get(o);
        if(index==null) 
        {
            index = lastIndex;
            nodeToIndex.put(o,index);
            indexToNode.put(index,o);
            lastIndex++;
        }
        return index;
    }

    public DirectedGraph<N> getGraph()
    {
        return graph;
    }

    public List<N> getDominators(Object node)
    {
        //reconstruct list of dominators from bitset
        List<N> result = new ArrayList<N>();
        BitSet bitSet = nodeToFlowSet.get(node);
        for(int i=0;i<bitSet.length();i++) {
            if(bitSet.get(i)) {
                result.add(indexToNode.get(i));
            }
        }
        return result;
    }
    }
